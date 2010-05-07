package com.drowltd.spellbook.core.service;

import com.drowltd.spellbook.core.exception.AuthenticationException;
import com.drowltd.spellbook.core.exception.UpdateServiceException;
import com.drowltd.spellbook.core.model.Dictionary;
import com.drowltd.spellbook.core.model.DictionaryEntry;
import com.drowltd.spellbook.core.model.LastUpdateEntity;
import com.drowltd.spellbook.core.model.RankEntry;
import com.drowltd.spellbook.core.model.RemoteDictionary;
import com.drowltd.spellbook.core.model.RemoteDictionaryEntry;
import com.drowltd.spellbook.core.model.RevisionEntry;
import com.drowltd.spellbook.core.model.UncommittedEntries;
import com.drowltd.spellbook.core.model.UpdateEntry;
import java.sql.DriverManager;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private static boolean isAuthenticated = false;
    private ConflictHandler handler;

    private UpdateService() throws UpdateServiceException {
        if (EM == null) {
            throw new IllegalStateException("DictionaryService not initialized");
        }
        initRemoteEntityManager();
    }

    private UpdateService(String userName, String password) throws AuthenticationException, UpdateServiceException {
        initRemoteEntityManager(userName, password);
    }

    public static UpdateService getInstance() throws UpdateServiceException {
        if (INSTANCE == null) {
            INSTANCE = new UpdateService();
        }
        return INSTANCE;
    }

    public static UpdateService getInstance(String userName, String password) throws AuthenticationException, UpdateServiceException {
        if (INSTANCE == null || !isAuthenticated) {
            INSTANCE = new UpdateService(userName, password);
        }
        return INSTANCE;
    }

    public static void initRemoteEntityManager(String userName, String password) throws AuthenticationException, UpdateServiceException {
        if (userName == null || userName.isEmpty() || password == null || password.isEmpty()) {
            throw new AuthenticationException();
        }
        try {
            Class.forName("com.mysql.jdbc.Driver");
            DriverManager.getConnection("jdbc:mysql://localhost:3306/SpellbookRemote", userName, password).close();
            Map<String, String> properties = new HashMap<String, String>();
            properties.put("hibernate.connection.username", userName);
            properties.put("hibernate.connection.password", password);
            EM_REMOTE = Persistence.createEntityManagerFactory("SpellbookRemote", properties).createEntityManager();
            isAuthenticated = true;
        } catch (Exception e) {
            throw new UpdateServiceException(e);
        }
    }

    protected static void initRemoteEntityManager() throws UpdateServiceException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            DriverManager.getConnection("jdbc:mysql://localhost:3306/SpellbookRemote", "spellbook", "").close();
            EM_REMOTE = Persistence.createEntityManagerFactory("SpellbookRemote").createEntityManager();
            isAuthenticated = false;
        } catch (Exception e) {
            throw new UpdateServiceException(e);
        }
    }
    private LastUpdateEntity lastUpdate;
    private final Map<String, Dictionary> dictMap = new HashMap<String, Dictionary>();
    private final Map<String, RemoteDictionary> remoteDictMap = new HashMap<String, RemoteDictionary>();

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

    private void initRemoteDictMap() {
        List<RemoteDictionary> dictionaries = EM_REMOTE.createNamedQuery("RemoteDictionary.gerRemoteDictionaries", RemoteDictionary.class).getResultList();
        for (RemoteDictionary d : dictionaries) {
            remoteDictMap.put(d.getName(), d);
        }
    }

    /**
     * Checks for updates. Must be called before update().
     *
     * @return true if updates are available
     */
    public boolean checkForUpdates() {
        LastUpdateEntity lastUpdate = getLastUpdateEntity();

        LOGGER.info("Last update on: " + lastUpdate.getModified());
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
            LOGGER.info("No updates available or not checked");
            return;
        }
        initDictMap();
        //get all UpdateEntries commited after the last update
        List<UpdateEntry> UpdateEntries = EM_REMOTE.createNamedQuery("UpdateEntry.checkForUpdates").setParameter("date", lastUpdate.getModified()).getResultList();
        if (UpdateEntries.isEmpty()) {
            LOGGER.info("No UpdateEntries found");
            throw new IllegalStateException("No UpdateEntries found");
        }

        UncommittedEntries uncommitted = DictionaryService.getInstance().getUncommitted();
        DictionaryService service = DictionaryService.getInstance();
        EntityTransaction t = EM.getTransaction();
        t.begin();

        Set<RemoteDictionaryEntry> remoteDictionaryEntries = new HashSet<RemoteDictionaryEntry>();

        //keeps track of the latest date we are updating to
        Date latestDate = lastUpdate.getModified();
        for (UpdateEntry updateEntry : UpdateEntries) {
            if (latestDate.before(updateEntry.getCreated())) {
                latestDate = updateEntry.getCreated();
            }
            Set<RemoteDictionaryEntry> rDictionaryEntries = updateEntry.getRemoteDictionaryEntries();
            //  Needed because the same RemoteDictionaryEntry may appear in different updates
            //  but we need it only once
            remoteDictionaryEntries.addAll(rDictionaryEntries);
        }

        LOGGER.info("Last UpdateEntry.date == " + latestDate.toString());

        for (RemoteDictionaryEntry entry : remoteDictionaryEntries) {
            /*
             * Checking for interruption, will be called frequently
             * keeping us responive.
             */
            if (Thread.interrupted()) {
                LOGGER.warn("update interrupted");
                t.rollback();
                throw new InterruptedException();
            }

            //try to get the local Dictionary
            Dictionary dictionary = dictMap.get(entry.getRemoteDictionary().getName());
            if (dictionary == null) {
                /*
                 * This will happen if more dictionaries are available in the
                 * remote database so we are skipping those entries.
                 */

                LOGGER.info("Dictionary not available skipping");
                continue;
            }

            //Getting the last revision
            RevisionEntry revisionEntry = EM_REMOTE.createNamedQuery("RemoteDictionaryEntry.getLastRevision", RevisionEntry.class).setParameter("remoteDictionaryEntry", entry).getSingleResult();

            try {
                service.getTranslation(entry.getWord(), dictionary);

                //Checking if the dictionary entry translation is being updated
                DictionaryEntry de = (DictionaryEntry) EM.createQuery("select de from DictionaryEntry de where de.word = :word and de.dictionary = :dictionary").setParameter("word", entry.getWord()).setParameter("dictionary", dictionary).getSingleResult();

                String translation = null;
                if (uncommitted.getDictionaryEntries().contains(de) && handler != null) {
                    translation = handler.handle(entry.getWord(), de.getTranslation(), revisionEntry.getTranslation());
                } else {
                    translation = revisionEntry.getTranslation();
                }

                de.setTranslation(translation);

                LOGGER.info("updating DictionaryEntry");
                EM.persist(de);
            } catch (NoResultException e) {

                //Not an update adding new dictionary entry
                DictionaryEntry de = revisionEntry.toDictionaryEntry();
                de.setDictionary(dictionary);

                //Adding RankEntry
                RankEntry re = revisionEntry.toRankEntry();

                LOGGER.info("adding DictionaryEntry");
                EM.persist(de);
                EM.persist(re);
            }

        }

        //Keep track of the latest update date
        lastUpdate.setModified(latestDate);
        t.commit();
    }

    public void commit() {
        if (!isAuthenticated) {
            return;
        }
        UncommittedEntries uncommitted = DictionaryService.getInstance().getUncommitted();

        initRemoteDictMap();

        EM_REMOTE.getTransaction().begin();

        UpdateEntry updateEntry = new UpdateEntry();
        EM_REMOTE.persist(updateEntry);

        for (DictionaryEntry de : uncommitted.getDictionaryEntries()) {
            RemoteDictionaryEntry rde;
            try {
                rde = EM_REMOTE.createNamedQuery("RemoteDictionaryEntry.getRemoteDictionaryEntry", RemoteDictionaryEntry.class).setParameter("word", de.getWord()).getSingleResult();
            } catch (NoResultException e) {
                rde = new RemoteDictionaryEntry();
                rde.setWord(de.getWord());
            }

            RevisionEntry revisionEntry = new RevisionEntry();
            revisionEntry.setTranslation(de.getTranslation());
            revisionEntry.setRemoteDictionaryEntry(rde);

            updateEntry.addRemoteDictionaryEntry(rde);

            Dictionary dictionary = de.getDictionary();
            RemoteDictionary remoteDictionary = remoteDictMap.get(dictionary.getName());

            if (remoteDictionary == null) {
                remoteDictionary = new RemoteDictionary();
                remoteDictionary.setName(dictionary.getName());
                remoteDictionary.setFromLanguage(dictionary.getFromLanguage());
                remoteDictionary.setToLanguage(dictionary.getToLanguage());

                EM_REMOTE.persist(remoteDictionary);
                remoteDictMap.put(remoteDictionary.getName(), remoteDictionary);
            }

            rde.setRemoteDictionary(remoteDictionary);


            EM_REMOTE.persist(rde);
            EM_REMOTE.persist(revisionEntry);
        }


        EM_REMOTE.getTransaction().commit();

        uncommitted.setCommitted(true);

        EM.getTransaction().begin();
        EM.merge(uncommitted);
        EM.getTransaction().commit();
    }

    public static EntityManager getEM_REMOTE() {
        return EM_REMOTE;
    }

    public void setHandler(ConflictHandler handler) {
        if (handler == null) {
            return;
        }
        this.handler = handler;
    }

    public static interface ConflictHandler {

        String handle(String word, String base, String remote) throws InterruptedException;
    }
}
