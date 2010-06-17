package com.drowltd.spellbook.ui.desktop.spellcheck;

import com.drowltd.spellbook.core.spellcheck.HunSpellChecker;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.drowltd.spellbook.core.spellcheck.SpellChecker;
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
    private final Set<String> userMisspelledSet = new HashSet<String>();
    private SpellCheckHighlighter highlighter;
    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private Future<?> currentFTask;

    public static MisspelledFinder getInstance() {
        return INSTANCE;
    }

    private MisspelledFinder() {
        
    }

    public MisspelledFinder(SpellCheckHighlighter highlighter){
        if(highlighter == null){
            throw new IllegalArgumentException("highlighter is null");
        }

        this.highlighter = highlighter;
    }

    public void addUserMisspelled(String misspelled) {
        if(misspelled == null || misspelled.isEmpty()){
            LOGGER.error("misspelled == null || misspelled.isEmpty()");
            throw new IllegalArgumentException("misspelled == null || misspelled.isEmpty()");
        }
        userMisspelledSet.add(misspelled);
    }

    public void findMisspelled(SpellCheckFrame.VisibleText text, boolean clearRegistry) {
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
        currentFTask = executor.submit(new SearchTask(text, HunSpellChecker.getInstance(), clearRegistry));
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


            if (clearRegistry) {
                registry.clear();
            }

            Pattern p = Pattern.compile("\\p{L}+");
            Matcher m = p.matcher(text.getText());

            int index = 0;

            while (m.find(index)) {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                String mWord = m.group();

                LOGGER.info("checking word " + mWord);

                if (misspelled(mWord)) {

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


            highlighter.highlightMisspelled();
            LOGGER.info("search ended");
        }

        private boolean misspelled(String word) {
            if (spellChecker == null || word == null) {
                return false;
            }
            if (word.length() == 1) {
                return false;
            }

            if (userMisspelledSet.contains(word.toLowerCase())) {
                return false;
            }

            return spellChecker.misspelled(word);
        }
    }
}
