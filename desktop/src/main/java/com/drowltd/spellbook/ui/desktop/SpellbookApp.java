package com.drowltd.spellbook.ui.desktop;

import com.drowltd.spellbook.core.i18n.Translator;
import com.drowltd.spellbook.core.model.Dictionary;
import com.drowltd.spellbook.core.model.Language;
import com.drowltd.spellbook.core.preferences.PreferencesManager;
import com.drowltd.spellbook.ui.swing.component.DownloadDialog;
import com.drowltd.spellbook.ui.swing.component.SpellbookDefaultExceptionHandler;
import com.drowltd.spellbook.util.ArchiveUtils;
import com.jidesoft.dialog.StandardDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import java.awt.EventQueue;
import java.io.File;
import java.util.Locale;

import static com.drowltd.spellbook.core.preferences.PreferencesManager.Preference;

/**
 * The entry point in Spellbook. Here the preferences manager get initialized, some
 * important settings are take into account(such as the initial look and feel and language)
 * and the application's main frame is created and displayed.
 *
 * @author Bozhidar Batsov
 * @since 0.1
 */
public class SpellbookApp {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpellbookApp.class);

    private static final Translator TRANSLATOR = Translator.getTranslator("SpellbookFrame");


    private static final String SPELLBOOK_USER_DIR = System.getProperty("user.home") + File.separator + ".spellbook";
    private static final String SPELLBOOK_DB_PATH = SPELLBOOK_USER_DIR + File.separator + "db" + File.separator + "spellbook.data.db";
    private static SpellbookFrame tAppFrame;
    private static boolean dbPresent = false;

    public static void main(final String[] args) {
        init();
    }

    public static void init() {
        // install the default exception handler
        Thread.setDefaultUncaughtExceptionHandler(new SpellbookDefaultExceptionHandler());

        PreferencesManager.init(SpellbookApp.class);

        final PreferencesManager pm = PreferencesManager.getInstance();

        if (pm.get(Preference.UI_LANG, Language.ENGLISH.getName()).equals(Language.BULGARIAN.getName())) {
            Locale.setDefault(new Locale("bg", "BG"));
            LOGGER.info("Selected locate is " + Locale.getDefault());
        } else {
            Locale.setDefault(Locale.ENGLISH);
        }

        Language.TRANSLATOR.reset();
        Dictionary.TRANSLATOR.reset();

        try {
            String selectedLookAndFeel = pm.get(Preference.LOOK_AND_FEEL, "System");

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

        // check the presence of the dictionary database
        if (!verifyDbPresence()) {
            JOptionPane.showMessageDialog(null, TRANSLATOR.translate("NoDbSelected(Message)"),
                    TRANSLATOR.translate("Error(Title)"), JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                tAppFrame = new SpellbookFrame(dbPresent);


            }
        });
    }

    private static boolean verifyDbPresence() {
        final File userDir = new File(SPELLBOOK_USER_DIR);
        if (!userDir.exists()) {
            if (userDir.mkdir()) {
                LOGGER.info("Successfully create user dir: " + SPELLBOOK_USER_DIR);
            }
        }

        File file = new File(SPELLBOOK_DB_PATH);

        if (!file.exists() || file.isDirectory()) {
            final DownloadDialog downloadDialog = new DownloadDialog();

            if (downloadDialog.showDialog() == StandardDialog.RESULT_AFFIRMED) {
                SwingWorker<Void, Void> swingWorker = new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        ArchiveUtils.extractDbFromArchive(downloadDialog.getDownloadedDbPath());

                        return null;
                    }

                    @Override
                    protected void done() {
                        tAppFrame.init();
                    }
                };

                swingWorker.execute();
            } else {
                return false;
            }
        } else {
            dbPresent = true;
        }

        return true;
    }
}
