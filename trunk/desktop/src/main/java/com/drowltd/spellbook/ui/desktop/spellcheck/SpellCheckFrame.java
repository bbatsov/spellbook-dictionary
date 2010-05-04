package com.drowltd.spellbook.ui.desktop.spellcheck;

import com.drowltd.spellbook.core.model.Dictionary;
import com.drowltd.spellbook.core.model.Language;
import com.drowltd.spellbook.core.service.DictionaryService;
import com.drowltd.spellbook.core.spellcheck.SpellChecker;
import com.drowltd.spellbook.ui.swing.util.IconManager;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.accessibility.AccessibleEditableText;
import javax.swing.JMenuItem;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.undo.UndoManager;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author iivalchev
 * @since 0.2
 */
public class SpellCheckFrame extends javax.swing.JFrame implements StatusManager.StatusObserver {

    private static final SpellCheckFrame INSTANCE = new SpellCheckFrame();
    private static final Logger LOGGER = LoggerFactory.getLogger(SpellCheckFrame.class);
    private UndoManager undoManager = new UndoManager();
    private SpellCheckPopupMenu popupMenu;
    private SpellCheckHighlighter checkHighlighter;
    private Timer documentChangedTimer;
    private Timer adjustmentValueTimer;
    private final int INTERVAL = 550;
    private Language selectedLanguage = Language.ENGLISH;
    //components
    private javax.swing.JMenuItem jCopyMenuItem;
    private javax.swing.JMenuItem jCutMenuItem;
    private javax.swing.JMenu jDictionaryMenu;
    private javax.swing.JMenuItem jExitMenuItem;
    private javax.swing.JLabel jLanguageLabel;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jPasteMenuItem;
    private javax.swing.JMenuItem jRedoMenuItem;
    private javax.swing.JScrollPane jScrollPane;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JLabel jStatusLabel;
    private javax.swing.JTextPane jTextPane;
    private javax.swing.JMenuItem jUndoMenuItem;

    public static SpellCheckFrame getInstance() {
        return INSTANCE;
    }

    /** Creates new form SpellCheckFrame */
    private SpellCheckFrame() {
        initComponents0();
        init();
        initLanguageMenu();
    }

    private void initComponents0() {
        jScrollPane = new javax.swing.JScrollPane();
        jTextPane = new javax.swing.JTextPane();
        jStatusLabel = new javax.swing.JLabel();
        jLanguageLabel = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jExitMenuItem = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jUndoMenuItem = new javax.swing.JMenuItem();
        jRedoMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jCutMenuItem = new javax.swing.JMenuItem();
        jCopyMenuItem = new javax.swing.JMenuItem();
        jPasteMenuItem = new javax.swing.JMenuItem();
        jDictionaryMenu = new javax.swing.JMenu();

        setTitle("SpellBook SpellChecker");

        jTextPane.setBackground(java.awt.Color.white);
        jTextPane.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextPaneMouseClicked(evt);
            }
        });
        jScrollPane.setViewportView(jTextPane);

        jLanguageLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLanguageLabel.setText("l");
        jLanguageLabel.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLanguageLabelMouseClicked(evt);
            }
        });

        jMenu1.setText("File");

        jExitMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/16x16/exit.png"))); // NOI18N
        jExitMenuItem.setText("Exit");
        jExitMenuItem.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jExitMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(jExitMenuItem);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");

        jUndoMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK));
        jUndoMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/16x16/undo.png"))); // NOI18N
        jUndoMenuItem.setText("Undo");
        jUndoMenuItem.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jUndoMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(jUndoMenuItem);

        jRedoMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/16x16/redo.png"))); // NOI18N
        jRedoMenuItem.setText("Redo");
        jRedoMenuItem.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRedoMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(jRedoMenuItem);
        jMenu2.add(jSeparator1);

        jCutMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/16x16/cut.png"))); // NOI18N
        jCutMenuItem.setText("Cut");
        jCutMenuItem.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCutMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(jCutMenuItem);

        jCopyMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/16x16/copy.png"))); // NOI18N
        jCopyMenuItem.setText("Copy");
        jCopyMenuItem.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCopyMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(jCopyMenuItem);

        jPasteMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/16x16/paste.png"))); // NOI18N
        jPasteMenuItem.setText("Paste");
        jPasteMenuItem.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
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

        add(jScrollPane, "growx, growy, w 530, h 540");
        add(jStatusLabel, "split 2, align left, growx");
        add(jLanguageLabel, "align right, growx");

        pack();

    }

    private void jTextPaneMouseClicked(java.awt.event.MouseEvent evt) {
        if (evt.getButton() == MouseEvent.BUTTON3) {
            popupMenu.show(evt);
        }
        //popupMenu.show(evt);
    }

    private void jUndoMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        if (undoManager.canUndo()) {
            undoManager.undo();
        }
    }

    private void jRedoMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        if (undoManager.canRedo()) {
            undoManager.redo();
        }
    }

    private void jCutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        jTextPane.cut();
    }

    private void jCopyMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        jTextPane.copy();
    }

    private void jPasteMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        jTextPane.paste();
    }

    private void jExitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        this.setVisible(false);
    }

    private void jLanguageLabelMouseClicked(java.awt.event.MouseEvent evt) {
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
                popupMenu = SpellCheckPopupMenu.init(SpellCheckFrame.getInstance());
                checkHighlighter = SpellCheckHighlighter.init(jTextPane.getHighlighter());
                loadSpellChecker();
            }
        });

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
        if (timer == null) {
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
        //TODO introduce selected language
        final Map<String, Integer> ratingsMap = DictionaryService.getInstance().getRatings(selectedLanguage);
        new SpellChecker(ratingsMap, selectedLanguage);
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
        int offset = 0;
        int length = 0;

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

        MisspelledFinder.getInstance().findMisspelled(SpellCheckFrame.getInstance().getVisibleText(), true);
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
