/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drowltd.spellbook.core.service;

import com.drowltd.spellbook.core.model.Dictionary;
import java.util.List;
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
    private static final EntityManager EM = Persistence.createEntityManagerFactory("Spellbook").createEntityManager();

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
                List<String> matches = EM.createQuery("select de.word from DictionaryEntry de where de.dictionary = :dictionary and de.word like :searchKey order by de.word asc")
                        .setParameter("dictionary", dictionary).setParameter("searchKey", builder.append("%").toString()).getResultList();

                if (matches.size() > 0) {
                    return matches.get(0);
                }

                builder.deleteCharAt(builder.length() - 1);
            } while (builder.length() > 0);
        }

        return null;
    }
}
