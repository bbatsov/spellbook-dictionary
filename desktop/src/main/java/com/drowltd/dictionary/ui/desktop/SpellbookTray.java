/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drowltd.dictionary.ui.desktop;

import com.drowltd.dictionary.core.i18n.Translator;
import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author bozhidar
 */
public class SpellbookTray {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpellbookTray.class);
    private static final Translator TRANSLATOR = new Translator("SpellbookTray");

    public static TrayIcon createTraySection(final JFrame appFrame) {
        //Check the SystemTray support
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return null;
        }

        final PopupMenu popup = new PopupMenu();
        final TrayIcon trayIcon =
                new TrayIcon(IconManager.getImageIcon("dictionary.png", IconManager.IconSize.SIZE48).getImage());
        trayIcon.setImageAutoSize(true);
        trayIcon.setToolTip("Spellbook Dictionary");

        final SystemTray tray = SystemTray.getSystemTray();

        // Create a popup menu components
        MenuItem aboutItem = new MenuItem(TRANSLATOR.translate("About(MenuItem)"));
        MenuItem exitItem = new MenuItem(TRANSLATOR.translate("Exit(MenuItem)"));

        //Add components to popup menu
        popup.add(aboutItem);
        popup.addSeparator();
        popup.add(exitItem);

        trayIcon.setPopupMenu(popup);

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
            return null;
        }

        trayIcon.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                LOGGER.info("Tray icon clicked");

                if (appFrame.getState() == JFrame.ICONIFIED) {
                    LOGGER.info("App is iconified");
                    appFrame.setState(JFrame.NORMAL);
                }

                appFrame.setVisible(!appFrame.isVisible());
            }
        });

        aboutItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                AboutDialog aboutDialog = new AboutDialog(appFrame, true);
                aboutDialog.setVisible(true);
            }
        });

        exitItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                LOGGER.info("Exit from tray");
                tray.remove(trayIcon);
                System.exit(0);
            }
        });

        return trayIcon;
    }
}
