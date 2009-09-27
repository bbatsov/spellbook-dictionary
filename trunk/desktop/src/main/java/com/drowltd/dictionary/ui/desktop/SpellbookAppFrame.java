package com.drowltd.dictionary.ui.desktop;

import com.drowltd.dictionary.core.i18n.Translator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * User: bozhidar
 * Date: Sep 5, 2009
 * Time: 1:26:50 PM
 */
public class SpellbookAppFrame extends JFrame {
    private static final Translator TRANSLATOR = new Translator("DesktopUI");

    private SpellbookPanel spellbookPanel;

    public SpellbookAppFrame() throws HeadlessException {
        setSize(640, 480);
        setTitle(TRANSLATOR.translate("ApplicationName(Title)"));
        spellbookPanel = new SpellbookPanel();
        setContentPane(spellbookPanel.getComponent());
        setJMenuBar(createmenuBar());
        setIconImage(IconManager.getImageIcon("dictionary.png", IconManager.IconSize.SIZE16).getImage());
        createTraySection();
    }

    private JMenuBar createmenuBar() {
        JMenuBar menuBar;
        JMenu menu;
        JMenuItem menuItem;
        JRadioButtonMenuItem rbMenuItem;

        //Create the menu bar.
        menuBar = new JMenuBar();

        //Build the first menu.
        menu = new JMenu(TRANSLATOR.translate("File(Menu)"));
        menu.setMnemonic(KeyEvent.VK_F);
        menu.getAccessibleContext().setAccessibleDescription("File menu");
        menuBar.add(menu);

        //a group of JMenuItems
        menuItem = new JMenuItem(TRANSLATOR.translate("FileExit(MenuItem)"), KeyEvent.VK_X);
        menuItem.setIcon(IconManager.getMenuIcon("exit.png"));
        menuItem.getAccessibleContext().setAccessibleDescription("Exit Spellbook Dict");

        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        menu.add(menuItem);

        //Build dictionaries menu
        menu = new JMenu(TRANSLATOR.translate("Dictionaries(Menu)"));
        menu.setMnemonic(KeyEvent.VK_D);
        menu.getAccessibleContext().setAccessibleDescription("Select the active dictionary");
        menuBar.add(menu);

        ButtonGroup group = new ButtonGroup();
        rbMenuItem = new JRadioButtonMenuItem(TRANSLATOR.translate("DictionariesEnBg(MenuItem)"));
        rbMenuItem.setSelected(true);

        rbMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                spellbookPanel.selectDictionary("en_bg");
            }
        });

        group.add(rbMenuItem);
        menu.add(rbMenuItem);

        rbMenuItem = new JRadioButtonMenuItem(TRANSLATOR.translate("DictionariesBgEn(MenuItem)"));

        rbMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                spellbookPanel.selectDictionary("bg_en");
            }
        });

        group.add(rbMenuItem);
        menu.add(rbMenuItem);

        //Build exam menu
        menu = new JMenu("Exams");
        menu.setMnemonic(KeyEvent.VK_E);
        menu.getAccessibleContext().setAccessibleDescription("Select the active dictionary");
        menuBar.add(menu);

        group = new ButtonGroup();
        rbMenuItem = new JRadioButtonMenuItem(TRANSLATOR.translate("ExamsEnBg(MenuItem)"));
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

        rbMenuItem = new JRadioButtonMenuItem(TRANSLATOR.translate("ExamsBgEn(MenuItem)"));

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
        menu = new JMenu(TRANSLATOR.translate("Help(Menu)"));
        menu.setMnemonic(KeyEvent.VK_H);
        menuBar.add(menu);

        menuItem = new JMenuItem(TRANSLATOR.translate("HelpAbout(MenuItem)"), KeyEvent.VK_A);
        menuItem.setIcon(IconManager.getMenuIcon("about.png"));
        menuItem.getAccessibleContext().setAccessibleDescription("About Spellbook Dict");

        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(SpellbookAppFrame.this, "Drow Ltd.");
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
        trayIcon.setToolTip("Drow Dictionary");
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

        trayIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null,
                        "This dialog box is run from System Tray");
            }
        });

        aboutItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null,
                        "This dialog box is run from the About menu item");
            }
        });


        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
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
                JFrame appFrame = new SpellbookAppFrame();
                appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                appFrame.setVisible(true);
            }
        });
    }
}
