package com.drowltd.spellbook.ui.desktop.spellcheck;

import com.drowltd.spellbook.core.exception.*;
import com.drowltd.spellbook.core.i18n.*;
import com.drowltd.spellbook.core.model.Dictionary;
import com.drowltd.spellbook.core.model.*;
import com.drowltd.spellbook.core.service.*;
import com.drowltd.spellbook.ui.swing.util.*;
import net.miginfocom.swing.*;
import org.slf4j.*;

import javax.swing.*;
import javax.swing.plaf.basic.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.List;

/**
 * @author iivalchev
 * @since 0.2
 */
public class SpellCheckFrame extends JFrame {

    private static final Translator TRANSLATOR = Translator.getTranslator("SpellCheckFrame");
    private static SpellCheckFrame INSTANCE;
    private static final Logger LOGGER = LoggerFactory.getLogger(SpellCheckFrame.class);
    private static final int MIN_WIDTH = 540;
    private static final int MIN_HEIGHT = 550;


    private Language selectedLanguage = Language.ENGLISH;
    //components
    private JMenu jDictionaryMenu;

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
        initComponents0();
        init();
        initLayout(parent);
        initLanguageMenu();
    }

    private void initComponents0() {

        jFileChooser = new JFileChooser();
        SpellCheckTab.setjFileChooser(jFileChooser);

        jTabbedPane = new JTabbedPane();
        SpellCheckTab tab = createNewTab();

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

        jFileMenu.setText("File");

        JMenuItem jNewJMenuItem = new JMenuItem("New");
        jNewJMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SpellCheckTab tab1 = createNewTab();
            }
        });

        jFileMenu.add(jNewJMenuItem);

        JMenuItem jOpenJMenuItem = new JMenuItem("Open");
        jOpenJMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int result = jFileChooser.showOpenDialog(SpellCheckFrame.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    SpellCheckTab tab1 = createNewTab(jFileChooser.getSelectedFile());
                }
            }
        });

        jFileMenu.add(jOpenJMenuItem);

        JMenuItem jSaveJMenuItem = new JMenuItem("Save");
        jSaveJMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SpellCheckTab tab1 = (SpellCheckTab) jTabbedPane.getSelectedComponent();
                if (tab1 != null)
                    tab1.save();
            }
        });
        jFileMenu.add(jSaveJMenuItem);

        JMenuItem jSaveAsJMenuItem = new JMenuItem("Save As");
        jSaveAsJMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SpellCheckTab tab1 = (SpellCheckTab) jTabbedPane.getSelectedComponent();
                if (tab1 != null) {
                    tab1.saveAs();
                    jTabbedPane.setTitleAt(jTabbedPane.getSelectedIndex(), tab1.getFileName());
                }
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
    }

    private void addTab(SpellCheckTab tab) {
        jTabbedPane.addTab(tab.getFileName(), tab);
        jTabbedPane.setTabComponentAt(jTabbedPane.indexOfComponent(tab), new CloseTabComponent(jTabbedPane));
    }

    private void initLayout(JFrame parent) {
        MigLayout migLayout = new MigLayout("wrap 1", "0[grow]0", "0[grow]0");
        getContentPane().setLayout(migLayout);

        add(jTabbedPane, "grow");

        setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
        setLocationRelativeTo(parent);
        pack();
    }

    private void jExitMenuItemActionPerformed(ActionEvent evt) {
        this.setVisible(false);
    }

    /**
     * Custom initializations.
     */
    private void init() throws SpellCheckerException {

        setIconImage(IconManager.getImageIcon("spellcheck.png", IconManager.IconSize.SIZE16).getImage());
    }


    private void setSelectedLanguage(Language language) {
        assert language != null : "selectedLanguage is null";

        if (selectedLanguage != language) {
            selectedLanguage = language;
            SpellCheckTab tab = (SpellCheckTab) jTabbedPane.getSelectedComponent();
            if (tab != null)
                tab.setSelectedLanguage(language);
        }
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
            setText(SwingUtil.languageToLowerCase(language));
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
        if (tab != null) {
            addTab(tab);
            return tab;
        } else {
            return createNewTab();
        }
    }

    private SpellCheckTab createNewTab() {
        SpellCheckTab tab = new SpellCheckTab();
        addTab(tab);
        return tab;
    }

    public class CloseTabComponent extends JPanel {
        private final JTabbedPane pane;

        public CloseTabComponent(final JTabbedPane pane) {
            //unset default FlowLayout' gaps
            super(new FlowLayout(FlowLayout.LEFT, 0, 0));
            if (pane == null) {
                throw new NullPointerException("TabbedPane is null");
            }
            this.pane = pane;
            setOpaque(false);

            //make JLabel read titles from JTabbedPane
            JLabel label = new JLabel() {
                public String getText() {
                    int i = pane.indexOfTabComponent(CloseTabComponent.this);
                    if (i != -1) {
                        return pane.getTitleAt(i);
                    }
                    return null;
                }
            };

            add(label);
            //add more space between the label and the button
            label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
            //tab button
            JButton button = new TabButton();
            add(button);
            //add more space to the top of the component
            setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
        }

        private class TabButton extends JButton implements ActionListener {
            public TabButton() {
                int size = 17;
                setPreferredSize(new Dimension(size, size));
                setToolTipText("close this tab");
                //Make the button looks the same for all Laf's
                setUI(new BasicButtonUI());
                //Make it transparent
                setContentAreaFilled(false);
                //No need to be focusable
                setFocusable(false);
                setBorder(BorderFactory.createEtchedBorder());
                setBorderPainted(false);
                //Making nice rollover effect
                //we use the same listener for all buttons
                addMouseListener(buttonMouseListener);
                setRolloverEnabled(true);
                //Close the proper tab by clicking the button
                addActionListener(this);
            }

            public void actionPerformed(ActionEvent e) {
                int i = pane.indexOfTabComponent(CloseTabComponent.this);
                if (i != -1) {
                    SpellCheckTab tab = (SpellCheckTab) jTabbedPane.getComponentAt(i);
                    if (!tab.isSaved()) {
                        Object[] options = {TRANSLATOR.translate("SaveChanges(Yes)"),
                                TRANSLATOR.translate("SaveChanges(No)"),
                                TRANSLATOR.translate("SaveChanges(Cancel)")};
                        int n = JOptionPane.showOptionDialog(SpellCheckFrame.this,
                                TRANSLATOR.translate("SaveChanges(Content)"),
                                TRANSLATOR.translate("SaveChanges(Title)"),
                                JOptionPane.YES_NO_CANCEL_OPTION,
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                options,
                                options[2]);
                        if (n == JOptionPane.YES_OPTION) {
                            tab.save();
                        } else if (n == JOptionPane.CANCEL_OPTION)
                            return;
                    }

                    pane.remove(i);
                }
            }

            //we don't want to update UI for this button

            public void updateUI() {
            }

            //paint the cross

            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                //shift the image for pressed buttons
                if (getModel().isPressed()) {
                    g2.translate(1, 1);
                }
                g2.setStroke(new BasicStroke(2));
                g2.setColor(Color.BLACK);
                if (getModel().isRollover()) {
                    g2.setColor(Color.MAGENTA);
                }
                int delta = 6;
                g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
                g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
                g2.dispose();
            }
        }

        private final MouseListener buttonMouseListener = new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                Component component = e.getComponent();
                if (component instanceof AbstractButton) {
                    AbstractButton button = (AbstractButton) component;
                    button.setBorderPainted(true);
                }
            }

            public void mouseExited(MouseEvent e) {
                Component component = e.getComponent();
                if (component instanceof AbstractButton) {
                    AbstractButton button = (AbstractButton) component;
                    button.setBorderPainted(false);
                }
            }
        };
    }
}
