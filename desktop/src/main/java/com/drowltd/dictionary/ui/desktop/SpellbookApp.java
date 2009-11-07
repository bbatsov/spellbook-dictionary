package com.drowltd.dictionary.ui.desktop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import java.awt.EventQueue;
import java.util.prefs.Preferences;

/**
 * User: bozhidar
 * Date: Sep 5, 2009
 * Time: 1:26:50 PM
 */
public class SpellbookApp {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpellbookApp.class);

    public static void main(final String[] args) {
        try {
            Preferences preferences = Preferences.userNodeForPackage(SpellbookApp.class);

            String selectedLookAndFeel = preferences.get("LOOK_AND_FEEL", "System");

            if (selectedLookAndFeel.equals("System")) {
                // Set System L&F
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } else {
                LookAndFeelInfo[] lookAndFeelInfos = UIManager.getInstalledLookAndFeels();

                for (LookAndFeelInfo lookAndFeelInfo : lookAndFeelInfos) {
                    if (lookAndFeelInfo.getName().equals(selectedLookAndFeel)) {
                        UIManager.setLookAndFeel(lookAndFeelInfo.getClassName());
                    }
                }
            }
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
            @Override
            public void run() {
                final SpellbookFrame tApp = new SpellbookFrame();
                tApp.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                tApp.setVisible(true);
            }
        });
    }
}
