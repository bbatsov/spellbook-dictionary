/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drowltd.spellbook.core.service;

import com.drowltd.spellbook.core.exception.DictionaryDbLockedException;
import com.drowltd.spellbook.core.model.Dictionary;
import com.drowltd.spellbook.core.model.DictionaryEntry;
import com.drowltd.spellbook.core.model.Language;
import com.drowltd.spellbook.core.model.RatingsEntry;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
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
        assertEquals("translation is not not in the db", translation, dictionaryService.getTranslation(word, dictionary));
    }

    @Test
    public void testAddWord() {

        final String nWord = "new";
        final String nTranslation = "new trans";

        dictionaryService.addWord(nWord, nTranslation, dictionary);

        assertTrue("word not added", dictionaryService.getWordsFromDictionary(dictionary).contains(nWord));
        assertEquals("translation not added", nTranslation, dictionaryService.getTranslation(nWord, dictionary));

    }

    @Test
    public void testUpateWord() {
        final String nTranslation = "new trans";

        dictionaryService.upateWord(word, nTranslation, dictionary);
        assertEquals("word not updated", nTranslation, dictionaryService.getTranslation(word, dictionary));

    }

    @Test
    public void testContainsWord() {
        assertTrue("word is not contained", dictionaryService.containsWord(word, dictionary));
    }

    @Test
    public void testGetApproximation() {
        assertEquals("approximation not found", word, dictionaryService.getApproximation(dictionary, word + "b"));
    }

    @Test
    public void testGetRatings() {
        Map<String, Integer> ratings = new HashMap<String, Integer>();
        ratings.put(word, Integer.MAX_VALUE);

        assertEquals("ratings doesn't match", ratings, dictionaryService.getRatings(dictionary));
    }

    @Test
    public void testGetDictionary() {

        assertEquals("dictionaries doesn't match", dictionary, dictionaryService.getDictionary(Language.ENGLISH, Language.BULGARIAN));
    }

    @Test
    public void testGetLanguagesTo() {
        
        assertTrue("languages doesn't match", dictionaryService.getToLanguages(Language.ENGLISH).contains(Language.BULGARIAN));
    }

    private static void init() {
        final EntityTransaction t = EM.getTransaction();
        t.begin();
        dictionary = new Dictionary();
        dictionary.setName("English-Bulgarian");
        dictionary.setIconName("en-bg.png");
        dictionary.setFromLanguage(Language.ENGLISH);
        dictionary.setToLanguage(Language.BULGARIAN);


        dictionaryEntry = new DictionaryEntry();
        dictionaryEntry.setDictionary(dictionary);
        dictionaryEntry.setWord(word);
        dictionaryEntry.setWordTranslation(translation);
        dictionaryEntry.setSpellcheckRank(1);
        dictionaryEntry.setAddedByUser(true);

        final RatingsEntry re = new RatingsEntry();
        re.setDictionary(dictionary);
        re.setLang(Language.ENGLISH);
        re.setWord(word);
        re.setSpellcheckRank(Integer.MAX_VALUE);


        EM.persist(dictionary);
        EM.persist(dictionaryEntry);
        EM.persist(re);
        t.commit();
    }
}
