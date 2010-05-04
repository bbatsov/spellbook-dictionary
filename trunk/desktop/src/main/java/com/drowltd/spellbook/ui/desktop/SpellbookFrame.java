package com.drowltd.spellbook.ui.desktop;

import com.drowltd.spellbook.core.i18n.Translator;
import com.drowltd.spellbook.core.model.Dictionary;
import com.drowltd.spellbook.core.model.Language;
import com.drowltd.spellbook.core.preferences.PreferencesManager;
import com.drowltd.spellbook.core.service.DictionaryService;
import com.drowltd.spellbook.ui.desktop.exam.ExamDialog;
import com.drowltd.spellbook.ui.desktop.spellcheck.SpellCheckFrame;
import com.drowltd.spellbook.ui.desktop.study.StudyWordsDialog;
import com.drowltd.spellbook.ui.swing.component.DownloadDialog;
import com.drowltd.spellbook.ui.swing.model.ListBackedListModel;
import com.drowltd.spellbook.ui.swing.util.IconManager;
import com.drowltd.spellbook.ui.swing.util.IconManager.IconSize;
import com.drowltd.spellbook.ui.swing.util.SwingUtil;
import com.drowltd.spellbook.util.ArchiveUtils;
import com.drowltd.spellbook.util.SearchUtils;
import com.jidesoft.dialog.StandardDialog;
import com.jidesoft.hints.ListDataIntelliHints;
import com.jidesoft.swing.FolderChooser;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.DefaultEditorKit;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.drowltd.spellbook.core.preferences.PreferencesManager.Preference;

/**
 * Spellbook's main application frame.
 *
 * @author Bozhidar Batsov
 * @since 0.1
 */
public class SpellbookFrame extends JFrame {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpellbookFrame.class);
    private static final Translator TRANSLATOR = Translator.getTranslator("SpellbookFrame");
    private static final PreferencesManager PM = PreferencesManager.getInstance();
    private List<String> words;
    private ClipboardIntegration clipboardIntegration;
    private ScheduledExecutorService memoryUsageExecutorService;
    private TrayIcon trayIcon;
    private boolean exactMatch = false;
    private List<String> searchedWords = new ArrayList<String>();
    private int searchWordsIndex = -1;
    private static final int BYTES_IN_ONE_MEGABYTE = 1024 * 1024;
    private static final String DEFAULT_DB_PATH = "/opt/spellbook/db/";
    private static final String DB_FILE_NAME = "spellbook.data.db";
    private static final String COMPRESSED_DB_NAME = "spellbook-db.tar.bz2";
    private static final String DB_URL = "http://spellbook-dictionary.googlecode.com/files/spellbook-db.tar.bz2";
    private static DictionaryService dictionaryService;
    private static Dictionary selectedDictionary;

    private JButton backButton;
    private JButton clearButton;
    private JButton copyButton;
    private JMenuItem copyMenuItem;
    private JButton cutButton;
    private JMenuItem cutMenuItem;
    private JButton deleteWordButton;
    private JMenuItem deleteWordMenuItem;
    private JButton dictionaryButton;
    private JMenu dictionaryMenu;
    private JButton forwardButton;
    private JPanel topPanel;
    private JToolBar.Separator lastToolbarSeparator;
    private JButton memoryButton;
    private JButton pasteButton;
    private JMenuItem pasteMenuItem;
    private JSplitPane splitPane;
    private JButton statusButton;
    private JButton updateWordButton;
    private JMenuItem updateWordMenuItem;
    private JTextField wordSearchField;
    private JTextPane wordTranslationTextPane;
    private JList wordsList;

    /**
     * Creates new form SpellbookFrame
     */
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

        // setup intellihints for words search field
        ListDataIntelliHints<String> intelliHints = new ListDataIntelliHints<String>(wordSearchField, searchedWords);
        intelliHints.setCaseSensitive(false);

        setDefaultFont();

        if (PM.getBoolean(Preference.SHOW_MEMORY_USAGE, false)) {
            showMemoryUsage();
        } else {
            hideMemoryUsage();
        }

        // implemented a very nasty clipboard ownership hack to simulate notifications
        clipboardIntegration = new ClipboardIntegration(this);
        clipboardIntegration.setClipboardContents(clipboardIntegration.getClipboardContents());
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

        // add the context popup
        ContextMenuMouseListener contextMenuMouseListener = new ContextMenuMouseListener();

        wordSearchField.addMouseListener(contextMenuMouseListener);
        wordTranslationTextPane.addMouseListener(contextMenuMouseListener);
    }

    private void addWordDefinition() throws HeadlessException {
        AddUpdateWordDialog addUpdateWordDialog = new AddUpdateWordDialog(this, true);
        addUpdateWordDialog.setVisible(true);

        if (addUpdateWordDialog.getDialogResult() == StandardDialog.RESULT_AFFIRMED) {
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

        clearButton.setEnabled(false);
    }


    public void clipboardCallback() {
        String transferredText = clipboardIntegration.getClipboardContents().trim();

        if (!transferredText.isEmpty()) {
            LOGGER.info("'" + transferredText + "' received from clipboard");
            String searchString = transferredText.split("\\W")[0].toLowerCase();
            String foundWord = "";
            LOGGER.info("Search string from clipboard is " + searchString);
            wordSearchField.setText(searchString);
            wordSearchField.selectAll();

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


    public String getApproximation(String searchKey) {
        if (searchKey != null && !searchKey.isEmpty()) {

            LOGGER.info("Getting approximation for " + searchKey);

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
        if (exactMatch) {
            LOGGER.info("Attempting to add " + wordSearchField.getText() + " to completions list");

            // don't add consecutively the same word
            if (searchedWords.isEmpty() || !searchedWords.get(searchedWords.size() - 1).equals(wordSearchField.getText())) {
                searchedWords.add(wordSearchField.getText());
                searchWordsIndex = searchedWords.size();
                backButton.setEnabled(true);
                forwardButton.setEnabled(false);
            }
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
        if (addUpdateWordDialog.getDialogResult() == StandardDialog.RESULT_CANCELLED) {
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
        
        String currentPath = System.getProperty("user.home");
        currentPath += File.separator + COMPRESSED_DB_NAME;
        
        if (!file.exists() || file.isDirectory()) {

            LOGGER.info("Checking for archived db ...");

            File archivedDbFile = new File(currentPath);

            if (archivedDbFile.exists()) {
                LOGGER.info("Found the archived db in " + currentPath);

                PM.put(Preference.PATH_TO_DB, ArchiveUtils.extractDbFromArchive(currentPath));
                return true;

            }

            Object[] options = {TRANSLATOR.translate("DownloadDb(Button)"),
                    TRANSLATOR.translate("SelectDb(Button)")};

            int choice = JOptionPane.showOptionDialog(null,
                    dbPath.isEmpty() ? TRANSLATOR.translate("SelectDb(Message") : TRANSLATOR.translate("MissingDb(Message)"),
                    TRANSLATOR.translate("SelectDb(Title)"), JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

            if (choice == 1) {
                FolderChooser folderChooser = new FolderChooser();
                folderChooser.setFileHidingEnabled(true); // show hidden folders
                int result = folderChooser.showOpenDialog(this);

                if (result == FolderChooser.APPROVE_OPTION) {
                    String selectedDbFolderPath = folderChooser.getSelectedFile().getPath();

                    File dbFile = new File(selectedDbFolderPath + File.separator + COMPRESSED_DB_NAME);

                    LOGGER.info("Looking for compressed spellbook database " + dbFile.getPath());

                    if (dbFile.exists()) {
                        PM.put(Preference.PATH_TO_DB, ArchiveUtils.extractDbFromArchive(currentPath));

                        return true;
                    }

                    dbFile = new File(selectedDbFolderPath + File.separator + DB_FILE_NAME);

                    LOGGER.info("Looking for spellbook db " + dbFile.getPath());

                    if (dbFile.exists()) {
                        PM.put(Preference.PATH_TO_DB, dbFile.getPath());

                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                DownloadDialog downloadDialog = new DownloadDialog();

                if (downloadDialog.showDialog(DB_URL) == StandardDialog.RESULT_AFFIRMED) {
                    PM.put(Preference.PATH_TO_DB, ArchiveUtils.extractDbFromArchive(downloadDialog.getDownloadPath()));
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

    private void initComponents() {

        topPanel = new JPanel(new MigLayout("wrap 1", "[grow]", "[grow]"));

        // this is where the magic happens
        setContentPane(topPanel);

        splitPane = new JSplitPane();

        wordsList = new JList();
        wordSearchField = new JTextField();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        splitPane.setBorder(null);
        splitPane.setDividerLocation(180);

        topPanel.add(splitPane, "growx, growy");

        wordsList.setModel(new ListBackedListModel(words));
        wordsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        wordsList.setToolTipText(TRANSLATOR.translate("WordsList(ToolTip)")); // NOI18N
        wordsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                wordsListMouseClicked(evt);
            }
        });
        wordsList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent evt) {
                wordsListValueChanged(evt);
            }
        });

        wordSearchField.setToolTipText(TRANSLATOR.translate("WordSearch(ToolTip)")); // NOI18N
        wordSearchField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                wordSearchFieldActionPerformed(evt);
            }
        });

        JPanel searchPanel = new JPanel(new MigLayout("wrap 1", "[grow]", "[][grow]"));

        searchPanel.add(wordSearchField, "growx");
        searchPanel.add(new JScrollPane(wordsList), "growx, growy");

        splitPane.setLeftComponent(searchPanel);

        JPanel translationPanel = new JPanel(new MigLayout("wrap 1", "[grow]", "[grow]"));
        wordTranslationTextPane = new JTextPane();

        wordTranslationTextPane.setContentType("text/html");
        wordTranslationTextPane.setEditable(false);

        translationPanel.add(new JScrollPane(wordTranslationTextPane), "growx, growy");

        splitPane.setRightComponent(translationPanel);

        initToolBar();

        initMenuBar();

        pack();
    }

    private void initMenuBar() {
        JMenuBar jMenuBar1 = new JMenuBar();
        JMenu jMenu1 = new JMenu();
        JMenuItem restartMenuItem = new JMenuItem();
        JMenuItem exitMenuItem = new JMenuItem();
        JMenu jMenu2 = new JMenu();
        JMenuItem addWordMenuItem = new JMenuItem();
        updateWordMenuItem = new JMenuItem();
        deleteWordMenuItem = new JMenuItem();
        JPopupMenu.Separator jSeparator2 = new JPopupMenu.Separator();
        cutMenuItem = new JMenuItem(new DefaultEditorKit.CutAction());
        copyMenuItem = new JMenuItem(new DefaultEditorKit.CopyAction());
        pasteMenuItem = new JMenuItem(new DefaultEditorKit.PasteAction());
        JPopupMenu.Separator jSeparator1 = new JPopupMenu.Separator();
        JMenuItem prefsMenuItem = new JMenuItem();
        dictionaryMenu = new JMenu();
        JMenu jMenu4 = new JMenu();
        JMenuItem studyWordsMenuItem = new JMenuItem();
        JMenuItem examMenuItem = new JMenuItem();
        JMenuItem spellcheckMenuItem = new JMenuItem();
        JMenu jMenu5 = new JMenu();
        JMenuItem helpContentsMenuItem = new JMenuItem();
        JMenuItem aboutMenuItem = new JMenuItem();

        jMenu1.setMnemonic('f');
        jMenu1.setText(TRANSLATOR.translate("File(Menu)")); // NOI18N

        restartMenuItem.setIcon(new ImageIcon(getClass().getResource("/icons/16x16/refresh.png"))); // NOI18N
        restartMenuItem.setMnemonic('r');
        restartMenuItem.setText(TRANSLATOR.translate("FileRestart(MenuItem)")); // NOI18N
        restartMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                restartMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(restartMenuItem);

        exitMenuItem.setIcon(new ImageIcon(getClass().getResource("/icons/16x16/exit.png"))); // NOI18N
        exitMenuItem.setMnemonic('x');
        exitMenuItem.setText(TRANSLATOR.translate("FileExit(MenuItem)")); // NOI18N
        exitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(exitMenuItem);

        jMenuBar1.add(jMenu1);

        jMenu2.setMnemonic('e');
        jMenu2.setText(TRANSLATOR.translate("Edit(Menu)")); // NOI18N

        addWordMenuItem.setIcon(new ImageIcon(getClass().getResource("/icons/16x16/add2.png"))); // NOI18N
        addWordMenuItem.setText(TRANSLATOR.translate("EditAddWord(MenuItem)")); // NOI18N
        addWordMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                addWordMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(addWordMenuItem);

        updateWordMenuItem.setIcon(new ImageIcon(getClass().getResource("/icons/16x16/edit.png"))); // NOI18N
        updateWordMenuItem.setText(TRANSLATOR.translate("EditUpdateWord(MenuItem)")); // NOI18N
        updateWordMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                updateWordMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(updateWordMenuItem);

        deleteWordMenuItem.setIcon(new ImageIcon(getClass().getResource("/icons/16x16/delete2.png"))); // NOI18N
        deleteWordMenuItem.setText(TRANSLATOR.translate("EditDeleteWord(MenuItem)")); // NOI18N
        jMenu2.add(deleteWordMenuItem);
        jMenu2.add(jSeparator2);

        cutMenuItem.setIcon(new ImageIcon(getClass().getResource("/icons/16x16/cut.png"))); // NOI18N
        cutMenuItem.setMnemonic('t');
        cutMenuItem.setText(TRANSLATOR.translate("EditCut(MenuItem)")); // NOI18N
        jMenu2.add(cutMenuItem);

        copyMenuItem.setIcon(new ImageIcon(getClass().getResource("/icons/16x16/copy.png"))); // NOI18N
        copyMenuItem.setMnemonic('c');
        copyMenuItem.setText(TRANSLATOR.translate("EditCopy(MenuItem)")); // NOI18N
        jMenu2.add(copyMenuItem);

        pasteMenuItem.setIcon(new ImageIcon(getClass().getResource("/icons/16x16/paste.png"))); // NOI18N
        pasteMenuItem.setMnemonic('p');
        pasteMenuItem.setText(TRANSLATOR.translate("EditPaste(MenuItem)")); // NOI18N
        jMenu2.add(pasteMenuItem);
        jMenu2.add(jSeparator1);

        prefsMenuItem.setIcon(new ImageIcon(getClass().getResource("/icons/16x16/preferences.png"))); // NOI18N
        prefsMenuItem.setMnemonic('e');
        prefsMenuItem.setText(TRANSLATOR.translate("EditPreferences(MenuItem)")); // NOI18N
        prefsMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                prefsMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(prefsMenuItem);

        jMenuBar1.add(jMenu2);

        dictionaryMenu.setMnemonic('d');
        dictionaryMenu.setText(TRANSLATOR.translate("Dictionaries(Menu)")); // NOI18N
        jMenuBar1.add(dictionaryMenu);

        jMenu4.setMnemonic('t');
        jMenu4.setText(TRANSLATOR.translate("Tools(Menu)")); // NOI18N

        studyWordsMenuItem.setIcon(new ImageIcon(getClass().getResource("/icons/16x16/teacher.png"))); // NOI18N
        studyWordsMenuItem.setMnemonic('w');
        studyWordsMenuItem.setText(TRANSLATOR.translate("StudyWords(MenuItem)")); // NOI18N
        studyWordsMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                StudyWordsMenuItemActionPerformed(evt);
            }
        });
        jMenu4.add(studyWordsMenuItem);

        examMenuItem.setIcon(new ImageIcon(getClass().getResource("/icons/16x16/blackboard.png"))); // NOI18N
        examMenuItem.setMnemonic('e');
        examMenuItem.setText(TRANSLATOR.translate("Exam(MenuItem)")); // NOI18N
        examMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                examMenuItemActionPerformed(evt);
            }
        });
        jMenu4.add(examMenuItem);

        spellcheckMenuItem.setIcon(new ImageIcon(getClass().getResource("/icons/16x16/spellcheck.png"))); // NOI18N
        spellcheckMenuItem.setMnemonic('s');
        spellcheckMenuItem.setText(TRANSLATOR.translate("SpellCheck(MenuItem)")); // NOI18N
        spellcheckMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                spellcheckMenuItemActionPerformed(evt);
            }
        });
        jMenu4.add(spellcheckMenuItem);

        jMenuBar1.add(jMenu4);

        jMenu5.setMnemonic('h');
        jMenu5.setText(TRANSLATOR.translate("Help(Menu)")); // NOI18N

        helpContentsMenuItem.setIcon(new ImageIcon(getClass().getResource("/icons/16x16/help2.png"))); // NOI18N
        helpContentsMenuItem.setText(TRANSLATOR.translate("HelpContents(MenuItem)")); // NOI18N
        jMenu5.add(helpContentsMenuItem);

        aboutMenuItem.setIcon(new ImageIcon(getClass().getResource("/icons/16x16/about.png"))); // NOI18N
        aboutMenuItem.setMnemonic('a');
        aboutMenuItem.setText(TRANSLATOR.translate("HelpAbout(MenuItem)")); // NOI18N
        aboutMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });
        jMenu5.add(aboutMenuItem);

        jMenuBar1.add(jMenu5);

        setJMenuBar(jMenuBar1);
    }

    private void initToolBar() {

        JToolBar mainToolBar = new JToolBar();
        backButton = new JButton();
        forwardButton = new JButton();
        clearButton = new JButton();
        statusButton = new JButton();
        dictionaryButton = new JButton();
        JToolBar.Separator jSeparator3 = new JToolBar.Separator();
        JButton addWordButton = new JButton();
        updateWordButton = new JButton();
        deleteWordButton = new JButton();
        JToolBar.Separator jSeparator4 = new JToolBar.Separator();
        cutButton = new JButton(new DefaultEditorKit.CutAction());
        cutButton.setText(null);
        copyButton = new JButton(new DefaultEditorKit.CopyAction());
        copyButton.setText(null);
        pasteButton = new JButton(new DefaultEditorKit.PasteAction());
        pasteButton.setText(null);
        JToolBar.Separator jSeparator5 = new JToolBar.Separator();
        JButton studyButton = new JButton();
        JButton examButton = new JButton();
        JButton spellcheckButton = new JButton();
        lastToolbarSeparator = new JToolBar.Separator();
        memoryButton = new JButton();

        mainToolBar.setFloatable(false);
        mainToolBar.setRollover(true);

        backButton.setIcon(new ImageIcon(getClass().getResource("/icons/24x24/arrow_left_blue.png"))); // NOI18N
        backButton.setToolTipText(TRANSLATOR.translate("PreviousWord(Label)")); // NOI18N
        backButton.setFocusable(false);
        backButton.setHorizontalTextPosition(SwingConstants.CENTER);
        backButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                backButtonActionPerformed(evt);
            }
        });
        mainToolBar.add(backButton);

        forwardButton.setIcon(new ImageIcon(getClass().getResource("/icons/24x24/arrow_right_blue.png"))); // NOI18N
        forwardButton.setToolTipText(TRANSLATOR.translate("NextWord(Label)")); // NOI18N
        forwardButton.setFocusable(false);
        forwardButton.setHorizontalTextPosition(SwingConstants.CENTER);
        forwardButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        forwardButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                forwardButtonActionPerformed(evt);
            }
        });
        mainToolBar.add(forwardButton);

        clearButton.setIcon(new ImageIcon(getClass().getResource("/icons/24x24/eraser.png"))); // NOI18N
        clearButton.setToolTipText(TRANSLATOR.translate("ClearButton(ToolTip)")); // NOI18N
        clearButton.setFocusable(false);
        clearButton.setHorizontalTextPosition(SwingConstants.CENTER);
        clearButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });
        mainToolBar.add(clearButton);

        statusButton.setIcon(new ImageIcon(getClass().getResource("/icons/24x24/bell2_grey.png"))); // NOI18N
        statusButton.setFocusable(false);
        statusButton.setHorizontalTextPosition(SwingConstants.CENTER);
        statusButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        statusButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                statusButtonActionPerformed(evt);
            }
        });
        mainToolBar.add(statusButton);

        dictionaryButton.setIcon(new ImageIcon(getClass().getResource("/icons/24x24/en-bg.png"))); // NOI18N
        dictionaryButton.setFocusable(false);
        dictionaryButton.setHorizontalTextPosition(SwingConstants.CENTER);
        dictionaryButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        dictionaryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                dictionaryButtonActionPerformed(evt);
            }
        });
        mainToolBar.add(dictionaryButton);
        mainToolBar.add(jSeparator3);

        addWordButton.setIcon(new ImageIcon(getClass().getResource("/icons/24x24/add2.png"))); // NOI18N
        addWordButton.setToolTipText(TRANSLATOR.translate("EditAddWord(MenuItem)")); // NOI18N
        addWordButton.setFocusable(false);
        addWordButton.setHorizontalTextPosition(SwingConstants.CENTER);
        addWordButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        addWordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                addWordButtonActionPerformed(evt);
            }
        });
        mainToolBar.add(addWordButton);

        updateWordButton.setIcon(new ImageIcon(getClass().getResource("/icons/24x24/edit.png"))); // NOI18N
        updateWordButton.setToolTipText(TRANSLATOR.translate("EditUpdateWord(MenuItem)")); // NOI18N
        updateWordButton.setFocusable(false);
        updateWordButton.setHorizontalTextPosition(SwingConstants.CENTER);
        updateWordButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        updateWordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                updateWordButtonActionPerformed(evt);
            }
        });
        mainToolBar.add(updateWordButton);

        deleteWordButton.setIcon(new ImageIcon(getClass().getResource("/icons/24x24/delete2.png"))); // NOI18N
        deleteWordButton.setToolTipText(TRANSLATOR.translate("EditDeleteWord(MenuItem)")); // NOI18N
        deleteWordButton.setFocusable(false);
        deleteWordButton.setHorizontalTextPosition(SwingConstants.CENTER);
        deleteWordButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        deleteWordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                deleteWordButtonActionPerformed(evt);
            }
        });
        mainToolBar.add(deleteWordButton);
        mainToolBar.add(jSeparator4);

        cutButton.setIcon(new ImageIcon(getClass().getResource("/icons/24x24/cut.png"))); // NOI18N
        cutButton.setToolTipText(TRANSLATOR.translate("EditCut(MenuItem)")); // NOI18N
        cutButton.setFocusable(false);
        cutButton.setHorizontalTextPosition(SwingConstants.CENTER);
        cutButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        mainToolBar.add(cutButton);

        copyButton.setIcon(new ImageIcon(getClass().getResource("/icons/24x24/copy.png"))); // NOI18N
        copyButton.setToolTipText(TRANSLATOR.translate("EditCopy(MenuItem)")); // NOI18N
        copyButton.setFocusable(false);
        copyButton.setHorizontalTextPosition(SwingConstants.CENTER);
        copyButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        mainToolBar.add(copyButton);

        pasteButton.setIcon(new ImageIcon(getClass().getResource("/icons/24x24/paste.png"))); // NOI18N
        pasteButton.setToolTipText(TRANSLATOR.translate("EditPaste(MenuItem)")); // NOI18N
        pasteButton.setFocusable(false);
        pasteButton.setHorizontalTextPosition(SwingConstants.CENTER);
        pasteButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        mainToolBar.add(pasteButton);
        mainToolBar.add(jSeparator5);

        studyButton.setIcon(new ImageIcon(getClass().getResource("/icons/24x24/teacher.png"))); // NOI18N
        studyButton.setToolTipText(TRANSLATOR.translate("StudyWords(MenuItem)")); // NOI18N
        studyButton.setFocusable(false);
        studyButton.setHorizontalTextPosition(SwingConstants.CENTER);
        studyButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        studyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                studyButtonActionPerformed(evt);
            }
        });
        mainToolBar.add(studyButton);

        examButton.setIcon(new ImageIcon(getClass().getResource("/icons/24x24/blackboard.png"))); // NOI18N
        examButton.setToolTipText(TRANSLATOR.translate("Exam(MenuItem)")); // NOI18N
        examButton.setFocusable(false);
        examButton.setHorizontalTextPosition(SwingConstants.CENTER);
        examButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        examButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                examButtonActionPerformed(evt);
            }
        });
        mainToolBar.add(examButton);

        spellcheckButton.setIcon(new ImageIcon(getClass().getResource("/icons/24x24/spellcheck.png"))); // NOI18N
        spellcheckButton.setToolTipText(TRANSLATOR.translate("SpellCheck(MenuItem)")); // NOI18N
        spellcheckButton.setFocusable(false);
        spellcheckButton.setHorizontalTextPosition(SwingConstants.CENTER);
        spellcheckButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        spellcheckButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                spellcheckButtonActionPerformed(evt);
            }
        });
        mainToolBar.add(spellcheckButton);
        mainToolBar.add(lastToolbarSeparator);

        memoryButton.setIcon(new ImageIcon(getClass().getResource("/icons/24x24/memory.png"))); // NOI18N
        memoryButton.setFocusable(false);
        memoryButton.setHorizontalTextPosition(SwingConstants.CENTER);
        memoryButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        memoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                memoryButtonActionPerformed(evt);
            }
        });
        mainToolBar.add(memoryButton);

        topPanel.add(mainToolBar, "north, growx");
    }

    private void exitMenuItemActionPerformed(ActionEvent evt) {
        saveFrameState();

        System.exit(0);
    }

    private void prefsMenuItemActionPerformed(ActionEvent evt) {
        PreferencesDialog preferencesDialog = new PreferencesDialog(this, true);

        // tray options should be disabled is the tray is not supported
        if (trayIcon == null) {
            preferencesDialog.disableTrayOptions();
        }

        preferencesDialog.setLocationRelativeTo(this);

        PreferencesExtractor.extract(this, preferencesDialog);
    }

    private void aboutMenuItemActionPerformed(ActionEvent evt) {
        AboutDialog aboutDialog = new AboutDialog(this, true);
        aboutDialog.setLocationRelativeTo(this);
        aboutDialog.setVisible(true);
    }

    private void spellcheckMenuItemActionPerformed(ActionEvent evt) {
        SpellCheckFrame.getInstance().setVisible(true);
    }

    private void restartMenuItemActionPerformed(ActionEvent evt) {
        restart();
    }

    private void examMenuItemActionPerformed(ActionEvent evt) {
        ExamDialog examDialog = new ExamDialog(this, true);
        examDialog.showExamDialog();
    }

    private void addWordMenuItemActionPerformed(ActionEvent evt) {
        addWordDefinition();
    }

    private void updateWordMenuItemActionPerformed(ActionEvent evt) {
        updateWordDefinition();
    }

    private void wordSearchFieldActionPerformed(ActionEvent evt) {
        onWordSearchFieldAction();
    }

    private void wordsListValueChanged(ListSelectionEvent evt) {
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
    }

    private void StudyWordsMenuItemActionPerformed(ActionEvent evt) {
        StudyWordsDialog studyWords = new StudyWordsDialog(this, true);
        studyWords.setLocationRelativeTo(this);
        studyWords.showLearningWordsDialog();
    }

    private void wordsListMouseClicked(MouseEvent evt) {
        if (evt.getClickCount() == 2) {
            updateWordDefinition();
        }
    }

    private void backButtonActionPerformed(ActionEvent evt) {
        if (searchedWords.size() > 0 && searchWordsIndex >= 1) {
            wordSearchField.setText(searchedWords.get(--searchWordsIndex));

            updateHistoryButtonsState();
        }
    }

    private void forwardButtonActionPerformed(ActionEvent evt) {
        if (searchedWords.size() - 1 > searchWordsIndex) {
            wordSearchField.setText(searchedWords.get(++searchWordsIndex));

            updateHistoryButtonsState();
        }
    }

    private void statusButtonActionPerformed(ActionEvent evt) {
        onWordSearchFieldAction();
    }

    private void memoryButtonActionPerformed(ActionEvent evt) {
        System.gc();
    }

    private void dictionaryButtonActionPerformed(ActionEvent evt) {
        if (dictionaryService.isComplemented(selectedDictionary)) {
            selectDictionary(dictionaryService.getComplement(selectedDictionary), true);
        }
    }

    private void studyButtonActionPerformed(ActionEvent evt) {
        StudyWordsDialog studyWords = new StudyWordsDialog(this, true);
        studyWords.setLocationRelativeTo(this);
        studyWords.showLearningWordsDialog();
    }

    private void examButtonActionPerformed(ActionEvent evt) {
        ExamDialog examDialog = new ExamDialog(this, true);
        examDialog.showExamDialog();
    }

    private void spellcheckButtonActionPerformed(ActionEvent evt) {
        SpellCheckFrame.getInstance().setVisible(true);
    }

    private void addWordButtonActionPerformed(ActionEvent evt) {
        addWordDefinition();
    }

    private void updateWordButtonActionPerformed(ActionEvent evt) {
        updateWordDefinition();
    }

    private void deleteWordButtonActionPerformed(ActionEvent evt) {
        deleteWordDefinition();
        throw new NullPointerException("test");
    }

    private void clearButtonActionPerformed(ActionEvent evt) {
        clear();
    }

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
