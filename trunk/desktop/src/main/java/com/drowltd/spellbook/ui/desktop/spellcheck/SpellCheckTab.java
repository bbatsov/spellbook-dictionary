package com.drowltd.spellbook.ui.desktop.spellcheck;

import com.drowltd.spellbook.core.exception.*;
import com.drowltd.spellbook.core.i18n.*;
import com.drowltd.spellbook.core.model.*;
import com.drowltd.spellbook.core.service.*;
import com.drowltd.spellbook.core.spellcheck.*;
import com.drowltd.spellbook.ui.swing.component.*;
import com.drowltd.spellbook.ui.swing.util.*;
import org.slf4j.*;

import javax.accessibility.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.undo.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.*;


public class SpellCheckTab extends JPanel implements SpellCheckFrame.SpellCheckable {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpellCheckTab.class);
    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private static final Map<Language, SpellChecker> spellCheckersMap = new HashMap<Language, SpellChecker>();
    private static final Translator TRANSLATOR = Translator.getTranslator("SpellCheckTab");

    private FileTextPane textPane;
    private UndoManager undoManager = new UndoManager();
    private SpellCheckPopupMenu popupMenu;
    private Language selectedLanguage = Language.ENGLISH;
    private Future<?> spellCheckTask;

    private final Map<String, MisspelledWord> misspelledMap = new ConcurrentHashMap<String, MisspelledWord>();
    private final Set<String> userMisspelledSet = new HashSet<String>();

    public void correct(String correction, MisspelledWord misspelledWord, int cursorPosition) {
        if (correction == null) {
            LOGGER.error("correction is null");
            throw new NullPointerException("correction is null");
        }

        if (correction.isEmpty()) {
            LOGGER.error("correction is empty");
            throw new IllegalArgumentException("correction is empty");
        }

        if (misspelledWord == null) {
            LOGGER.error("misspelledWord is null");
            throw new NullPointerException("misspelledWord is null");
        }

        if (cursorPosition < 0) {
            LOGGER.error("cursorPosition < 0");
            throw new IllegalArgumentException("cursorPosition < 0");
        }

        //@todo Extract Method
        if (Character.isUpperCase(misspelledWord.getWord().charAt(0))) {
            if (misspelledWord.getWord().equals(misspelledWord.getWord().toUpperCase())) {
                correction = correction.toUpperCase();
            } else {
                correction = Character.toUpperCase(correction.charAt(0)) + correction.substring(1);
            }
        }

        final AccessibleEditableText editableText = textPane.getAccessibleContext().getAccessibleEditableText();

        Pattern p = Pattern.compile("\\b" + misspelledWord.getWord() + "\\b");
        do {
            Matcher m = p.matcher(textPane.getText());
            if (!m.find()) {
                break;
            }
            String misspelled = m.group();
            int end = m.end();
            int start = end - (misspelled.length());

            editableText.replaceText(start, end, correction);

        } while (true);

        if (misspelledMap.keySet().contains(misspelledWord.getWord())) {
            misspelledMap.remove(misspelledWord.getWord());
        }

        if (cursorPosition > -1) {
            textPane.setCaretPosition(cursorPosition);
        }

        StatusManager.getInstance().setStatus(misspelledWord.getWord() + " corrected with " + correction);

        spellCheck(true);

    }

    private SpellChecker getSpellChecker(Language language) {
        SpellChecker spellChecker = spellCheckersMap.get(language);
        if (spellChecker == null) {
            try {
                spellChecker = new HunSpellChecker(language);
                spellCheckersMap.put(language, spellChecker);
            } catch (SpellCheckerException e) {
                LOGGER.error(e.getMessage(), e);
                JOptionPane.showMessageDialog(this, TRANSLATOR.translate("Message(Content)"), TRANSLATOR.translate("Message(Title)"), JOptionPane.INFORMATION_MESSAGE);
            }
        }

        return spellChecker;
    }

    @Override
    public List<String> getCorrections(MisspelledWord misspelledWord) {
        if (misspelledWord == null) {
            throw new IllegalArgumentException("misspelledWord is null");
        }
        return getSpellChecker(selectedLanguage).correct(misspelledWord.getWord());
    }

    @Override
    public boolean misspelled(String word) {
        if (word == null) {
            return false;
        }
        if (word.length() == 1) {
            return false;
        }

        if (userMisspelledSet.contains(word.toLowerCase())) {
            return false;
        }

        return getSpellChecker(selectedLanguage).misspelled(word);
    }

    @Override
    public void spellCheck(boolean clear) {
        if (clear) {
            misspelledMap.clear();
        }

        if (spellCheckTask != null && !spellCheckTask.isDone()) {
            spellCheckTask.cancel(true);
        }

        spellCheckTask = executor.submit(new Runnable() {
            @Override
            public void run() {
                SpellCheckFrame.VisibleText text = getVisibleText();

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

                        if (contains(mWord)) {
                            LOGGER.info("misspelled word already in the Factory, adding occurance");

                            addOccurance(mWord, start + text.getOffset());

                        } else {
                            LOGGER.info("misspelled word not in the Factory, adding");

                            addMisspelled(new MisspelledWord(mWord, start + text.getOffset()));
                        }

                    } else {
                        int indexOfWord = text.getText().indexOf(mWord, index);
                        index = indexOfWord + mWord.length();
                    }
                }


                highlightMisspelled();
                LOGGER.info("search ended");
            }
        });
    }

    @Override
    public MisspelledWord getMisspelledWord(int cursorPosition) {
        if (cursorPosition < 0) {
            LOGGER.error("cursorPosition < 0");
            throw new IllegalArgumentException("cursorPosition < 0");
        }
        for (MisspelledWord misspelledWord : misspelledMap.values()) {
            if (misspelledWord.isIndexInWord(cursorPosition)) {

                LOGGER.info("misspelled found: " + misspelledWord);

                return misspelledWord;
            }
        }
        return null;
    }

    public boolean contains(String word) {
        return misspelledMap.keySet().contains(word);
    }

    @Override
    public void addMisspelled(MisspelledWord misspelledWord) {
        if (misspelledWord == null) {
            LOGGER.error("misspelledWord is null");
            throw new NullPointerException("misspelledWord is null");
        }

        if (!contains(misspelledWord.getWord())) {
            misspelledMap.put(misspelledWord.getWord(), misspelledWord);
        }
    }

    @Override
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

        misspelledMap.get(mWord).addOccurance(mWord, startIndex);
    }

    @Override
    public Collection<MisspelledWord> getMisspelled() {
        return Collections.unmodifiableCollection(misspelledMap.values());
    }

    public void addUserMisspelled(String misspelled) {
        if (misspelled == null || misspelled.isEmpty()) {
            LOGGER.error("misspelled == null || misspelled.isEmpty()");
            throw new IllegalArgumentException("misspelled == null || misspelled.isEmpty()");
        }
        userMisspelledSet.add(misspelled);

        DictionaryService.getInstance().addRankEntry(misspelled, selectedLanguage);
    }

    @Override
    public void highlightMisspelled() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    private SpellCheckFrame.VisibleText getVisibleText() {
        int offset;
        int length;

        JViewport viewPort = (JViewport) textPane.getParent();
        offset = textPane.viewToModel(viewPort.getViewRect().getLocation());

        LOGGER.info("offset: " + offset);
        int x = (int) viewPort.getViewRect().getWidth();
        int y = (int) viewPort.getVisibleRect().getHeight();

        Point endPoint = new Point(x, y);
        length = textPane.viewToModel(endPoint);

        LOGGER.info("length: " + length);

        final int actualLength = textPane.getDocument().getLength();

        if ((offset + length) > actualLength) {
            length = actualLength - offset;
        }

        try {
            return new SpellCheckFrame.VisibleText(textPane.getText(offset, length), offset);
        } catch (BadLocationException ex) {
            LOGGER.error(ex.getMessage() + " offset: " + offset + " length: " + length + " text length: " + textPane.getDocument().getLength());
            throw new IllegalStateException(ex.getMessage());
        }
    }

    public void save() {

    }

    public void saveAs() {
    }
}
