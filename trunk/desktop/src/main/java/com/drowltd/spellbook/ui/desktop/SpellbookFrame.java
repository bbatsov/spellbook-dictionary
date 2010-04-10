package com.drowltd.spellbook.ui.desktop;

import com.drowltd.spellbook.ui.swing.util.SwingUtil;
import com.drowltd.spellbook.ui.swing.util.IconManager;
import com.drowltd.spellbook.ui.swing.model.ListBackedListModel;
import com.drowltd.spellbook.ui.swing.component.AutocompletingTextField;
import com.drowltd.spellbook.ui.desktop.exam.ExamDialog;
import com.drowltd.spellbook.core.i18n.Translator;
import com.drowltd.spellbook.core.model.Dictionary;
import com.drowltd.spellbook.core.model.Language;
import com.drowltd.spellbook.core.preferences.PreferencesManager;
import com.drowltd.spellbook.core.service.DictionaryService;
import com.drowltd.spellbook.ui.swing.util.IconManager.IconSize;
import com.drowltd.spellbook.ui.desktop.spellcheck.SpellCheckFrame;
import com.drowltd.spellbook.ui.desktop.study.StudyWordsDialog;
import com.drowltd.spellbook.util.SearchUtils;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultEditorKit;
import org.apache.tools.bzip2.CBZip2InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.drowltd.spellbook.core.preferences.PreferencesManager.Preference;

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
    private List<String> words;
    private ClipboardIntegration clipboardIntegration;
    private String lastTransfer;
    private ScheduledExecutorService clipboardExecutorService;
    private ScheduledExecutorService memoryUsageExecutorService;
    private TrayIcon trayIcon;
    private boolean exactMatch = false;
    private List<String> searchedWords = new ArrayList<String>();
    private int searchWordsIndex = -1;
    private static final int BYTES_IN_ONE_MEGABYTE = 1024 * 1024;
    private static final String DEFAULT_DB_PATH = "/opt/spellbook/db/";
    private static final String DB_FILE_NAME = "spellbook.data.db";
    private static final String COMPRESSED_DB_NAME = "dictionary-db.tar.bz2";
    private static final String ARICHIVIED_DB_NAME = "dictionary-db.tar";
    private static DictionaryService dictionaryService;
    private static Dictionary selectedDictionary;

    /** Creates new form SpellbookFrame */
    public SpellbookFrame() {
        TRANSLATOR.reset();

        // check a list of known possible locations for the db first
        checkDefaultDbLocations();

        // check the presence of the dictionary database
        if (!verifyDbPresence()) {
            JOptionPane.showMessageDialog(null, TRANSLATOR.translate("NoDbSelected(Message)"),
                    TRANSLATOR.translate("Error(Title)"), JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }

        DictionaryService.init(PM.get(Preference.PATH_TO_DB, ""));
        dictionaryService = DictionaryService.getInstance();

        // the first invocation of a db related method is special - we have to check
        // if another process is not using the connection already
        try {
            if (dictionaryService.getDictionaries().size() == 0) {
                // todo add translation
                JOptionPane.showMessageDialog(null, "No Dictionaries available", "Warning", JOptionPane.WARNING_MESSAGE);

                System.exit(0);
            }
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(null, TRANSLATOR.translate("AlreadyRunning(Message)"), "Warning", JOptionPane.WARNING_MESSAGE);
            System.exit(0);
        }

        // select default dictionary if set
        String defaultDictionaryName = PM.get(Preference.DEFAULT_DICTIONARY, "NONE");

        if (defaultDictionaryName.equals("NONE")) {
            setSelectedDictionary(dictionaryService.getDictionaries().get(0));
        } else {
            setSelectedDictionary(dictionaryService.getDictionary(defaultDictionaryName));
        }


        //set the frame title
        setTitle(TRANSLATOR.translate("ApplicationName(Title)"));

        //set the frame icon
        setIconImage(IconManager.getImageIcon("dictionary.png", IconManager.IconSize.SIZE16).getImage());

        //create tray
        trayIcon = SpellbookTray.createTraySection(this);

        initComponents();

        initDictionaries();

        addListeners();

        // restore the divider location from the last session
        splitPane.setDividerLocation(PM.getInt(Preference.DIVIDER_LOCATION, 160));

        // we need to pass the completable search field a reference to the word list
        ((AutocompletingTextField) wordSearchField).setWordsList(wordsList);
        ((AutocompletingTextField) wordSearchField).setOwner(this);

        updateDictionaryButton(selectedDictionary);

        // update word menu item is initially disabled
        updateWordMenuItem.setEnabled(false);
        updateWordButton.setEnabled(false);
        deleteWordMenuItem.setEnabled(false);
        deleteWordButton.setEnabled(false);

        // history buttons should be disabled initially
        forwardButton.setEnabled(false);
        backButton.setEnabled(false);

        // clear button is disabled initially
        clearButton.setEnabled(false);

        cutButton.setEnabled(false);
        cutMenuItem.setEnabled(false);
        copyButton.setEnabled(false);
        copyButton.setEnabled(false);

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

    private void addListeners() {
        // we need this to intercept events such as frame minimize/close
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

        // monitor any changes in the search text field
        wordSearchField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                // needs to be run in a separate thread
                // because we may need to switch the dictionary
                // based on the user input
                EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        onSearchChange(true);
                    }
                });
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                onSearchChange(false);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                onSearchChange(false);
            }
        });

        wordSearchField.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                if (wordSearchField.getText().isEmpty()) {
                    clear();
                }
            }
        });

        // needed to update the state of the clipboard controls
        wordSearchField.addCaretListener(new CaretListener() {

            @Override
            public void caretUpdate(CaretEvent e) {
                if (wordSearchField.getSelectedText() != null && !wordSearchField.getSelectedText().isEmpty()) {
                    cutButton.setEnabled(true);
                    cutMenuItem.setEnabled(true);
                    copyButton.setEnabled(true);
                    copyMenuItem.setEnabled(true);
                } else {
                    cutButton.setEnabled(false);
                    cutMenuItem.setEnabled(false);
                    copyButton.setEnabled(false);
                    copyButton.setEnabled(false);
                }
            }
        });

        // paste should only work when the focus is in the search field
        wordSearchField.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                pasteButton.setEnabled(true);
                pasteMenuItem.setEnabled(true);
            }

            @Override
            public void focusLost(FocusEvent e) {
                pasteButton.setEnabled(false);
                pasteMenuItem.setEnabled(false);
                cutButton.setEnabled(false);
                cutMenuItem.setEnabled(false);
            }
        });

        wordTranslationTextPane.addCaretListener(new CaretListener() {

            @Override
            public void caretUpdate(CaretEvent e) {
                if (wordTranslationTextPane.getSelectedText() != null && !wordTranslationTextPane.getSelectedText().isEmpty()) {
                    copyButton.setEnabled(true);
                    copyMenuItem.setEnabled(true);
                } else {
                    copyButton.setEnabled(false);
                    copyButton.setEnabled(false);
                }
            }
        });

        // we have update the completion window's position when the frame moves
        addComponentListener(new ComponentAdapter() {

            @Override
            public void componentMoved(ComponentEvent e) {
                if (wordSearchField.hasFocus()) {
                    ((AutocompletingTextField) wordSearchField).showCompletions();
                }
            }
        });

        // add the context popup
        ContextMenuMouseListener contextMenuMouseListener = new ContextMenuMouseListener();

        wordSearchField.addMouseListener(contextMenuMouseListener);
        wordTranslationTextPane.addMouseListener(contextMenuMouseListener);
    }

    private void addWordDefinition() throws HeadlessException {
        AddUpdateWordDialog addUpdateWordDialog = new AddUpdateWordDialog(this, true);
        addUpdateWordDialog.setVisible(true);

        if (addUpdateWordDialog.getReturnStatus() == AddUpdateWordDialog.RET_OK) {
            if (words.contains(addUpdateWordDialog.getWord())) {
                JOptionPane.showMessageDialog(null, TRANSLATOR.translate("WordAlreadyExists(Message)"), "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int insertionIndex = SearchUtils.findInsertionIndex(words, addUpdateWordDialog.getWord());
            System.out.println("insertion index is " + insertionIndex);
            // this is a references to the cache as well
            words.add(insertionIndex, addUpdateWordDialog.getWord());
            wordsList.setModel(new ListBackedListModel(words));

            dictionaryService.addWord(addUpdateWordDialog.getWord(), addUpdateWordDialog.getTranslation(), selectedDictionary);

            // select the freshly inserted word
            wordsList.setSelectedIndex(insertionIndex);
        }
    }

    private void onSearchChange(boolean insert) {
        clearButton.setEnabled(true);

        String searchString = wordSearchField.getText();

        if (!searchString.isEmpty() && insert) {
            // switches to complementary dictionary if needed
            // this can only happen on insert for obvious reasons
            autoCorrectDictionary(searchString);
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

            statusButton.setIcon(IconManager.getImageIcon("bell2_green.png", IconSize.SIZE24));
            statusButton.setToolTipText(TRANSLATOR.translate("MatchFound(ToolTip)"));

            exactMatch = true;
        } else if ((approximation = getApproximation(searchString)) != null) {

            int index = words.indexOf(approximation);

            wordsList.setSelectedIndex(index);
            wordsList.ensureIndexIsVisible(index);

            statusButton.setIcon(IconManager.getImageIcon("bell2_gold.png", IconSize.SIZE24));
            statusButton.setToolTipText(TRANSLATOR.translate("PartialMatchFound(ToolTip)"));

            exactMatch = false;
        } else {
            statusButton.setIcon(IconManager.getImageIcon("bell2_red.png", IconSize.SIZE24));
            statusButton.setToolTipText(TRANSLATOR.translate("NoMatchFound(ToolTip)"));

            exactMatch = false;
        }
    }

    public void showMemoryUsage() {
        lastToolbarSeparator.setVisible(true);
        memoryButton.setVisible(true);

        if (memoryUsageExecutorService == null) {
            Runnable memoryRunnable = new Runnable() {

                @Override
                public void run() {
                    final Runtime runtime = Runtime.getRuntime();
                    final long freeMemory = runtime.freeMemory();
                    final long totalMemory = runtime.totalMemory();

                    int usedMemInMb = (int) (totalMemory - freeMemory) / BYTES_IN_ONE_MEGABYTE;
                    int totalMemInMb = (int) totalMemory / BYTES_IN_ONE_MEGABYTE;

                    //memoryButton.setText(usedMemInMb + "M of " + totalMemInMb + "M");
                    memoryButton.setToolTipText(String.format(TRANSLATOR.translate("MemoryUsage(ToolTip)"), totalMemInMb, usedMemInMb));
                }
            };

            memoryUsageExecutorService = Executors.newSingleThreadScheduledExecutor();
            memoryUsageExecutorService.scheduleAtFixedRate(memoryRunnable, 0, 10, TimeUnit.SECONDS);
        }
    }

    public void hideMemoryUsage() {
        lastToolbarSeparator.setVisible(false);
        memoryButton.setVisible(false);
    }

    private void clear() {
        LOGGER.info("Clear action invoked");

        wordSearchField.setText(null);
        wordSearchField.requestFocusInWindow();
        wordsList.ensureIndexIsVisible(0);
        wordsList.clearSelection();
        updateWordButton.setEnabled(false);
        updateWordMenuItem.setEnabled(false);
        deleteWordButton.setEnabled(false);
        deleteWordMenuItem.setEnabled(false);
        wordTranslationTextPane.setText(null);
        statusButton.setIcon(IconManager.getImageIcon("bell2_grey.png", IconSize.SIZE24));
        lastTransfer = null;

        clearButton.setEnabled(false);
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

                        autoCorrectDictionary(searchString);

                        if (words.contains(searchString)) {
                            foundWord = searchString;
                            int index = words.indexOf(foundWord);

                            wordsList.setSelectedIndex(index);
                            wordsList.ensureIndexIsVisible(index);

                            match = true;

                            statusButton.setIcon(IconManager.getImageIcon("bell2_green.png", IconSize.SIZE24));
                            statusButton.setToolTipText(TRANSLATOR.translate("MatchFound(ToolTip)"));
                        } else if ((approximation = getApproximation(searchString)) != null) {
                            foundWord = approximation;
                            int index = words.indexOf(foundWord);

                            wordsList.setSelectedIndex(index);
                            wordsList.ensureIndexIsVisible(index);

                            match = true;

                            statusButton.setIcon(IconManager.getImageIcon("bell2_gold.png", IconSize.SIZE24));
                            statusButton.setToolTipText(TRANSLATOR.translate("PartialMatchFound(ToolTip)"));
                        }

                        // the tray popup translation should appear is the main frame is either not visible or minimized
                        if ((trayIcon != null) && match && (!SpellbookFrame.this.isVisible()
                                || (SpellbookFrame.this.getState() == JFrame.ICONIFIED))
                                && PM.getBoolean(Preference.TRAY_POPUP, false)) {
                            trayIcon.displayMessage(foundWord, dictionaryService.getTranslation((String) wordsList.getSelectedValue(), selectedDictionary),
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

    public String getApproximation(String searchKey) {
        if (searchKey != null && !searchKey.isEmpty()) {

            LOGGER.info("Getting approximation for " + searchKey);

            String word = searchKey;
            final int index = SearchUtils.findInsertionIndex(words, searchKey);

            // special consideration must be take if the insertion index is past the last index
            return words.get(index == words.size() ? index - 1 : index);
        }

        return null;
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

    private void onWordSearchFieldAction() {
        wordSearchField.selectAll();
        AutocompletingTextField completableJTextField = (AutocompletingTextField) wordSearchField;
        if (exactMatch) {
            completableJTextField.addCompletion(wordSearchField.getText());
            searchedWords.add(wordSearchField.getText());
            searchWordsIndex = searchedWords.size();
            backButton.setEnabled(true);
            forwardButton.setEnabled(false);
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

    private void checkDefaultDbLocations() {
        LOGGER.info("Checking default db locations...");

        String[] defaultDbLocations = new String[]{DEFAULT_DB_PATH, "db/"};

        for (String path : defaultDbLocations) {
            File dbFile = new File(path + DB_FILE_NAME);

            if (dbFile.exists()) {
                LOGGER.info("Found db in " + path);
                PM.put(Preference.PATH_TO_DB, path + DB_FILE_NAME);

                break;
            }
        }
    }

    private void updateWordDefinition() throws IllegalStateException {
        AddUpdateWordDialog addUpdateWordDialog = new AddUpdateWordDialog(this, true);
        addUpdateWordDialog.setDictionary(selectedDictionary);
        if (wordsList.isSelectionEmpty()) {
            throw new IllegalStateException("No word selected");
        }
        final String originalWord = (String) wordsList.getSelectedValue();
        addUpdateWordDialog.setWord(originalWord);
        addUpdateWordDialog.setTranslation(dictionaryService.getTranslation(originalWord, selectedDictionary));
        addUpdateWordDialog.setVisible(true);
        if (addUpdateWordDialog.getReturnStatus() == AddUpdateWordDialog.RET_OK) {
            String newWord = addUpdateWordDialog.getWord();
            String newTranslation = addUpdateWordDialog.getTranslation();

            if (!originalWord.equals(newWord)) {
                words.remove(originalWord);
                int insertionIndex = SearchUtils.findInsertionIndex(words, newWord);
                System.out.println("insertion index is " + insertionIndex);
                // this is a references to the cache as well
                words.add(insertionIndex, newWord);
                wordsList.setModel(new ListBackedListModel(words));
            }

            dictionaryService.updateWord(originalWord, newWord, newTranslation, selectedDictionary);

            // select the freshly updated word
            wordsList.setSelectedValue(newWord, true);

            // if only the translation was changed we need to update it manually
            if (originalWord.equals(newWord)) {
                wordTranslationTextPane.setText(SwingUtil.formatTranslation(newWord, newTranslation));
            }
        }
    }

    private void setSelectedDictionary(Dictionary dictionary) {
        selectedDictionary = dictionary;
        words = dictionaryService.getWordsFromDictionary(dictionary);
    }

    private void updateDictionaryButton(Dictionary dictionary) {
        dictionaryButton.setToolTipText(String.format(dictionary.getName() + "dictionary containing %d words", words.size()));
        dictionaryButton.setIcon(IconManager.getImageIcon(dictionary.getIconName(), IconManager.IconSize.SIZE24));

        if (dictionary.getName().equals("English-Bulgarian")) {
            //SwingUtil.showBalloonTip(dictionaryButton, TRANSLATOR.translate("EnBgDictLoaded(Message)"));
            dictionaryButton.setToolTipText(String.format(TRANSLATOR.translate("EnBgDictSize(Label)"), words.size()));
            dictionaryButton.setIcon(IconManager.getImageIcon(dictionary.getIconName(), IconManager.IconSize.SIZE24));
        } else if (dictionary.getName().equals("Bulgarian-English")) {
            //SwingUtil.showBalloonTip(dictionaryButton, TRANSLATOR.translate("BgEnDictLoaded(Message)"));
            dictionaryButton.setToolTipText(String.format(TRANSLATOR.translate("BgEnDictSize(Label)"), words.size()));
            dictionaryButton.setIcon(IconManager.getImageIcon(dictionary.getIconName(), IconManager.IconSize.SIZE24));
        } else {
            throw new IllegalArgumentException("Unknown dictionary " + dictionary);
        }
    }

    private boolean verifyDbPresence() {
        final String dbPath = PM.get(Preference.PATH_TO_DB, "");

        File file = new File(dbPath);

        String currentPath = "";
        try {
            currentPath = new java.io.File(".").getCanonicalPath();
            LOGGER.info("Current path: " + currentPath);
            currentPath += "\\" + COMPRESSED_DB_NAME;
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        if (!file.exists()) {

            LOGGER.info("Checking for archivied db ...");

            File archiviedDbFile = new File(currentPath);

            if (archiviedDbFile.exists()) {
                LOGGER.info("Found the archivied db in " + currentPath);
                extractDbFromArchive(currentPath);
                return true;

            }

            if (dbPath.isEmpty()) {

                Object[] options = {"Download from internet!",
                    "Find in the filesystem!",};

                int choice = JOptionPane.showOptionDialog(null, "What to do?", "Missing Db!", JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

                // JOptionPane.showMessageDialog(null, TRANSLATOR.translate("MissingDb(Message)"));
                if (choice == 1) {
                    JFileChooser fileChooser = new JFileChooser();
                    final int result = fileChooser.showDialog(null, TRANSLATOR.translate("SelectDb(Title)"));

                    if (result == JFileChooser.APPROVE_OPTION) {
                        String selectedDbPath = fileChooser.getSelectedFile().getPath();

                        if (selectedDbPath.endsWith(COMPRESSED_DB_NAME)) {
                            extractDbFromArchive(selectedDbPath);
                            return true;
                        } else if (selectedDbPath.endsWith(DB_FILE_NAME)) {
                            PM.put(Preference.PATH_TO_DB, selectedDbPath);
                            return true;
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                } else {
                    try {
                        BufferedInputStream in = new BufferedInputStream(new URL("http://spellbook-dictionary.googlecode.com/files/dictionary-db.tar.bz2").openStream());
                        FileOutputStream fos = new FileOutputStream(COMPRESSED_DB_NAME);
                        BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);
                        byte[] data = new byte[1024];
                        int x = 0;
                        LOGGER.info("Downloading db ...");
                        while ((x = in.read(data, 0, 1024)) >= 0) {
                            bout.write(data, 0, x);
                        }
                        bout.close();
                        in.close();
                    } catch (FileNotFoundException e) {
                    } catch (IOException ex) {
                    }

                    extractDbFromArchive(currentPath);
                }
            }
        }
        return true;
    }

    public void setSelectedFont(Font font) {
        wordSearchField.setFont(font);
        wordsList.setFont(font);
        wordTranslationTextPane.setFont(font);
        dictionaryButton.setFont(font);
    }

    public void selectDictionary(Dictionary dictionary, boolean clear) {
        // if we select the currently selected dictionary we don't have to do nothing
        if (selectedDictionary == dictionary) {
            LOGGER.info("Dictionary " + dictionary + " is already selected");
            return;
        }

        if (clear) {
            // otherwise begin the switch to the new dictionary by cleaning everything in the UI
            clear();
        }

        setSelectedDictionary(dictionary);

        wordsList.setModel(new ListBackedListModel(words));
        updateDictionaryButton(dictionary);

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
        splitPane = new javax.swing.JSplitPane();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        wordsList = new javax.swing.JList();
        wordSearchField = new AutocompletingTextField();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        wordTranslationTextPane = new javax.swing.JTextPane();
        jToolBar1 = new javax.swing.JToolBar();
        backButton = new javax.swing.JButton();
        forwardButton = new javax.swing.JButton();
        clearButton = new javax.swing.JButton();
        statusButton = new javax.swing.JButton();
        dictionaryButton = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        addWordButton = new javax.swing.JButton();
        updateWordButton = new javax.swing.JButton();
        deleteWordButton = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        cutButton = new JButton(new DefaultEditorKit.CutAction());
        cutButton.setText(null);
        copyButton = new JButton(new DefaultEditorKit.CopyAction());
        copyButton.setText(null);
        pasteButton = new JButton(new DefaultEditorKit.PasteAction());
        pasteButton.setText(null);
        jSeparator5 = new javax.swing.JToolBar.Separator();
        studyButton = new javax.swing.JButton();
        examButton = new javax.swing.JButton();
        spellcheckButton = new javax.swing.JButton();
        lastToolbarSeparator = new javax.swing.JToolBar.Separator();
        memoryButton = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        restartMenuItem = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        addWordMenuItem = new javax.swing.JMenuItem();
        updateWordMenuItem = new javax.swing.JMenuItem();
        deleteWordMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        cutMenuItem = new javax.swing.JMenuItem(new DefaultEditorKit.CutAction());
        copyMenuItem = new javax.swing.JMenuItem(new DefaultEditorKit.CopyAction());
        pasteMenuItem = new javax.swing.JMenuItem(new DefaultEditorKit.PasteAction());
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        prefsMenuItem = new javax.swing.JMenuItem();
        dictionaryMenu = new javax.swing.JMenu();
        jMenu4 = new javax.swing.JMenu();
        StudyWordsMenuItem = new javax.swing.JMenuItem();
        examMenuItem = new javax.swing.JMenuItem();
        spellcheckMenuItem = new javax.swing.JMenuItem();
        jMenu5 = new javax.swing.JMenu();
        helpContentsMenuItem = new javax.swing.JMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        splitPane.setBorder(null);
        splitPane.setDividerLocation(180);

        wordsList.setModel(new com.drowltd.spellbook.ui.swing.model.ListBackedListModel(words));
        wordsList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("i18n/SpellbookForm"); // NOI18N
        wordsList.setToolTipText(bundle.getString("WordsList(ToolTip)")); // NOI18N
        wordsList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                wordsListMouseClicked(evt);
            }
        });
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
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 412, Short.MAX_VALUE))
        );

        splitPane.setLeftComponent(jPanel2);

        wordTranslationTextPane.setContentType("text/html");
        wordTranslationTextPane.setEditable(false);
        jScrollPane1.setViewportView(wordTranslationTextPane);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 592, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 446, Short.MAX_VALUE)
        );

        splitPane.setRightComponent(jPanel3);

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        backButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/24x24/arrow_left_blue.png"))); // NOI18N
        backButton.setToolTipText(bundle.getString("PreviousWord(Label)")); // NOI18N
        backButton.setFocusable(false);
        backButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        backButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        backButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(backButton);

        forwardButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/24x24/arrow_right_blue.png"))); // NOI18N
        forwardButton.setToolTipText(bundle.getString("NextWord(Label)")); // NOI18N
        forwardButton.setFocusable(false);
        forwardButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        forwardButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        forwardButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                forwardButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(forwardButton);

        clearButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/24x24/eraser.png"))); // NOI18N
        clearButton.setToolTipText(bundle.getString("ClearButton(ToolTip)")); // NOI18N
        clearButton.setFocusable(false);
        clearButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        clearButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(clearButton);

        statusButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/24x24/bell2_grey.png"))); // NOI18N
        statusButton.setFocusable(false);
        statusButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        statusButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        statusButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                statusButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(statusButton);

        dictionaryButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/24x24/en-bg.png"))); // NOI18N
        dictionaryButton.setFocusable(false);
        dictionaryButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        dictionaryButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        dictionaryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dictionaryButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(dictionaryButton);
        jToolBar1.add(jSeparator3);

        addWordButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/24x24/add2.png"))); // NOI18N
        addWordButton.setToolTipText(bundle.getString("EditAddWord(MenuItem)")); // NOI18N
        addWordButton.setFocusable(false);
        addWordButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        addWordButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        addWordButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addWordButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(addWordButton);

        updateWordButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/24x24/edit.png"))); // NOI18N
        updateWordButton.setToolTipText(bundle.getString("EditUpdateWord(MenuItem)")); // NOI18N
        updateWordButton.setFocusable(false);
        updateWordButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        updateWordButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        updateWordButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateWordButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(updateWordButton);

        deleteWordButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/24x24/delete2.png"))); // NOI18N
        deleteWordButton.setToolTipText(bundle.getString("EditDeleteWord(MenuItem)")); // NOI18N
        deleteWordButton.setFocusable(false);
        deleteWordButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        deleteWordButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        deleteWordButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteWordButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(deleteWordButton);
        jToolBar1.add(jSeparator4);

        cutButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/24x24/cut.png"))); // NOI18N
        cutButton.setToolTipText(bundle.getString("EditCut(MenuItem)")); // NOI18N
        cutButton.setFocusable(false);
        cutButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cutButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(cutButton);

        copyButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/24x24/copy.png"))); // NOI18N
        copyButton.setToolTipText(bundle.getString("EditCopy(MenuItem)")); // NOI18N
        copyButton.setFocusable(false);
        copyButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        copyButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(copyButton);

        pasteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/24x24/paste.png"))); // NOI18N
        pasteButton.setToolTipText(bundle.getString("EditPaste(MenuItem)")); // NOI18N
        pasteButton.setFocusable(false);
        pasteButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        pasteButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(pasteButton);
        jToolBar1.add(jSeparator5);

        studyButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/24x24/teacher.png"))); // NOI18N
        studyButton.setToolTipText(bundle.getString("StudyWords(MenuItem)")); // NOI18N
        studyButton.setFocusable(false);
        studyButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        studyButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        studyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                studyButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(studyButton);

        examButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/24x24/blackboard.png"))); // NOI18N
        examButton.setToolTipText(bundle.getString("Exam(MenuItem)")); // NOI18N
        examButton.setFocusable(false);
        examButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        examButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        examButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                examButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(examButton);

        spellcheckButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/24x24/spellcheck.png"))); // NOI18N
        spellcheckButton.setToolTipText(bundle.getString("SpellCheck(MenuItem)")); // NOI18N
        spellcheckButton.setFocusable(false);
        spellcheckButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        spellcheckButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        spellcheckButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                spellcheckButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(spellcheckButton);
        jToolBar1.add(lastToolbarSeparator);

        memoryButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/24x24/memory.png"))); // NOI18N
        memoryButton.setFocusable(false);
        memoryButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        memoryButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        memoryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                memoryButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(memoryButton);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jToolBar1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 778, Short.MAX_VALUE)
                    .addComponent(splitPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 778, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(splitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 446, Short.MAX_VALUE)
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

        deleteWordMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/16x16/delete2.png"))); // NOI18N
        deleteWordMenuItem.setText(bundle.getString("EditDeleteWord(MenuItem)")); // NOI18N
        jMenu2.add(deleteWordMenuItem);
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

        dictionaryMenu.setMnemonic('d');
        dictionaryMenu.setText(bundle.getString("Dictionaries(Menu)")); // NOI18N
        jMenuBar1.add(dictionaryMenu);

        jMenu4.setMnemonic('t');
        jMenu4.setText(bundle.getString("Tools(Menu)")); // NOI18N

        StudyWordsMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/16x16/teacher.png"))); // NOI18N
        StudyWordsMenuItem.setMnemonic('w');
        StudyWordsMenuItem.setText(bundle.getString("StudyWords(MenuItem)")); // NOI18N
        StudyWordsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                StudyWordsMenuItemActionPerformed(evt);
            }
        });
        jMenu4.add(StudyWordsMenuItem);

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
        addWordDefinition();
    }//GEN-LAST:event_addWordMenuItemActionPerformed

    private void updateWordMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateWordMenuItemActionPerformed
        updateWordDefinition();
    }//GEN-LAST:event_updateWordMenuItemActionPerformed

    private void wordSearchFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wordSearchFieldActionPerformed
        onWordSearchFieldAction();
    }//GEN-LAST:event_wordSearchFieldActionPerformed

    private void wordsListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_wordsListValueChanged
        // TODO refine synchronization
        if (!wordsList.isSelectionEmpty()) {
            // word field needs to be updated in a separate thread
            EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    onWordSelectionChange();
                }
            });
        }
    }//GEN-LAST:event_wordsListValueChanged

    private void StudyWordsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_StudyWordsMenuItemActionPerformed
        StudyWordsDialog studyWords = new StudyWordsDialog(this, true);
        studyWords.setLocationRelativeTo(this);
        studyWords.showLearningWordsDialog();
    }//GEN-LAST:event_StudyWordsMenuItemActionPerformed

    private void wordsListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_wordsListMouseClicked
        if (evt.getClickCount() == 2) {
            updateWordDefinition();
        }
    }//GEN-LAST:event_wordsListMouseClicked

    private void backButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backButtonActionPerformed
        if (searchedWords.size() > 0 && searchWordsIndex >= 1) {
            wordSearchField.setText(searchedWords.get(--searchWordsIndex));

            updateHistoryButtonsState();
        }
    }//GEN-LAST:event_backButtonActionPerformed

    private void forwardButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_forwardButtonActionPerformed
        if (searchedWords.size() - 1 > searchWordsIndex) {
            wordSearchField.setText(searchedWords.get(++searchWordsIndex));

            updateHistoryButtonsState();
        }
    }//GEN-LAST:event_forwardButtonActionPerformed

    private void statusButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statusButtonActionPerformed
        onWordSearchFieldAction();
    }//GEN-LAST:event_statusButtonActionPerformed

    private void memoryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_memoryButtonActionPerformed
        System.gc();
    }//GEN-LAST:event_memoryButtonActionPerformed

    private void dictionaryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dictionaryButtonActionPerformed
        if (dictionaryService.isComplemented(selectedDictionary)) {
            selectDictionary(dictionaryService.getComplement(selectedDictionary), true);
        }
    }//GEN-LAST:event_dictionaryButtonActionPerformed

    private void studyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_studyButtonActionPerformed
        StudyWordsDialog studyWords = new StudyWordsDialog(this, true);
        studyWords.setLocationRelativeTo(this);
        studyWords.showLearningWordsDialog();
    }//GEN-LAST:event_studyButtonActionPerformed

    private void examButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_examButtonActionPerformed
        ExamDialog examDialog = new ExamDialog(this, true);
        examDialog.showExamDialog();
    }//GEN-LAST:event_examButtonActionPerformed

    private void spellcheckButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_spellcheckButtonActionPerformed
        SpellCheckFrame.getInstance().setVisible(true);
    }//GEN-LAST:event_spellcheckButtonActionPerformed

    private void addWordButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addWordButtonActionPerformed
        addWordDefinition();
    }//GEN-LAST:event_addWordButtonActionPerformed

    private void updateWordButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateWordButtonActionPerformed
        updateWordDefinition();
    }//GEN-LAST:event_updateWordButtonActionPerformed

    private void deleteWordButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteWordButtonActionPerformed
        deleteWordDefinition();
    }//GEN-LAST:event_deleteWordButtonActionPerformed

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
        clear();
    }//GEN-LAST:event_clearButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem StudyWordsMenuItem;
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JButton addWordButton;
    private javax.swing.JMenuItem addWordMenuItem;
    private javax.swing.JButton backButton;
    private javax.swing.JButton clearButton;
    private javax.swing.JButton copyButton;
    private javax.swing.JMenuItem copyMenuItem;
    private javax.swing.JButton cutButton;
    private javax.swing.JMenuItem cutMenuItem;
    private javax.swing.JButton deleteWordButton;
    private javax.swing.JMenuItem deleteWordMenuItem;
    private javax.swing.JButton dictionaryButton;
    private javax.swing.JMenu dictionaryMenu;
    private javax.swing.JButton examButton;
    private javax.swing.JMenuItem examMenuItem;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JButton forwardButton;
    private javax.swing.JMenuItem helpContentsMenuItem;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
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
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToolBar.Separator lastToolbarSeparator;
    private javax.swing.JButton memoryButton;
    private javax.swing.JButton pasteButton;
    private javax.swing.JMenuItem pasteMenuItem;
    private javax.swing.JMenuItem prefsMenuItem;
    private javax.swing.JMenuItem restartMenuItem;
    private javax.swing.JButton spellcheckButton;
    private javax.swing.JMenuItem spellcheckMenuItem;
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JButton statusButton;
    private javax.swing.JButton studyButton;
    private javax.swing.JButton updateWordButton;
    private javax.swing.JMenuItem updateWordMenuItem;
    private javax.swing.JTextField wordSearchField;
    private javax.swing.JTextPane wordTranslationTextPane;
    private javax.swing.JList wordsList;
    // End of variables declaration//GEN-END:variables

    //Must be called after initComponents in the constructor
    private void initDictionaries() {
        dictionaryMenu.removeAll();

        final List<Dictionary> availableDictionaries = dictionaryService.getDictionaries();
        for (Dictionary dictionary : availableDictionaries) {
            dictionaryMenu.add(new DictionaryItem(dictionary));
        }

    }

    private void autoCorrectDictionary(String searchString) {
        if (searchString == null || searchString.isEmpty()) {
            return;
        }

        Language from = selectedDictionary.getFromLanguage();
        boolean valid = true;

        for (String letter : explodeString(searchString)) {
            if (!from.getAlphabet().contains(letter.toLowerCase())) {
                valid = false;
                break;
            }
        }

        if (!valid && dictionaryService.isComplemented(selectedDictionary)) {
            Language to = selectedDictionary.getToLanguage();
            valid = true;

            for (String letter : explodeString(searchString)) {
                if (!to.getAlphabet().contains(letter.toLowerCase())) {
                    valid = false;
                    break;
                }
            }

            if (valid) {
                LOGGER.info("Auto switching to complementing dictinary...");
                selectDictionary(dictionaryService.getComplement(selectedDictionary), false);
            }
        }
    }

    private List<String> explodeString(String string) {
        List<String> result = new ArrayList<String>();

        for (int i = 0; i < string.length(); i++) {
            result.add(string.substring(i, i + 1));
        }

        return result;
    }

    private void deleteWordDefinition() {
        if (JOptionPane.showConfirmDialog(this, TRANSLATOR.translate("ConfirmWordDeletion(Message)"),
                TRANSLATOR.translate("ConfirmWordDeletion(Title)"),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {

            String selectedWord = (String) wordsList.getSelectedValue();
            int selectedIndex = wordsList.getSelectedIndex();

            words.remove(selectedWord);
            wordsList.setModel(new ListBackedListModel(words));
            // this selects the word after the deleted word
            wordsList.setSelectedIndex(selectedIndex);
            dictionaryService.deleteWord(selectedWord, selectedDictionary);
        }
    }

    private void extractDbFromArchive(String pathToArchive) {
        // Get the current path, where the database will be extracted
        String currentPath = "";
        try {
            currentPath = new java.io.File(".").getCanonicalPath();
        } catch (IOException ex) {
        }
        LOGGER.info("Current path: " + currentPath);
        currentPath += "\\";
        try {
            //Open the archive
            FileInputStream archiveFileStream = new FileInputStream(pathToArchive);
            // Read two bytes from the stream before it used by CBZip2InputStream
            int oneByte;
            for (int i = 0; i < 2; i++) {
                oneByte = archiveFileStream.read();
            }

            // Open the gzip file and open the output file
            CBZip2InputStream bz2 = new CBZip2InputStream(archiveFileStream);
            FileOutputStream out = new FileOutputStream(ARICHIVIED_DB_NAME);

            LOGGER.info("Extracting the tar file...");
            // Transfer bytes from the compressed file to the output file
            byte[] buffer = new byte[1024];
            int len;
            while ((len = bz2.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }

            // Close the file and stream
            bz2.close();
            out.close();

        } catch (FileNotFoundException e) {
        } catch (IOException ex) {
        }

        try {

            TarInputStream tarInputStream = null;
            TarEntry tarEntry;
            tarInputStream = new TarInputStream(new FileInputStream(ARICHIVIED_DB_NAME));

            tarEntry = tarInputStream.getNextEntry();

            byte[] buf1 = new byte[1024];

            while (tarEntry != null) {
                //For each entry to be extracted
                String entryName = currentPath + tarEntry.getName();
                entryName = entryName.replace('/', File.separatorChar);
                entryName = entryName.replace('\\', File.separatorChar);

                LOGGER.info("Extracting entry: " + entryName);
                FileOutputStream fileOutputStream;
                File newFile = new File(entryName);
                if (tarEntry.isDirectory()) {
                    if (!newFile.mkdirs()) {
                        break;
                    }
                    tarEntry = tarInputStream.getNextEntry();
                    continue;
                }

                fileOutputStream = new FileOutputStream(entryName);
                int n;
                while ((n = tarInputStream.read(buf1, 0, 1024)) > -1) {
                    fileOutputStream.write(buf1, 0, n);
                }

                fileOutputStream.close();
                tarEntry = tarInputStream.getNextEntry();

            }
            tarInputStream.close();
        } catch (Exception e) {
        }

        currentPath += "\\db\\" + DB_FILE_NAME;
        if (!currentPath.isEmpty()) {
            LOGGER.info("DB placed in : " + currentPath);
            PM.put(Preference.PATH_TO_DB, currentPath);
        }


    }

    private class DictionaryItem extends JMenuItem implements ActionListener {

        private final String dictionaryName;

        public DictionaryItem(Dictionary dictionary) {
            if (dictionary == null) {
                throw new IllegalArgumentException("dictionary is null");
            }

            dictionaryName = dictionary.getName();
            setIcon(IconManager.getMenuIcon(dictionary.getIconName()));
            setText(TRANSLATOR.translate(dictionary.getName() + "(Dictionary)"));
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            selectDictionary(dictionaryService.getDictionary(dictionaryName), true);
        }
    }

    private synchronized void onWordSelectionChange() {
        final int selectedIndex = wordsList.getSelectedIndex();

        final String selectedWord = words.get(selectedIndex);

        // some things should happen only if the user is selecting words directly from the words list
        if (!wordSearchField.hasFocus()) {
            wordSearchField.setText(selectedWord);

            statusButton.setIcon(IconManager.getImageIcon("bell2_green.png", IconSize.SIZE24));
            statusButton.setToolTipText(TRANSLATOR.translate("MatchFound(ToolTip)"));
        }

        wordTranslationTextPane.setText(SwingUtil.formatTranslation(selectedWord, dictionaryService.getTranslation(words.get(selectedIndex), selectedDictionary)));
        wordTranslationTextPane.setCaretPosition(0);

        // words can be updated only when selected
        updateWordMenuItem.setEnabled(true);
        updateWordButton.setEnabled(true);

        deleteWordButton.setEnabled(true);
    }

    private void updateHistoryButtonsState() {
        backButton.setEnabled(searchWordsIndex > 0);
        forwardButton.setEnabled(searchWordsIndex < searchedWords.size() - 1);
    }
}
