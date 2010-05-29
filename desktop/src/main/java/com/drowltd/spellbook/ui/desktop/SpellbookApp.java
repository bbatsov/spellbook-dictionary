package com.drowltd.spellbook.ui.desktop;

import com.drowltd.spellbook.core.i18n.Translator;
import com.drowltd.spellbook.core.model.Dictionary;
import com.drowltd.spellbook.core.model.Language;
import com.drowltd.spellbook.core.preferences.PreferencesManager;
import com.drowltd.spellbook.ui.swing.component.SelectDbDialog;
import com.drowltd.spellbook.ui.swing.component.SpellbookDefaultExceptionHandler;
import com.drowltd.spellbook.util.ArchiveUtils;
import com.jidesoft.dialog.StandardDialog;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.awt.SplashScreen;
import java.awt.Toolkit;
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

    private static JWindow splashWindow;
    private static JProgressBar progressBar;
    private static boolean startup = true;

    public static void main(final String[] args) {
        init();
    }

    private static void createSplashWindow() {
        final SplashScreen splashScreen = SplashScreen.getSplashScreen();

        final ImageIcon icon = new ImageIcon(SpellbookApp.class.getResource("/images/spellbook-splash.png"));

        Rectangle bounds;

        if (splashScreen != null) {
            bounds = splashScreen.getBounds();
            System.out.println(bounds);
        } else {
            splashWindow = new JWindow();
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            bounds = new Rectangle(toolkit.getScreenSize().width / 2, toolkit.getScreenSize().height / 2, icon.getIconWidth(), icon.getIconHeight());
        }

        splashWindow.setBounds((int) bounds.getX() - icon.getIconWidth() / 2, (int) bounds.getY() - icon.getIconHeight() / 2, (int) bounds.getWidth(), (int) bounds.getHeight());
        JPanel panel = new JPanel(new MigLayout("wrap 1", "[][grow]", "[][grow]"));

        JLabel splashImage = new JLabel();

        splashImage.setIcon(icon);

        panel.add(splashImage);
        progressBar = new JProgressBar(0, 4);
        progressBar.setStringPainted(true);
        progressBar.setString("Starting up Spellbook");
        panel.add(progressBar, "grow");

        splashWindow.setContentPane(panel);
        splashWindow.pack();
        splashWindow.setVisible(true);
    }

    private static void closeSplashWindow() {
        progressBar.setValue(progressBar.getMaximum());
        progressBar.setString("Done");
        splashWindow.setVisible(false);
    }

    private static void increaseProgress(String message) {
        progressBar.setValue(progressBar.getValue() + 1);
        progressBar.setString(message);
    }

    public static void init() {
        // don't show splash on restart
        if (startup) {
            createSplashWindow();
        }

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

        increaseProgress("Verifying database...");

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

                tAppFrame.init();

                closeSplashWindow();

                if (!pm.getBoolean(Preference.START_IN_TRAY, false)) {
                    tAppFrame.setVisible(true);
                    tAppFrame.showWordOfTheDay();
                }
            }
        });

        // next time we enter this method it will be a restart
        startup = false;
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
            final File archiveFile = new File(SPELLBOOK_USER_DIR + File.separator + "spellbook-db-0.3.tar.bz2");

            final SelectDbDialog selectDbDialog = new SelectDbDialog();

            if (archiveFile.exists() || selectDbDialog.showDialog() == StandardDialog.RESULT_AFFIRMED) {
                increaseProgress("Extracting database");
                ArchiveUtils.extractDbFromArchive(archiveFile.exists() ? archiveFile.getAbsolutePath() : selectDbDialog.getDbPath());
                increaseProgress("Loading database");
            } else {
                return false;
            }
        } else {
            increaseProgress("Loading database");

            dbPresent = true;
        }

        return true;
    }
}
