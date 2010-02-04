package com.drowltd.dictionary.core.db;

import com.drowltd.dictionary.core.exception.NoDictionariesAvailableException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
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
    public void init() throws SQLException, NoDictionariesAvailableException {
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
    public void testGetWordsFromDictionary() throws SQLException{
        Map<String, Integer> wordsBgExpected = new HashMap<String, Integer>();
        wordsBgExpected.put("\u0430", 16);

        final Map<String, Integer> wordsBgActual = dictionaryService.getWordsFromDictionary(dictionaryBG_EN);

        assertTrue("Words from db doesn't match",wordsBgExpected.equals(wordsBgActual));
    }

    @Test
    public void testGetTranslation() throws SQLException{
        String translationExpected = "a";
        String word = "\u0430";

        final String translationActual = dictionaryService.getTranslation(dictionaryBG_EN, word);

        assertTrue("Translation doesn't matctch", translationExpected.equals(translationActual));

    }
    
}
