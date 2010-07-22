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
    public boolean misspelled(String word) {
        return !nWords.containsKey(word.toLowerCase());
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
    public List<String> correct(String word) {

        if (word == null) {
            return Collections.emptyList();
        }

        String wordInLowerCase = word.toLowerCase();

        if (nWords.containsKey(wordInLowerCase)) {
            return Collections.emptyList();
        }

        List<String> list = edits0(wordInLowerCase, language.getAlphabet());

        if (list.size() > 0) {
            return list;
        }
        return Collections.emptyList();
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
