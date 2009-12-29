package com.drowltd.dictionary.ui.desktop.spellcheck;

import com.drowltd.dictionary.core.db.Dictionary;
import com.drowltd.dictionary.core.spellcheck.SpellChecker;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author iivalchev
 * @since 0.2
 */
public class MisspelledFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(MisspelledFinder.class);
    private static final MisspelledFinder INSTANCE = new MisspelledFinder();
    private final MisspelledWordsRegistry registry = MisspelledWordsRegistry.getInstance();

    private final Map<Dictionary, Set<String>> userMisspelledMap = new HashMap<Dictionary, Set<String>>();
    private final SpellCheckHighlighter checkHighlighter = SpellCheckHighlighter.getInstance();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Future<?> currentFTask;

    public static MisspelledFinder getInstance() {
        return INSTANCE;
    }

    private MisspelledFinder() {
        userMisspelledMap.put(Dictionary.EN_BG, Collections.synchronizedSet(new HashSet<String>()));
        userMisspelledMap.put(Dictionary.BG_EN, Collections.synchronizedSet(new HashSet<String>()));
    }

    public void addUserMisspelled(String misspelled) {
        userMisspelledMap.get(SpellChecker.getInstance().getDictionary()).add(misspelled);
    }

    public synchronized void findMisspelled(SpellCheckFrame.VisibleText text, boolean clearRegistry) {
        if (text == null) {
            LOGGER.error("text is null");
            return;
        }

        if (currentFTask != null) {
            if (!currentFTask.isDone()) {
                LOGGER.info("Stoping running search");
                currentFTask.cancel(true);
            }
        }
        LOGGER.info("Starting execution of new search");
        currentFTask = executor.submit(new SearchTask(text, SpellChecker.getInstance(), clearRegistry));
    }

    private class SearchTask implements Runnable {

        private final SpellCheckFrame.VisibleText text;
        private final SpellChecker spellChecker;
        private final boolean clearRegistry;

        public SearchTask(SpellCheckFrame.VisibleText text, SpellChecker spellChecker, boolean clearRegistry) {
            if (text == null) {
                LOGGER.error("text is null");
                throw new NullPointerException("text is null");
            }

            if (spellChecker == null) {
                LOGGER.error("spellChecker is null");
                throw new NullPointerException("spellChecker is null");
            }

            this.text = text;
            this.spellChecker = spellChecker;
            this.clearRegistry = clearRegistry;
        }

        @Override
        public void run() {

            LOGGER.info("search started");

            synchronized (registry) {
                if (clearRegistry) {
                    registry.clear();
                }

                Pattern p = Pattern.compile("\\pL+");
                Matcher m = p.matcher(text.getText());

                int index = 0;

                while (m.find(index)) {
                    String mWord = m.group();

                    LOGGER.info("checking word " + mWord);

                    if (isWordMisspelled(mWord)) {

                        int start = text.getText().indexOf(mWord, index);
                        index = start + mWord.length();

                        LOGGER.info("misspelled word found: " + mWord + " startIndex: " + (start + text.getOffset()) + " endIndex: " + (mWord.length() + start + text.getOffset()));

                        if (registry.contains(mWord)) {
                            LOGGER.info("misspelled word already in the Factory, adding occurance");

                            registry.addOccurance(mWord, start + text.getOffset());

                        } else {
                            LOGGER.info("misspelled word not in the Factory, adding");

                            registry.addMisspelled(new MisspelledWord(mWord, start + text.getOffset()));
                        }

                    } else {
                        int indexOfWord = text.getText().indexOf(mWord, index);
                        index = indexOfWord + mWord.length();
                    }
                }
            }

            checkHighlighter.highlightMisspelled();
            LOGGER.info("search ended");
        }

        private boolean isWordMisspelled(String word) {
            if (spellChecker == null || word == null) {
                return false;
            }
            if (word.length() == 1) {
                return false;
            }

            if (userMisspelledMap.get(SpellChecker.getInstance().getDictionary()).contains(word)) {
                return false;
            }
            
            return !spellChecker.checkWord(word.toLowerCase());
        }
    }
}
