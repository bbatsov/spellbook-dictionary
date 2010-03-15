package com.drowltd.spellbook.core.spellcheck;

import com.drowltd.spellbook.core.model.Language;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author iivalchev
 * @since 0.2
 *
 * Implementation of the Peter Norvig Toy Spelling corrector
 * by <a href="http://raelcunha.com/spell-correct.php">Rael Cunha</a>.
 *
 */
public final class SpellChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpellChecker.class);
    private static SpellChecker INSTANCE;
    //private final Dictionary dictionary;
    private final Language language;
    private final Map<String, Integer> nWords;

    /**
     *
     * @param the Map<String, Integer> containing the words
     */
//    public SpellChecker(Map<String, Integer> nWords) {
//        this(nWords, Dictionary.getSelectedDictionary());
//
//    }

    public SpellChecker(Map<String, Integer> nWords, Language language) {
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

    private final ArrayList<String> edits(String word) {

        String alphabet = language.getAlphabet();

        ArrayList<String> result = new ArrayList<String>();
        // Deletion
        for (int i = 0; i < word.length(); ++i) {
            result.add(word.substring(0, i) + word.substring(i + 1));
        }
        // Transposition
        for (int i = 0; i < word.length() - 1; ++i) {
            result.add(word.substring(0, i) + word.substring(i + 1, i + 2) + word.substring(i, i + 1) + word.substring(i + 2));
        }
        // Alternation
        for (int i = 0; i < word.length(); ++i) {
            for (char c = alphabet.charAt(0); c <= alphabet.charAt(alphabet.length() - 1); ++c) {
                result.add(word.substring(0, i) + String.valueOf(c) + word.substring(i + 1));
            }
        }
        // Insertion
        for (int i = 0; i <= word.length(); ++i) {
            for (char c = alphabet.charAt(0); c <= alphabet.charAt(alphabet.length() - 1); ++c) {
                result.add(word.substring(0, i) + String.valueOf(c) + word.substring(i));
            }
        }
        return result;
    }

    /**
     * Check if the word is in the Collection of words in other way
     * if the word is correct.
     *
     * @param word
     * @return true if the word is contained, false otherwise.
     */
    public boolean checkWord(String word) {
        return nWords.containsKey(word.toLowerCase());
    }

    /**
     * Returns correction candidates for given word.
     *
     * @param word the word to be corrected
     * @retun an empty map if word is null ,
     *        Map<Integer, String> with key == 0 && value == word if
     *        the word does not need correction or if
     *        no correction candidates are found.
     */
    public final Map<String, Integer> correct(String word) {

        if (word == null) {
            return Collections.emptyMap();
        }

        String wordInLowerCase = word.toLowerCase();

        if (nWords.containsKey(wordInLowerCase)) {
            return Collections.emptyMap();
        }

        ArrayList<String> list = edits(wordInLowerCase);
        Map<String, Integer> candidates = new HashMap<String, Integer>();
        for (String s : list) {
            if (nWords.containsKey(s)) {
                candidates.put(s,nWords.get(s));
            }
        }
        if (candidates.size() > 0) {
            return candidates;
        }
        for (String s : list) {
            for (String w : edits(s)) {
                if (nWords.containsKey(w)) {
                    candidates.put(w,nWords.get(w));
                }
            }
        }

        if (candidates.size() > 0) {
            return candidates;
        }
        return Collections.emptyMap();
    }

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
            SpellChecker.INSTANCE = instance;
        }
    }
}
