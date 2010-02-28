/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drowltd.spellbook.core.service;

import com.drowltd.spellbook.core.model.Dictionary;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;

/**
 *
 * @author bozhidar
 */
public class DictionaryService {
    private static final EntityManager EM = Persistence.createEntityManagerFactory("Spellbook").createEntityManager();

    public List<Dictionary> getDictionaries() {
        return EM.createNamedQuery("Dictionary.getAllDictionaries").getResultList();
    }

    public List<String> getWordsFromDictionary(Dictionary d) {
        return EM.createQuery("select de.word from DictionaryEntry de where de.dictionary = :dictionary").setParameter("dictionary", d).getResultList();
    }

    public String getTranslation(String word, Dictionary d) {
        return (String)EM.createQuery("select de.wordTranslation from DictionaryEntry de where de.word = :word and de.dictionary = :dictionary")
                .setParameter("word", word).setParameter("dictionary", d).getSingleResult();
    }
}
