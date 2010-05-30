package com.drowltd.spellbook.ui.desktop;

import com.drowltd.spellbook.core.exception.SpellCheckerException;
import com.drowltd.spellbook.core.i18n.Translator;
import com.drowltd.spellbook.core.model.Dictionary;
import com.drowltd.spellbook.core.model.Language;
import com.drowltd.spellbook.core.preferences.PreferencesManager;
import com.drowltd.spellbook.core.service.DictionaryService;
import com.drowltd.spellbook.ui.desktop.exam.ExamDialog;
import com.drowltd.spellbook.ui.desktop.spellcheck.HeapSizeException;
import com.drowltd.spellbook.ui.desktop.spellcheck.SpellCheckFrame;
import com.drowltd.spellbook.ui.desktop.study.StudyWordsDialog;
import com.drowltd.spellbook.ui.swing.component.WordOfTheDayDialog;
import com.drowltd.spellbook.ui.swing.model.ListBackedListModel;
import com.drowltd.spellbook.ui.swing.util.IconManager;
import com.drowltd.spellbook.ui.swing.util.IconManager.IconSize;
import com.drowltd.spellbook.ui.swing.util.SwingUtil;
import com.drowltd.spellbook.util.SearchUtils;
import com.jidesoft.dialog.StandardDialog;
import com.jidesoft.hints.ListDataIntelliHints;
import com.jidesoft.swing.JideSplitButton;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.DefaultEditorKit;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.Toolkit;
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
    private JideSplitButton dictionaryButton;
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
    private boolean initialized = false;
    private static final int DEFAULT_FONT_SIZE = 14;
    private static final int DIVIDER_LOCATION = 180;
    private static final int MIN_FRAME_WIDTH = 640;
    private static final int MIN_FRAME_HEIGHT = 200;

    public SpellbookFrame(boolean dbPresent) {
        setMinimumSize(new Dimension(MIN_FRAME_WIDTH, MIN_FRAME_HEIGHT));

        if (PM.getBoolean(Preference.CLOSE_TO_TRAY, false)) {
            LOGGER.info("Minimize to tray on close is enabled");
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        } else {
            LOGGER.info("Minimize to tray on close is disabled");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }

        setAlwaysOnTop(PM.getBoolean(Preference.ALWAYS_ON_TOP, false));

        //set the frame title
        setTitle(TRANSLATOR.translate("ApplicationName(Title)"));

        //set the frame icon
        setIconImage(IconManager.getImageIcon("dictionary.png", IconSize.SIZE16).getImage());

        //create tray
        trayIcon = SpellbookTray.createTraySection(this);

        // implemented a very nasty clipboard ownership hack to simulate notifications
        clipboardIntegration = ClipboardIntegration.getInstance(this);

        if (dbPresent) {
            init();
        }
    }

    public void init() {
        TRANSLATOR.reset();

        DictionaryService.init("~/.spellbook/db/spellbook.data.db");

        dictionaryService = DictionaryService.getInstance();

        // the first invocation of a db related method is special - we have to check
        // if another process is not using the connection already
        try {
            if (dictionaryService.getDictionaries().size() == 0) {
                JOptionPane.showMessageDialog(null, TRANSLATOR.translate("NoDictionaries(Message)"), TRANSLATOR.translate("Error(Title)"), JOptionPane.WARNING_MESSAGE);

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

        initComponents();

        initDictionaries();

        addListeners();

        // restore last size and position of the frame
        if (PM.getDouble(Preference.FRAME_X, 0.0) > 0) {
            double x = PM.getDouble(Preference.FRAME_X, 0.0);
            double y = PM.getDouble(Preference.FRAME_Y, 0.0);
            double width = PM.getDouble(Preference.FRAME_WIDTH, 0.0);
            double height = PM.getDouble(Preference.FRAME_HEIGHT, 0.0);

            setBounds((int) x, (int) y, (int) width, (int) height);
        } else {
            //or dynamically determine an adequate frame size
            Toolkit toolkit = Toolkit.getDefaultToolkit();

            Dimension screenSize = toolkit.getScreenSize();

            setSize(screenSize.width / 2, screenSize.height / 2);
            // center on screen
            setLocationRelativeTo(null);
        }

        initialized = true;
    }

    public void showWordOfTheDay() {
        if (PM.getBoolean(Preference.WORD_OF_THE_DAY, true)) {
            WordOfTheDayDialog wordOfTheDayDialog = new WordOfTheDayDialog(words, selectedDictionary);
            wordOfTheDayDialog.setLocationRelativeTo(this);
            wordOfTheDayDialog.setVisible(true);
        }
    }

    public boolean isInitialized() {
        return initialized;
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
        addUpdateWordDialog.setWhetherAddWord(true);
        addUpdateWordDialog.setLocationRelativeTo(this);
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

        // ignore empty text and urls
        if (!transferredText.isEmpty()
                && !transferredText.startsWith("http://")
                && !transferredText.startsWith("mailto:")) {
            LOGGER.info("'" + transferredText + "' received from clipboard");
            String searchString = transferredText.split("\\s")[0].toLowerCase();
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

            // the tray popup translation should appear is the main frame is not active and not always on top
            if ((trayIcon != null) && match && !SpellbookFrame.this.isActive() && !SpellbookFrame.this.isAlwaysOnTop()
                    && PM.getBoolean(Preference.TRAY_POPUP, true)) {
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
                wordTranslationTextPane.setFont(new Font("Arial", Font.PLAIN, DEFAULT_FONT_SIZE));
            }
        } else {
            String fontName = PM.get(Preference.FONT_NAME, "SansSerif");
            int fontSize = PM.getInt(Preference.FONT_SIZE, DEFAULT_FONT_SIZE);
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

    private void updateWordDefinition() throws IllegalStateException {
        AddUpdateWordDialog addUpdateWordDialog = new AddUpdateWordDialog(this, true);
        addUpdateWordDialog.setDictionary(selectedDictionary);

        if (wordsList.isSelectionEmpty()) {
            throw new IllegalStateException("No word selected");
        }

        final String originalWord = (String) wordsList.getSelectedValue();
        addUpdateWordDialog.setWord(originalWord);
        addUpdateWordDialog.setTranslation(dictionaryService.getTranslation(originalWord, selectedDictionary));
        addUpdateWordDialog.setLocationRelativeTo(this);
        addUpdateWordDialog.setVisible(true);

        if (addUpdateWordDialog.getDialogResult() == StandardDialog.RESULT_AFFIRMED) {
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
            dictionaryButton.setToolTipText(String.format(TRANSLATOR.translate("EnBgDictSize(Label)"), words.size()));
            dictionaryButton.setIcon(IconManager.getImageIcon(dictionary.getIconName(), IconManager.IconSize.SIZE24));
        } else if (dictionary.getName().equals("Bulgarian-English")) {
            dictionaryButton.setToolTipText(String.format(TRANSLATOR.translate("BgEnDictSize(Label)"), words.size()));
            dictionaryButton.setIcon(IconManager.getImageIcon(dictionary.getIconName(), IconManager.IconSize.SIZE24));
        } else {
            throw new IllegalArgumentException("Unknown dictionary " + dictionary);
        }
    }

    public void setSelectedFont(Font font) {
        wordSearchField.setFont(font);
        wordsList.setFont(font);
        wordTranslationTextPane.setFont(font);
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

        splitPane.setBorder(null);
        splitPane.setDividerLocation(DIVIDER_LOCATION);

        topPanel.add(splitPane, "grow");

        wordsList.setModel(new ListBackedListModel(words));
        wordsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        wordsList.setToolTipText(TRANSLATOR.translate("WordsList(ToolTip)"));
        wordsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    updateWordDefinition();
                }
            }
        });
        wordsList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent evt) {
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
        });

        wordSearchField.setToolTipText(TRANSLATOR.translate("WordSearch(ToolTip)"));
        wordSearchField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                onWordSearchFieldAction();
            }
        });

        JPanel searchPanel = new JPanel(new MigLayout("wrap 1", "[grow]", "[][grow]"));

        searchPanel.add(wordSearchField, "growx");
        searchPanel.add(new JScrollPane(wordsList), "grow");

        splitPane.setLeftComponent(searchPanel);

        JPanel translationPanel = new JPanel(new MigLayout("wrap 1", "[grow]", "[grow]"));
        wordTranslationTextPane = new JTextPane();

        wordTranslationTextPane.setContentType("text/html");
        wordTranslationTextPane.setEditable(false);

        translationPanel.add(new JScrollPane(wordTranslationTextPane), "grow");

        splitPane.setRightComponent(translationPanel);

        initToolBar();

        initMenuBar();

        // restore the divider location from the last session
        splitPane.setDividerLocation(PM.getInt(Preference.DIVIDER_LOCATION, DIVIDER_LOCATION));

        updateDictionaryButton(selectedDictionary);

        // setup intellihints for words search field
        ListDataIntelliHints<String> intelliHints = new ListDataIntelliHints<String>(wordSearchField, searchedWords);
        intelliHints.setCaseSensitive(false);

        setDefaultFont();

        if (PM.getBoolean(Preference.SHOW_MEMORY_USAGE, false)) {
            showMemoryUsage();
        } else {
            hideMemoryUsage();
        }

        pack();
    }

    private void initMenuBar() {
        JMenuBar spellbookMenuBar = new JMenuBar();

        // build the file menu
        JMenu fileMenu = new JMenu();
        JMenuItem restartMenuItem = new JMenuItem();
        JMenuItem exitMenuItem = new JMenuItem();

        fileMenu.setMnemonic('f');
        fileMenu.setText(TRANSLATOR.translate("File(Menu)"));

        restartMenuItem.setIcon(IconManager.getMenuIcon("refresh.png"));
        restartMenuItem.setMnemonic('r');
        restartMenuItem.setText(TRANSLATOR.translate("FileRestart(MenuItem)"));
        restartMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                restart();
            }
        });
        fileMenu.add(restartMenuItem);

        exitMenuItem.setIcon(IconManager.getMenuIcon("exit.png"));
        exitMenuItem.setMnemonic('x');
        exitMenuItem.setText(TRANSLATOR.translate("FileExit(MenuItem)"));
        exitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                saveFrameState();

                System.exit(0);
            }
        });
        fileMenu.add(exitMenuItem);

        spellbookMenuBar.add(fileMenu);

        // build the edit menu
        JMenu editMenu = new JMenu();
        JMenuItem addWordMenuItem = new JMenuItem();
        updateWordMenuItem = new JMenuItem();
        deleteWordMenuItem = new JMenuItem();
        cutMenuItem = new JMenuItem(new DefaultEditorKit.CutAction());
        copyMenuItem = new JMenuItem(new DefaultEditorKit.CopyAction());
        pasteMenuItem = new JMenuItem(new DefaultEditorKit.PasteAction());
        JMenuItem prefsMenuItem = new JMenuItem();

        editMenu.setMnemonic('e');
        editMenu.setText(TRANSLATOR.translate("Edit(Menu)"));

        addWordMenuItem.setIcon(IconManager.getMenuIcon("add2.png"));
        addWordMenuItem.setText(TRANSLATOR.translate("EditAddWord(MenuItem)"));
        addWordMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                addWordDefinition();
            }
        });
        editMenu.add(addWordMenuItem);

        updateWordMenuItem.setIcon(IconManager.getMenuIcon("edit.png"));
        updateWordMenuItem.setText(TRANSLATOR.translate("EditUpdateWord(MenuItem)"));
        updateWordMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                updateWordDefinition();
            }
        });
        editMenu.add(updateWordMenuItem);

        deleteWordMenuItem.setIcon(IconManager.getMenuIcon("delete2.png"));
        deleteWordMenuItem.setText(TRANSLATOR.translate("EditDeleteWord(MenuItem)"));
        editMenu.add(deleteWordMenuItem);
        editMenu.add(new JPopupMenu.Separator());

        cutMenuItem.setIcon(IconManager.getMenuIcon("cut.png"));
        cutMenuItem.setMnemonic('t');
        cutMenuItem.setText(TRANSLATOR.translate("EditCut(MenuItem)"));
        editMenu.add(cutMenuItem);

        copyMenuItem.setIcon(IconManager.getMenuIcon("copy.png"));
        copyMenuItem.setMnemonic('c');
        copyMenuItem.setText(TRANSLATOR.translate("EditCopy(MenuItem)"));
        editMenu.add(copyMenuItem);

        pasteMenuItem.setIcon(IconManager.getMenuIcon("paste.png"));
        pasteMenuItem.setMnemonic('p');
        pasteMenuItem.setText(TRANSLATOR.translate("EditPaste(MenuItem)"));
        editMenu.add(pasteMenuItem);
        editMenu.add(new JPopupMenu.Separator());

        prefsMenuItem.setIcon(IconManager.getMenuIcon("preferences.png"));
        prefsMenuItem.setMnemonic('e');
        prefsMenuItem.setText(TRANSLATOR.translate("EditPreferences(MenuItem)"));
        prefsMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                showPreferencesDialog();
            }
        });
        editMenu.add(prefsMenuItem);

        spellbookMenuBar.add(editMenu);

        // build dictionary menu
        dictionaryMenu = new JMenu();

        dictionaryMenu.setMnemonic('d');
        dictionaryMenu.setText(TRANSLATOR.translate("Dictionaries(Menu)"));
        spellbookMenuBar.add(dictionaryMenu);

        // build tools menu
        JMenu toolMenu = new JMenu();
        JMenuItem studyWordsMenuItem = new JMenuItem();
        JMenuItem examMenuItem = new JMenuItem();
        JMenuItem spellcheckMenuItem = new JMenuItem();

        toolMenu.setMnemonic('t');
        toolMenu.setText(TRANSLATOR.translate("Tools(Menu)"));

        studyWordsMenuItem.setIcon(IconManager.getMenuIcon("teacher.png"));
        studyWordsMenuItem.setMnemonic('w');
        studyWordsMenuItem.setText(TRANSLATOR.translate("StudyWords(MenuItem)"));
        studyWordsMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                showStudyDialog();
            }
        });
        toolMenu.add(studyWordsMenuItem);

        examMenuItem.setIcon(IconManager.getMenuIcon("blackboard.png"));
        examMenuItem.setMnemonic('e');
        examMenuItem.setText(TRANSLATOR.translate("Exam(MenuItem)"));
        examMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                showExamDialog();
            }
        });
        toolMenu.add(examMenuItem);

        spellcheckMenuItem.setIcon(IconManager.getMenuIcon("spellcheck.png"));
        spellcheckMenuItem.setMnemonic('s');
        spellcheckMenuItem.setText(TRANSLATOR.translate("SpellCheck(MenuItem)"));
        spellcheckMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                showSpellChecker();
            }
        });
        toolMenu.add(spellcheckMenuItem);

        spellbookMenuBar.add(toolMenu);

        // build help menu
        JMenu helpMenu = new JMenu();
        JMenuItem helpContentsMenuItem = new JMenuItem();
        JMenuItem aboutMenuItem = new JMenuItem();

        helpMenu.setMnemonic('h');
        helpMenu.setText(TRANSLATOR.translate("Help(Menu)"));

        helpContentsMenuItem.setIcon(IconManager.getMenuIcon("help2.png"));
        helpContentsMenuItem.setText(TRANSLATOR.translate("HelpContents(MenuItem)"));
        helpMenu.add(helpContentsMenuItem);

        aboutMenuItem.setIcon(IconManager.getMenuIcon("about.png"));
        aboutMenuItem.setMnemonic('a');
        aboutMenuItem.setText(TRANSLATOR.translate("HelpAbout(MenuItem)"));
        aboutMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                showAboutDialog();
            }
        });
        helpMenu.add(aboutMenuItem);

        spellbookMenuBar.add(helpMenu);

        cutMenuItem.setEnabled(false);
        // update word menu item is initially disabled
        updateWordMenuItem.setEnabled(false);
        deleteWordMenuItem.setEnabled(false);

        setJMenuBar(spellbookMenuBar);
    }

    private void showSpellChecker() {
        try {

            SpellCheckFrame.getInstance(this).setVisible(true);

        } catch (HeapSizeException e) {
            JOptionPane.showMessageDialog(this, TRANSLATOR.translate("HeapLimit(Message)"),
                    TRANSLATOR.translate("HeapLimit(Title)"), JOptionPane.ERROR_MESSAGE);
        } catch (SpellCheckerException e) {
            JOptionPane.showMessageDialog(this, TRANSLATOR.translate("SpellCheckerException(Message)"),
                    TRANSLATOR.translate("SpellCheckerException(Title)"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initToolBar() {

        JToolBar mainToolBar = new JToolBar();
        backButton = new JButton();
        forwardButton = new JButton();
        clearButton = new JButton();
        statusButton = new JButton();
        dictionaryButton = new JideSplitButton();
        dictionaryButton.setButtonStyle(JideSplitButton.TOOLBAR_STYLE);
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
        JButton studyButton = new JButton();
        JButton examButton = new JButton();
        JButton spellcheckButton = new JButton();
        lastToolbarSeparator = new JToolBar.Separator();
        memoryButton = new JButton();

        mainToolBar.setFloatable(false);
        mainToolBar.setRollover(true);

        backButton.setIcon(IconManager.getImageIcon("arrow_left_blue.png", IconSize.SIZE24));
        backButton.setToolTipText(TRANSLATOR.translate("PreviousWord(Label)"));
        backButton.setFocusable(false);
        backButton.setHorizontalTextPosition(SwingConstants.CENTER);
        backButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if (searchedWords.size() > 0 && searchWordsIndex >= 1) {
                    wordSearchField.setText(searchedWords.get(--searchWordsIndex));

                    updateHistoryButtonsState();
                }
            }
        });
        mainToolBar.add(backButton);

        forwardButton.setIcon(IconManager.getImageIcon("arrow_right_blue.png", IconSize.SIZE24));
        forwardButton.setToolTipText(TRANSLATOR.translate("NextWord(Label)"));
        forwardButton.setFocusable(false);
        forwardButton.setHorizontalTextPosition(SwingConstants.CENTER);
        forwardButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        forwardButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if (searchedWords.size() - 1 > searchWordsIndex) {
                    wordSearchField.setText(searchedWords.get(++searchWordsIndex));

                    updateHistoryButtonsState();
                }
            }
        });
        mainToolBar.add(forwardButton);

        clearButton.setIcon(IconManager.getImageIcon("eraser.png", IconSize.SIZE24));
        clearButton.setToolTipText(TRANSLATOR.translate("ClearButton(ToolTip)"));
        clearButton.setFocusable(false);
        clearButton.setHorizontalTextPosition(SwingConstants.CENTER);
        clearButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                clear();
            }
        });
        mainToolBar.add(clearButton);

        statusButton.setIcon(IconManager.getImageIcon("bell2_grey.png", IconSize.SIZE24));
        statusButton.setFocusable(false);
        statusButton.setHorizontalTextPosition(SwingConstants.CENTER);
        statusButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        statusButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                onWordSearchFieldAction();
            }
        });
        mainToolBar.add(statusButton);

        dictionaryButton.setIcon(IconManager.getImageIcon("en-bg.png", IconSize.SIZE24));
        dictionaryButton.setFocusable(false);
        dictionaryButton.setHorizontalTextPosition(SwingConstants.CENTER);
        dictionaryButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        dictionaryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if (dictionaryService.isComplemented(selectedDictionary)) {
                    selectDictionary(dictionaryService.getComplement(selectedDictionary), true);
                }
            }
        });

        final List<Dictionary> availableDictionaries = dictionaryService.getDictionaries();
        for (Dictionary dictionary : availableDictionaries) {
            dictionaryButton.add(new DictionaryItem(dictionary));
        }

        mainToolBar.add(dictionaryButton);
        mainToolBar.add(new JToolBar.Separator());

        addWordButton.setIcon(IconManager.getImageIcon("add2.png", IconSize.SIZE24));
        addWordButton.setToolTipText(TRANSLATOR.translate("EditAddWord(MenuItem)"));
        addWordButton.setFocusable(false);
        addWordButton.setHorizontalTextPosition(SwingConstants.CENTER);
        addWordButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        addWordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                addWordDefinition();
            }
        });
        mainToolBar.add(addWordButton);

        updateWordButton.setIcon(IconManager.getImageIcon("edit.png", IconSize.SIZE24));
        updateWordButton.setToolTipText(TRANSLATOR.translate("EditUpdateWord(MenuItem)"));
        updateWordButton.setFocusable(false);
        updateWordButton.setHorizontalTextPosition(SwingConstants.CENTER);
        updateWordButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        updateWordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                updateWordDefinition();
            }
        });
        mainToolBar.add(updateWordButton);

        deleteWordButton.setIcon(IconManager.getImageIcon("delete2.png", IconSize.SIZE24));
        deleteWordButton.setToolTipText(TRANSLATOR.translate("EditDeleteWord(MenuItem)"));
        deleteWordButton.setFocusable(false);
        deleteWordButton.setHorizontalTextPosition(SwingConstants.CENTER);
        deleteWordButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        deleteWordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                deleteWordDefinition();
            }
        });
        mainToolBar.add(deleteWordButton);
        mainToolBar.add(jSeparator4);

        cutButton.setIcon(IconManager.getImageIcon("cut.png", IconSize.SIZE24));
        cutButton.setToolTipText(TRANSLATOR.translate("EditCut(MenuItem)"));
        cutButton.setFocusable(false);
        cutButton.setHorizontalTextPosition(SwingConstants.CENTER);
        cutButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        mainToolBar.add(cutButton);

        copyButton.setIcon(IconManager.getImageIcon("copy.png", IconSize.SIZE24));
        copyButton.setToolTipText(TRANSLATOR.translate("EditCopy(MenuItem)"));
        copyButton.setFocusable(false);
        copyButton.setHorizontalTextPosition(SwingConstants.CENTER);
        copyButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        mainToolBar.add(copyButton);

        pasteButton.setIcon(IconManager.getImageIcon("paste.png", IconSize.SIZE24));
        pasteButton.setToolTipText(TRANSLATOR.translate("EditPaste(MenuItem)"));
        pasteButton.setFocusable(false);
        pasteButton.setHorizontalTextPosition(SwingConstants.CENTER);
        pasteButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        mainToolBar.add(pasteButton);
        mainToolBar.add(new JToolBar.Separator());

        studyButton.setIcon(IconManager.getImageIcon("teacher.png", IconSize.SIZE24));
        studyButton.setToolTipText(TRANSLATOR.translate("StudyWords(MenuItem)"));
        studyButton.setFocusable(false);
        studyButton.setHorizontalTextPosition(SwingConstants.CENTER);
        studyButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        studyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                showStudyDialog();
            }
        });
        mainToolBar.add(studyButton);

        examButton.setIcon(IconManager.getImageIcon("blackboard.png", IconSize.SIZE24));
        examButton.setToolTipText(TRANSLATOR.translate("Exam(MenuItem)"));
        examButton.setFocusable(false);
        examButton.setHorizontalTextPosition(SwingConstants.CENTER);
        examButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        examButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                showExamDialog();
            }
        });
        mainToolBar.add(examButton);

        spellcheckButton.setIcon(IconManager.getImageIcon("spellcheck.png", IconSize.SIZE24));
        spellcheckButton.setToolTipText(TRANSLATOR.translate("SpellCheck(MenuItem)"));
        spellcheckButton.setFocusable(false);
        spellcheckButton.setHorizontalTextPosition(SwingConstants.CENTER);
        spellcheckButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        spellcheckButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                showSpellChecker();
            }
        });
        mainToolBar.add(spellcheckButton);
        mainToolBar.add(lastToolbarSeparator);

        memoryButton.setIcon(IconManager.getImageIcon("memory.png", IconSize.SIZE24));
        memoryButton.setFocusable(false);
        memoryButton.setHorizontalTextPosition(SwingConstants.CENTER);
        memoryButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        memoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                System.gc();
            }
        });
        mainToolBar.add(memoryButton);

        // history buttons should be disabled initially
        forwardButton.setEnabled(false);
        backButton.setEnabled(false);

        // clear button is disabled initially
        clearButton.setEnabled(false);

        cutButton.setEnabled(false);
        copyButton.setEnabled(false);
        copyButton.setEnabled(false);

        updateWordButton.setEnabled(false);
        deleteWordButton.setEnabled(false);

        topPanel.add(mainToolBar, "north, growx");
    }

    private void showPreferencesDialog() {
        PreferencesDialog preferencesDialog = new PreferencesDialog(this, true);

        // tray options should be disabled is the tray is not supported
        if (trayIcon == null) {
            preferencesDialog.disableTrayOptions();
        }

        preferencesDialog.setLocationRelativeTo(this);

        PreferencesExtractor.extract(this, preferencesDialog);
    }

    private void showAboutDialog() {
        AboutDialog aboutDialog = new AboutDialog(this, true);
        aboutDialog.setLocationRelativeTo(this);
        aboutDialog.setVisible(true);
    }

    private void showStudyDialog() {
        StudyWordsDialog studyWords = new StudyWordsDialog(this, true);
        studyWords.setLocationRelativeTo(this);
        studyWords.showLearningWordsDialog();
    }

    private void showExamDialog() {
        ExamDialog examDialog = new ExamDialog(this, true);
        examDialog.showExamDialog();
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

        // safeguard against no selection
        if (selectedIndex < 0) {
            return;
        }

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
