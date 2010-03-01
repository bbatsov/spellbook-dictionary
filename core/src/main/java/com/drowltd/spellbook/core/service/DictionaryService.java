/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drowltd.spellbook.core.service;

import com.drowltd.spellbook.core.exception.DictionaryDbLockedException;
import com.drowltd.spellbook.core.model.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author bozhidar
 */
public class DictionaryService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DictionaryService.class);
    private static EntityManager EM;
    private static DictionaryService instance;

    private DictionaryService(String dictDbFile) throws DictionaryDbLockedException {
        LOGGER.info("dictionary database: " + dictDbFile.replace(".data.db", ""));

        String url = "jdbc:h2:" + dictDbFile.replace(".data.db", "");

        try {
            // we need to override the db url from persistence.xml
            Map<String, String> properties = new HashMap<String, String>();
            properties.put("hibernate.connection.url", url);

            EM = Persistence.createEntityManagerFactory("Spellbook", properties).createEntityManager();
        } catch (javax.persistence.PersistenceException e) {
            if (e.getMessage() != null) {
                if (e.getMessage().contains("Cannot open connection")) {
                    throw new DictionaryDbLockedException();
                }
            }

            e.printStackTrace();
        }
    }

    /**
     * Bootstraps the dictionary service. The method can be executed only once.
     *
     * @param dictDbFile the dictionary database file
     *
     * @throws DictionaryDbLockedException if another process is already using the db file
     */
    public static void init(String dictDbFile) throws DictionaryDbLockedException {
        if (instance == null) {
            instance = new DictionaryService(dictDbFile);
        } else {
            LOGGER.info("Dictionary service is already initialized");
        }
    }

    public static DictionaryService getInstance() {
        return instance;
    }

    public List<Dictionary> getDictionaries() {
        return EM.createNamedQuery("Dictionary.getAllDictionaries").getResultList();
    }

    public List<String> getWordsFromDictionary(Dictionary d) {
        return EM.createQuery("select de.word from DictionaryEntry de where de.dictionary = :dictionary").setParameter("dictionary", d).getResultList();
    }

    public String getTranslation(String word, Dictionary d) {
        return (String) EM.createQuery("select de.wordTranslation from DictionaryEntry de where de.word = :word and de.dictionary = :dictionary").setParameter("word", word).setParameter("dictionary", d).getSingleResult();
    }

    public String getApproximation(Dictionary dictionary, String searchKey) {
        if (searchKey != null && !searchKey.isEmpty()) {

            LOGGER.info("Getting approximation for " + searchKey);

            StringBuilder builder = new StringBuilder(searchKey);

            // we start looking for approximate matches of the full search key, but if we fail - we start looking
            // for shorter matches
            do {
                List<String> matches = EM.createQuery("select de.word from DictionaryEntry de where de.dictionary = :dictionary and de.word like :searchKey order by de.word asc").setParameter("dictionary", dictionary).setParameter("searchKey", builder.append("%").toString()).getResultList();

                if (matches.size() > 0) {
                    return matches.get(0);
                }

                builder.deleteCharAt(builder.length() - 1);
            } while (builder.length() > 0);
        }

        return null;
    }
}
