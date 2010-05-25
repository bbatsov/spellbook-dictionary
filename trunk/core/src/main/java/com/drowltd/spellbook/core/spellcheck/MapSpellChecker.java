package com.drowltd.spellbook.core.spellcheck;

import com.drowltd.spellbook.core.model.Language;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author iivalchev
 * @since 0.2
 *        <p/>
 *        Implementation of the Peter Norvig Toy Spelling corrector
 */
public class MapSpellChecker implements SpellChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(MapSpellChecker.class);
    private static SpellChecker INSTANCE;
    private final Language language;
    private final Map<String, Integer> nWords;
    private final EditsGenerator generator = new EditsGenerator();

    /**
     * @param nWords containing the words
     */
//    public MapSpellChecker(Map<String, Integer> nWords) {
//        this(nWords, Dictionary.getSelectedDictionary());
//
//    }
    public MapSpellChecker(Map<String, Integer> nWords, Language language) {
        if (nWords == null) {
            throw new NullPointerException("nWords is null");
        }

        if (nWords.isEmpty()) {
            throw new IllegalArgumentException("nWords is empty");
        }

        this.nWords = nWords;

        if (language == null) {
            LOGGER.error("language is null");
            throw new NullPointerException("language is null");
        }

        this.language = language;

        setInstance(this);
    }

    protected final List<String> edits0(String word, String alphabet) {
        assert alphabet != null && !alphabet.isEmpty() : "alphabet == null  || alphabet.isEmpty()";

        return generator.recycle(word, alphabet).generate();

    }

    /**
     * Check if the word is in the Collection of words in other way
     * if the word is correct.
     *
     * @param word
     * @return true if the word is contained, false otherwise.
     */
    @Override
    public boolean checkWord(String word) {
        return nWords.containsKey(word.toLowerCase());
    }

    /**
     * Returns correction candidates for given word.
     *
     * @param word the word to be corrected
     * @retun an empty map if word is null ,
     * Map<Integer, String> with key == 0 && value == word if
     * the word does not need correction or if
     * no correction candidates are found.
     */
    @Override
    public Map<String, Integer> correct(String word) {

        if (word == null) {
            return Collections.emptyMap();
        }

        String wordInLowerCase = word.toLowerCase();

        if (nWords.containsKey(wordInLowerCase)) {
            return Collections.emptyMap();
        }

        List<String> list = edits0(wordInLowerCase, language.getAlphabet());
        Map<String, Integer> candidates = new HashMap<String, Integer>();
        for (String s : list) {
            if (nWords.containsKey(s)) {
                candidates.put(s, nWords.get(s));
            }
        }
        if (candidates.size() > 0) {
            return candidates;
        }
//        System.gc();
//        for (String s : list) {
//            for (String w : edits0(s, language.getAlphabet())) {
//                if (nWords.containsKey(w)) {
//                    candidates.put(w, nWords.get(w));
//                }
//            }
//        }
//
//        if (candidates.size() > 0) {
//            return candidates;
//        }
        return Collections.emptyMap();
    }

    @Override
    public Language getLanguage() {
        return language;
    }

    public synchronized static SpellChecker getInstance() {
        if (INSTANCE == null) {
            LOGGER.error("instance is null");
            throw new IllegalStateException("instance is null");
        }
        return INSTANCE;
    }

    private synchronized static void setInstance(SpellChecker instance) {
        if (instance != null) {
            MapSpellChecker.INSTANCE = instance;
        }
    }
}
