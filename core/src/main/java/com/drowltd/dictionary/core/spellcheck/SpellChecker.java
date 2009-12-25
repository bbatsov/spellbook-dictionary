package com.drowltd.dictionary.core.spellcheck;

import com.drowltd.dictionary.core.db.Dictionary;
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

    private static Logger LOGGER = LoggerFactory.getLogger(SpellChecker.class);
    private static SpellChecker instance;
    private final String alphabet;
    private final Map<String, Integer> nWords;

    /**
     *
     * @param the Map<String, Integer> containing the words
     */
    public SpellChecker(Map<String, Integer> nWords) {
        this(nWords, Dictionary.getSelectedDictionary().getAlphabet());

    }

    public SpellChecker(Map<String, Integer> nWords, String alphabet) {
        if (nWords == null) {
            throw new NullPointerException("nWords is null");
        }

        if (nWords.isEmpty()) {
            throw new IllegalArgumentException("nWords is empty");
        }

        this.nWords = nWords;

        if (alphabet == null || alphabet.isEmpty()) {
            LOGGER.error("alphabet is null || empty");
            throw new IllegalArgumentException("alphabet is null || empty");
        }
        this.alphabet = alphabet;

        setInstance(this);
    }

    private final ArrayList<String> edits(String word) {
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
    public final Map<Integer, String> correct(String word) {

        if (word == null) {
            return Collections.emptyMap();
        }

        String wordInLowerCase = word.toLowerCase();

        if (nWords.containsKey(wordInLowerCase)) {
            return Collections.emptyMap();
        }

        ArrayList<String> list = edits(wordInLowerCase);
        Map<Integer, String> candidates = new HashMap<Integer, String>();
        for (String s : list) {
            if (nWords.containsKey(s)) {
                candidates.put(nWords.get(s), s);
            }
        }
        if (candidates.size() > 0) {
            return candidates;
        }
        for (String s : list) {
            for (String w : edits(s)) {
                if (nWords.containsKey(w)) {
                    candidates.put(nWords.get(w), w);
                }
            }
        }

        if (candidates.size() > 0) {
            return candidates;
        }
        return Collections.emptyMap();
    }

    public synchronized static SpellChecker getInstance() {
        if (instance == null) {
            LOGGER.error("instance is null");
            throw new IllegalStateException("instance is null");
        }
        return instance;
    }

    private synchronized static void setInstance(SpellChecker instance) {
        if (instance != null) {
            SpellChecker.instance = instance;
        }
    }
}
