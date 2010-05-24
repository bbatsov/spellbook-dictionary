package com.drowltd.spellbook.ui.desktop.spellcheck;

import com.drowltd.spellbook.core.model.Dictionary;
import com.drowltd.spellbook.core.model.Language;
import com.drowltd.spellbook.core.service.DictionaryService;
import com.drowltd.spellbook.core.spellcheck.SpellChecker;
import com.drowltd.spellbook.ui.swing.util.IconManager;
import com.jidesoft.swing.DefaultOverlayable;
import com.jidesoft.swing.OverlayableUtils;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.accessibility.AccessibleEditableText;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author iivalchev
 * @since 0.2
 */
public class SpellCheckFrame extends JFrame implements StatusManager.StatusObserver {

    private static SpellCheckFrame INSTANCE;
    private static final Logger LOGGER = LoggerFactory.getLogger(SpellCheckFrame.class);
    private static final int MIN_WIDTH = 540;
    private static final int MIN_HEIGHT = 550;
    private static final long MIN_SPELLCHECK_MSIZE = 104857600l;

    private UndoManager undoManager = new UndoManager();
    private SpellCheckPopupMenu popupMenu;
    private SpellCheckHighlighter checkHighlighter;
    private Timer documentChangedTimer;
    private Timer adjustmentValueTimer;
    private Language selectedLanguage = Language.ENGLISH;
    //components
    private JMenu jDictionaryMenu;
    private JLabel jLanguageLabel;
    private JScrollPane jScrollPane;
    private JLabel jStatusLabel;
    private JTextPane jTextPane;
    private DefaultOverlayable overlay;
    private JProgressBar progressBar;
    private boolean eventsEnabled = false;
    private Executor executor = Executors.newSingleThreadExecutor();


    public static SpellCheckFrame getInstance(JFrame parent) throws HeapSizeException {
        if (Runtime.getRuntime().maxMemory() < MIN_SPELLCHECK_MSIZE){
            throw new HeapSizeException();
        }
            if (INSTANCE == null) {
                INSTANCE = new SpellCheckFrame(parent);
            }
        return INSTANCE;
    }

    /**
     * Creates new form SpellCheckFrame
     */
    private SpellCheckFrame(JFrame parent) {
        initComponents0(parent);
        init();
        initLanguageMenu();
    }

    private void initComponents0(JFrame parent) {


        jScrollPane = new JScrollPane() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (!eventsEnabled)
                    OverlayableUtils.repaintOverlayable(this);

            }
        };

        jTextPane = new JTextPane();
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        overlay = new DefaultOverlayable(jScrollPane, progressBar, DefaultOverlayable.CENTER);

        jStatusLabel = new JLabel();
        jLanguageLabel = new JLabel();
        JMenuBar jMenuBar1 = new JMenuBar();
        JMenu jMenu1 = new JMenu();
        JMenuItem jExitMenuItem = new JMenuItem();
        JMenu jMenu2 = new JMenu();
        JMenuItem jUndoMenuItem = new JMenuItem();
        JMenuItem jRedoMenuItem = new JMenuItem();
        JPopupMenu.Separator jSeparator1 = new JPopupMenu.Separator();
        JMenuItem jCutMenuItem = new JMenuItem();
        JMenuItem jCopyMenuItem = new JMenuItem();
        JMenuItem jPasteMenuItem = new JMenuItem();
        jDictionaryMenu = new JMenu();

        setTitle("SpellBook SpellChecker");

        jTextPane.setBackground(Color.white);
        jTextPane.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent evt) {
                jTextPaneMouseClicked(evt);
            }
        });
        jScrollPane.setViewportView(jTextPane);
//        jScrollPane.setViewportView(overlay);

        jLanguageLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        jLanguageLabel.setText("l");
        jLanguageLabel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent evt) {
                jLanguageLabelMouseClicked(evt);
            }
        });

        jMenu1.setText("File");

        jExitMenuItem.setIcon(IconManager.getMenuIcon("exit.png")); // NOI18N
        jExitMenuItem.setText("Exit");
        jExitMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                jExitMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(jExitMenuItem);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");

        jUndoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
        jUndoMenuItem.setIcon(IconManager.getMenuIcon("undo.png"));
        jUndoMenuItem.setText("Undo");
        jUndoMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                jUndoMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(jUndoMenuItem);

        jRedoMenuItem.setIcon(IconManager.getMenuIcon("redo.png"));
        jRedoMenuItem.setText("Redo");
        jRedoMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                jRedoMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(jRedoMenuItem);
        jMenu2.add(jSeparator1);

        jCutMenuItem.setIcon(IconManager.getMenuIcon("cut.png"));
        jCutMenuItem.setText("Cut");
        jCutMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                jCutMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(jCutMenuItem);

        jCopyMenuItem.setIcon(IconManager.getMenuIcon("copy.png"));
        jCopyMenuItem.setText("Copy");
        jCopyMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                jCopyMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(jCopyMenuItem);

        jPasteMenuItem.setIcon(IconManager.getMenuIcon("paste.png"));
        jPasteMenuItem.setText("Paste");
        jPasteMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                jPasteMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(jPasteMenuItem);

        jMenuBar1.add(jMenu2);

        jDictionaryMenu.setText("Languages");
        jMenuBar1.add(jDictionaryMenu);

        setJMenuBar(jMenuBar1);


        MigLayout migLayout = new MigLayout("wrap 1", "0[grow]0", "0[grow][]");
        getContentPane().setLayout(migLayout);

        add(overlay, "grow");
        add(jStatusLabel, "split 2, align left, growx");
        add(jLanguageLabel, "align right, growx");

        setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
        setLocationRelativeTo(parent);
        pack();

    }

    private void setTextEditable(boolean canEdit) {
        overlay.setOverlayVisible(!canEdit);
        jTextPane.setEnabled(canEdit);
        eventsEnabled = canEdit;
    }

    private void jTextPaneMouseClicked(MouseEvent evt) {
        if (evt.getButton() == MouseEvent.BUTTON3) {
            popupMenu.show(evt);
        }
        //popupMenu.show(evt);
    }

    private void jUndoMenuItemActionPerformed(ActionEvent evt) {
        if (undoManager.canUndo()) {
            undoManager.undo();
        }
    }

    private void jRedoMenuItemActionPerformed(ActionEvent evt) {
        if (undoManager.canRedo()) {
            undoManager.redo();
        }
    }

    private void jCutMenuItemActionPerformed(ActionEvent evt) {
        jTextPane.cut();
    }

    private void jCopyMenuItemActionPerformed(ActionEvent evt) {
        jTextPane.copy();
    }

    private void jPasteMenuItemActionPerformed(ActionEvent evt) {
        jTextPane.paste();
    }

    private void jExitMenuItemActionPerformed(ActionEvent evt) {
        this.setVisible(false);
    }

    private void jLanguageLabelMouseClicked(MouseEvent evt) {
    }

    /**
     * Custom initializations.
     */
    private void init() {

        setIconImage(IconManager.getImageIcon("spellcheck.png", IconManager.IconSize.SIZE16).getImage());

        jScrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {

            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                jScrollPaneAdjustmentValueChanged(e);
            }
        });

        jTextPane.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                triggerMisspelledSearch(documentChangedTimer, true);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                triggerMisspelledSearch(documentChangedTimer, true);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                triggerMisspelledSearch(documentChangedTimer, true);
            }
        });

        jTextPane.getDocument().addUndoableEditListener(undoManager);

        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                popupMenu = SpellCheckPopupMenu.init(INSTANCE);
                checkHighlighter = SpellCheckHighlighter.init(jTextPane.getHighlighter());
                loadSpellChecker();
            }
        });

        int INTERVAL = 550;
        documentChangedTimer = new Timer(INTERVAL, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                documentChangedTimer.stop();
                MisspelledFinder.getInstance().findMisspelled(getVisibleText(), true);
            }
        });

        adjustmentValueTimer = new Timer(INTERVAL, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                adjustmentValueTimer.stop();
                MisspelledFinder.getInstance().findMisspelled(getVisibleText(), false);
            }
        });

        StatusManager.getInstance().addObserver(this);
        setLanguageStatus(languageToLowerCase(selectedLanguage));

    }

    private String languageToLowerCase(Language language) {
        return language.toString().substring(0, 1) + language.toString().substring(1).toLowerCase();
    }

    private void triggerMisspelledSearch(Timer timer, boolean removeHighlightOnCaret) {
        if (timer == null || !eventsEnabled) {
            return;
        }

        if (removeHighlightOnCaret) {
            checkHighlighter.removeHighlight(jTextPane.getCaretPosition(), jTextPane.getCaretPosition());
        }

        if (timer.isRunning()) {
            timer.restart();
        } else {
            timer.start();
        }
    }

    private void jScrollPaneAdjustmentValueChanged(AdjustmentEvent e) {

        if (e.getValueIsAdjusting() || documentChangedTimer.isRunning() || jTextPane.getSelectedText() != null) {
            return;
        }

        triggerMisspelledSearch(adjustmentValueTimer, false);
    }

    /**
     *
     */
    private void loadSpellChecker() {
        setTextEditable(false);

        executor.execute(new Runnable() {
            @Override
            public void run() {
                Map<String, Integer> ratingsMap = DictionaryService.getInstance().getRatings(selectedLanguage);
                new SpellChecker(ratingsMap, selectedLanguage);

                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        setTextEditable(true);
                        triggerMisspelledSearch(documentChangedTimer, true);
                        jTextPane.requestFocus();
                    }
                });
            }
        });
    }

    private void setSelectedLanguage(Language language) {
        assert language != null : "selectedLanguage is null";

        if (selectedLanguage != language) {
            selectedLanguage = language;
            loadSpellChecker();
            triggerMisspelledSearch(documentChangedTimer, true);
            setLanguageStatus(languageToLowerCase(language));
        }
    }

    public void setLanguageStatus(String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        jLanguageLabel.setText(message);

        jLanguageLabel.setIcon(IconManager.getMenuIcon(selectedLanguage.getIconName()));


    }

    /**
     * Gets the currently visible text in the jTextPane.
     *
     * @return VisibleText repersentig currently visible text
     */
    public VisibleText getVisibleText() {
        int offset;
        int length;

        JViewport viewPort = (JViewport) jTextPane.getParent();
        offset = jTextPane.viewToModel(viewPort.getViewRect().getLocation());

        LOGGER.info("offset: " + offset);
        int x = (int) viewPort.getViewRect().getWidth();
        int y = (int) viewPort.getVisibleRect().getHeight();

        Point endPoint = new Point(x, y);
        length = jTextPane.viewToModel(endPoint);

        LOGGER.info("length: " + length);

        final int actualLength = jTextPane.getDocument().getLength();

        if ((offset + length) > actualLength) {
            length = actualLength - offset;
        }

        try {
            return new VisibleText(jTextPane.getText(offset, length), offset);
        } catch (BadLocationException ex) {
            LOGGER.error(ex.getMessage() + " offset: " + offset + " length: " + length + " text length: " + jTextPane.getDocument().getLength());
            throw new IllegalStateException(ex.getMessage());
        }
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

        //@todo Extract Method
        if (Character.isUpperCase(misspelledWord.getWord().charAt(0))) {
            if (misspelledWord.getWord().equals(misspelledWord.getWord().toUpperCase())) {
                correction = correction.toUpperCase();
            } else {
                correction = Character.toUpperCase(correction.charAt(0)) + correction.substring(1);
            }
        }

        final AccessibleEditableText editableText = jTextPane.getAccessibleContext().getAccessibleEditableText();

        Pattern p = Pattern.compile("\\b" + misspelledWord.getWord() + "\\b");
        do {
            Matcher m = p.matcher(jTextPane.getText());
            if (!m.find()) {
                break;
            }
            String misspelled = m.group();
            int end = m.end();
            int start = end - (misspelled.length());

            checkHighlighter.removeHighlight(start, end);
            editableText.replaceText(start, end, correction);

        } while (true);

        if (cursorPosition > -1) {
            jTextPane.setCaretPosition(cursorPosition);
        }

        StatusManager.getInstance().setStatus(misspelledWord.getWord() + " corrected with " + correction);

        MisspelledFinder.getInstance().findMisspelled(INSTANCE.getVisibleText(), true);
    }

    JTextPane getjTextPane() {
        return jTextPane;
    }

    @Override
    public void setStatus(final String message) {
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                jStatusLabel.setText(message);
            }
        });
    }

    private void initLanguageMenu() {
        List<Dictionary> dictionaries = DictionaryService.getInstance().getDictionaries();
        for (Dictionary d : dictionaries) {
            jDictionaryMenu.add(new LanguageItem(d.getFromLanguage()));
        }
    }

    private class LanguageItem extends JMenuItem implements ActionListener {

        private Language language;

        public LanguageItem(Language language) {

            this.language = language;
            setIcon(IconManager.getMenuIcon(language.getIconName()));
            setText(languageToLowerCase(language));
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setSelectedLanguage(language);
        }
    }

    /**
     * Represents a visble text.
     */
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
