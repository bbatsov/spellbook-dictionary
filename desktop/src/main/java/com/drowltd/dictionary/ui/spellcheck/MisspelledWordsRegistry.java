package com.drowltd.dictionary.ui.spellcheck;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author iivalchev
 */
public class MisspelledWordsRegistry {

    private static final MisspelledWordsRegistry INSTANCE = new MisspelledWordsRegistry();
    private static final Logger LOGGER = LoggerFactory.getLogger(MisspelledWordsRegistry.class);
    
    private final Map<String, MisspelledWord> misspelled = new HashMap<String, MisspelledWord>();

    public static MisspelledWordsRegistry getInstance() {
        return INSTANCE;
    }

    private MisspelledWordsRegistry() {
    }

    public MisspelledWord getMisspelledWord(int cursorPosition) {
        if (cursorPosition < 0) {
            LOGGER.error("cursorPosition < 0");
            throw new IllegalArgumentException("cursorPosition < 0");
        }
        for (MisspelledWord misspelledWord : misspelled.values()) {
            if (misspelledWord.isIndexInWord(cursorPosition)) {

                LOGGER.info("misspelled found: " + misspelledWord);

                return misspelledWord;
            }
        }
        return null;
    }

    public void corrected(MisspelledWord misspelledWord) {
        if (misspelled == null) {
            LOGGER.warn("corrected() is called before is initialized MisspelledWordsRegistry");
            return;
        }

        if (misspelledWord == null) {
            LOGGER.error("misspelledWord is null");
            throw new NullPointerException("misspelledWord is null");
        }

        if (!contains(misspelledWord.getWord())) {
            LOGGER.error("misspelledWord is not in the registry");
            return;
        }

        LOGGER.info("misspelled corrected: " + misspelledWord.getWord());
        misspelled.remove(misspelledWord.getWord());
    }

    public Collection<MisspelledWord> getMisspelled() {
        return Collections.unmodifiableCollection(misspelled.values());
    }

    public void addMisspelled(MisspelledWord misspelledWord) {
        if (misspelledWord == null) {
            LOGGER.error("misspelledWord is null");
            throw new NullPointerException("misspelledWord is null");
        }

        if (!contains(misspelledWord.getWord())) {
            misspelled.put(misspelledWord.getWord(), misspelledWord);
        }

    }

    public void addOccurance(String mWord, int startIndex) {
        if (mWord == null) {
            LOGGER.error("word is null");
            throw new NullPointerException("word is null");
        }

        if (startIndex < 0) {
            LOGGER.error("startIndex < 0");
            throw new IllegalArgumentException("startIndex < 0");
        }

        if (!contains(mWord)) {
            return;
        }

        misspelled.get(mWord).addOccurance(mWord, startIndex);
    }

    public boolean contains(String word) {
        return misspelled.keySet().contains(word);
    }

    public void clear() {
        misspelled.clear();
    }

    public void putAll(Map<String, ? extends MisspelledWord> misspelled) {
        if (misspelled == null) {
            LOGGER.error("misspelled is null");
            throw new NullPointerException("misspelled is null");
        }

        synchronized (misspelled) {
            clear();
            this.misspelled.putAll(misspelled);
        }
    }
}
