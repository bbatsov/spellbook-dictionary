package com.drowltd.spellbook.core.service;

import com.drowltd.spellbook.core.exception.UpdateServiceException;
import com.drowltd.spellbook.core.model.Dictionary;
import com.drowltd.spellbook.core.model.DictionaryEntry;
import com.drowltd.spellbook.core.model.LastUpdateEntity;
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
    private LastUpdateEntity lastUpdate;
    private final Map<String, Dictionary> dictMap = new HashMap<String, Dictionary>();

    private LastUpdateEntity getLastUpdateEntity() {
        LastUpdateEntity lastUEntity;
        try {
            lastUEntity = (LastUpdateEntity) EM.createQuery("select lue from LastUpdateEntity lue").getSingleResult();
        } catch (javax.persistence.PersistenceException e) {
            lastUEntity = new LastUpdateEntity();
            lastUEntity.setModified(new Date(0));
            EntityTransaction t = EM.getTransaction();
            t.begin();
            EM.persist(lastUEntity);
            t.commit();
        }
        return lastUEntity;
    }

    private void initDictMap() {
        List<Dictionary> dictionaries = DictionaryService.getInstance().getDictionaries();
        for (Dictionary d : dictionaries) {
            dictMap.put(d.getName(), d);
        }
    }

    public boolean checkForUpdates() {
        LastUpdateEntity lastUpdate = getLastUpdateEntity();

        if (!EM_REMOTE.createNamedQuery("UpdateEntry.checkForUpdates").setParameter("date", lastUpdate.getModified()).getResultList().isEmpty()) {
            this.lastUpdate = lastUpdate;
            return true;
        }
        this.lastUpdate = null;
        return false;
    }

    public void update() {
        if (lastUpdate == null) {
            return;
        }
        initDictMap();
        List<UpdateEntry> Entries = EM_REMOTE.createNamedQuery("UpdateEntry.checkForUpdates").setParameter("date", lastUpdate.getModified()).getResultList();

        DictionaryService service = DictionaryService.getInstance();
        EntityTransaction t = EM.getTransaction();
        t.begin();
        Date lastDate = new Date(0);
        for (UpdateEntry updateEntry : Entries) {
            if (lastDate.before(updateEntry.getCreated())) {
                lastDate = updateEntry.getCreated();
            }
            List<RemoteDictionaryEntry> remoteDictionaryEntries = EM_REMOTE.createNamedQuery("UpdateEntry.getRemoteEntries").setParameter("updateEntry", updateEntry).getResultList();
            if (remoteDictionaryEntries.isEmpty()) {
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
        lastUpdate.setModified(lastDate);
        t.commit();
    }
}
