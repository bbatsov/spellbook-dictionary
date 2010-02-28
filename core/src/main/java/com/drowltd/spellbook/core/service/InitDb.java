/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drowltd.spellbook.core.service;

import com.drowltd.spellbook.core.db.DatabaseService;
import com.drowltd.spellbook.core.exception.DictionaryDbLockedException;
import com.drowltd.spellbook.core.model.Dictionary;
import com.drowltd.spellbook.core.model.DictionaryEntry;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.transaction.Transaction;

/**
 *
 * @author bozhidar
 */
public class InitDb {
    private static EntityManager em;

    private static DatabaseService databaseService;

    public static void main(String[] args) throws DictionaryDbLockedException {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("Spellbook");

        em = emf.createEntityManager();

        DatabaseService.init("/opt/spellbook/db/dictionary.data.db");
        databaseService = DatabaseService.getInstance();

        copyLegacyDict(com.drowltd.spellbook.core.db.Dictionary.EN_BG);
        copyLegacyDict(com.drowltd.spellbook.core.db.Dictionary.BG_EN);
    }

    private static void copyLegacyDict(com.drowltd.spellbook.core.db.Dictionary d) {
        EntityTransaction t = em.getTransaction();
        t.begin();

        Dictionary newDict = new Dictionary();
        newDict.setName(d.toString());

        em.persist(newDict);

        List<String> allWords = databaseService.getWordsFromDictionary(d);

        for (String string : allWords) {
            String translation = databaseService.getTranslation(d, string);

            DictionaryEntry de = new DictionaryEntry();
            de.setDictionary(newDict);
            de.setWord(string);
            de.setWordTranslation(translation);
            de.setSpellcheckRank(0);
            de.setAddedByUser(false);

            em.persist(de);
        }

        t.commit();
    }
}
