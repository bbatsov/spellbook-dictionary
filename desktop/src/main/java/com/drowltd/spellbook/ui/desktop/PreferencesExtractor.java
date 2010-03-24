/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drowltd.spellbook.ui.desktop;

import com.drowltd.spellbook.core.i18n.Translator;
import com.drowltd.spellbook.core.preferences.PreferencesManager;
import java.awt.Font;
import java.util.logging.Level;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.drowltd.spellbook.core.preferences.PreferencesManager.Preference;

/**
 * A helper class that extracts the preferences from the preferences dialog.
 *
 * @author Bozhidar Batsov
 * @since 0.3
 */
public class PreferencesExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpellbookFrame.class);
    private static final Translator TRANSLATOR = Translator.getTranslator("SpellbookForm");
    private static final PreferencesManager PM = PreferencesManager.getInstance();

    public static void extract(SpellbookFrame spellbookFrame, PreferencesDialog preferencesDialog) {
        if (preferencesDialog.showDialog()) {
            String oldLanguage = PM.get(Preference.UI_LANG, "EN");
            final String newLanguage = preferencesDialog.getSelectedLanguage().toString();
            PM.put(Preference.UI_LANG, newLanguage);

            if (!oldLanguage.equals(newLanguage)) {
                LOGGER.info("Language changed from " + oldLanguage + " to " + newLanguage);
                int selectedOption = JOptionPane.showConfirmDialog(spellbookFrame, TRANSLATOR.translate("Restart(Message)"), "Restart",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

                if (selectedOption == JOptionPane.OK_OPTION) {
                    spellbookFrame.restart();
                }
            }

            PM.put(Preference.DEFAULT_DICTIONARY, preferencesDialog.getDefaultDictionary());

            final boolean minimizeToTrayEnabled = preferencesDialog.isMinimizeToTrayEnabled();

            if (minimizeToTrayEnabled) {
                LOGGER.info("Minimize to tray is enabled");
            } else {
                LOGGER.info("Minimize to tray is disabled");
            }

            PM.putBoolean(Preference.MIN_TO_TRAY, minimizeToTrayEnabled);

            boolean minimizeToTrayOnCloseEnabled = preferencesDialog.isMinimizeToTrayOnCloseEnabled();

            if (minimizeToTrayOnCloseEnabled) {
                LOGGER.info("Minimize to tray on close is enabled");
                spellbookFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            } else {
                LOGGER.info("Minimize to tray on close is disabled");
                spellbookFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            }

            PM.putBoolean(Preference.CLOSE_TO_TRAY, minimizeToTrayOnCloseEnabled);

            final boolean clipboardIntegrationEnabled = preferencesDialog.isClipboardIntegrationEnabled();

            if (clipboardIntegrationEnabled) {
                spellbookFrame.activateClipboardMonitoring();
                LOGGER.info("Clipboard integration is enabled");
            } else {
                spellbookFrame.shutdownClipboardMonitoring();
                LOGGER.info("Clipboard integration is disabled");
            }

            PM.putBoolean(Preference.CLIPBOARD_INTEGRATION, clipboardIntegrationEnabled);

            final boolean trayPopupEnabled = preferencesDialog.isTrayPopupEnabled();

            if (trayPopupEnabled) {
                LOGGER.info("Tray popup is enabled");
            } else {
                LOGGER.info("Tray popup is disabled");
            }

            PM.putBoolean(Preference.TRAY_POPUP, trayPopupEnabled);

            final boolean showMemoryUsageEnabled = preferencesDialog.isShowMemoryUsageEnabled();

            if (showMemoryUsageEnabled) {
                LOGGER.info("Show memory usage is enabled");
                spellbookFrame.showMemoryUsage();
            } else {
                LOGGER.info("Show memory usage is disabled");
                spellbookFrame.hideMemoryUsage();
            }

            PM.putBoolean(Preference.SHOW_MEMORY_USAGE, showMemoryUsageEnabled);

            final boolean alwaysOnTopEnabled = preferencesDialog.isAlwaysOnTopEnabled();

            if (alwaysOnTopEnabled) {
                LOGGER.info("Always on top enabled");
                spellbookFrame.setAlwaysOnTop(true);
            } else {
                LOGGER.info("Always on top disabled");
                spellbookFrame.setAlwaysOnTop(false);
            }

            PM.putBoolean(Preference.ALWAYS_ON_TOP, alwaysOnTopEnabled);

            String selectedLookAndFeel = preferencesDialog.getSelectedLookAndFeel();

            if (!selectedLookAndFeel.equals(PM.get(Preference.LOOK_AND_FEEL, "System"))) {
                PM.put(Preference.LOOK_AND_FEEL, selectedLookAndFeel);
            }

            final boolean emptyLineSelected = preferencesDialog.isEmptyLineEnabled();

            if (emptyLineSelected) {
                LOGGER.info("Empty line after each meaning enabled");
            } else {
                LOGGER.info("Empty line after each meaning disabled");
            }

            PM.putBoolean(Preference.EMPTY_LINE, emptyLineSelected);

            // set the font
            final Font selectedFont = preferencesDialog.getSelectedFont();

            PM.put(Preference.FONT_NAME, selectedFont.getName());
            PM.putInt(Preference.FONT_SIZE, selectedFont.getSize());
            PM.putInt(Preference.FONT_STYLE, selectedFont.getStyle());

            spellbookFrame.setSelectedFont(selectedFont);

            // exam prefs
            PM.put(Preference.EXAM_DIFFICULTY, preferencesDialog.getExamDifficulty().toString());
            PM.putInt(Preference.EXAM_WORDS, preferencesDialog.getExamWords());
            PM.putBoolean(Preference.EXAM_TIMER, preferencesDialog.isExamTimerEnabled());

        } else {
            // we need to restore the old look and feel manually since it was changed on selection
            LookAndFeelInfo[] lookAndFeelInfos = UIManager.getInstalledLookAndFeels();

            String selectedLookAndFeel = PM.get(Preference.LOOK_AND_FEEL, "System");

            if (selectedLookAndFeel.equals("System")) {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException ex) {
                    java.util.logging.Logger.getLogger(SpellbookFrame.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InstantiationException ex) {
                    java.util.logging.Logger.getLogger(SpellbookFrame.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    java.util.logging.Logger.getLogger(SpellbookFrame.class.getName()).log(Level.SEVERE, null, ex);
                } catch (UnsupportedLookAndFeelException ex) {
                    java.util.logging.Logger.getLogger(SpellbookFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                for (LookAndFeelInfo lookAndFeelInfo : lookAndFeelInfos) {
                    if (lookAndFeelInfo.getName().equals(selectedLookAndFeel)) {
                        try {
                            UIManager.setLookAndFeel(lookAndFeelInfo.getClassName());
                        } catch (ClassNotFoundException e1) {
                            e1.printStackTrace();
                        } catch (InstantiationException e1) {
                            e1.printStackTrace();
                        } catch (IllegalAccessException e1) {
                            e1.printStackTrace();
                        } catch (UnsupportedLookAndFeelException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }

            SwingUtilities.updateComponentTreeUI(spellbookFrame);
        }
    }
}
