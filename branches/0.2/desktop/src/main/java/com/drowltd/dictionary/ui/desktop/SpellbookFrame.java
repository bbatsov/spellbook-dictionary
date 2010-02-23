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

import com.drowltd.dictionary.ui.desktop.exam.ExamDialog;
import com.drowltd.dictionary.core.db.DatabaseService;
import com.drowltd.dictionary.core.db.Dictionary;
import com.drowltd.dictionary.core.exception.DictionaryDbLockedException;
import com.drowltd.dictionary.core.i18n.Translator;
import com.drowltd.dictionary.core.preferences.PreferencesManager;
import com.drowltd.dictionary.ui.desktop.IconManager.IconSize;
import com.drowltd.dictionary.ui.desktop.spellcheck.SpellCheckFrame;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.TrayIcon;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.DefaultEditorKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author bozhidar
 * @since 0.1
 */
public class SpellbookFrame extends javax.swing.JFrame {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpellbookFrame.class);
    private static final Translator TRANSLATOR = Translator.getTranslator("SpellbookForm");
    private static final PreferencesManager PM = PreferencesManager.getInstance();
    private DatabaseService databaseService;
    private List<String> words;
    private ClipboardIntegration clipboardIntegration;
    private String lastTransfer;
    private ScheduledExecutorService clipboardExecutorService;
    private TrayIcon trayIcon;
    private Dictionary selectedDictionary = Dictionary.getSelectedDictionary();

    /** Creates new form SpellbookFrame */
    public SpellbookFrame() {
        TRANSLATOR.reset();

        // check the presence of the dictionary database
        if (!verifyDbPresence()) {
            JOptionPane.showMessageDialog(null, TRANSLATOR.translate("NoDbSelected(Message)"),
                    TRANSLATOR.translate("Error(Title)"), JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }

        try {
            DatabaseService.init(PM.get("PATH_TO_DB", ""));
        } catch (DictionaryDbLockedException e) {
            JOptionPane.showMessageDialog(null, TRANSLATOR.translate("AlreadyRunning(Message)"), "Warning", JOptionPane.WARNING_MESSAGE);
            System.exit(0);
        }

        databaseService = DatabaseService.getInstance();

        words = databaseService.getWordsFromDictionary(selectedDictionary);

        
        //set the frame title
        setTitle(TRANSLATOR.translate("ApplicationName(Title)"));

        //set the frame icon
        setIconImage(IconManager.getImageIcon("dictionary.png", IconManager.IconSize.SIZE16).getImage());

        //create tray
        trayIcon = SpellbookTray.createTraySection(this);

        addWindowListener(new WindowAdapter() {

            @Override
            public void windowIconified(WindowEvent e) {
                if (PM.getBoolean("MIN_TO_TRAY", false)) {
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
                if (PM.getBoolean("CLOSE_TO_TRAY", false)) {
                    LOGGER.info("Minimizing Spellbook to tray on window close");
                    setVisible(false);
                } else {
                    saveFrameState();
                }
            }
        });

        initComponents();

        // add the context popup
        ContextMenuMouseListener contextMenuMouseListener = new ContextMenuMouseListener();

        wordSearchField.addMouseListener(contextMenuMouseListener);
        wordTranslationTextArea.addMouseListener(contextMenuMouseListener);

        statusBar.setText(String.format(TRANSLATOR.translate("EnBgDictSize(Label)"), words.size()));
        statusBar.setIcon(IconManager.getImageIcon("en-bg.png", IconSize.SIZE24));

        setDefaultFont();

        if (PM.getBoolean("CLIPBOARD_INTEGRATION", false)) {
            activateClipboardMonitoring();
        }
    }

    private void clear() {
        LOGGER.info("Clear action invoked");

        wordSearchField.setText(null);
        wordSearchField.requestFocus();
        wordsList.ensureIndexIsVisible(0);
        wordsList.clearSelection();
        wordTranslationTextArea.setText(null);
        matchLabel.setIcon(IconManager.getImageIcon("bell2_red.png", IconSize.SIZE24));
        lastTransfer = null;
    }

    public void activateClipboardMonitoring() {
        LOGGER.info("Activating clipboard monitoring");

        if (clipboardExecutorService == null || clipboardExecutorService.isShutdown()) {

            clipboardIntegration = new ClipboardIntegration();

            Runnable clipboardRunnable = new Runnable() {

                @Override
                public void run() {
                    String transferredText = clipboardIntegration.getClipboardContents().trim();

                    if (lastTransfer == null) {
                        lastTransfer = transferredText;
                    }

                    if (!transferredText.equalsIgnoreCase(lastTransfer)) {
                        LOGGER.info("'" + transferredText + "' received from clipboard");
                        String searchString = transferredText.split("\\PL")[0].toLowerCase();
                        String foundWord = "";
                        LOGGER.info("Search string from clipboard is " + searchString);
                        wordSearchField.setText(searchString);
                        wordSearchField.selectAll();
                        lastTransfer = transferredText;

                        String approximation;
                        boolean match = false;

                        if (words.contains(searchString)) {
                            foundWord = searchString;
                            int index = words.indexOf(foundWord);

                            wordsList.setSelectedIndex(index);
                            wordsList.ensureIndexIsVisible(index);

                            match = true;

                            matchLabel.setIcon(IconManager.getImageIcon("bell2_green.png", IconSize.SIZE24));
                            matchLabel.setToolTipText(TRANSLATOR.translate("MatchFound(ToolTip)"));
                        } else if ((approximation = databaseService.getApproximation(selectedDictionary, searchString)) != null) {
                            foundWord = approximation;
                            int index = words.indexOf(foundWord);

                            wordsList.setSelectedIndex(index);
                            wordsList.ensureIndexIsVisible(index);

                            match = true;

                            matchLabel.setIcon(IconManager.getImageIcon("bell2_gold.png", IconSize.SIZE24));
                            matchLabel.setToolTipText(TRANSLATOR.translate("PartialMatchFound(ToolTip)"));
                        }

                        // the tray popup translation should appear is the main frame is either not visible or minimized
                        if ((trayIcon != null) && match && (!SpellbookFrame.this.isVisible() || (SpellbookFrame.this.getState() == JFrame.ICONIFIED)) && PM.getBoolean("TRAY_POPUP", false)) {
                            trayIcon.displayMessage(foundWord, wordTranslationTextArea.getText(), TrayIcon.MessageType.INFO);
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

    private void restart() {
        this.dispose();
        SpellbookTray.destroyTrayIcon();
        SpellbookApp.init();
    }

    private void setDefaultFont() {
        if (PM.get("FONT_NAME", "").isEmpty()) {
            // dirty fix for windows - it seem that the default font there is too small, so we set
            // a more appropriate one
            String osName = System.getProperty("os.name");

            if (osName.contains("Windows")) {
                wordTranslationTextArea.setFont(new Font("Arial", Font.PLAIN, 14));
            }
        } else {
            String fontName = PM.get("FONT_NAME", "SansSerif");
            int fontSize = PM.getInt("FONT_SIZE", 14);
            int fontStyle = PM.getInt("FONT_STYLE", Font.PLAIN);

            setSelectedFont(new Font(fontName, fontStyle, fontSize));
        }
    }

     private void saveFrameState() {
        Rectangle r = getBounds();
        PM.putDouble("FRAME_X", r.getX());
        PM.putDouble("FRAME_Y", r.getY());
        PM.putDouble("FRAME_WIDTH", r.getWidth());
        PM.putDouble("FRAME_HEIGHT", r.getHeight());
    }

    private boolean verifyDbPresence() {
        final String dbPath = PM.get("PATH_TO_DB", "");

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
                    PM.put("PATH_TO_DB", selectedDbPath);
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

    public void selectDictionary(Dictionary dictionary) {
        // if we select the currently selected dictionary we don't have to do nothing
        if (selectedDictionary == dictionary) {
            LOGGER.info("Dictionary " + dictionary + " is already selected");
            return;
        }

        // otherwise begin the switch to the new dictionary by cleaning everything in the UI
        clear();

        selectedDictionary = dictionary;
        Dictionary.setSelectedDictionary(selectedDictionary);

        words = databaseService.getWordsFromDictionary(dictionary);
        wordsList.setListData(words.toArray());

        if (dictionary == Dictionary.EN_BG) {
            statusBar.setText(String.format(TRANSLATOR.translate("EnBgDictSize(Label)"), words.size()));
            statusBar.setIcon(IconManager.getImageIcon("en-bg.png", IconSize.SIZE24));
        } else if (dictionary == Dictionary.BG_EN) {
            statusBar.setText(String.format(TRANSLATOR.translate("BgEnDictSize(Label)"), words.size()));
            statusBar.setIcon(IconManager.getImageIcon("bg-en.png", IconSize.SIZE24));
        } else {
            throw new IllegalArgumentException("Unknown dictionary " + dictionary);
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
        restartMenuItem = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        cutMenuItem = new javax.swing.JMenuItem(new DefaultEditorKit.CutAction());
        copyMenuItem = new javax.swing.JMenuItem(new DefaultEditorKit.CopyAction());
        pasteMenuItem = new javax.swing.JMenuItem(new DefaultEditorKit.PasteAction());
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        fontMenuItem = new javax.swing.JMenuItem();
        prefsMenuItem = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        enBgDictMenuItem = new javax.swing.JMenuItem();
        bgEnDictMenuItem = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();
        examMenuItem = new javax.swing.JMenuItem();
        spellcheckMenuItem = new javax.swing.JMenuItem();
        jMenu5 = new javax.swing.JMenu();
        aboutMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        wordSearchField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wordSearchFieldActionPerformed(evt);
            }
        });
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
        wordTranslationTextArea.setEditable(false);
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

        jMenu1.setMnemonic('f');
        jMenu1.setText(bundle.getString("File(Menu)")); // NOI18N

        restartMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/16x16/refresh.png"))); // NOI18N
        restartMenuItem.setMnemonic('r');
        restartMenuItem.setText(bundle.getString("FileRestart(MenuItem)")); // NOI18N
        restartMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                restartMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(restartMenuItem);

        exitMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/16x16/exit.png"))); // NOI18N
        exitMenuItem.setMnemonic('x');
        exitMenuItem.setText(bundle.getString("FileExit(MenuItem)")); // NOI18N
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(exitMenuItem);

        jMenuBar1.add(jMenu1);

        jMenu2.setMnemonic('e');
        jMenu2.setText(bundle.getString("Edit(Menu)")); // NOI18N

        cutMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/16x16/cut.png"))); // NOI18N
        cutMenuItem.setMnemonic('t');
        cutMenuItem.setText(bundle.getString("EditCut(MenuItem)")); // NOI18N
        jMenu2.add(cutMenuItem);

        copyMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/16x16/copy.png"))); // NOI18N
        copyMenuItem.setMnemonic('c');
        copyMenuItem.setText(bundle.getString("EditCopy(MenuItem)")); // NOI18N
        jMenu2.add(copyMenuItem);

        pasteMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/16x16/paste.png"))); // NOI18N
        pasteMenuItem.setMnemonic('p');
        pasteMenuItem.setText(bundle.getString("EditPaste(MenuItem)")); // NOI18N
        jMenu2.add(pasteMenuItem);
        jMenu2.add(jSeparator1);

        fontMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/16x16/font.png"))); // NOI18N
        fontMenuItem.setMnemonic('f');
        fontMenuItem.setText(bundle.getString("EditFont(MenuItem)")); // NOI18N
        fontMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fontMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(fontMenuItem);

        prefsMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/16x16/preferences.png"))); // NOI18N
        prefsMenuItem.setMnemonic('e');
        prefsMenuItem.setText(bundle.getString("EditPreferences(MenuItem)")); // NOI18N
        prefsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prefsMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(prefsMenuItem);

        jMenuBar1.add(jMenu2);

        jMenu3.setMnemonic('d');
        jMenu3.setText(bundle.getString("Dictionaries(Menu)")); // NOI18N

        enBgDictMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/16x16/en-bg.png"))); // NOI18N
        enBgDictMenuItem.setMnemonic('e');
        enBgDictMenuItem.setText(bundle.getString("DictionariesEnBg(MenuItem)")); // NOI18N
        enBgDictMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enBgDictMenuItemActionPerformed(evt);
            }
        });
        jMenu3.add(enBgDictMenuItem);

        bgEnDictMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/16x16/bg-en.png"))); // NOI18N
        bgEnDictMenuItem.setMnemonic('b');
        bgEnDictMenuItem.setText(bundle.getString("DictionariesBgEn(MenuItem)")); // NOI18N
        bgEnDictMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bgEnDictMenuItemActionPerformed(evt);
            }
        });
        jMenu3.add(bgEnDictMenuItem);

        jMenuBar1.add(jMenu3);

        jMenu4.setMnemonic('t');
        jMenu4.setText(bundle.getString("Tools(Menu)")); // NOI18N

        examMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/16x16/blackboard.png"))); // NOI18N
        examMenuItem.setMnemonic('e');
        examMenuItem.setText(bundle.getString("Exam(MenuItem)")); // NOI18N
        examMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                examMenuItemActionPerformed(evt);
            }
        });
        jMenu4.add(examMenuItem);

        spellcheckMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/16x16/spellcheck.png"))); // NOI18N
        spellcheckMenuItem.setMnemonic('s');
        spellcheckMenuItem.setText(bundle.getString("SpellCheck(MenuItem)")); // NOI18N
        spellcheckMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                spellcheckMenuItemActionPerformed(evt);
            }
        });
        jMenu4.add(spellcheckMenuItem);

        jMenuBar1.add(jMenu4);

        jMenu5.setMnemonic('h');
        jMenu5.setText(bundle.getString("Help(Menu)")); // NOI18N

        aboutMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/16x16/about.png"))); // NOI18N
        aboutMenuItem.setMnemonic('a');
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
        int selectedIndex = wordsList.getSelectedIndex();
        if(selectedIndex < 0){
            return;
        }

        String selectedWord = words.get(selectedIndex);
            
            if (!wordSearchField.hasFocus()) {
                wordSearchField.setText(selectedWord);
                wordSearchField.selectAll();
            }

        wordTranslationTextArea.setText(databaseService.getTranslation(selectedDictionary, words.get(selectedIndex)));
        wordTranslationTextArea.setCaretPosition(0);
        matchLabel.setIcon(IconManager.getImageIcon("bell2_green.png", IconSize.SIZE24));
        matchLabel.setToolTipText(TRANSLATOR.translate("MatchFound(ToolTip)"));
    }//GEN-LAST:event_wordsListValueChanged

    private void wordSearchFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_wordSearchFieldKeyReleased
        String searchString = wordSearchField.getText();

        // in case the user types enough backspaces
        if (searchString.isEmpty()) {
            clear();
        }

        String approximation;

        // if we have an exact match for the search string or the search string in lowercase
        if (words.contains(searchString) || words.contains(searchString.toLowerCase())) {
            int index = words.indexOf(searchString);

            // if the index is negative the match was for the lowercase version
            if (index < 0) {
                searchString = searchString.toLowerCase();
                index = words.indexOf(searchString);
            }

            // invoking this method will trigger the list value changed listener,
            // so there is no need to obtain the translation explicitly here
            wordsList.setSelectedIndex(index);
            wordsList.ensureIndexIsVisible(index);

            matchLabel.setIcon(IconManager.getImageIcon("bell2_green.png", IconSize.SIZE24));
            matchLabel.setToolTipText(TRANSLATOR.translate("MatchFound(ToolTip)"));
        } else if ((approximation = databaseService.getApproximation(selectedDictionary, searchString)) != null) {
            int index = words.indexOf(approximation);

            wordsList.setSelectedIndex(index);
            wordsList.ensureIndexIsVisible(index);

            matchLabel.setIcon(IconManager.getImageIcon("bell2_gold.png", IconSize.SIZE24));
            matchLabel.setToolTipText(TRANSLATOR.translate("PartialMatchFound(ToolTip)"));
        } else {
            matchLabel.setIcon(IconManager.getImageIcon("bell2_red.png", IconSize.SIZE24));
            matchLabel.setToolTipText(TRANSLATOR.translate("NoMatchFound(ToolTip)"));
        }
    }//GEN-LAST:event_wordSearchFieldKeyReleased

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
        clear();
    }//GEN-LAST:event_clearButtonActionPerformed

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        saveFrameState();
        System.exit(0);
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void prefsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prefsMenuItemActionPerformed
        PreferencesDialog preferencesDialog = new PreferencesDialog(this, true);
        preferencesDialog.setLocationRelativeTo(this);

        if (preferencesDialog.showDialog()) {
            String oldLanguage = PM.get("LANG", "EN");
            final String newLanguage = preferencesDialog.getSelectedLanguage().toString();
            PM.put("LANG", newLanguage);

            if (!oldLanguage.equals(newLanguage)) {
                LOGGER.info("Language changed from " + oldLanguage + " to " + newLanguage);
                int selectedOption = JOptionPane.showConfirmDialog(this, TRANSLATOR.translate("Restart(Message)"), "Restart",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

                if (selectedOption == JOptionPane.OK_OPTION) {
                    restart();
                }
            }

            final boolean minimizeToTrayEnabled = preferencesDialog.isMinimizeToTrayEnabled();

            if (minimizeToTrayEnabled) {
                LOGGER.info("Minimize to tray is enabled");
            } else {
                LOGGER.info("Minimize to tray is disabled");
            }

            PM.putBoolean("MIN_TO_TRAY", minimizeToTrayEnabled);

            boolean minimizeToTrayOnCloseEnabled = preferencesDialog.isMinimizeToTrayOnCloseEnabled();

            if (minimizeToTrayOnCloseEnabled) {
                LOGGER.info("Minimize to tray on close is enabled");
                setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            } else {
                LOGGER.info("Minimize to tray on close is disabled");
                setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            }

            PM.putBoolean("CLOSE_TO_TRAY", minimizeToTrayOnCloseEnabled);

            final boolean clipboardIntegrationEnabled = preferencesDialog.isClipboardIntegrationEnabled();

            if (clipboardIntegrationEnabled) {
                activateClipboardMonitoring();
                LOGGER.info("Clipboard integration is enabled");
            } else {
                shutdownClipboardMonitoring();
                LOGGER.info("Clipboard integration is disabled");
            }

            PM.putBoolean("CLIPBOARD_INTEGRATION", clipboardIntegrationEnabled);

            PM.putInt("EXAM_WORDS", preferencesDialog.getExamWords());

            String selectedLookAndFeel = preferencesDialog.getSelectedLookAndFeel();

            if (!selectedLookAndFeel.equals(PM.get("LOOK_AND_FEEL", "System"))) {
                PM.put("LOOK_AND_FEEL", selectedLookAndFeel);
            }
        } else {
            // we need to restore the old look and feel manually since it was changed on selection
            LookAndFeelInfo[] lookAndFeelInfos = UIManager.getInstalledLookAndFeels();

            String selectedLookAndFeel = PM.get("LOOK_AND_FEEL", "System");

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

            SwingUtilities.updateComponentTreeUI(this);
        }

        // tray options should be disabled is the tray is not supported
        if (trayIcon == null) {
            preferencesDialog.disableTrayOptions();
        }
    }//GEN-LAST:event_prefsMenuItemActionPerformed

    private void fontMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fontMenuItemActionPerformed
        FontChooserDialog fontChooserDialog = new FontChooserDialog(this, true);
        fontChooserDialog.setLocationRelativeTo(this);

        if (fontChooserDialog.showDialog()) {
            final Font selectedFont = fontChooserDialog.getSelectedFont();

            PM.put("FONT_NAME", selectedFont.getFontName());
            PM.putInt("FONT_SIZE", selectedFont.getSize());
            PM.putInt("FONT_STYLE", selectedFont.getStyle());

            setSelectedFont(selectedFont);
        }
    }//GEN-LAST:event_fontMenuItemActionPerformed

    private void enBgDictMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enBgDictMenuItemActionPerformed
        selectDictionary(Dictionary.EN_BG);
    }//GEN-LAST:event_enBgDictMenuItemActionPerformed

    private void bgEnDictMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bgEnDictMenuItemActionPerformed
        selectDictionary(Dictionary.BG_EN);
    }//GEN-LAST:event_bgEnDictMenuItemActionPerformed

    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        AboutDialog aboutDialog = new AboutDialog(this, true);
        aboutDialog.setLocationRelativeTo(this);
        aboutDialog.setVisible(true);
    }//GEN-LAST:event_aboutMenuItemActionPerformed

    private void spellcheckMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_spellcheckMenuItemActionPerformed
        SpellCheckFrame.getInstance().setVisible(true);
    }//GEN-LAST:event_spellcheckMenuItemActionPerformed

    private void restartMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_restartMenuItemActionPerformed
        restart();
    }//GEN-LAST:event_restartMenuItemActionPerformed

    private void examMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_examMenuItemActionPerformed
        ExamDialog examDialog = new ExamDialog(this, true);
        examDialog.showExamDialog();
    }//GEN-LAST:event_examMenuItemActionPerformed

    private void wordSearchFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wordSearchFieldActionPerformed
        wordSearchField.selectAll();
    }//GEN-LAST:event_wordSearchFieldActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JMenuItem bgEnDictMenuItem;
    private javax.swing.JButton clearButton;
    private javax.swing.JMenuItem copyMenuItem;
    private javax.swing.JMenuItem cutMenuItem;
    private javax.swing.JLabel drowLabel;
    private javax.swing.JMenuItem enBgDictMenuItem;
    private javax.swing.JMenuItem examMenuItem;
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
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JLabel matchLabel;
    private javax.swing.JMenuItem pasteMenuItem;
    private javax.swing.JMenuItem prefsMenuItem;
    private javax.swing.JMenuItem restartMenuItem;
    private javax.swing.JMenuItem spellcheckMenuItem;
    private javax.swing.JLabel statusBar;
    private javax.swing.JTextField wordSearchField;
    private javax.swing.JTextArea wordTranslationTextArea;
    private javax.swing.JList wordsList;
    // End of variables declaration//GEN-END:variables
}
