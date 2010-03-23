package com.drowltd.spellbook.core.spellcheck;

import com.drowltd.spellbook.core.model.Language;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author iivalchev
 * @since 0.2
 *
 * Implementation of the Peter Norvig Toy Spelling corrector
 *
 */
public final class SpellChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpellChecker.class);
    private static SpellChecker INSTANCE;
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

    protected final static List<String> edits(String word, String alphabet) {

        assert alphabet != null && !alphabet.isEmpty() : "alphabet == null  || alphabet.isEmpty()";

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

    protected final static List<String> edits0(String word, String alphabet) {
        assert alphabet != null && !alphabet.isEmpty() : "alphabet == null  || alphabet.isEmpty()";

        return new CSBuilder(word, alphabet).edits();

    }
    private static class CSBuilder {

        private final StringBuilder builder;
        final char[] arr;
        String word;
        String alphabet;

        CSBuilder(String word, String alphabet) {

            int capacity = word.length() + 1;

            builder = new StringBuilder();
            arr = new char[capacity];
            this.word = word;
            this.alphabet = alphabet;
        }

       private void setGapToArr(int gap) {

            assert gap >= 0 : "gap < 0";

            zeroArr();

            boolean gapped = false;
            for (int i = 0, j = 0; i < word.length(); ++i, ++j) {
                

                if (i == gap && !gapped) {
                    arr[j] = 0x7;
                    --i;
                    gapped = true;

                } else {
                    arr[j] = word.charAt(i);
                }
            }
        }

        private void plainCopytoArr() {
            for (int i = 0; i < word.length(); ++i) {
                arr[i] = word.charAt(i);
            }
        }

        private void swapChars(int c0, int c1) {
            plainCopytoArr();
            char t = arr[c0];
            arr[c0] = arr[c1];
            arr[c1] = t;
        }

        private void removeCharFromArr(int index) {
            zeroArr();
            for (int i = 0; i < word.length(); ++i) {
                if (index == i) {
                    arr[i] = 0x7;
                } else {
                    arr[i] = word.charAt(i);
                }
            }
        }

        private void zeroArr() {
            for (int i = 0; i < arr.length; ++i) {
                arr[i] = 0x7;

            }
        }

        private String build() {
            if (builder.length() > 0) {
                builder.delete(0, builder.length());
            }
            for (char c : arr) {

                if (c != 0x7) {

                    builder.append(c);
                }
            }

            return builder.toString();
        }

        public List<String> edits() {

            ArrayList<String> result = new ArrayList<String>();
            // Deletion
            for (int i = 0; i < word.length(); ++i) {
                removeCharFromArr(i);
                result.add(build());
            }

            // Transposition
            for (int i = 0; i < word.length() - 1; ++i) {

                swapChars(i, i + 1);
                result.add(build());
            }

            // Alternation
            for (int i = 0; i < word.length(); ++i) {
                removeCharFromArr(i);
                for (char c = alphabet.charAt(0); c <= alphabet.charAt(alphabet.length() - 1); ++c) {
                    arr[i] = c;
                    result.add(build());
                }
            }
            // Insertion
            for (int i = 0; i <= word.length(); ++i) {
                setGapToArr(i);
                for (char c = alphabet.charAt(0); c <= alphabet.charAt(alphabet.length() - 1); ++c) {
                    arr[i] = c;
                    result.add(build());
                }
            }
            return result;
        }
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
