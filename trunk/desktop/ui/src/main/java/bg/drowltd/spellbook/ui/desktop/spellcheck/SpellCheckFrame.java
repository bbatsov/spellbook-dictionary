package bg.drowltd.spellbook.ui.desktop.spellcheck;

import bg.drowltd.spellbook.ui.swing.util.IconManager;
import bg.drowltd.spellbook.core.exception.SpellCheckerException;
import bg.drowltd.spellbook.core.i18n.Translator;
import bg.drowltd.spellbook.core.model.Language;
import bg.drowltd.spellbook.ui.swing.util.SwingUtil;
import net.miginfocom.swing.MigLayout;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.plaf.basic.BasicButtonUI;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

/**
 * @author iivalchev
 * @since 0.2
 */
public class SpellCheckFrame extends JFrame {

    private static final Translator TRANSLATOR = Translator.getTranslator("SpellCheckFrame");
    private static SpellCheckFrame INSTANCE;
//    private static final Logger LOGGER = LoggerFactory.getLogger(SpellCheckFrame.class);
    private static final int MIN_WIDTH = 540;
    private static final int MIN_HEIGHT = 550;


    private Language selectedLanguage = Language.ENGLISH;
    private JMenu jDictionaryMenu;
    private JTabbedPane jTabbedPane;
    private JFileChooser jFileChooser;

    public static SpellCheckFrame getInstance(JFrame parent) throws HeapSizeException, SpellCheckerException {

        if (INSTANCE == null) {
            INSTANCE = new SpellCheckFrame(parent);
        }
        return INSTANCE;
    }

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
        createNewTab();

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

        setTitle(TRANSLATOR.translate("Frame(Title)"));

        jFileMenu.setText(TRANSLATOR.translate("FileMenu(Title)"));

        JMenuItem jNewJMenuItem = new JMenuItem(TRANSLATOR.translate("FileMenu(New)"));
        jNewJMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
        jNewJMenuItem.setIcon(IconManager.getMenuIcon("new.png"));
        jNewJMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createNewTab();
            }
        });

        jFileMenu.add(jNewJMenuItem);

        JMenuItem jOpenJMenuItem = new JMenuItem(TRANSLATOR.translate("FileMenu(Open)"));
        jOpenJMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
        jOpenJMenuItem.setIcon(IconManager.getMenuIcon("open.png"));
        jOpenJMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int result = jFileChooser.showOpenDialog(SpellCheckFrame.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    createNewTab(jFileChooser.getSelectedFile());
                }
            }
        });

        jFileMenu.add(jOpenJMenuItem);

        JMenuItem jSaveJMenuItem = new JMenuItem(TRANSLATOR.translate("FileMenu(Save)"));
        jSaveJMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
        jSaveJMenuItem.setIcon(IconManager.getMenuIcon("save.png"));
        jSaveJMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SpellCheckTab tab1 = (SpellCheckTab) jTabbedPane.getSelectedComponent();
                if (tab1 != null) {
                    tab1.save();
                    jTabbedPane.setTitleAt(jTabbedPane.getSelectedIndex(), tab1.getFileName());
                }
            }
        });
        jFileMenu.add(jSaveJMenuItem);

        JMenuItem jSaveAsJMenuItem = new JMenuItem(TRANSLATOR.translate("FileMenu(SaveAs)"));
        jSaveAsJMenuItem.setIcon(IconManager.getMenuIcon("save_as.png"));
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

        jEditMenu.setText(TRANSLATOR.translate("EditMenu(Title)"));

        jUndoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
        jUndoMenuItem.setIcon(IconManager.getMenuIcon("undo.png"));
        jUndoMenuItem.setText(TRANSLATOR.translate("EditMenu(Undo)"));
        jUndoMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                ((SpellCheckTab) jTabbedPane.getSelectedComponent()).undo();
            }
        });
        jEditMenu.add(jUndoMenuItem);

        jRedoMenuItem.setIcon(IconManager.getMenuIcon("redo.png"));
        jRedoMenuItem.setText(TRANSLATOR.translate("EditMenu(Redo)"));
        jRedoMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                ((SpellCheckTab) jTabbedPane.getSelectedComponent()).redo();
            }
        });
        jEditMenu.add(jRedoMenuItem);
        jEditMenu.add(jSeparator1);

        jCutMenuItem.setIcon(IconManager.getMenuIcon("cut.png"));
        jCutMenuItem.setText(TRANSLATOR.translate("EditMenu(Cut)"));
        jCutMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                ((SpellCheckTab) jTabbedPane.getSelectedComponent()).cut();
            }
        });
        jEditMenu.add(jCutMenuItem);

        jCopyMenuItem.setIcon(IconManager.getMenuIcon("copy.png"));
        jCopyMenuItem.setText(TRANSLATOR.translate("EditMenu(Copy)"));
        jCopyMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                ((SpellCheckTab) jTabbedPane.getSelectedComponent()).copy();
            }
        });
        jEditMenu.add(jCopyMenuItem);

        jPasteMenuItem.setIcon(IconManager.getMenuIcon("paste.png"));
        jPasteMenuItem.setText(TRANSLATOR.translate("EditMenu(Paste)"));
        jPasteMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                ((SpellCheckTab) jTabbedPane.getSelectedComponent()).paste();
            }
        });
        jEditMenu.add(jPasteMenuItem);

        jMenuBar1.add(jEditMenu);

        jDictionaryMenu.setText(TRANSLATOR.translate("LanguageMenu(Title)"));
        jMenuBar1.add(jDictionaryMenu);

        setJMenuBar(jMenuBar1);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                checkTabsForSave();
            }
        });
    }

    private void addTab(SpellCheckTab tab) {
        jTabbedPane.addTab(tab.getFileName(), tab);
        jTabbedPane.setTabComponentAt(jTabbedPane.indexOfComponent(tab), new CloseTabComponent(jTabbedPane));
        jTabbedPane.setSelectedComponent(tab);
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

    private void init() throws SpellCheckerException {

        setIconImage(IconManager.getImageIcon("spellcheck.png", IconManager.IconSize.SIZE16).getImage());
    }


    private void setSelectedLanguage(Language language) {
        assert language != null : "selectedLanguage is null";

        if (selectedLanguage != language) {
            selectedLanguage = language;
            SpellCheckTab tab = (SpellCheckTab) jTabbedPane.getSelectedComponent();
            if (tab != null) {
                tab.setSelectedLanguage(language);
                tab.spellCheck(true);
            }
        }
    }

    private void initLanguageMenu() {
        for (Language l : Language.values()) {
            jDictionaryMenu.add(new LanguageItem(l));
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
                JOptionPane.showMessageDialog(this, TRANSLATOR.translate("Message(OpenFileError)"), "Info", JOptionPane.INFORMATION_MESSAGE);
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
                    checkTabForSave(i);
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

    private void checkTabsForSave() {
        for (Component c : jTabbedPane.getComponents()) {
            int index = jTabbedPane.indexOfComponent(c);
            if (index != -1)
                checkTabForSave(index);
        }
    }

    private void checkTabForSave(int indexOfTab) {
        SpellCheckTab tab = (SpellCheckTab) jTabbedPane.getComponentAt(indexOfTab);
        if (!tab.isSaved()) {
            int n = showSaveDialog(tab.getFileName());
            if (n == JOptionPane.YES_OPTION) {
                tab.save();
            } else if (n == JOptionPane.CANCEL_OPTION)
                return;
        }
        jTabbedPane.remove(indexOfTab);
    }

    private int showSaveDialog(String docName) {
        Object[] options = {TRANSLATOR.translate("SaveChanges(Yes)"),
                TRANSLATOR.translate("SaveChanges(No)"),
                TRANSLATOR.translate("SaveChanges(Cancel)")};
        return JOptionPane.showOptionDialog(this,
                TRANSLATOR.translate("SaveChanges(Content)", docName),
                TRANSLATOR.translate("SaveChanges(Title)"),
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[2]);
    }
}
