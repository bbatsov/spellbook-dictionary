package com.drowltd.dictionary.ui.spellcheck;

import com.drowltd.dictionary.core.spellcheck.SpellChecker;
import java.awt.Color;
import java.awt.EventQueue;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author iivalchev
 */
public class MisspelledFinder {

    private static Logger LOGGER = LoggerFactory.getLogger(MisspelledFinder.class);
    private static final MisspelledFinder INSTANCE = new MisspelledFinder();
    private final Map<String, MisspelledWord> misspelled = new HashMap<String, MisspelledWord>();
    private final MisspelledWordsRegistry registry = MisspelledWordsRegistry.getInstance();
    private SpellChecker spellChecker;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Future<?> currentFTask;
    private Highlighter highlighter;
    private final Highlighter.HighlightPainter painter = new UnderlineHighlightPainter(Color.red);

    public static MisspelledFinder getInstance() {
        return INSTANCE != null ? INSTANCE : null;
    }

    private MisspelledFinder() {
    }

    public void findMisspelled(SpellCheckFrame.VisibleText text) {
        if (spellChecker == null) {
            LOGGER.error("spellChecker is null");
            return;
        }

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
        currentFTask = executor.submit(new SearchTask(text));
    }

    void setSpellChecker(SpellChecker spellChecker) {
        if (spellChecker == null) {
            LOGGER.error("spellChecker is null");
            throw new NullPointerException("spellChecker is null");
        }

        this.spellChecker = spellChecker;
    }

    public void setHighlighter(Highlighter highlighter) {
        if (highlighter == null) {
            LOGGER.error("highlighter is null");
            throw new NullPointerException("highlighter is null");
        }

        this.highlighter = highlighter;
    }

    private void addHighligths() {
        if (highlighter == null || registry.getMisspelled().isEmpty()) {
            return;
        }

        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {  
                synchronized (registry) {
                    for (MisspelledWord misspelledWord : registry.getMisspelled()) {
                        for (MisspelledWord.Position position : misspelledWord.getOccurances()) {
                            try {
                                highlighter.addHighlight(position.getStartIndex(), position.getEndIndex() + 1, painter);
                            } catch (BadLocationException ex) {
                                LOGGER.error("start: " + position.getStartIndex() + " end:" + position.getEndIndex() + " " + ex.getMessage());
                            }
                        }
                    }
                }
            }
        });
    }

    private class SearchTask implements Runnable {

        private SpellCheckFrame.VisibleText text;

        public SearchTask(SpellCheckFrame.VisibleText text) {
            if (text == null) {
                LOGGER.error("text is null");
                throw new NullPointerException("text is null");
            }

            this.text = text;
        }

        @Override
        public void run() {
            if (spellChecker == null) {
                return;
            }

            LOGGER.info("search started");

            synchronized (registry) {
                registry.clear();

                Pattern p = Pattern.compile("\\w+");
                Matcher m = p.matcher(text.getText());

                int index = 0;

                while (m.find(index)) {
                    String mWord = m.group();

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

            addHighligths();

            LOGGER.info("search ended");
        }

        private boolean isWordMisspelled(String word) {
            if (spellChecker == null || word == null) {
                return false;
            }
            return !spellChecker.checkWord(word.toLowerCase());
        }
    }
}
