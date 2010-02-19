package com.drowltd.dictionary.ui.desktop;

import com.drowltd.dictionary.ui.desktop.exam.ExamDialog;
import com.drowltd.dictionary.core.db.DatabaseService;
import com.drowltd.dictionary.core.db.Dictionary;
import com.drowltd.dictionary.core.exception.DictionaryDbLockedException;
import com.drowltd.dictionary.core.i18n.Translator;
import com.drowltd.dictionary.core.preferences.PreferencesManager;
import com.drowltd.dictionary.ui.desktop.IconManager.IconSize;
import com.drowltd.dictionary.ui.desktop.spellcheck.SpellCheckFrame;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.TrayIcon;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultEditorKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.drowltd.dictionary.core.preferences.PreferencesManager.Preference;

/**
 * Spellbook's main application frame.
 *
 * @author Bozhidar Batsov
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
    private ScheduledExecutorService memoryUsageExecutorService;
    private TrayIcon trayIcon;
    private Dictionary selectedDictionary = Dictionary.getSelectedDictionary();
    private static final int BYTES_IN_ONE_MEGABYTE = 1024 * 1024;

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
            DatabaseService.init(PM.get(Preference.PATH_TO_DB, ""));
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
                if (PM.getBoolean(Preference.MIN_TO_TRAY, false)) {
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
                if (PM.getBoolean(Preference.CLOSE_TO_TRAY, false)) {
                    LOGGER.info("Minimizing Spellbook to tray on window close");
                    setVisible(false);
                } else {
                    saveFrameState();
                }
            }
        });

        initComponents();

        // restore the divider location from the last session
        splitPane.setDividerLocation(PM.getInt(Preference.DIVIDER_LOCATION, 160));

        // we need to pass the completable search field a reference to the word list
        ((AutocompletingTextField)wordSearchField).setWordsList(wordsList);
        ((AutocompletingTextField)wordSearchField).setFrame(this);

        // monitor any changes in the search text field
        wordSearchField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                onSearchChange();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                onSearchChange();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                onSearchChange();
            }
        });

        addComponentListener(new ComponentAdapter() {

            @Override
            public void componentMoved(ComponentEvent e) {
                ((AutocompletingTextField)wordSearchField).showCompletions();
            }

        });

        // add the context popup
        ContextMenuMouseListener contextMenuMouseListener = new ContextMenuMouseListener();

        wordSearchField.addMouseListener(contextMenuMouseListener);
        wordTranslationTextPane.addMouseListener(contextMenuMouseListener);

        statusBar.setText(String.format(TRANSLATOR.translate("EnBgDictSize(Label)"), words.size()));
        statusBar.setIcon(IconManager.getImageIcon("en-bg.png", IconSize.SIZE24));

        // update word menu item is initially disabled
        updateWordMenuItem.setEnabled(false);

        setDefaultFont();

        if (PM.getBoolean(Preference.CLIPBOARD_INTEGRATION, false)) {
            activateClipboardMonitoring();
        }

        if (PM.getBoolean(Preference.SHOW_MEMORY_USAGE, false)) {
            showMemoryUsage();
        } else {
            hideMemoryUsage();
        }
    }

    private void onSearchChange() {
        String searchString = wordSearchField.getText();

        // in case the user types enough backspaces
        if (searchString.isEmpty()) {
            //();
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
    }

    public void showMemoryUsage() {
        memoryProgressBar.setVisible(true);
        runGcButton.setVisible(true);

        if (memoryUsageExecutorService == null) {
            Runnable memoryRunnable = new Runnable() {

                @Override
                public void run() {
                    final Runtime runtime = Runtime.getRuntime();
                    final long freeMemory = runtime.freeMemory();
                    final long totalMemory = runtime.totalMemory();
                    memoryProgressBar.setMaximum((int) totalMemory);
                    memoryProgressBar.setValue((int) (totalMemory - freeMemory));

                    int usedMemInMb = (int) (totalMemory - freeMemory) / BYTES_IN_ONE_MEGABYTE;
                    int totalMemInMb = (int) totalMemory / BYTES_IN_ONE_MEGABYTE;

                    memoryProgressBar.setString(usedMemInMb + "M of " + totalMemInMb + "M");
                    memoryProgressBar.setToolTipText(String.format(TRANSLATOR.translate("MemoryUsage(ToolTip)"), totalMemInMb, usedMemInMb));
                }
            };

            memoryUsageExecutorService = Executors.newSingleThreadScheduledExecutor();
            memoryUsageExecutorService.scheduleAtFixedRate(memoryRunnable, 0, 10, TimeUnit.SECONDS);
        }
    }

    public void hideMemoryUsage() {
        memoryProgressBar.setVisible(false);
        runGcButton.setVisible(false);
    }

    private void clear() {
        LOGGER.info("Clear action invoked");

        wordSearchField.setText(null);
        wordSearchField.requestFocusInWindow();
        wordsList.ensureIndexIsVisible(0);
        wordsList.clearSelection();
        updateWordMenuItem.setEnabled(false);
        wordTranslationTextPane.setText(null);
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
                        if ((trayIcon != null) && match && (!SpellbookFrame.this.isVisible() ||
                                (SpellbookFrame.this.getState() == JFrame.ICONIFIED)) &&
                                PM.getBoolean(Preference.TRAY_POPUP, false)) {
                            trayIcon.displayMessage(foundWord, wordTranslationTextPane.getText(),
                                    TrayIcon.MessageType.INFO);
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

    public void restart() {
        this.dispose();
        SpellbookTray.destroyTrayIcon();
        SpellbookApp.init();
    }

    public void setDefaultFont() {
        if (PM.get(Preference.FONT_NAME, "").isEmpty()) {
            // dirty fix for windows - it seem that the default font there is too small, so we set
            // a more appropriate one
            String osName = System.getProperty("os.name");

            if (osName.contains("Windows")) {
                wordTranslationTextPane.setFont(new Font("Arial", Font.PLAIN, 14));
            }
        } else {
            String fontName = PM.get(Preference.FONT_NAME, "SansSerif");
            int fontSize = PM.getInt(Preference.FONT_SIZE, 14);
            int fontStyle = PM.getInt(Preference.FONT_STYLE, Font.PLAIN);

            setSelectedFont(new Font(fontName, fontStyle, fontSize));
        }
    }

    private void saveFrameState() {
        Rectangle r = getBounds();
        PM.putDouble(Preference.FRAME_X, r.getX());
        PM.putDouble(Preference.FRAME_Y, r.getY());
        PM.putDouble(Preference.FRAME_WIDTH, r.getWidth());
        PM.putDouble(Preference.FRAME_HEIGHT, r.getHeight());
        PM.putInt(Preference.DIVIDER_LOCATION, splitPane.getDividerLocation());
    }

    private boolean verifyDbPresence() {
        final String dbPath = PM.get(Preference.PATH_TO_DB, "");

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
                    PM.put(Preference.PATH_TO_DB, selectedDbPath);
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
        wordTranslationTextPane.setFont(font);
        statusBar.setFont(font);
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
        wordsList.setModel(new ListBackedListModel(words));

        if (dictionary == Dictionary.EN_BG) {
            SwingUtil.showBalloonTip(statusBar, TRANSLATOR.translate("EnBgDictLoaded(Message)"));
            statusBar.setText(String.format(TRANSLATOR.translate("EnBgDictSize(Label)"), words.size()));
            statusBar.setIcon(IconManager.getImageIcon("en-bg.png", IconSize.SIZE24));
        } else if (dictionary == Dictionary.BG_EN) {
            SwingUtil.showBalloonTip(statusBar, TRANSLATOR.translate("BgEnDictLoaded(Message)"));
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
        statusBar = new javax.swing.JLabel();
        memoryProgressBar = new javax.swing.JProgressBar();
        runGcButton = new javax.swing.JButton();
        splitPane = new javax.swing.JSplitPane();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        wordsList = new javax.swing.JList();
        wordSearchField = new AutocompletingTextField();
        jPanel3 = new javax.swing.JPanel();
        clearButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        wordTranslationTextPane = new javax.swing.JTextPane();
        matchLabel = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        restartMenuItem = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        addWordMenuItem = new javax.swing.JMenuItem();
        updateWordMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        cutMenuItem = new javax.swing.JMenuItem(new DefaultEditorKit.CutAction());
        copyMenuItem = new javax.swing.JMenuItem(new DefaultEditorKit.CopyAction());
        pasteMenuItem = new javax.swing.JMenuItem(new DefaultEditorKit.PasteAction());
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        prefsMenuItem = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        enBgDictMenuItem = new javax.swing.JMenuItem();
        bgEnDictMenuItem = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();
        examMenuItem = new javax.swing.JMenuItem();
        spellcheckMenuItem = new javax.swing.JMenuItem();
        jMenu5 = new javax.swing.JMenu();
        helpContentsMenuItem = new javax.swing.JMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        statusBar.setText("Status");

        memoryProgressBar.setStringPainted(true);

        runGcButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/16x16/recycle.png"))); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("i18n/SpellbookForm"); // NOI18N
        runGcButton.setToolTipText(bundle.getString("RunGC(ToolTip)")); // NOI18N
        runGcButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runGcButtonActionPerformed(evt);
            }
        });

        splitPane.setDividerLocation(180);

        wordsList.setModel(new ListBackedListModel(words));
        wordsList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        wordsList.setToolTipText(bundle.getString("WordsList(ToolTip)")); // NOI18N
        wordsList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                wordsListValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(wordsList);

        wordSearchField.setToolTipText(bundle.getString("WordSearch(ToolTip)")); // NOI18N
        wordSearchField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wordSearchFieldActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
            .addComponent(wordSearchField, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(wordSearchField, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 383, Short.MAX_VALUE))
        );

        splitPane.setLeftComponent(jPanel2);

        clearButton.setText(bundle.getString("ClearButton(Label)")); // NOI18N
        clearButton.setToolTipText(bundle.getString("ClearButton(ToolTip)")); // NOI18N
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });

        wordTranslationTextPane.setContentType("text/html");
        wordTranslationTextPane.setEditable(false);
        jScrollPane1.setViewportView(wordTranslationTextPane);

        matchLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/24x24/bell2_grey.png"))); // NOI18N
        matchLabel.setText(bundle.getString("FuelledBy(Label)")); // NOI18N
        matchLabel.setToolTipText(bundle.getString("NoMatchFound(ToolTip)")); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(clearButton, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(matchLabel)
                .addContainerGap(70, Short.MAX_VALUE))
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 558, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(clearButton)
                    .addComponent(matchLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 382, Short.MAX_VALUE))
        );

        splitPane.setRightComponent(jPanel3);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(statusBar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 520, Short.MAX_VALUE)
                        .addComponent(memoryProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(runGcButton))
                    .addComponent(splitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 744, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(splitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 417, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(statusBar)
                    .addComponent(runGcButton)
                    .addComponent(memoryProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {memoryProgressBar, runGcButton});

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

        addWordMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/16x16/add2.png"))); // NOI18N
        addWordMenuItem.setText(bundle.getString("EditAddWord(MenuItem)")); // NOI18N
        addWordMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addWordMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(addWordMenuItem);

        updateWordMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/16x16/edit.png"))); // NOI18N
        updateWordMenuItem.setText(bundle.getString("EditUpdateWord(MenuItem)")); // NOI18N
        updateWordMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateWordMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(updateWordMenuItem);
        jMenu2.add(jSeparator2);

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

        helpContentsMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/16x16/help2.png"))); // NOI18N
        helpContentsMenuItem.setText(bundle.getString("HelpContents(MenuItem)")); // NOI18N
        jMenu5.add(helpContentsMenuItem);

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
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        saveFrameState();

        System.exit(0);
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void prefsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prefsMenuItemActionPerformed
        PreferencesDialog preferencesDialog = new PreferencesDialog(this, true);

        // tray options should be disabled is the tray is not supported
        if (trayIcon == null) {
            preferencesDialog.disableTrayOptions();
        }

        preferencesDialog.setLocationRelativeTo(this);

        PreferencesExtractor.extract(this, preferencesDialog);
    }//GEN-LAST:event_prefsMenuItemActionPerformed

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

    private void addWordMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addWordMenuItemActionPerformed
        AddUpdateWordDialog addUpdateWordDialog = new AddUpdateWordDialog(this, true);

        addUpdateWordDialog.setDictionary(selectedDictionary);
        addUpdateWordDialog.setVisible(true);

        if (addUpdateWordDialog.getReturnStatus() == AddUpdateWordDialog.RET_OK) {
            // save word
            words.add(addUpdateWordDialog.getWord());
            // TODO calculate insertion index
            wordsList.setModel(new ListBackedListModel(words));
        }
    }//GEN-LAST:event_addWordMenuItemActionPerformed

    private void updateWordMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateWordMenuItemActionPerformed
        AddUpdateWordDialog addUpdateWordDialog = new AddUpdateWordDialog(this, true);

        addUpdateWordDialog.setDictionary(selectedDictionary);

        if (wordsList.isSelectionEmpty()) {
            throw new IllegalStateException("No word selected");
        }

        addUpdateWordDialog.setWord((String) wordsList.getSelectedValue());
        addUpdateWordDialog.setTranslation(wordTranslationTextPane.getText());

        addUpdateWordDialog.setVisible(true);

        if (addUpdateWordDialog.getReturnStatus() == AddUpdateWordDialog.RET_OK) {
            // update word
        }
    }//GEN-LAST:event_updateWordMenuItemActionPerformed

    private void runGcButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runGcButtonActionPerformed
        System.gc();
    }//GEN-LAST:event_runGcButtonActionPerformed

    private void wordSearchFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wordSearchFieldActionPerformed
wordSearchField.selectAll();
        AutocompletingTextField completableJTextField = (AutocompletingTextField) wordSearchField;

        completableJTextField.addCompletion(wordSearchField.getText());        // TODO add your handling code here:
    }//GEN-LAST:event_wordSearchFieldActionPerformed

    private void wordsListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_wordsListValueChanged
if (!wordsList.isSelectionEmpty()) {
            int selectedIndex = wordsList.getSelectedIndex();

            final String selectedWord = words.get(selectedIndex);

            // word field needs to be updated in a separate thread
            if (!wordSearchField.hasFocus()) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        wordSearchField.setText(selectedWord);
                    }
                });
            }

            wordTranslationTextPane.setText(SwingUtil.formatTranslation(selectedWord, databaseService.getTranslation(selectedDictionary, words.get(selectedIndex))));
            wordTranslationTextPane.setCaretPosition(0);
            matchLabel.setIcon(IconManager.getImageIcon("bell2_green.png", IconSize.SIZE24));
            matchLabel.setToolTipText(TRANSLATOR.translate("MatchFound(ToolTip)"));

            updateWordMenuItem.setEnabled(true);
        }        // TODO add your handling code here:
    }//GEN-LAST:event_wordsListValueChanged

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
    clear();        // TODO add your handling code here:
    }//GEN-LAST:event_clearButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JMenuItem addWordMenuItem;
    private javax.swing.JMenuItem bgEnDictMenuItem;
    private javax.swing.JButton clearButton;
    private javax.swing.JMenuItem copyMenuItem;
    private javax.swing.JMenuItem cutMenuItem;
    private javax.swing.JMenuItem enBgDictMenuItem;
    private javax.swing.JMenuItem examMenuItem;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenuItem helpContentsMenuItem;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JLabel matchLabel;
    private javax.swing.JProgressBar memoryProgressBar;
    private javax.swing.JMenuItem pasteMenuItem;
    private javax.swing.JMenuItem prefsMenuItem;
    private javax.swing.JMenuItem restartMenuItem;
    private javax.swing.JButton runGcButton;
    private javax.swing.JMenuItem spellcheckMenuItem;
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JLabel statusBar;
    private javax.swing.JMenuItem updateWordMenuItem;
    private javax.swing.JTextField wordSearchField;
    private javax.swing.JTextPane wordTranslationTextPane;
    private javax.swing.JList wordsList;
    // End of variables declaration//GEN-END:variables
}
