/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drowltd.spellbook.core.service;

import com.drowltd.spellbook.core.exception.DictionaryDbLockedException;
import com.drowltd.spellbook.core.model.Dictionary;
import com.drowltd.spellbook.core.model.DictionaryEntry;
import java.lang.reflect.Field;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ikkari
 */
public class DictionaryServiceTest {

    private static DictionaryService dictionaryService;
    private static EntityManager EM = null;
    private static Dictionary dictionary;
    private static DictionaryEntry dictionaryEntry;
    private static String word = "word";
    private static String translation = "translation";

    public DictionaryServiceTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception, DictionaryDbLockedException {
        DictionaryService.init("mem:db1.data.db");
        dictionaryService = DictionaryService.getInstance();

        Field field = DictionaryService.class.getDeclaredField("EM");
        field.setAccessible(true);

        EM = (EntityManager) field.get(dictionaryService);
        init();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }


    @Test
    public void testGetInstance() {
        assertNotNull(dictionaryService);
    }

    @Test
    public void testGetDictionaries() {

        assertTrue("dictionary not added", dictionaryService.getDictionaries().contains(dictionary));
    }

    @Test
    public void testGetWordsFromDictionary() {
        assertTrue("word is not not in the db", dictionaryService.getWordsFromDictionary(dictionary).contains(word));
    }

    @Test
    public void testGetTranslation() {
        assertTrue("translation is not not in the db", translation.equals(dictionaryService.getTranslation(word, dictionary)));
    }

    @Test
    public void testAddWord() {

        final String nWord = "new";
        final String nTranslation = "new trans";

        dictionaryService.addWord(nWord, nTranslation, dictionary);

        assertTrue("word not added", dictionaryService.getWordsFromDictionary(dictionary).contains(nWord));
        assertTrue("translation not added", nTranslation.equals(dictionaryService.getTranslation(nWord, dictionary)));

    }

    @Test
    public void testUpateWord() {
        final String nTranslation = "new trans";

        dictionaryService.upateWord(word, nTranslation, dictionary);
        assertTrue("word not updated", nTranslation.equals(dictionaryService.getTranslation(word, dictionary)));
        
    }

    @Test
    public void testContainsWord() {
        assertTrue("word is not contained", dictionaryService.containsWord(word, dictionary));
    }

   @Test
    public void testGetApproximation() {
        assertTrue("approximation not found", word.equals(dictionaryService.getApproximation(dictionary, word+"b")));
    }

    private static void init() {
        final EntityTransaction t = EM.getTransaction();
        t.begin();
        dictionary = new Dictionary();
        dictionary.setName("English-Bulgarian");
        dictionary.setIconName("en-bg.png");


        dictionaryEntry = new DictionaryEntry();
        dictionaryEntry.setDictionary(dictionary);
        dictionaryEntry.setWord(word);
        dictionaryEntry.setWordTranslation(translation);
        dictionaryEntry.setSpellcheckRank(1);
        dictionaryEntry.setAddedByUser(true);

        EM.persist(dictionary);
        EM.persist(dictionaryEntry);
        t.commit();
    }
}
