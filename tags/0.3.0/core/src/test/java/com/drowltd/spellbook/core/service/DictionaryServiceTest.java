package com.drowltd.spellbook.core.service;

import com.drowltd.spellbook.core.model.Dictionary;
import com.drowltd.spellbook.core.model.DictionaryEntry;
import com.drowltd.spellbook.core.model.Language;
import com.drowltd.spellbook.core.model.RankEntry;
import com.drowltd.spellbook.core.model.UncommittedEntries;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ikkari
 */
@Ignore
public class DictionaryServiceTest {

    private static DictionaryService dictionaryService;
    private static EntityManager EM = null;
    private static Dictionary dictionary;
    private static DictionaryEntry dictionaryEntry;
    private static Language English = Language.ENGLISH;
    private static Language Bulgarian = Language.BULGARIAN;
    private static String word = "word";
    private static String ratingsWord = "rating";
    private static String translation = "translation";

    public DictionaryServiceTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        //DictionaryService.init("mem:db1.data.db");
        DictionaryService.init("/opt/spellbook/db/spellbook(copy).data.db");
        dictionaryService = DictionaryService.getInstance();

        Field field = AbstractPersistenceService.class.getDeclaredField("EM");
        field.setAccessible(true);

        EM = (EntityManager) field.get(dictionaryService);
        //init();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    //@Test
    public void testGetInstance() {
        assertNotNull(dictionaryService);
    }

    //@Test
    public void testGetDictionaries() {

        assertTrue("dictionary not added", dictionaryService.getDictionaries().contains(dictionary));
    }

    //@Test
    public void testGetWordsFromDictionary() {
        assertTrue("word is not not in the db", dictionaryService.getWordsFromDictionary(dictionary).contains(word));
    }

    //@Test
    public void testGetTranslation() {
        assertEquals("translation is not not in the db", translation, dictionaryService.getTranslation(word, dictionary));
    }

    @Test
    public void testAddWord() {

        final String nWord = "neww";
        final String nTranslation = "new trans";

        //dictionaryService.addWord(nWord, nTranslation, dictionary);


//        assertTrue("word not added", dictionaryService.getWordsFromDictionary(dictionary).contains(nWord));
//        assertEquals("translation not added", nTranslation, dictionaryService.getTranslation(nWord, dictionary));
//        assertTrue("ratings entry not inserted", dictionaryService.getRatings(English).keySet().contains(nWord));
        UncommittedEntries ue = EM.createNamedQuery("UncommittedEntries.getUncommittedEntries", UncommittedEntries.class).getSingleResult();
        assertTrue(ue != null);
        Set<DictionaryEntry> de = ue.getDictionaryEntries();
        assertTrue(!de.isEmpty());
        for(DictionaryEntry d: de){
            System.out.println(d.getWord());
        }


    }

    //@Test
    public void testContainsWord() {
        assertTrue("word is not contained", dictionaryService.containsWord(word, dictionary));
    }

    //@Test
    public void testGetRatings() {
        assertTrue("ratings doesn't match", dictionaryService.getRatings(English).containsKey(ratingsWord));
    }

   // @Test
    public void testGetDictionary() {

        assertEquals("dictionaries doesn't match", dictionary, dictionaryService.getDictionary(English, Bulgarian));
    }

    private static void init() {
        final EntityTransaction t = EM.getTransaction();
        t.begin();

        dictionary = new Dictionary();
        dictionary.setName("English-Bulgarian");
        dictionary.setIconName("en-bg.png");
        dictionary.setFromLanguage(English);
        dictionary.setToLanguage(Bulgarian);


        dictionaryEntry = new DictionaryEntry();
        dictionaryEntry.setDictionary(dictionary);
        dictionaryEntry.setWord(word);
        dictionaryEntry.setTranslation(translation);
        dictionaryEntry.setAddedByUser(true);

        final RankEntry re = new RankEntry();
        re.setLanguage(English);
        re.setWord(ratingsWord);
        re.setRank(Integer.MAX_VALUE);

        EM.persist(dictionary);
        EM.persist(dictionaryEntry);
        EM.persist(re);
        t.commit();
    }
}