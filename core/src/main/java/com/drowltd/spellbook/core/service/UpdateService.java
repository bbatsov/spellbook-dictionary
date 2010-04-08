package com.drowltd.spellbook.core.service;

import com.drowltd.spellbook.core.exception.UpdateServiceException;
import com.drowltd.spellbook.core.model.Dictionary;
import com.drowltd.spellbook.core.model.DictionaryEntry;
import com.drowltd.spellbook.core.model.LastUpdateEntity;
import com.drowltd.spellbook.core.model.RemoteDictionaryEntry;
import com.drowltd.spellbook.core.model.UpdateEntry;
import java.sql.DriverManager;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * This class provides the basic functionality for updating
 * the local database.
 *
 * IMPORTANT: There must be happens-before relationship between the
 * thread that uses DictionaryService and the thread that is using UpdateService.
 * Any use of DictionaryService must happen-before any use of UpdateService,
 * and any subsequent use of DictionaryService must happen-after any use of UpdateService.
 *
 * @author iivalchev
 */
public class UpdateService extends AbstractPersistenceService {

    protected static EntityManager EM_REMOTE;
    private static UpdateService INSTANCE;
    private static Logger LOGGER = LoggerFactory.getLogger(UpdateService.class);

    private UpdateService() throws UpdateServiceException {
        if (EM == null) {
            throw new IllegalStateException("DictionaryService not initialized");
        }
        initRemoteEntityManager();
    }

    public static UpdateService getInstance() throws UpdateServiceException {
          return new UpdateService();
    }

    protected static void initRemoteEntityManager() throws UpdateServiceException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            DriverManager.getConnection("jdbc:mysql://localhost:3306/SpellbookRemote", "iivalchev", "").close();
            EM_REMOTE = Persistence.createEntityManagerFactory("SpellbookRemote").createEntityManager();
        } catch (Exception e) {
            throw new UpdateServiceException();
        }
    }
    private LastUpdateEntity lastUpdate;
    private final Map<String, Dictionary> dictMap = new HashMap<String, Dictionary>();

    /*
     * Getting the single LastUpdateEntity
     */
    private LastUpdateEntity getLastUpdateEntity() {
        LastUpdateEntity lastUEntity;
        try {
            lastUEntity = (LastUpdateEntity) EM.createQuery("select lue from LastUpdateEntity lue").getSingleResult();
        } catch (NoResultException e) {

            //Checking for update for first time, need to create LastUpdateEntity
            lastUEntity = new LastUpdateEntity();
            lastUEntity.setModified(new Date(0));

            EntityTransaction t = EM.getTransaction();
            t.begin();
            EM.persist(lastUEntity);
            t.commit();
        }
        return lastUEntity;
    }

    /*
     * This method maps the names of the available dictionaries
     * to the local dictionary instances. This is needed because
     * we can't assume the id of the remote database will match those
     * of the local.
     */
    private void initDictMap() {
        List<Dictionary> dictionaries = DictionaryService.getInstance().getDictionaries();
        for (Dictionary d : dictionaries) {
            dictMap.put(d.getName(), d);
        }
    }

    /**
     * Checks for updates. Must be called before update().
     *
     * @return true if updates are available
     */

    public boolean checkForUpdates() {
        LastUpdateEntity lastUpdate = getLastUpdateEntity();

        LOGGER.info("Last update on: "+lastUpdate.getModified());
        if (!EM_REMOTE.createNamedQuery("UpdateEntry.checkForUpdates").setParameter("date", lastUpdate.getModified()).getResultList().isEmpty()) {
            this.lastUpdate = lastUpdate;
            return true;
        }
        this.lastUpdate = null;
        return false;
    }

    /**
     * Updates the local database.
     */
    public void update() throws InterruptedException {
        if (lastUpdate == null) {
            return;
        }
        initDictMap();
        //get all UpdateEntries commited after the last update
        List<UpdateEntry> UpdateEntries = EM_REMOTE.createNamedQuery("UpdateEntry.checkForUpdates").setParameter("date", lastUpdate.getModified()).getResultList();

        DictionaryService service = DictionaryService.getInstance();
        EntityTransaction t = EM.getTransaction();
        t.begin();

        //keeps track of the latest date we are updating to
        Date latestDate = new Date(0);
        for (UpdateEntry updateEntry : UpdateEntries) {
            if (latestDate.before(updateEntry.getCreated())) {
                latestDate = updateEntry.getCreated();
            }
            //get all RemoteDictionaryEntries in the current UpdateEntry
            List<RemoteDictionaryEntry> remoteDictionaryEntries = EM_REMOTE.createNamedQuery("UpdateEntry.getRemoteEntries").setParameter("updateEntry", updateEntry).getResultList();
            if (remoteDictionaryEntries.isEmpty()) {
                LOGGER.info("No remoteDictionaryEntries found");
                throw new IllegalStateException("No remoteDictionaryEntries found");
            }

            for (RemoteDictionaryEntry entry : remoteDictionaryEntries) {
                /*
                 * Checking for interruption, will be called frequently
                 * keeping us responive.
                 */
                if(Thread.interrupted()){
                    t.rollback();
                    throw new InterruptedException();
                }

                //try to get the local Dictionary
                Dictionary dictionary = dictMap.get(entry.getDictionary().getName());
                if (dictionary == null) {
                    /*
                     * This will happen if more dictionaries are available in the
                     * remote database so we are skipping those entries.
                     */

                    LOGGER.info("Dictionary not available skipping");
                    continue;
                }

                try {
                    service.getTranslation(entry.getWord(), dictionary);

                    //Checking if the dictionary entry translation is being updated
                    DictionaryEntry de = (DictionaryEntry) EM.createQuery("select de from DictionaryEntry de where de.word = :word and de.dictionary = :dictionary").setParameter("word", entry.getWord()).setParameter("dictionary", dictionary).getSingleResult();
                    de.setTranslation(entry.getTranslation());

                    LOGGER.info("updating DictionaryEntry");
                    EM.persist(de);
                } catch (NoResultException e) {

                    //Not an update adding new dictionary entry
                    DictionaryEntry de = entry.toDictionaryEntry();
                    de.setDictionary(dictionary);

                    LOGGER.info("adding DictionaryEntry");
                    EM.persist(de);
                }

            }
        }
        //Keep track of the latest update date
        lastUpdate.setModified(latestDate);
        t.commit();
    }
}
