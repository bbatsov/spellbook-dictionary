package com.drowltd.spellbook.core.service;

import com.drowltd.spellbook.core.exception.UpdateServiceException;
import com.drowltd.spellbook.core.model.Dictionary;
import com.drowltd.spellbook.core.model.DictionaryEntry;
import com.drowltd.spellbook.core.model.RemoteDictionaryEntry;
import com.drowltd.spellbook.core.model.UpdateEntry;
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
        if (INSTANCE == null) {
            INSTANCE = new UpdateService();
            return INSTANCE;
        }
        return INSTANCE;
    }

    protected static void initRemoteEntityManager() throws UpdateServiceException {
        try {
            EM_REMOTE = Persistence.createEntityManagerFactory("SpellbookRemote").createEntityManager();
        } catch (javax.persistence.PersistenceException e) {
            if (e.getMessage() != null) {
                if (e.getMessage().contains("Cannot open connection")) {
                    throw new UpdateServiceException();
                }
            }
        }
    }
    private Date updateFromDate;
    private final Map<String, Dictionary> dictMap = new HashMap<String, Dictionary>();

    private void initDictMap() {
        List<Dictionary> dictionaries = DictionaryService.getInstance().getDictionaries();
        for (Dictionary d : dictionaries) {
            dictMap.put(d.getName(), d);
        }
    }

    public boolean checkForUpdates(Date date) {
        if (date == null) {
            throw new IllegalArgumentException("date is null");
        }
        if (!EM_REMOTE.createNamedQuery("UpdateEntry.checkForUpdates").setParameter("date", date).getResultList().isEmpty()) {
            this.updateFromDate = date;
            return true;
        }
        return false;
    }

    public void update() {
        if (updateFromDate == null) {
            return;
        }
        initDictMap();
        List<UpdateEntry> Entries = EM_REMOTE.createNamedQuery("UpdateEntry.checkForUpdates").setParameter("date", updateFromDate).getResultList();

        DictionaryService service = DictionaryService.getInstance();
        EntityTransaction t = EM.getTransaction();
        t.begin();
        for (UpdateEntry updateEntry : Entries) {

            List<RemoteDictionaryEntry> remoteDictionaryEntries = EM_REMOTE.createNamedQuery("UpdateEntry.getRemoteEntries").setParameter("updateEntry", updateEntry).getResultList();
            if(remoteDictionaryEntries.isEmpty()){
                LOGGER.info("No remoteDictionaryEntries found");
                throw new IllegalStateException("No remoteDictionaryEntries found");
            }
            for (RemoteDictionaryEntry entry : remoteDictionaryEntries) {
                Dictionary dictionary = dictMap.get(entry.getDictionary().getName());
                if (dictionary == null) {
                    LOGGER.info("Dictionary not available skipping");
                    continue;
                }

                try {
                    service.getTranslation(entry.getWord(), dictionary);
                    DictionaryEntry de = (DictionaryEntry) EM.createQuery("select de from DictionaryEntry de where de.word = :word and de.dictionary = :dictionary").setParameter("word", entry.getWord()).setParameter("dictionary", dictionary).getSingleResult();
                    de.setTranslation(entry.getTranslation());
                    LOGGER.info("updating DictionaryEntry");
                    EM.persist(de);
                } catch (NoResultException e) {
                    DictionaryEntry de = entry.toDictionaryEntry();
                    de.setDictionary(dictionary);
                    LOGGER.info("adding DictionaryEntry");
                    EM.persist(de);
                }

            }
        }
        t.commit();
    }
}
