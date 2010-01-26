package com.drowltd.dictionary.ui.desktop;

import com.drowltd.dictionary.core.preferences.PreferencesManager;
import com.drowltd.dictionary.core.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import java.awt.EventQueue;
import java.util.Locale;

/**
 * The entry point in Spellbook. Here the preferences manager get initialized, some 
 * important settings are take into account(such as the initial look and feel and language)
 * and the application's main frame is created and displayed.
 * 
 * @author Bozhidar Batsov
 * @since 0.1
 *
 */
public class SpellbookApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpellbookApp.class);

    public static void main(final String[] args) {
        init();
    }

    public static void init() {
        // install the default exception handler
        Thread.setDefaultUncaughtExceptionHandler(new SpellbookDefaultExceptionHandler());

        PreferencesManager.init(SpellbookApp.class);

        final PreferencesManager pm = PreferencesManager.getInstance();

        if (pm.get("LANG", "EN").equals("BG")) {
            Locale.setDefault(new Locale("bg", "BG"));
            LOGGER.info("Selected locate is " + Locale.getDefault());
        } else {
            Locale.setDefault(Locale.ENGLISH);
        }

        try {
            String selectedLookAndFeel = pm.get("LOOK_AND_FEEL", "System");

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
        } catch (UnsupportedLookAndFeelException e) {
            // handle exception
        } catch (ClassNotFoundException e) {
            // handle exception
        } catch (InstantiationException e) {
            // handle exception
        } catch (IllegalAccessException e) {
            // handle exception
        }

        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                final SpellbookFrame tApp = new SpellbookFrame();

                if (pm.getBoolean("CLOSE_TO_TRAY", false)) {
                    LOGGER.info("Minimize to tray on close is enabled");
                    tApp.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                } else {
                    LOGGER.info("Minimize to tray on close is disabled");
                    tApp.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                }

                tApp.setVisible(true);
            }
        });
    }
}
