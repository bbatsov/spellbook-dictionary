package com.drowltd.spellbook.ui.desktop.spellcheck;

import com.drowltd.spellbook.core.exception.SpellCheckerException;
import com.drowltd.spellbook.core.i18n.Translator;
import com.drowltd.spellbook.core.model.Language;
import com.drowltd.spellbook.core.service.DictionaryServiceImpl;
import com.drowltd.spellbook.core.spellcheck.HunSpellChecker;
import com.drowltd.spellbook.core.spellcheck.SpellChecker;
import com.drowltd.spellbook.ui.swing.component.FileTextPane;
import com.drowltd.spellbook.ui.swing.util.IconManager;
import com.drowltd.spellbook.ui.swing.util.SwingUtil;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.accessibility.AccessibleEditableText;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.LayeredHighlighter;
import javax.swing.text.Position;
import javax.swing.text.View;
import javax.swing.undo.UndoManager;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SpellCheckTab extends JPanel implements FileTextPane.NoFileHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpellCheckTab.class);
    private static final Map<Language, SpellChecker> spellCheckersMap = new HashMap<Language, SpellChecker>();
    private static final Translator TRANSLATOR = Translator.getTranslator("SpellCheckTab");
    private static final Set<String> userMisspelledSet = new HashSet<String>();
    private static JFileChooser jFileChooser;
    private static final long WAITING_PERIOD = 500L;

    private final ExecutorService executor = new WaitingExecutor(1, 1,
            60, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(1),
            new ThreadPoolExecutor.DiscardOldestPolicy());
    private volatile boolean added = false;

    private FileTextPane fileTextPane;
    private UndoManager undoManager = new UndoManager();
    private SpellCheckPopupMenu popupMenu;
    private JScrollPane scrollPane;
    private JLabel jLanguageLabel;
    private JLabel jStatusLabel;
    private Language selectedLanguage;
    private Future<?> spellCheckTask;
    private Highlighter highlighter;
    private final javax.swing.text.Highlighter.HighlightPainter painter = new UnderlineHighlightPainter(Color.red);

    private final Map<String, MisspelledWord> misspelledMap = new ConcurrentHashMap<String, MisspelledWord>();

    public SpellCheckTab(File file) throws IOException {
        fileTextPane = new FileTextPane(file);
        init();
    }

    public SpellCheckTab() {
        fileTextPane = new FileTextPane();
        init();
    }

    private void init() {
        scrollPane = new JScrollPane(fileTextPane);
        jStatusLabel = new JLabel();
        jLanguageLabel = new JLabel();
        popupMenu = new SpellCheckPopupMenu(this);
        fileTextPane.setHandler(this);
        fileTextPane.getDocument().addUndoableEditListener(undoManager);
        highlighter = fileTextPane.getHighlighter();

        jLanguageLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        setSelectedLanguage(Language.ENGLISH);

        initLayout();
        initListeners();
    }

    private void initLayout() {
        MigLayout migLayout = new MigLayout("wrap 1", "0[grow]0", "0[grow][]");
        setLayout(migLayout);

        add(scrollPane, "grow");
        add(jStatusLabel, "split 2, align left, growx");
        add(jLanguageLabel, "align right, growx");
    }

    private void initListeners() {
        fileTextPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                jFileTextPaneMouseClicked(evt);
            }
        });

        fileTextPane.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                spellCheck(true);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                spellCheck(true);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                spellCheck(true);
            }
        });

        scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {

            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                if (e.getValueIsAdjusting() || fileTextPane.getSelectedText() != null) {
                    return;
                }

                spellCheck(false);
            }
        });
    }

    private void jFileTextPaneMouseClicked(MouseEvent evt) {
        if (evt.getButton() == MouseEvent.BUTTON3) {
            popupMenu.show(evt);
        }
    }

    public void setSelectedLanguage(Language selectedLanguage) {
        this.selectedLanguage = selectedLanguage;
        setLanguageStatus(SwingUtil.languageToLowerCase(selectedLanguage));
    }

    public void setLanguageStatus(String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        jLanguageLabel.setText(message);
        jLanguageLabel.setIcon(IconManager.getMenuIcon(selectedLanguage.getIconName()));
    }

    private void setStatus(String message) {
        jStatusLabel.setText(message);
    }

    public static void setjFileChooser(JFileChooser jFileChooser) {
        if (jFileChooser == null)
            throw new IllegalArgumentException("jFileChooser is null");
        SpellCheckTab.jFileChooser = jFileChooser;
    }

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

        if (Character.isUpperCase(misspelledWord.getWord().charAt(0))) {
            if (misspelledWord.getWord().equals(misspelledWord.getWord().toUpperCase())) {
                correction = correction.toUpperCase();
            } else {
                correction = Character.toUpperCase(correction.charAt(0)) + correction.substring(1);
            }
        }

        final AccessibleEditableText editableText = fileTextPane.getAccessibleContext().getAccessibleEditableText();

        Pattern p = Pattern.compile("\\b" + misspelledWord.getWord() + "\\b");
        do {
            Matcher m = p.matcher(fileTextPane.getText());
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
            fileTextPane.setCaretPosition(cursorPosition);
        }

        setStatus(misspelledWord.getWord() + " " + TRANSLATOR.translate("MessageCorrected(Content)") + " " + correction);

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
                showMessage(TRANSLATOR.translate("MessageSpellCheckerError(Content)"), TRANSLATOR.translate("MessageSpellCheckerError(Title)"));
            }
        }

        return spellChecker;
    }

    private void showMessage(String content, String title) {
        JOptionPane.showMessageDialog(this, content, title, JOptionPane.INFORMATION_MESSAGE);
    }


    public List<String> getCorrections(MisspelledWord misspelledWord) {
        if (misspelledWord == null) {
            throw new IllegalArgumentException("misspelledWord is null");
        }
        return getSpellChecker(selectedLanguage).correct(misspelledWord.getWord());
    }


    private boolean misspelled(String word) {
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
                if (added) return;

                VisibleText text = getVisibleText();

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

    private boolean contains(String word) {
        return misspelledMap.keySet().contains(word);
    }


    private void addMisspelled(MisspelledWord misspelledWord) {
        if (misspelledWord == null) {
            LOGGER.error("misspelledWord is null");
            throw new NullPointerException("misspelledWord is null");
        }

        if (!contains(misspelledWord.getWord())) {
            misspelledMap.put(misspelledWord.getWord(), misspelledWord);
        }
    }


    private void addOccurance(String mWord, int startIndex) {
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


    public void addUserMisspelled(String misspelled) {
        if (misspelled == null || misspelled.isEmpty()) {
            LOGGER.error("misspelled == null || misspelled.isEmpty()");
            throw new IllegalArgumentException("misspelled == null || misspelled.isEmpty()");
        }
        userMisspelledSet.add(misspelled);

        DictionaryServiceImpl.getInstance().addRankEntry(misspelled, selectedLanguage);
        spellCheck(false);
    }

    private void highlightMisspelled() {
        if (!SwingUtilities.isEventDispatchThread()) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    highlight();
                }
            });
        } else {
            highlight();
        }
    }

    private void highlight() {
        LOGGER.info("Removing all highlights");
        removeAllHighlights();
        if (misspelledMap.isEmpty()) {
            return;
        }
        for (MisspelledWord misspelledWord : misspelledMap.values()) {
            for (MisspelledWord.Position position : misspelledWord.getOccurances()) {
                try {
                    highlighter.addHighlight(position.getStartIndex(), position.getEndIndex() + 1, painter);
                } catch (BadLocationException ex) {
                    LOGGER.error("start: " + position.getStartIndex() + " end:" + position.getEndIndex() + " " + ex.getMessage());
                }
            }
        }

    }

    private void removeAllHighlights() {

        Highlighter.Highlight[] highlights = highlighter.getHighlights();
        for (Highlighter.Highlight h : highlights) {
            highlighter.removeHighlight(h);
        }
    }

    private VisibleText getVisibleText() {
        int offset;
        int length;

        JViewport viewPort = (JViewport) fileTextPane.getParent();
        offset = fileTextPane.viewToModel(viewPort.getViewRect().getLocation());

        LOGGER.info("offset: " + offset);
        int x = (int) viewPort.getViewRect().getWidth();
        int y = (int) viewPort.getVisibleRect().getHeight();

        Point endPoint = new Point(x, y);
        length = fileTextPane.viewToModel(endPoint);

        LOGGER.info("length: " + length);

        final int actualLength = fileTextPane.getDocument().getLength();

        if ((offset + length) > actualLength) {
            length = actualLength - offset;
        }

        try {
            return new VisibleText(fileTextPane.getText(offset, length), offset);
        } catch (BadLocationException ex) {
            LOGGER.error(ex.getMessage() + " offset: " + offset + " length: " + length + " text length: " + fileTextPane.getDocument().getLength());
            throw new IllegalStateException(ex.getMessage());
        }
    }

    public void save() {
        try {
            fileTextPane.save();
        } catch (IOException e) {
            showMessage(TRANSLATOR.translate("MessageSave(Error)"), TRANSLATOR.translate("MessageSave(Title)"));
        }
    }

    public void saveAs() {
        try {
            fileTextPane.saveAs();
        } catch (IOException e) {
            showMessage(TRANSLATOR.translate("MessageSave(Error)"), TRANSLATOR.translate("MessageSave(Title)"));
        }
    }

    public boolean isSaved() {
        return fileTextPane.isSaved();
    }

    public void undo() {
        if (undoManager.canUndo()) {
            undoManager.undo();
        }
    }

    public void redo() {
        if (undoManager.canRedo()) {
            undoManager.redo();
        }
    }

    public void cut() {
        fileTextPane.cut();
    }

    public void copy() {
        fileTextPane.copy();
    }

    public void paste() {
        fileTextPane.paste();
    }

/*    public void open(File file) {
        try {
            fileTextPane.open(file);
        } catch (IOException e) {
            showMessage("can't open", "Open");
        }
    }*/

    public FileTextPane getFileTextPane() {
        return fileTextPane;
    }

    @Override
    public File handle() {
        int result = jFileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION)
            return jFileChooser.getSelectedFile();
        else
            return null;
    }

    public String getFileName() {
        return fileTextPane.getFileName();
    }

    private class UnderlineHighlightPainter extends LayeredHighlighter.LayerPainter {

        private Color color; // The color for the underline

        public UnderlineHighlightPainter(Color c) {
            if (c == null) {
                color = Color.RED;
            }
            color = c;

        }

        @Override
        public void paint(Graphics g, int offs0, int offs1, Shape bounds,
                          JTextComponent c) {
            // Do nothing: this method will never be called
        }

        @Override
        public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds,
                                JTextComponent c, View view) {
            g.setColor(color == null ? c.getSelectionColor() : color);

            Rectangle alloc = null;
            if (offs0 == view.getStartOffset() && offs1 == view.getEndOffset()) {
                if (bounds instanceof Rectangle) {
                    alloc = (Rectangle) bounds;
                } else {
                    alloc = bounds.getBounds();
                }
            } else {
                try {
                    Shape shape = view.modelToView(offs0,
                            Position.Bias.Forward, offs1,
                            Position.Bias.Backward, bounds);
                    alloc = (shape instanceof Rectangle) ? (Rectangle) shape
                            : shape.getBounds();
                } catch (BadLocationException e) {
                    LOGGER.error("BadLocationException : " + e.getMessage());
                }
            }

            FontMetrics fm = c.getFontMetrics(c.getFont());
            int baseline = alloc.y + alloc.height - fm.getDescent() + 1;
            g.drawLine(alloc.x, baseline, alloc.x + alloc.width, baseline);
            g.drawLine(alloc.x, baseline + 1, alloc.x + alloc.width,
                    baseline + 1);

            return alloc;
        }
    }

    private class WaitingExecutor extends ThreadPoolExecutor {


        public WaitingExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
        }

        @Override
        protected void beforeExecute(Thread t, Runnable r) {

            try {
                added = false;
                Thread.sleep(WAITING_PERIOD);
            } catch (InterruptedException e) {
                t.interrupt();
            }

        }

        @Override
        public Future<?> submit(Runnable r) {
            added = true;
            return super.submit(r);
        }

    }

    public static class VisibleText {

        private String text;
        private int offset;

        public VisibleText(String text, int offset) {
            if (text == null) {
                LOGGER.error("text is null");
                throw new NullPointerException("text is null");
            }

            if (offset < 0) {
                LOGGER.error("offset is < 0");
                throw new IllegalArgumentException("offset is < 0");
            }

            this.text = text;
            this.offset = offset;
        }

        public int getOffset() {
            return offset;
        }

        public String getText() {
            return text;
        }
    }
}
