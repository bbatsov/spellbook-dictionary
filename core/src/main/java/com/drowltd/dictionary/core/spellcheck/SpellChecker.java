package com.drowltd.dictionary.core.spellcheck;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author iivalchev
 *
 * Implementation of the Peter Norvig Toy Spelling corrector
 * by <a href="http://raelcunha.com/spell-correct.php">Rael Cunha</a>.
 *
 */
public class SpellChecker {
    public static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";

    private Map<String, Integer> nWords;

    public Map<String, Integer> getnWords() {
        return nWords;
    }

    /**
     *
     * @param the Map<String, Integer> containing the words
     */
    public SpellChecker(Map<String, Integer> nWords) {
        if (nWords == null) {
            throw new NullPointerException("nWords is null");
        }

        if (nWords.isEmpty()) {
            throw new IllegalArgumentException("nWords is empty");
        }

        this.nWords = nWords;
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
            for (char c = ALPHABET.charAt(0); c <= ALPHABET.charAt(ALPHABET.length()-1); ++c) {
                result.add(word.substring(0, i) + String.valueOf(c) + word.substring(i + 1));
            }
        }
        // Insertion
        for (int i = 0; i <= word.length(); ++i) {
            for (char c = ALPHABET.charAt(0); c <= ALPHABET.charAt(ALPHABET.length()-1); ++c) {
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
//
//        Map<Integer, String> wordMap = new HashMap<Integer, String>();
//        wordMap.put(0, wordInLowerCase);

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
}
