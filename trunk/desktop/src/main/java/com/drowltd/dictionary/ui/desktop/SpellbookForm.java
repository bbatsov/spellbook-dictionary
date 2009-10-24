package com.drowltd.dictionary.ui.desktop;

import com.drowltd.dictionary.core.db.DictDb;
import com.drowltd.dictionary.core.exception.DictionaryDbLockedException;
import com.drowltd.dictionary.core.i18n.Translator;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

/**
 * User: bozhidar
 * Date: Sep 5, 2009
 * Time: 12:52:27 PM
 */
public class SpellbookForm {
    private static final Translator TRANSLATOR = new Translator("DesktopUI");

    private static final Logger LOGGER = LoggerFactory.getLogger(SpellbookForm.class);

    private JTextField wordSearchField;
    private JPanel topPanel;
    private JButton clearButton;
    private JList wordsList;
    private JTextArea wordTranslationTextArea;
    private JLabel statusBar;
    private JLabel drowLabel;
    private JLabel matchLabel;

    private DictDb dictDb;
    private List<String> words;
    private ClipboardTextTransfer clipboardTextTransfer;
    private String lastTransfer;
    private ScheduledExecutorService clipboardExecutorService;

    public SpellbookForm() {
        Preferences prefs = Preferences.userNodeForPackage(this.getClass());

        while (true) {
            if (verifyDbPresence(prefs)) {
                break;
            }
        }

        try {
            DictDb.init(prefs.get("PATH_TO_DB", ""));
        } catch (DictionaryDbLockedException e) {
            JOptionPane.showMessageDialog(topPanel, "Another instance of Spellbook is already running");
            System.exit(0);
        }

        dictDb = DictDb.getInstance();

        words = dictDb.getWordsFromSelectedDictionary();

        wordsList.setListData(words.toArray());

        statusBar.setText(String.format(TRANSLATOR.translate("EnBgDictSize(Label)"), words.size()));

        wordsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clear();
            }
        });

        wordsList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                int firstIndex = e.getFirstIndex();
                int lastIndex = e.getLastIndex();

                if (wordsList.isSelectedIndex(firstIndex)) {
                    wordTranslationTextArea.setText(dictDb.getTranslation(words.get(firstIndex)));
                } else {
                    wordTranslationTextArea.setText(dictDb.getTranslation(words.get(lastIndex)));
                }

                matchLabel.setIcon(IconManager.getImageIcon("bell2_green.png", IconManager.IconSize.SIZE24));
            }
        });

        wordSearchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String searchString = wordSearchField.getText();

                // in case the user types backspaces
                if (searchString.isEmpty()) {
                    clear();
                }

                if (words.contains(searchString)) {
                    final int index = words.indexOf(searchString);
                    wordsList.setSelectedIndex(index);
                    wordsList.ensureIndexIsVisible(index);
                    wordTranslationTextArea.setText(dictDb.getTranslation(searchString));
                    matchLabel.setIcon(IconManager.getImageIcon("bell2_green.png", IconManager.IconSize.SIZE24));
                } else {
                    matchLabel.setIcon(IconManager.getImageIcon("bell2_red.png", IconManager.IconSize.SIZE24));
                }
            }
        });

        setDefaultFont();

        if (prefs.getBoolean("CLIPBOARD_INTEGRATION", false)) {
            activateClipboardMonitoring();
        }
    }

    private void clear() {
        wordSearchField.setText(null);
        wordsList.ensureIndexIsVisible(0);
        wordsList.clearSelection();
        wordTranslationTextArea.setText(null);
        matchLabel.setIcon(IconManager.getImageIcon("bell2_red.png", IconManager.IconSize.SIZE24));
    }

    public void activateClipboardMonitoring() {
        LOGGER.info("Activating clipboard monitoring");

        if (clipboardExecutorService == null || clipboardExecutorService.isShutdown()) {

            clipboardTextTransfer = new ClipboardTextTransfer();

            Runnable clipboardRunnable = new Runnable() {
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
        if (!clipboardExecutorService.isShutdown()) {
            LOGGER.info("Shutting down clipboard monitoring");
            clipboardExecutorService.shutdown();
        }
    }

    private void setDefaultFont() {
        Preferences preferences = Preferences.userNodeForPackage(SpellbookApp.class);

        if (preferences.get("FONT_NAME", "").isEmpty()) {
            // dirty fix for windows - it seem that the default font there is too small, so we set
            // a more appropriate one
            String osName = System.getProperty("os.name");

            if (osName.contains("Windows")) {
                wordTranslationTextArea.setFont(new Font("Arial", Font.PLAIN, 14));
            }
        } else {
            String fontName = preferences.get("FONT_NAME", "SansSerif");
            int fontSize = preferences.getInt("FONT_SIZE", 14);
            int fontStyle = preferences.getInt("FONT_STYLE", Font.PLAIN);

            setFont(new Font(fontName, fontStyle, fontSize));
        }
    }

    private boolean verifyDbPresence(Preferences prefs) {
        final String dbPath = prefs.get("PATH_TO_DB", "");

        File file = new File(dbPath);

        if (!file.exists()) {
            if (dbPath.isEmpty()) {
                JOptionPane.showMessageDialog(topPanel, TRANSLATOR.translate("SelectDb(Message)"));
            } else {
                JOptionPane.showMessageDialog(topPanel, TRANSLATOR.translate("MissingDb(Message)"));
            }

            JFileChooser fileChooser = new JFileChooser();
            final int result = fileChooser.showDialog(topPanel, TRANSLATOR.translate("SelectDb(Title)"));

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

    public void setFont(Font font) {
        wordSearchField.setFont(font);
        wordsList.setFont(font);
        wordTranslationTextArea.setFont(font);
        statusBar.setFont(font);
        drowLabel.setFont(font);
        clearButton.setFont(font);
    }

    public JComponent getComponent() {
        return topPanel;
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
        } else {
            statusBar.setText(String.format(TRANSLATOR.translate("BgEnDictSize(Label)"), words.size()));
        }
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        topPanel = new JPanel();
        topPanel.setLayout(new FormLayout("fill:120dlu:noGrow,left:4dlu:noGrow,fill:60dlu:noGrow,left:6dlu:noGrow,fill:20dlu:noGrow,left:5dlu:noGrow,fill:max(d;4px):grow", "center:20dlu:noGrow,top:4dlu:noGrow,center:344px:grow,top:4dlu:noGrow,center:15dlu:noGrow"));
        wordSearchField = new JTextField();
        CellConstraints cc = new CellConstraints();
        topPanel.add(wordSearchField, cc.xy(1, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        final JScrollPane scrollPane1 = new JScrollPane();
        topPanel.add(scrollPane1, cc.xy(1, 3, CellConstraints.FILL, CellConstraints.FILL));
        wordsList = new JList();
        scrollPane1.setViewportView(wordsList);
        final JScrollPane scrollPane2 = new JScrollPane();
        topPanel.add(scrollPane2, cc.xyw(3, 3, 5, CellConstraints.FILL, CellConstraints.FILL));
        wordTranslationTextArea = new JTextArea();
        wordTranslationTextArea.setFont(new Font(wordTranslationTextArea.getFont().getName(), wordTranslationTextArea.getFont().getStyle(), wordTranslationTextArea.getFont().getSize()));
        scrollPane2.setViewportView(wordTranslationTextArea);
        clearButton = new JButton();
        this.$$$loadButtonText$$$(clearButton, ResourceBundle.getBundle("i18n/DesktopUI").getString("ClearButton(Label)"));
        topPanel.add(clearButton, cc.xy(3, 1));
        statusBar = new JLabel();
        statusBar.setText("Status");
        topPanel.add(statusBar, cc.xyw(1, 5, 7));
        drowLabel = new JLabel();
        this.$$$loadLabelText$$$(drowLabel, ResourceBundle.getBundle("i18n/DesktopUI").getString("FuelledBy(Label)"));
        topPanel.add(drowLabel, cc.xy(7, 1, CellConstraints.CENTER, CellConstraints.DEFAULT));
        matchLabel = new JLabel();
        matchLabel.setIcon(new ImageIcon(getClass().getResource("/icons/24x24/bell2_red.png")));
        matchLabel.setText("");
        topPanel.add(matchLabel, cc.xy(5, 1, CellConstraints.CENTER, CellConstraints.DEFAULT));
    }

    /**
     * @noinspection ALL
     */
    private void $$$loadLabelText$$$(JLabel component, String text) {
        StringBuffer result = new StringBuffer();
        boolean haveMnemonic = false;
        char mnemonic = '\0';
        int mnemonicIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&') {
                i++;
                if (i == text.length()) {
                    break;
                }
                if (!haveMnemonic && text.charAt(i) != '&') {
                    haveMnemonic = true;
                    mnemonic = text.charAt(i);
                    mnemonicIndex = result.length();
                }
            }
            result.append(text.charAt(i));
        }
        component.setText(result.toString());
        if (haveMnemonic) {
            component.setDisplayedMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
    }

    /**
     * @noinspection ALL
     */
    private void $$$loadButtonText$$$(AbstractButton component, String text) {
        StringBuffer result = new StringBuffer();
        boolean haveMnemonic = false;
        char mnemonic = '\0';
        int mnemonicIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&') {
                i++;
                if (i == text.length()) {
                    break;
                }
                if (!haveMnemonic && text.charAt(i) != '&') {
                    haveMnemonic = true;
                    mnemonic = text.charAt(i);
                    mnemonicIndex = result.length();
                }
            }
            result.append(text.charAt(i));
        }
        component.setText(result.toString());
        if (haveMnemonic) {
            component.setMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return topPanel;
    }
}
