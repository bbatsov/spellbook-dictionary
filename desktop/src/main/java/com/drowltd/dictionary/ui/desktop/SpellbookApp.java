package com.drowltd.dictionary.ui.desktop;

import com.drowltd.dictionary.core.i18n.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Locale;
import java.util.prefs.Preferences;

/**
 * User: bozhidar
 * Date: Sep 5, 2009
 * Time: 1:26:50 PM
 */
public class SpellbookApp extends JFrame {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpellbookApp.class);

    private Translator translator;

    private SpellbookForm spellbookForm;

    private static Preferences preferences = Preferences.userNodeForPackage(SpellbookApp.class);

    public SpellbookApp() throws HeadlessException {
        if (preferences.get("LANG", "EN").equals("BG")) {
            Locale.setDefault(new Locale("bg", "BG"));
            LOGGER.info("Selected locate is " + Locale.getDefault());
        }

        translator = new Translator("DesktopUI");

        //dynamically determine an adequate frame size
        Toolkit toolkit = Toolkit.getDefaultToolkit();

        Dimension screenSize = toolkit.getScreenSize();

        setSize(screenSize.width / 2, screenSize.height / 2);
        setLocationByPlatform(true);

        //set the frame title
        setTitle(translator.translate("ApplicationName(Title)"));

        //set the content of the frame
        spellbookForm = new SpellbookForm();
        setContentPane(spellbookForm.getComponent());

        //set the menu
        setJMenuBar(createmenuBar());

        //set the frame icon
        setIconImage(IconManager.getImageIcon("dictionary.png", IconManager.IconSize.SIZE16).getImage());

        //create tray
        createTraySection();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowIconified(WindowEvent e) {
                if (preferences.getBoolean("MIN_TO_TRAY", false)) {
                    LOGGER.info("Minimizing Spellbook to tray");
                    setVisible(false);
                }
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
                LOGGER.info("deiconified");
            }

            @Override
            public void windowClosing(WindowEvent e) {
                if (preferences.getBoolean("CLOSE_TO_TRAY", false)) {
                    LOGGER.info("Minimizing Spellbook to tray on window close");
                    setVisible(false);
                }
            }
        });
    }

    private JMenuBar createmenuBar() {
        JMenuBar menuBar;
        JMenu menu;
        JMenuItem menuItem;
        JRadioButtonMenuItem rbMenuItem;

        //Create the menu bar.
        menuBar = new JMenuBar();

        //Build the file menu.
        menu = new JMenu(translator.translate("File(Menu)"));
        menu.setMnemonic(KeyEvent.VK_F);
        menu.getAccessibleContext().setAccessibleDescription("File menu");
        menuBar.add(menu);

        menuItem = new JMenuItem(translator.translate("FileExit(MenuItem)"), KeyEvent.VK_X);
        menuItem.setIcon(IconManager.getMenuIcon("exit.png"));
        menuItem.getAccessibleContext().setAccessibleDescription("Exit Spellbook Dict");

        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                LOGGER.info("Exit from menu");
                System.exit(0);
            }
        });

        menu.add(menuItem);

        //Build the edit menu
        menu = new JMenu(translator.translate("Edit(Menu)"));
        menu.setMnemonic(KeyEvent.VK_E);
        menu.getAccessibleContext().setAccessibleDescription("Edit menu");
        menuBar.add(menu);

        menuItem = new JMenuItem(translator.translate("EditPreferences(MenuItem)"), KeyEvent.VK_P);
        menuItem.getAccessibleContext().setAccessibleDescription("Edit Spellbook Dict preferences");

        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                PreferencesForm preferencesForm = new PreferencesForm();

                int response = JOptionPane.showConfirmDialog(SpellbookApp.this, preferencesForm.getComponent(),
                        translator.translate("Preferences(Title)"), JOptionPane.OK_CANCEL_OPTION);

                if (response == JOptionPane.OK_OPTION) {
                    String oldLanguage = preferences.get("LANG", "EN");
                    final String newLanguage = preferencesForm.getSelectedLanguage().toString();
                    preferences.put("LANG", newLanguage);

                    if (!oldLanguage.equals(newLanguage)) {
                        LOGGER.info("Language changed from " + oldLanguage + " to " + newLanguage);
                        JOptionPane.showMessageDialog(SpellbookApp.this, translator.translate("Restart(Message)"));
                    }

                    final boolean minimizeToTrayEnabled = preferencesForm.isMinimizeToTrayEnabled();

                    if (minimizeToTrayEnabled) {
                        LOGGER.info("Minimize to tray is enabled");
                    } else {
                        LOGGER.info("Minimize to tray is disabled");
                    }

                    preferences.putBoolean("MIN_TO_TRAY", minimizeToTrayEnabled);

                    boolean minimizeToTrayOnCloseEnabled = preferencesForm.isMinimizeToTrayOnCloseEnabled();

                    if (minimizeToTrayOnCloseEnabled) {
                        LOGGER.info("Minimize to tray on close is enabled");
                        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                    } else {
                        LOGGER.info("Minimize to tray on close is disabled");
                    }

                    preferences.putBoolean("CLOSE_TO_TRAY", minimizeToTrayOnCloseEnabled);

                    final boolean clipboardIntegrationEnabled = preferencesForm.isClipboardIntegrationEnabled();

                    if (clipboardIntegrationEnabled) {
                        LOGGER.info("Clipboard integration is enabled");
                    } else {
                        LOGGER.info("Clipboard integration is disabled");
                    }

                    preferences.putBoolean("CLIPBOARD_INTEGRATION", clipboardIntegrationEnabled);
                }
            }
        });

        menu.add(menuItem);

        //Build dictionaries menu
        menu = new JMenu(translator.translate("Dictionaries(Menu)"));
        menu.setMnemonic(KeyEvent.VK_D);
        menu.getAccessibleContext().setAccessibleDescription("Select the active dictionary");
        menuBar.add(menu);

        ButtonGroup group = new ButtonGroup();
        rbMenuItem = new JRadioButtonMenuItem(translator.translate("DictionariesEnBg(MenuItem)"));
        rbMenuItem.setSelected(true);

        rbMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                spellbookForm.selectDictionary("en_bg");
            }
        });

        group.add(rbMenuItem);
        menu.add(rbMenuItem);

        rbMenuItem = new JRadioButtonMenuItem(translator.translate("DictionariesBgEn(MenuItem)"));

        rbMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                spellbookForm.selectDictionary("bg_en");
            }
        });

        group.add(rbMenuItem);
        menu.add(rbMenuItem);

        //Build exam menu
        menu = new JMenu(translator.translate("Exams(Menu)"));
        menu.setMnemonic(KeyEvent.VK_E);
        menu.getAccessibleContext().setAccessibleDescription("Select the active dictionary");
        menuBar.add(menu);

        group = new ButtonGroup();
        rbMenuItem = new JRadioButtonMenuItem(translator.translate("ExamsEnBg(MenuItem)"));
        rbMenuItem.setActionCommand("en_bg");
        rbMenuItem.setSelected(true);

        rbMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFrame examFrame = new ExamFrame();
                examFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                examFrame.setVisible(true);
            }
        });

        group.add(rbMenuItem);
        menu.add(rbMenuItem);

        rbMenuItem = new JRadioButtonMenuItem(translator.translate("ExamsBgEn(MenuItem)"));

        rbMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFrame examFrame = new ExamFrame();
                examFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                examFrame.setVisible(true);
            }
        });

        group.add(rbMenuItem);
        menu.add(rbMenuItem);

        //help menu
        menu = new JMenu(translator.translate("Help(Menu)"));
        menu.setMnemonic(KeyEvent.VK_H);
        menuBar.add(menu);

        menuItem = new JMenuItem(translator.translate("HelpAbout(MenuItem)"), KeyEvent.VK_A);
        menuItem.setIcon(IconManager.getMenuIcon("about.png"));
        menuItem.getAccessibleContext().setAccessibleDescription("About Spellbook Dict");

        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(SpellbookApp.this, translator.translate("About(Message)"));
            }
        });

        menu.add(menuItem);

        return menuBar;
    }

    private void createTraySection() {
        //Check the SystemTray support
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }
        final PopupMenu popup = new PopupMenu();
        final TrayIcon trayIcon =
                new TrayIcon(IconManager.getImageIcon("dictionary.png", IconManager.IconSize.SIZE48).getImage());
        trayIcon.setImageAutoSize(true);
        trayIcon.setToolTip("Spellbook Dictionary");
        final SystemTray tray = SystemTray.getSystemTray();

        // Create a popup menu components
        MenuItem aboutItem = new MenuItem("About");
        MenuItem exitItem = new MenuItem("Exit");

        //Add components to popup menu
        popup.add(aboutItem);
        popup.addSeparator();
        popup.add(exitItem);

        trayIcon.setPopupMenu(popup);

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
            return;
        }

        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                LOGGER.info("Tray icon clicked");

                if (SpellbookApp.this.getState() == Frame.ICONIFIED) {
                    LOGGER.info("App is iconified");
                    SpellbookApp.this.setState(Frame.NORMAL);
                }

                SpellbookApp.this.setVisible(!SpellbookApp.this.isVisible());

            }
        });

        aboutItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null,
                        translator.translate("About(Message)"));
            }
        });


        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                LOGGER.info("Exit from tray");
                tray.remove(trayIcon);
                System.exit(0);
            }
        });
    }

    public static void main(String[] args) {
        try {
            // Set System L&F
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (UnsupportedLookAndFeelException e) {
            // handle exception
        }
        catch (ClassNotFoundException e) {
            // handle exception
        }
        catch (InstantiationException e) {
            // handle exception
        }
        catch (IllegalAccessException e) {
            // handle exception
        }

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                JFrame appFrame = new SpellbookApp();
                appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                appFrame.setVisible(true);
            }
        });
    }
}