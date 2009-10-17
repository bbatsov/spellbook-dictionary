package com.drowltd.dictionary.ui.desktop;

import com.drowltd.dictionary.core.db.DictDb;
import com.drowltd.dictionary.core.i18n.Translator;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

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

    private JTextField wordSearchField;
    private JPanel topPanel;
    private JButton clearButton;
    private JList wordsList;
    private JTextArea wordTranslationTextArea;
    private JLabel statusBar;
    private JLabel drowLabel;

    private DictDb dictDb;
    private List<String> words;
    private ClipboardTextTransfer clipboardTextTransfer;
    private String lastTransfer;

    public SpellbookForm() {
        Preferences prefs = Preferences.userNodeForPackage(this.getClass());

        while (true) {
            if (verifyDbPresence(prefs)) {
                break;
            }
        }

        DictDb.init(prefs.get("PATH_TO_DB", ""));

        dictDb = DictDb.getInstance();

        words = dictDb.getWordsFromSelectedDictionary();

        wordsList.setListData(words.toArray());

        statusBar.setText(String.format(TRANSLATOR.translate("EnBgDictSize(Label)"), words.size()));

        wordsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                wordSearchField.setText(null);
                wordsList.ensureIndexIsVisible(0);
                wordsList.clearSelection();
                wordTranslationTextArea.setText(null);
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
            }
        });

        wordSearchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                String searchString = wordSearchField.getText() + e.getKeyChar();

                if (words.contains(searchString)) {
                    final int index = words.indexOf(searchString);
                    wordsList.setSelectedIndex(index);
                    wordsList.ensureIndexIsVisible(index);
                    wordTranslationTextArea.setText(dictDb.getTranslation(searchString));
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                String searchString = wordSearchField.getText();

                if (words.contains(searchString)) {
                    final int index = words.indexOf(searchString);
                    wordsList.setSelectedIndex(index);
                    wordsList.ensureIndexIsVisible(index);
                    wordTranslationTextArea.setText(dictDb.getTranslation(searchString));
                }
            }
        });


        setOsSpecificSettings();

        activateClipboardMonitoring();
    }

    private void activateClipboardMonitoring() {
        clipboardTextTransfer = new ClipboardTextTransfer();

        Runnable clipboardRunnable = new Runnable() {
            public void run() {
                String transferredText = clipboardTextTransfer.getClipboardContents();

                if (lastTransfer == null) {
                    lastTransfer = transferredText;
                }

                if (!transferredText.equalsIgnoreCase(lastTransfer)) {
                    String searchString = transferredText.split("\\W")[0];
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

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(clipboardRunnable, 0, 1, TimeUnit.SECONDS);
    }

    private void setOsSpecificSettings() {
        String osName = System.getProperty("os.name");

        if (osName.contains("Windows")) {
            wordTranslationTextArea.setFont(new Font("Arial", Font.PLAIN, 12));
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

    public JComponent getComponent() {
        return topPanel;
    }

    public void selectDictionary(String dictionary) {
        dictDb.setSelectedDictionary(dictionary);

        words = dictDb.getWordsFromSelectedDictionary();
        wordsList.setListData(words.toArray());

        if (dictionary.equalsIgnoreCase("en_bg"))
            statusBar.setText(String.format(TRANSLATOR.translate("EnBgDictSize(Label)"), words.size()));
        else
            statusBar.setText(String.format(TRANSLATOR.translate("BgEnDictSize(Label)"), words.size()));

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
        topPanel.setLayout(new FormLayout("fill:120dlu:noGrow,left:4dlu:noGrow,fill:60dlu:noGrow,left:5dlu:noGrow,fill:max(d;4px):grow", "center:20dlu:noGrow,top:4dlu:noGrow,center:344px:grow,top:4dlu:noGrow,center:10dlu:noGrow"));
        wordSearchField = new JTextField();
        CellConstraints cc = new CellConstraints();
        topPanel.add(wordSearchField, cc.xy(1, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        final JScrollPane scrollPane1 = new JScrollPane();
        topPanel.add(scrollPane1, cc.xy(1, 3, CellConstraints.FILL, CellConstraints.FILL));
        wordsList = new JList();
        scrollPane1.setViewportView(wordsList);
        final JScrollPane scrollPane2 = new JScrollPane();
        topPanel.add(scrollPane2, cc.xyw(3, 3, 3, CellConstraints.FILL, CellConstraints.FILL));
        wordTranslationTextArea = new JTextArea();
        wordTranslationTextArea.setFont(new Font(wordTranslationTextArea.getFont().getName(), wordTranslationTextArea.getFont().getStyle(), wordTranslationTextArea.getFont().getSize()));
        scrollPane2.setViewportView(wordTranslationTextArea);
        clearButton = new JButton();
        this.$$$loadButtonText$$$(clearButton, ResourceBundle.getBundle("i18n/DesktopUI").getString("ClearButton(Label)"));
        topPanel.add(clearButton, cc.xy(3, 1));
        statusBar = new JLabel();
        statusBar.setText("Status");
        topPanel.add(statusBar, cc.xyw(1, 5, 5));
        drowLabel = new JLabel();
        this.$$$loadLabelText$$$(drowLabel, ResourceBundle.getBundle("i18n/DesktopUI").getString("FuelledBy(Label)"));
        topPanel.add(drowLabel, cc.xy(5, 1, CellConstraints.CENTER, CellConstraints.DEFAULT));
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
                if (i == text.length()) break;
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
                if (i == text.length()) break;
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
