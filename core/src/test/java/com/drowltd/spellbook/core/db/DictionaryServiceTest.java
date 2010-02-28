package com.drowltd.spellbook.core.db;

import com.drowltd.spellbook.core.db.SDictionary;
import com.drowltd.spellbook.core.db.Language;
import com.drowltd.spellbook.core.db.DictionaryService.DictionaryConfig;
import com.drowltd.spellbook.core.exam.Difficulty;
import com.drowltd.spellbook.core.exception.NoDictionariesAvailableException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author iivalchev
 */
@Ignore
public class DictionaryServiceTest extends AbstractDBTestCase {

    static Map<SDictionary, DictionaryConfig> dictConfigMap = new HashMap<SDictionary, DictionaryConfig>();
    DictionaryService dictionaryService;

    @BeforeClass
    public static void setUpClass() throws Exception {

        setUpDB();

        DictionaryConfig configEN_BG = new DictionaryConfig(Dictionaries.dictionaryEN_BG, "EN_BG", "SPELLCHECK_EN");
        DictionaryConfig configBG_EN = new DictionaryConfig(Dictionaries.dictionaryBG_EN, "BG_EN", "SPELLCHECK_BG");


        dictConfigMap.put(Dictionaries.dictionaryEN_BG, configEN_BG);
        dictConfigMap.put(Dictionaries.dictionaryBG_EN, configBG_EN);
    }

    @Before
    public void init() throws SQLException, NoDictionariesAvailableException, IOException {
        initDB();
        dictionaryService = new DictionaryService(connection);

    }

    public DictionaryServiceTest() {
    }

    @Test
    public void testConstructorAndPopulate() {
        assertTrue("ConfigMaps doesn't match", dictionaryService.getDictConfigMap().equals(dictConfigMap));
    }

    @Test
    public void testGetAvailableDictionaries() {
        assertTrue("Dictionaries doesn't match", dictionaryService.getAvailableDictionaries().equals(new ArrayList<SDictionary>(dictConfigMap.keySet())));
    }

    @Test
    public void testGetWordsFromDictionary() throws SQLException {
        Map<String, Integer> wordsBgExpected = new HashMap<String, Integer>();
        wordsBgExpected.put("\u0430", 16);

        final Map<String, Integer> wordsBgActual = dictionaryService.getWordsFromDictionary(Dictionaries.dictionaryBG_EN);

        assertTrue("Words from db doesn't match", wordsBgExpected.equals(wordsBgActual));
    }

    @Test
    public void testGetTranslation() throws SQLException {
        String translationExpected = "a";
        String word = "\u0430";

        final String translationActual = dictionaryService.getTranslation(Dictionaries.dictionaryBG_EN, word);

        assertTrue("Translation doesn't matctch", translationExpected.equals(translationActual));

    }

    @Test
    public void testAddWordWithRating() throws SQLException {
        String word = "b";
        String translation = "ab";
        int rating = 7;

        assertTrue("word not inserted", dictionaryService.addWord(Dictionaries.dictionaryBG_EN, word, translation, rating));
        final ResultSet rs = connection.prepareStatement("SELECT WORD, TRANSLATION, RATING FROM BG_EN WHERE WORD='b' AND TRANSLATION = 'ab' AND RATING = 7").executeQuery();
        assertTrue("word not selected", rs.next());

    }

    @Test
    public void testGetRatings() throws SQLException {
        Map<String, Integer> ratingsMapExpected = new HashMap<String, Integer>();
        ratingsMapExpected.put("\u0430", 16);
        ratingsMapExpected.put("\u0431", 5);

        final Map<String, Integer> ratingsMapActual = dictionaryService.getRatings(Dictionaries.bulgarian);

        assertTrue("ratings map do not match", ratingsMapExpected.equals(ratingsMapActual));

    }

    @Test
    public void testUpdateTranslation() throws SQLException {
        String newTranslation = "spellbook";
        String word = "\u0430";

        dictionaryService.updateTranslation(Dictionaries.dictionaryBG_EN, word, newTranslation);
        final ResultSet rs = connection.prepareStatement("SELECT TRANSLATION FROM BG_EN").executeQuery();

        final boolean next = rs.next();
        assertTrue("No translation selected", next);
        assertTrue("Translation doesn't match", rs.getString(1).equals(newTranslation));

    }

    @Test
    public void testUpdateWord() throws SQLException {

        String oldWord = "\u0430";
        String newWord = "\u0432";

        dictionaryService.updateWord(Dictionaries.dictionaryBG_EN, oldWord, newWord);
        final ResultSet rs = connection.prepareStatement("SELECT WORD FROM BG_EN WHERE WORD = \'" + newWord + "\'").executeQuery();
        final boolean next = rs.next();
        assertTrue("No word selected", next);
        assertTrue("Words do not match", newWord.equals(rs.getString(1)));

    }

    @Test
    public void testGetDictionary() {

        assertTrue("Dictionaries doesn't match", Dictionaries.dictionaryBG_EN.equals(dictionaryService.getDictionary(Dictionaries.bulgarian, Dictionaries.english)));
    }

    @Test
    public void testGetLanguagesTo() {
        List<Language> languages = new ArrayList<Language>(1);
        languages.add(Dictionaries.english);

        assertTrue("Languages list doesn't match", languages.equals(dictionaryService.getLanguagesTo(Dictionaries.bulgarian)));
    }

    //@Test
    public void testGetDifficultyWords() throws SQLException {
        final List<String> difficultyWords = dictionaryService.getDifficultyWords(Dictionaries.dictionaryBG_EN, Difficulty.MEDIUM);

        assertTrue(difficultyWords.contains("\u0430"));
    }

    @Test
    public void testAddMisspelled() throws SQLException {
        dictionaryService.addMisspelled(Dictionaries.dictionaryBG_EN, "m");
        assertTrue("misspelled not inserted", connection.prepareStatement(
                "SELECT WORD FROM SPELLCHECK_BG WHERE WORD = 'm'").executeQuery().next());
    }

    @Test
    public void testGetRatingsCache() throws SQLException {
        Map<String, Integer> ratingsMapExpected = new HashMap<String, Integer>();
        ratingsMapExpected.put("\u0430", 16);
        ratingsMapExpected.put("\u0431", 5);

        Map<String, Integer> ratingsMapActual = dictionaryService.getRatings(Dictionaries.bulgarian);
        ratingsMapActual = dictionaryService.getRatings(Dictionaries.bulgarian);

        assertTrue("ratings map do not match", ratingsMapExpected.equals(ratingsMapActual));

    }

    @Test
    public void testGetApproxmiation() throws SQLException{
        final String expected = "aproximate";
        dictionaryService.addWord(Dictionaries.dictionaryEN_BG, expected, "trans" );
        final String actual = dictionaryService.getApproximation(Dictionaries.dictionaryEN_BG, "aproxximate");

        assertTrue("aproximation doesn't match",expected.equals(actual));

    }
}
