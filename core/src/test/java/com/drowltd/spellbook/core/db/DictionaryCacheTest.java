package com.drowltd.spellbook.core.db;

import com.drowltd.spellbook.core.db.DictionaryCache;
import java.util.ArrayList;
import java.util.HashMap;
import static com.drowltd.spellbook.core.db.Dictionaries.*;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author iivalchev
 */
public class DictionaryCacheTest {

    final DictionaryCache cache;
    static Map<String, Integer> wordsBgExpected = new LinkedHashMap<String, Integer>();
    static Map<String, Integer> wordsEnExpected = new LinkedHashMap<String, Integer>();
    static Map<String, Integer> ratingsBgExpected = new LinkedHashMap<String, Integer>();
    static Map<String, Integer> ratingsEnExpected = new LinkedHashMap<String, Integer>();

    static {
        wordsBgExpected.put("\u0430", 16);
        wordsEnExpected.put("a", 16);
        ratingsBgExpected.put("\u0431", 16);
        ratingsEnExpected.put("b", 16);
    }

    public DictionaryCacheTest() {
        cache = new DictionaryCache();
    }

    @Before
    public void addAndDecrease() {
        cache.addDictionary(dictionaryEN_BG, wordsEnExpected);
        cache.addDictionary(dictionaryBG_EN, wordsBgExpected);

        cache.addRatings(dictionaryEN_BG, ratingsEnExpected);
        cache.addRatings(dictionaryBG_EN, ratingsBgExpected);

        cache.setCacheSize(1);
    }

    @Test
    public void testGetWordsList() {
        final ArrayList<String> wordsList = new ArrayList<String>(wordsBgExpected.keySet());
        assertTrue("wordList doesn't match",wordsList.equals(cache.getWordsList(dictionaryBG_EN)));
    }

    @Test
    public void testGetDictionarySizeDecreased() {
        assertTrue("word map that should be returned doesn't match",
                wordsBgExpected.equals(cache.getWordsMap(dictionaryBG_EN)));
        assertTrue("word map != null ", cache.getWordsMap(dictionaryEN_BG) == null);
    }

    @Test
    public void testGetRatingsSizeDecreased() {

        Map<String, Integer> ratingsBgExpected = new HashMap<String, Integer>();
        
        ratingsBgExpected.putAll(wordsBgExpected);
        ratingsBgExpected.putAll(DictionaryCacheTest.ratingsBgExpected);

        assertTrue("ratings map that should be returned doesn't match",
                ratingsBgExpected.equals(cache.getRatingMap(dictionaryBG_EN)));
        assertTrue("ratings map != null ", cache.getRatingMap(dictionaryEN_BG) == null);
    }
}
