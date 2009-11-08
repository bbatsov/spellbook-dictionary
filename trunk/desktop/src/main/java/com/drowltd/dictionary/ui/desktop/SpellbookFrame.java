/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * SpellbookFrame.java
 *
 * Created on Nov 5, 2009, 11:34:18 PM
 */
package com.drowltd.dictionary.ui.desktop;

import com.drowltd.dictionary.core.db.DictDb;
import com.drowltd.dictionary.core.exception.DictionaryDbLockedException;
import com.drowltd.dictionary.core.i18n.Translator;
import com.drowltd.dictionary.ui.desktop.IconManager.IconSize;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author bozhidar
 */
public class SpellbookFrame extends javax.swing.JFrame {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpellbookFrame.class);
    private static final Translator TRANSLATOR = new Translator("SpellbookForm");
    private static final Preferences PREFS = Preferences.userNodeForPackage(SpellbookApp.class);
    private DictDb dictDb;
    private List<String> words;
    private ClipboardTextTransfer clipboardTextTransfer;
    private String lastTransfer;
    private ScheduledExecutorService clipboardExecutorService;
    private TrayIcon trayIcon;

    /** Creates new form SpellbookFrame */
    public SpellbookFrame() {
        //dynamically determine an adequate frame size
        Toolkit toolkit = Toolkit.getDefaultToolkit();

        Dimension screenSize = toolkit.getScreenSize();

        setSize(screenSize.width / 2, screenSize.height / 2);
        setLocationByPlatform(true);

        //set the frame title
        setTitle(TRANSLATOR.translate("ApplicationName(Title)"));

        //set the frame icon
        setIconImage(IconManager.getImageIcon("dictionary.png", IconManager.IconSize.SIZE16).getImage());

        //create tray
        trayIcon = SpellbookTray.createTraySection(this);

        addWindowListener(new WindowAdapter() {

            @Override
            public void windowIconified(WindowEvent e) {
                if (PREFS.getBoolean("MIN_TO_TRAY", false)) {
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
                if (PREFS.getBoolean("CLOSE_TO_TRAY", false)) {
                    LOGGER.info("Minimizing Spellbook to tray on window close");
                    setVisible(false);
                }
            }
        });

        while (true) {
            if (verifyDbPresence(PREFS)) {
                break;
            }
        }

        try {
            DictDb.init(PREFS.get("PATH_TO_DB", ""));
        } catch (DictionaryDbLockedException e) {
            JOptionPane.showMessageDialog(null, TRANSLATOR.translate("AlreadyRunning(Message)"));
            System.exit(0);
        }

        dictDb = DictDb.getInstance();

        words = dictDb.getWordsFromSelectedDictionary();

        initComponents();

        statusBar.setText(String.format(TRANSLATOR.translate("EnBgDictSize(Label)"), words.size()));
        statusBar.setIcon(IconManager.getImageIcon("en-bg.png", IconSize.SIZE24));

        setDefaultFont();

        if (PREFS.getBoolean("CLIPBOARD_INTEGRATION", false)) {
            activateClipboardMonitoring();
        }
    }

    private void clear() {
        wordSearchField.setText(null);
        wordsList.ensureIndexIsVisible(0);
        wordsList.clearSelection();
        wordTranslationTextArea.setText(null);
        matchLabel.setIcon(IconManager.getImageIcon("bell2_red.png", IconSize.SIZE24));
    }

    public void activateClipboardMonitoring() {
        LOGGER.info("Activating clipboard monitoring");

        if (clipboardExecutorService == null || clipboardExecutorService.isShutdown()) {

            clipboardTextTransfer = new ClipboardTextTransfer();

            Runnable clipboardRunnable = new Runnable() {

                @Override
                public void run() {
                    String transferredText = clipboardTextTransfer.getClipboardContents();

                    if (lastTransfer == null) {
                        lastTransfer = transferredText;
                    }

                    if (!transferredText.equalsIgnoreCase(lastTransfer)) {
                        LOGGER.info("'" + transferredText + "' received from clipboard");
                        String searchString = transferredText.split("\\W")[0].toLowerCase();
                        LOGGER.info("Search string from clipboard is " + searchString);
                        wordSearchField.setText(searchString);
                        wordSearchField.selectAll();
                        lastTransfer = transferredText;

                        if (words.contains(searchString)) {
                            int index = words.indexOf(searchString);
                            wordsList.setSelectedIndex(index);
                            wordsList.ensureIndexIsVisible(index);
                            wordTranslationTextArea.setText(dictDb.getTranslation(searchString));
                        }
                    }
                }
            };

            clipboardExecutorService = Executors.newSingleThreadScheduledExecutor();
            clipboardExecutorService.scheduleAtFixedRate(clipboardRunnable, 0, 1, TimeUnit.SECONDS);
        } else {
            LOGGER.info("Clipboard monitoring is already running");
        }
    }

    public void shutdownClipboardMonitoring() {
        if (clipboardExecutorService != null && !clipboardExecutorService.isShutdown()) {
            LOGGER.info("Shutting down clipboard monitoring");
            clipboardExecutorService.shutdown();
        }
    }

    private void setDefaultFont() {
        if (PREFS.get("FONT_NAME", "").isEmpty()) {
            // dirty fix for windows - it seem that the default font there is too small, so we set
            // a more appropriate one
            String osName = System.getProperty("os.name");

            if (osName.contains("Windows")) {
                wordTranslationTextArea.setFont(new Font("Arial", Font.PLAIN, 14));
            }
        } else {
            String fontName = PREFS.get("FONT_NAME", "SansSerif");
            int fontSize = PREFS.getInt("FONT_SIZE", 14);
            int fontStyle = PREFS.getInt("FONT_STYLE", Font.PLAIN);

            setSelectedFont(new Font(fontName, fontStyle, fontSize));
        }
    }

    private boolean verifyDbPresence(Preferences prefs) {
        final String dbPath = prefs.get("PATH_TO_DB", "");

        File file = new File(dbPath);

        if (!file.exists()) {
            if (dbPath.isEmpty()) {
                JOptionPane.showMessageDialog(null, TRANSLATOR.translate("SelectDb(Message)"));
            } else {
                JOptionPane.showMessageDialog(null, TRANSLATOR.translate("MissingDb(Message)"));
            }

            JFileChooser fileChooser = new JFileChooser();
            final int result = fileChooser.showDialog(null, TRANSLATOR.translate("SelectDb(Title)"));

            if (result == JFileChooser.APPROVE_OPTION) {
                String selectedDbPath = fileChooser.getSelectedFile().getPath();

                if (selectedDbPath.endsWith("dictionary.data.db")) {
                    prefs.put("PATH_TO_DB", selectedDbPath);
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

        return true;
    }

    public void setSelectedFont(Font font) {
        wordSearchField.setFont(font);
        wordsList.setFont(font);
        wordTranslationTextArea.setFont(font);
        statusBar.setFont(font);
        drowLabel.setFont(font);
        clearButton.setFont(font);
    }

    public void selectDictionary(String dictionary) {
        // if we select the currently selected dictionary we don't have to do nothing
        if (dictionary.equalsIgnoreCase(dictDb.getSelectedDictionary())) {
            LOGGER.info("Dictionary " + dictionary + " is already selected");
            return;
        }

        dictDb.setSelectedDictionary(dictionary);

        words = dictDb.getWordsFromSelectedDictionary();
        wordsList.setListData(words.toArray());

        if (dictionary.equalsIgnoreCase("en_bg")) {
            statusBar.setText(String.format(TRANSLATOR.translate("EnBgDictSize(Label)"), words.size()));
            statusBar.setIcon(IconManager.getImageIcon("en-bg.png", IconSize.SIZE24));
        } else {
            statusBar.setText(String.format(TRANSLATOR.translate("BgEnDictSize(Label)"), words.size()));
            statusBar.setIcon(IconManager.getImageIcon("bg-en.png", IconSize.SIZE24));
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        wordSearchField = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        wordsList = new javax.swing.JList();
        clearButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        wordTranslationTextArea = new javax.swing.JTextArea();
        matchLabel = new javax.swing.JLabel();
        drowLabel = new javax.swing.JLabel();
        statusBar = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        exitMenuItem = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        fontMenuItem = new javax.swing.JMenuItem();
        prefsMenuItem = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        enBgDictMenuItem = new javax.swing.JMenuItem();
        bgEnDictMenuItem = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();
        jMenu5 = new javax.swing.JMenu();
        aboutMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        wordSearchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                wordSearchFieldKeyReleased(evt);
            }
        });

        wordsList.setModel(new WordsListModel(words));
        wordsList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        wordsList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                wordsListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(wordsList);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("i18n/SpellbookForm"); // NOI18N
        clearButton.setText(bundle.getString("ClearButton(Label)")); // NOI18N
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });

        wordTranslationTextArea.setColumns(20);
        wordTranslationTextArea.setRows(5);
        jScrollPane2.setViewportView(wordTranslationTextArea);

        matchLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/24x24/bell2_red.png"))); // NOI18N

        drowLabel.setText(bundle.getString("FuelledBy(Label)")); // NOI18N

        statusBar.setText("Status");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(wordSearchField)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 191, Short.MAX_VALUE))
                        .addGap(16, 16, 16)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(clearButton, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(matchLabel)
                                .addGap(18, 18, 18)
                                .addComponent(drowLabel))
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 512, Short.MAX_VALUE)))
                    .addComponent(statusBar))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(wordSearchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(clearButton)
                    .addComponent(matchLabel)
                    .addComponent(drowLabel))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 385, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 385, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusBar)
                .addContainerGap())
        );

        jMenu1.setText(bundle.getString("File(Menu)")); // NOI18N

        exitMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/16x16/exit.png"))); // NOI18N
        exitMenuItem.setText(bundle.getString("FileExit(MenuItem)")); // NOI18N
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(exitMenuItem);

        jMenuBar1.add(jMenu1);

        jMenu2.setText(bundle.getString("Edit(Menu)")); // NOI18N

        fontMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/16x16/font.png"))); // NOI18N
        fontMenuItem.setText(bundle.getString("EditFont(MenuItem)")); // NOI18N
        fontMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fontMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(fontMenuItem);

        prefsMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/16x16/preferences.png"))); // NOI18N
        prefsMenuItem.setText(bundle.getString("EditPreferences(MenuItem)")); // NOI18N
        prefsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prefsMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(prefsMenuItem);

        jMenuBar1.add(jMenu2);

        jMenu3.setText(bundle.getString("Dictionaries(Menu)")); // NOI18N

        enBgDictMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/16x16/en-bg.png"))); // NOI18N
        enBgDictMenuItem.setText(bundle.getString("DictionariesEnBg(MenuItem)")); // NOI18N
        enBgDictMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enBgDictMenuItemActionPerformed(evt);
            }
        });
        jMenu3.add(enBgDictMenuItem);

        bgEnDictMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/16x16/bg-en.png"))); // NOI18N
        bgEnDictMenuItem.setText(bundle.getString("DictionariesBgEn(MenuItem)")); // NOI18N
        bgEnDictMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bgEnDictMenuItemActionPerformed(evt);
            }
        });
        jMenu3.add(bgEnDictMenuItem);

        jMenuBar1.add(jMenu3);

        jMenu4.setText(bundle.getString("Exams(Menu)")); // NOI18N
        jMenuBar1.add(jMenu4);

        jMenu5.setText(bundle.getString("Help(Menu)")); // NOI18N

        aboutMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/16x16/about.png"))); // NOI18N
        aboutMenuItem.setText(bundle.getString("HelpAbout(MenuItem)")); // NOI18N
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });
        jMenu5.add(aboutMenuItem);

        jMenuBar1.add(jMenu5);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void wordsListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_wordsListValueChanged
        int firstIndex = evt.getFirstIndex();
        int lastIndex = evt.getLastIndex();

        if (wordsList.isSelectedIndex(firstIndex)) {
            wordTranslationTextArea.setText(dictDb.getTranslation(words.get(firstIndex)));
        } else {
            wordTranslationTextArea.setText(dictDb.getTranslation(words.get(lastIndex)));
        }

        matchLabel.setIcon(IconManager.getImageIcon("bell2_green.png", IconSize.SIZE24));
        matchLabel.setToolTipText(TRANSLATOR.translate("MatchFound(ToolTip)"));
    }//GEN-LAST:event_wordsListValueChanged

    private void wordSearchFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_wordSearchFieldKeyReleased
        String searchString = wordSearchField.getText();

        // in case the user types backspaces
        if (searchString.isEmpty()) {
            clear();
        }

        if (words.contains(searchString) || words.contains(searchString.toLowerCase())) {
            int index = words.indexOf(searchString);

            if (index < 0) {
                searchString = searchString.toLowerCase();
                index = words.indexOf(searchString);
            }

            wordsList.setSelectedIndex(index);
            wordsList.ensureIndexIsVisible(index);
            wordTranslationTextArea.setText(dictDb.getTranslation(searchString));
            matchLabel.setIcon(IconManager.getImageIcon("bell2_green.png", IconSize.SIZE24));
            matchLabel.setToolTipText(TRANSLATOR.translate("MatchFound(ToolTip)"));
        } else {
            matchLabel.setIcon(IconManager.getImageIcon("bell2_red.png", IconSize.SIZE24));
            matchLabel.setToolTipText(TRANSLATOR.translate("NoMatchFound(ToolTip)"));
        }
    }//GEN-LAST:event_wordSearchFieldKeyReleased

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
        clear();
    }//GEN-LAST:event_clearButtonActionPerformed

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        System.exit(0);
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void prefsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prefsMenuItemActionPerformed
        PreferencesDialog preferencesDialog = new PreferencesDialog(this, true);

        if (preferencesDialog.showDialog()) {
            String oldLanguage = PREFS.get("LANG", "EN");
            final String newLanguage = preferencesDialog.getSelectedLanguage().toString();
            PREFS.put("LANG", newLanguage);

            if (!oldLanguage.equals(newLanguage)) {
                LOGGER.info("Language changed from " + oldLanguage + " to " + newLanguage);
                JOptionPane.showMessageDialog(this, TRANSLATOR.translate("Restart(Message)"));
            }

            final boolean minimizeToTrayEnabled = preferencesDialog.isMinimizeToTrayEnabled();

            if (minimizeToTrayEnabled) {
                LOGGER.info("Minimize to tray is enabled");
            } else {
                LOGGER.info("Minimize to tray is disabled");
            }

            PREFS.putBoolean("MIN_TO_TRAY", minimizeToTrayEnabled);

            boolean minimizeToTrayOnCloseEnabled = preferencesDialog.isMinimizeToTrayOnCloseEnabled();

            if (minimizeToTrayOnCloseEnabled) {
                LOGGER.info("Minimize to tray on close is enabled");
                setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            } else {
                LOGGER.info("Minimize to tray on close is disabled");
            }

            PREFS.putBoolean("CLOSE_TO_TRAY", minimizeToTrayOnCloseEnabled);

            final boolean clipboardIntegrationEnabled = preferencesDialog.isClipboardIntegrationEnabled();

            if (clipboardIntegrationEnabled) {
                activateClipboardMonitoring();
                LOGGER.info("Clipboard integration is enabled");
            } else {
                shutdownClipboardMonitoring();
                LOGGER.info("Clipboard integration is disabled");
            }

            PREFS.putBoolean("CLIPBOARD_INTEGRATION", clipboardIntegrationEnabled);

            PREFS.putInt("EXAM_WORDS", preferencesDialog.getExamWords());

            String selectedLookAndFeel = preferencesDialog.getSelectedLookAndFeel();

            if (!selectedLookAndFeel.equals(PREFS.get("LOOK_AND_FEEL", "System"))) {
                PREFS.put("LOOK_AND_FEEL", selectedLookAndFeel);
            }
        }
    }//GEN-LAST:event_prefsMenuItemActionPerformed

    private void fontMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fontMenuItemActionPerformed
        FontChooserDialog fontChooserDialog = new FontChooserDialog(this, true);

        if (fontChooserDialog.showDialog()) {
            final Font selectedFont = fontChooserDialog.getSelectedFont();

            PREFS.put("FONT_NAME", selectedFont.getFontName());
            PREFS.putInt("FONT_SIZE", selectedFont.getSize());
            PREFS.putInt("FONT_STYLE", selectedFont.getStyle());

            setSelectedFont(selectedFont);
        }
    }//GEN-LAST:event_fontMenuItemActionPerformed

    private void enBgDictMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enBgDictMenuItemActionPerformed
        selectDictionary("EN_BG");
    }//GEN-LAST:event_enBgDictMenuItemActionPerformed

    private void bgEnDictMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bgEnDictMenuItemActionPerformed
        selectDictionary("BG_EN");
    }//GEN-LAST:event_bgEnDictMenuItemActionPerformed

    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        AboutDialog aboutDialog = new AboutDialog(this, true);
        aboutDialog.setVisible(true);
    }//GEN-LAST:event_aboutMenuItemActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JMenuItem bgEnDictMenuItem;
    private javax.swing.JButton clearButton;
    private javax.swing.JLabel drowLabel;
    private javax.swing.JMenuItem enBgDictMenuItem;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenuItem fontMenuItem;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel matchLabel;
    private javax.swing.JMenuItem prefsMenuItem;
    private javax.swing.JLabel statusBar;
    private javax.swing.JTextField wordSearchField;
    private javax.swing.JTextArea wordTranslationTextArea;
    private javax.swing.JList wordsList;
    // End of variables declaration//GEN-END:variables
}
