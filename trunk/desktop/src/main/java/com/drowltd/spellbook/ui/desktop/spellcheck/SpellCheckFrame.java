package com.drowltd.spellbook.ui.desktop.spellcheck;

import com.drowltd.spellbook.core.exception.SpellCheckerException;
import com.drowltd.spellbook.core.i18n.*;
import com.drowltd.spellbook.core.model.Dictionary;
import com.drowltd.spellbook.core.model.Language;
import com.drowltd.spellbook.core.service.DictionaryService;
import com.drowltd.spellbook.core.spellcheck.HunSpellChecker;
import com.drowltd.spellbook.ui.swing.component.*;
import com.drowltd.spellbook.ui.swing.util.*;
import com.jidesoft.swing.DefaultOverlayable;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * @author iivalchev
 * @since 0.2
 */
public class SpellCheckFrame extends JFrame implements StatusManager.StatusObserver {

    private static final Translator TRANSLATOR = Translator.getTranslator("SpellCheckFrame");
    private static SpellCheckFrame INSTANCE;
    private static final Logger LOGGER = LoggerFactory.getLogger(SpellCheckFrame.class);
    private static final int MIN_WIDTH = 540;
    private static final int MIN_HEIGHT = 550;


    private Language selectedLanguage = Language.ENGLISH;
    //components
    private JMenu jDictionaryMenu;
    private JLabel jLanguageLabel;
    private JLabel jStatusLabel;
    private JTabbedPane jTabbedPane;
    private JFileChooser jFileChooser;

    public static SpellCheckFrame getInstance(JFrame parent) throws HeapSizeException, SpellCheckerException {

        if (INSTANCE == null) {
            INSTANCE = new SpellCheckFrame(parent);
        }
        return INSTANCE;
    }

    /**
     * Creates new form SpellCheckFrame
     */
    private SpellCheckFrame(JFrame parent) throws SpellCheckerException {
        initComponents0(parent);
        init();
        initLanguageMenu();
    }

    private void initComponents0(JFrame parent) {

        jFileChooser = new JFileChooser();
        SpellCheckTab.setjFileChooser(jFileChooser);

        jTabbedPane = new JTabbedPane();
        JComponent tab = createNewTab();
        jTabbedPane.addTab("tab", tab);

        jStatusLabel = new JLabel();
        jLanguageLabel = new JLabel();
        JMenuBar jMenuBar1 = new JMenuBar();
        JMenu jFileMenu = new JMenu();
        JMenuItem jExitMenuItem = new JMenuItem();
        JMenu jEditMenu = new JMenu();
        JMenuItem jUndoMenuItem = new JMenuItem();
        JMenuItem jRedoMenuItem = new JMenuItem();
        JPopupMenu.Separator jSeparator1 = new JPopupMenu.Separator();
        JMenuItem jCutMenuItem = new JMenuItem();
        JMenuItem jCopyMenuItem = new JMenuItem();
        JMenuItem jPasteMenuItem = new JMenuItem();
        jDictionaryMenu = new JMenu();

        setTitle("SpellBook SpellChecker");

        jLanguageLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        jLanguageLabel.setText("l");
        jLanguageLabel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent evt) {
                jLanguageLabelMouseClicked(evt);
            }
        });

        jFileMenu.setText("File");

        JMenuItem jOpenJMenuItem = new JMenuItem("Open");
        jOpenJMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int result = jFileChooser.showOpenDialog(SpellCheckFrame.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    SpellCheckTab tab1 = createNewTab(jFileChooser.getSelectedFile());
                    jTabbedPane.addTab(tab1.getFileName(), tab1);
                }
            }
        });

        jFileMenu.add(jOpenJMenuItem);

        JMenuItem jSaveJMenuItem = new JMenuItem("Save");
        jSaveJMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((SpellCheckTab)jTabbedPane.getSelectedComponent()).save();
            }
        });
        jFileMenu.add(jSaveJMenuItem);

        JMenuItem jSaveAsJMenuItem = new JMenuItem("Save as");
        jSaveAsJMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SpellCheckTab tab1 = (SpellCheckTab) jTabbedPane.getSelectedComponent();
                tab1.saveAs();
                jTabbedPane.setTitleAt(jTabbedPane.getSelectedIndex(), tab1.getFileName());
            }
        });
        jFileMenu.add(jSaveAsJMenuItem);

        jExitMenuItem.setIcon(IconManager.getMenuIcon("exit.png")); // NOI18N
        jExitMenuItem.setText("Exit");
        jExitMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                jExitMenuItemActionPerformed(evt);
            }
        });
        jFileMenu.add(jExitMenuItem);

        jMenuBar1.add(jFileMenu);

        jEditMenu.setText("Edit");

        jUndoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
        jUndoMenuItem.setIcon(IconManager.getMenuIcon("undo.png"));
        jUndoMenuItem.setText("Undo");
        jUndoMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {

            }
        });
        jEditMenu.add(jUndoMenuItem);

        jRedoMenuItem.setIcon(IconManager.getMenuIcon("redo.png"));
        jRedoMenuItem.setText("Redo");
        jRedoMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {

            }
        });
        jEditMenu.add(jRedoMenuItem);
        jEditMenu.add(jSeparator1);

        jCutMenuItem.setIcon(IconManager.getMenuIcon("cut.png"));
        jCutMenuItem.setText("Cut");
        jCutMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {

            }
        });
        jEditMenu.add(jCutMenuItem);

        jCopyMenuItem.setIcon(IconManager.getMenuIcon("copy.png"));
        jCopyMenuItem.setText("Copy");
        jCopyMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {

            }
        });
        jEditMenu.add(jCopyMenuItem);

        jPasteMenuItem.setIcon(IconManager.getMenuIcon("paste.png"));
        jPasteMenuItem.setText("Paste");
        jPasteMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {

            }
        });
        jEditMenu.add(jPasteMenuItem);

        jMenuBar1.add(jEditMenu);

        jDictionaryMenu.setText("Languages");
        jMenuBar1.add(jDictionaryMenu);

        setJMenuBar(jMenuBar1);


        MigLayout migLayout = new MigLayout("wrap 1", "0[grow]0", "0[grow][]");
        getContentPane().setLayout(migLayout);

        add(jTabbedPane, "grow");
        add(jStatusLabel, "split 2, align left, growx");
        add(jLanguageLabel, "align right, growx");

        setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
        setLocationRelativeTo(parent);
        pack();

    }

    private void jExitMenuItemActionPerformed(ActionEvent evt) {
        this.setVisible(false);
    }

    private void jLanguageLabelMouseClicked(MouseEvent evt) {
    }

    /**
     * Custom initializations.
     */
    private void init() throws SpellCheckerException {

        setIconImage(IconManager.getImageIcon("spellcheck.png", IconManager.IconSize.SIZE16).getImage());
    }

    private String languageToLowerCase(Language language) {
        return language.toString().substring(0, 1) + language.toString().substring(1).toLowerCase();
    }


    private void setSelectedLanguage(Language language) {
        assert language != null : "selectedLanguage is null";

        if (selectedLanguage != language) {
            selectedLanguage = language;
//            loadSpellChecker();

            try {
                HunSpellChecker.init(selectedLanguage);
            } catch (SpellCheckerException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

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

    private SpellCheckTab createNewTab(File file) {
        SpellCheckTab tab = null;

        if (file != null) {
            try {
                tab = new SpellCheckTab(file);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "cant open file", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        }

        return tab != null ? tab : createNewTab();
    }

    private SpellCheckTab createNewTab() {
        SpellCheckTab tab = new SpellCheckTab();
        return tab;
    }
}
