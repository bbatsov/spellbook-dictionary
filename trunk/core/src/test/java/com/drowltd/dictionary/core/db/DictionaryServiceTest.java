package com.drowltd.dictionary.core.db;

import com.drowltd.dictionary.core.exception.NoDictionariesAvailableException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author iivalchev
 */
public class DictionaryServiceTest extends AbstractDictionaryServiceTest {

    private DictionaryService dictionaryService;

    public DictionaryServiceTest() {
    }

    @Before
    public void init() throws SQLException, NoDictionariesAvailableException, IOException {
        final String pathToDB = "resources/db.sql";
        connection.prepareStatement(readDbFromFile(pathToDB)).execute();

        dictionaryService = new DictionaryService(connection);

    }

    @Test
    public void testConstructorAndPopulate() {
        assertTrue("ConfigMaps doesn't match", dictionaryService.getDictConfigMap().equals(dictConfigMap));
    }

    @Test
    public void testGetLoadedDictionaries() {
        assertTrue("Dictionaries doesn't match", dictionaryService.getLoadedDictionaries().equals(new ArrayList<SDictionary>(dictConfigMap.keySet())));
    }

    @Test
    public void testGetWordsFromDictionary() throws SQLException {
        Map<String, Integer> wordsBgExpected = new HashMap<String, Integer>();
        wordsBgExpected.put("\u0430", 16);

        final Map<String, Integer> wordsBgActual = dictionaryService.getWordsFromDictionary(dictionaryBG_EN);

        assertTrue("Words from db doesn't match", wordsBgExpected.equals(wordsBgActual));
    }

    @Test
    public void testGetTranslation() throws SQLException {
        String translationExpected = "a";
        String word = "\u0430";

        final String translationActual = dictionaryService.getTranslation(dictionaryBG_EN, word);

        assertTrue("Translation doesn't matctch", translationExpected.equals(translationActual));

    }

    @Test
    public void testAddWordWithRating() throws SQLException {
        String word = "b";
        String translation = "ab";
        int rating = 7;

        assertTrue("word not inserted", dictionaryService.addWord(dictionaryBG_EN, word, translation, rating));
        final ResultSet rs = connection.prepareStatement("SELECT WORD, TRANSLATION, RATING FROM BG_EN WHERE WORD='b' AND TRANSLATION = 'ab' AND RATING = 7").executeQuery();
        assertTrue("word not selected", rs.next());

    }

    @Test
    public void testGetRatings() throws SQLException {
        Map<String, Integer> ratingsMapExpected = new HashMap<String, Integer>();
        ratingsMapExpected.put("\u0430", 16);
        ratingsMapExpected.put("\u0431", 5);

        final Map<String, Integer> ratingsMapActual = dictionaryService.getRatings(bulgarian);

        assertTrue("ratings map do not match", ratingsMapExpected.equals(ratingsMapActual));

    }

    @Test
    public void testUpdateTranslation() throws SQLException {
        String newTranslation = "spellbook";
        String word = "\u0430";

        dictionaryService.updateTranslation(dictionaryBG_EN, word, newTranslation);
        final ResultSet rs = connection.prepareStatement("SELECT TRANSLATION FROM BG_EN").executeQuery();

        final boolean next = rs.next();
        assertTrue("No translation selected", next);
        assertTrue("Translation doesn't match", rs.getString(1).equals(newTranslation));

    }

    @Test
    public void testUpdateWord() throws SQLException {

        String oldWord = "\u0430";
        String newWord = "\u0432";

        dictionaryService.updateWord(dictionaryBG_EN, oldWord, newWord);
        final ResultSet rs = connection.prepareStatement("SELECT WORD FROM BG_EN WHERE WORD = \'" + newWord + "\'").executeQuery();
        final boolean next = rs.next();
        assertTrue("No word selected", next);
        assertTrue("Words do not match", newWord.equals(rs.getString(1)));

    }

    @Test
    public void testGetDictionary() {

        assertTrue("Dictionaries doesn't match", dictionaryBG_EN.equals(dictionaryService.getDictionary(bulgarian, english)));
    }

    @Test
    public void testGetLanguagesTo() {
        List<Language> languages = new ArrayList<Language>(1);
        languages.add(english);

        assertTrue("Languages list doesn't match", languages.equals(dictionaryService.getLanguagesTo(bulgarian)));
    }
}
