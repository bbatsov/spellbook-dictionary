package com.drowltd.spellbook.ui.desktop;

import com.drowltd.spellbook.core.i18n.Translator;
import com.drowltd.spellbook.core.model.Dictionary;
import com.drowltd.spellbook.core.model.Difficulty;
import com.drowltd.spellbook.core.model.Language;
import com.drowltd.spellbook.core.preferences.PreferencesManager;
import com.drowltd.spellbook.core.service.DictionaryService;
import com.drowltd.spellbook.ui.swing.component.DictionaryComboBox;
import com.drowltd.spellbook.ui.swing.component.DifficultyComboBox;
import com.drowltd.spellbook.ui.swing.util.IconManager;
import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.dialog.ButtonResources;
import com.jidesoft.dialog.StandardDialog;
import com.jidesoft.plaf.UIDefaultsLookup;
import net.miginfocom.swing.MigLayout;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.drowltd.spellbook.core.preferences.PreferencesManager.Preference;

/**
 * Spellbook's preferences dialog.
 *
 * @author Bozhidar Batsov
 * @since 0.2
 */
public class PreferencesDialog extends StandardDialog {

    private static final Translator TRANSLATOR = Translator.getTranslator("PreferencesDialog");
    private static final PreferencesManager PM = PreferencesManager.getInstance();
    private static final DictionaryService DICTIONARY_SERVICE = DictionaryService.getInstance();

    private JCheckBox alwaysOnTopCheckBox;
    private JCheckBox startMinimizedCheckBox;
    private JCheckBox clipboardIntegrationCheckBox;
    private JTextField currentFontField;
    private JTextField currentFontSizeField;
    private JTextField currentStyleField;
    private JComboBox defaultDictionaryComboBox;
    private JCheckBox emptyLineCheckBox;
    private JList fontList;
    private JList fontSizeList;
    private JList fontStyleList;
    private JComboBox languageComboBox;
    private JComboBox lookAndFeelComboBox;
    private JCheckBox minimizeToTrayCheckBox;
    private JCheckBox minimizeToTrayOnCloseCheckBox;
    private JLabel previewText;
    private JCheckBox showMemoryUsageCheckBox;
    private JTabbedPane tabbedPane;
    private JCheckBox timerCheckBox;
    private JCheckBox trayPopupCheckBox;
    private JTextField wordCountField;
    private DifficultyComboBox difficultyComboBox;

    public PreferencesDialog(final Frame parent, boolean modal) {
        super(parent, modal);

        TRANSLATOR.reset();

        initGuiComponents();

        initGeneralTab(parent);

        initFontTab();

        initExamTab();

        setResizable(false);
    }

    @Override
    public JComponent createBannerPanel() {
        return null;
    }

    @Override
    public JComponent createContentPanel() {
        return tabbedPane;
    }

    @Override
    public ButtonPanel createButtonPanel() {
        ButtonPanel buttonPanel = new ButtonPanel();
        JButton okButton = new JButton();
        JButton cancelButton = new JButton();
        JButton helpButton = new JButton();
        okButton.setName(OK);
        cancelButton.setName(CANCEL);
        helpButton.setName(HELP);
        buttonPanel.addButton(okButton, ButtonPanel.AFFIRMATIVE_BUTTON);
        buttonPanel.addButton(cancelButton, ButtonPanel.CANCEL_BUTTON);
        buttonPanel.addButton(helpButton, ButtonPanel.HELP_BUTTON);

        okButton.setAction(new AbstractAction(UIDefaultsLookup.getString("OptionPane.okButtonText")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                setDialogResult(RESULT_AFFIRMED);
                setVisible(false);
                dispose();
            }
        });
        cancelButton.setAction(new AbstractAction(UIDefaultsLookup.getString("OptionPane.cancelButtonText")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                setDialogResult(RESULT_CANCELLED);
                setVisible(false);
                dispose();
            }
        });
        final ResourceBundle resourceBundle = ButtonResources.getResourceBundle(Locale.getDefault());
        helpButton.setAction(new AbstractAction(resourceBundle.getString("Button.help")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                // do something
            }
        });
        helpButton.setMnemonic(resourceBundle.getString("Button.help.mnemonic").charAt(0));

        setDefaultCancelAction(cancelButton.getAction());
        setDefaultAction(okButton.getAction());
        getRootPane().setDefaultButton(okButton);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return buttonPanel;
    }

    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    private void initGeneralTab(final Frame parent) {
        Language[] availableLangs = Language.values();

        // this workaround is needed to avoid problems with older versions of spellbook
        Language selectedLanguage = Language.ENGLISH;

        String selectedLanguageName = PM.get(Preference.UI_LANG, Language.ENGLISH.getName());

        for (Language language : availableLangs) {
            if (language.getName().equals(selectedLanguageName)) {
                selectedLanguage = language;
                break;
            }
        }

        // set the selected values from preferences
        languageComboBox.setSelectedItem(selectedLanguage);

        defaultDictionaryComboBox.setSelectedItem(PM.get(Preference.DEFAULT_DICTIONARY, null));

        // select the first dictionary in case none is selected
        if (defaultDictionaryComboBox.getSelectedItem() == null) {
            defaultDictionaryComboBox.setSelectedIndex(0);
        }

        minimizeToTrayCheckBox.setSelected(PM.getBoolean(Preference.MIN_TO_TRAY, false));

        minimizeToTrayOnCloseCheckBox.setSelected(PM.getBoolean(Preference.CLOSE_TO_TRAY, false));

        clipboardIntegrationCheckBox.setSelected(PM.getBoolean(Preference.CLIPBOARD_INTEGRATION, false));

        if (!clipboardIntegrationCheckBox.isSelected()) {
            trayPopupCheckBox.setEnabled(false);
            trayPopupCheckBox.setSelected(false);
        } else {
            trayPopupCheckBox.setSelected(PM.getBoolean(Preference.TRAY_POPUP, false));
        }

        showMemoryUsageCheckBox.setSelected(PM.getBoolean(Preference.SHOW_MEMORY_USAGE, false));

        alwaysOnTopCheckBox.setSelected(PM.getBoolean(Preference.ALWAYS_ON_TOP, false));

        startMinimizedCheckBox.setSelected(PM.getBoolean(Preference.START_IN_TRAY, false));

        // build the look and feel section
        final LookAndFeelInfo[] lookAndFeelInfos = UIManager.getInstalledLookAndFeels();
        List<String> lookAndFeelNames = new ArrayList<String>();
        lookAndFeelNames.add("System");

        for (LookAndFeelInfo lookAndFeelInfo : lookAndFeelInfos) {
            if (!lookAndFeelInfo.getName().equals("CDE/Motif")) {
                lookAndFeelNames.add(lookAndFeelInfo.getName());
            }
        }

        lookAndFeelComboBox.setModel(new DefaultComboBoxModel(lookAndFeelNames.toArray()));

        lookAndFeelComboBox.setSelectedItem(PM.get(Preference.LOOK_AND_FEEL, "System"));

        lookAndFeelComboBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedLookAndFeel = (String) lookAndFeelComboBox.getSelectedItem();

                if (selectedLookAndFeel.equals("System")) {
                    try {
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(SpellbookFrame.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (InstantiationException ex) {
                        Logger.getLogger(SpellbookFrame.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalAccessException ex) {
                        Logger.getLogger(SpellbookFrame.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (UnsupportedLookAndFeelException ex) {
                        Logger.getLogger(SpellbookFrame.class.getName()).log(Level.SEVERE, null, ex);
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

                SwingUtilities.updateComponentTreeUI(getContentPane());
                SwingUtilities.updateComponentTreeUI(parent);
            }
        });
    }

    private void initFontTab() {
        emptyLineCheckBox.setSelected(PM.getBoolean(Preference.EMPTY_LINE, false));

        String[] availableFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        fontList.setListData(availableFonts);

        // select the current font
        String selectedFontName = PM.get(Preference.FONT_NAME, getFont().getName());
        fontList.setSelectedValue(selectedFontName, true);

        fontList.addListSelectionListener(new SelectionUpdater());

        fontSizeList.setListData(new Integer[]{8, 10, 12, 14, 16, 18});

        int currentFontSize = PM.getInt(Preference.FONT_SIZE, getFont().getSize());
        fontSizeList.setSelectedValue(currentFontSize, true);

        fontSizeList.addListSelectionListener(new SelectionUpdater());

        fontStyleList.setListData(new String[]{"Regular", "Bold", "Italic"});
        fontStyleList.addListSelectionListener(new SelectionUpdater());
        fontStyleList.setSelectedIndex(PM.getInt(Preference.FONT_STYLE, getFont().getStyle()));

        previewText.setFont(generateFont());

        currentFontField.setEnabled(false);
        currentFontField.setText(PM.get(Preference.FONT_NAME, getFont().getName()));
        currentStyleField.setEnabled(false);
        currentStyleField.setText(fontStyleList.getSelectedValue().toString());
        currentFontSizeField.setEnabled(false);
        currentFontSizeField.setText(fontSizeList.getSelectedValue().toString());
    }

    public Font generateFont() {
        String fontName = (String) fontList.getSelectedValue();

        Integer sizeInt = (Integer) fontSizeList.getSelectedValue();

        return new Font(fontName,
                (fontStyleList.isSelectedIndex(2) ? Font.ITALIC : Font.PLAIN)
                        | (fontStyleList.isSelectedIndex(1) ? Font.BOLD : Font.PLAIN)
                        | (fontStyleList.isSelectedIndex(0) ? Font.PLAIN : Font.PLAIN),
                sizeInt);
    }

    public boolean isStartMinimizedEnabled() {
        return startMinimizedCheckBox.isSelected();
    }

    private class SelectionUpdater implements ChangeListener, ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            previewText.setFont(generateFont());
            currentFontField.setText(generateFont().getFontName());
            currentStyleField.setText(fontStyleList.getSelectedValue().toString());
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            previewText.setFont(generateFont());

        }
    }

    private void initExamTab() {
        difficultyComboBox.setSelectedItem(Difficulty.valueOf(PM.get(Preference.EXAM_DIFFICULTY, Difficulty.EASY.name())));

        wordCountField.setText(String.valueOf(PM.getInt(Preference.EXAM_WORDS, 10)));
        timerCheckBox.setSelected(PM.getBoolean(Preference.EXAM_TIMER, false));
    }

    public int getExamWords() {
        return Integer.parseInt(wordCountField.getText());
    }

    public boolean isExamTimerEnabled() {
        return timerCheckBox.isSelected();
    }

    public Difficulty getExamDifficulty() {
        return (Difficulty) difficultyComboBox.getSelectedItem();
    }

    public Language getSelectedLanguage() {
        return (Language) languageComboBox.getSelectedItem();
    }

    public String getDefaultDictionary() {
        return ((Dictionary) defaultDictionaryComboBox.getSelectedItem()).getName();
    }

    public boolean isMinimizeToTrayEnabled() {
        return minimizeToTrayCheckBox.isSelected();
    }

    public boolean isClipboardIntegrationEnabled() {
        return clipboardIntegrationCheckBox.isSelected();
    }

    public boolean isTrayPopupEnabled() {
        return trayPopupCheckBox.isSelected();
    }

    public boolean isShowMemoryUsageEnabled() {
        return showMemoryUsageCheckBox.isSelected();
    }

    public boolean isAlwaysOnTopEnabled() {
        return alwaysOnTopCheckBox.isSelected();
    }

    public boolean isMinimizeToTrayOnCloseEnabled() {
        return minimizeToTrayOnCloseCheckBox.isSelected();
    }

    public boolean isEmptyLineEnabled() {
        return emptyLineCheckBox.isSelected();
    }

    public String getSelectedLookAndFeel() {
        return (String) lookAndFeelComboBox.getSelectedItem();
    }

    public boolean showDialog() {
        setVisible(true);

        return getDialogResult() == RESULT_AFFIRMED;
    }

    public void disableTrayOptions() {
        minimizeToTrayCheckBox.setSelected(false);
        minimizeToTrayCheckBox.setEnabled(false);

        minimizeToTrayOnCloseCheckBox.setSelected(false);
        minimizeToTrayOnCloseCheckBox.setEnabled(false);

        trayPopupCheckBox.setSelected(false);
        trayPopupCheckBox.setEnabled(false);
    }

    private void initGuiComponents() {
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab(TRANSLATOR.translate("GeneralSettings(Title)"), IconManager.getMenuIcon("preferences.png"), createGeneralPreferencesPanel());
        tabbedPane.addTab(TRANSLATOR.translate("FontTab(Label)"), IconManager.getMenuIcon("font.png"), createFontPreferencesPanel());
        tabbedPane.addTab(TRANSLATOR.translate("Exam(Title)"), IconManager.getMenuIcon("blackboard.png"), createExamPreferencesPanel());

        pack();
    }

    private JPanel createExamPreferencesPanel() {
        JPanel examSettingsPanel = new JPanel(new MigLayout("wrap 2", "[grow][grow]"));

        timerCheckBox = new JCheckBox();
        wordCountField = new JTextField();
        // accept only numbers
        wordCountField.setDocument(new NumberDocument());
        difficultyComboBox = new DifficultyComboBox();

        examSettingsPanel.add(new JLabel(TRANSLATOR.translate("DefaultExamDifficulty(Label)")), "growx");
        examSettingsPanel.add(difficultyComboBox);
        examSettingsPanel.add(new JLabel(TRANSLATOR.translate("ExamSize(Label)")), "growx");
        examSettingsPanel.add(wordCountField, "wrap");
        examSettingsPanel.add(timerCheckBox);

        timerCheckBox.setText(TRANSLATOR.translate("TimeBasedExam(Label)"));
        return examSettingsPanel;
    }

    private JPanel createFontPreferencesPanel() {
        JPanel fontSettingsPanel = new JPanel(new MigLayout("wrap 4", "[200:300:400][][140:140:200][100:80:200]", "[][][grow][grow]"));
        fontList = new JList();
        fontSizeList = new JList();
        fontStyleList = new JList();
        previewText = new JLabel();
        currentFontField = new JTextField();
        currentStyleField = new JTextField();
        currentFontSizeField = new JTextField();

        fontSettingsPanel.add(new JLabel(TRANSLATOR.translate("Font(Label)")), "span 2, growx");
        fontSettingsPanel.add(new JLabel(TRANSLATOR.translate("Style(Label)")), "growx");
        fontSettingsPanel.add(new JLabel(TRANSLATOR.translate("Size(Label)")), "growx");
        fontSettingsPanel.add(currentFontField, "span 2, growx");
        fontSettingsPanel.add(currentStyleField, "growx");
        fontSettingsPanel.add(currentFontSizeField, "growx");
        fontSettingsPanel.add(new JScrollPane(fontList), "span 2, grow");
        fontSettingsPanel.add(new JScrollPane(fontStyleList), "grow");
        fontSettingsPanel.add(new JScrollPane(fontSizeList), "grow");
        fontSettingsPanel.add(new JLabel(TRANSLATOR.translate("Preview(Label)")), "span 4, gaptop 10, gapbottom 10");
        fontSettingsPanel.add(previewText, "span 4, grow");

        previewText.setText(TRANSLATOR.translate("PreviewText(Label)"));

        return fontSettingsPanel;
    }

    private JPanel createGeneralPreferencesPanel() {
        JPanel generalSettingsPanel = new JPanel(new MigLayout("wrap 2", "[grow][grow]", "[][][]20[]10[]10[]10[]10[]"));
        minimizeToTrayCheckBox = new JCheckBox();
        minimizeToTrayOnCloseCheckBox = new JCheckBox();
        clipboardIntegrationCheckBox = new JCheckBox();
        trayPopupCheckBox = new JCheckBox();
        showMemoryUsageCheckBox = new JCheckBox();
        alwaysOnTopCheckBox = new JCheckBox();
        emptyLineCheckBox = new JCheckBox();
        languageComboBox = new JComboBox();
        lookAndFeelComboBox = new JComboBox();
        defaultDictionaryComboBox = new DictionaryComboBox(DICTIONARY_SERVICE.getDictionaries());

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(TRANSLATOR.translate("Preferences(Title)"));

        minimizeToTrayCheckBox.setText(TRANSLATOR.translate("MinimizeToTray(Label)"));

        minimizeToTrayOnCloseCheckBox.setText(TRANSLATOR.translate("CloseToTray(Label)"));

        clipboardIntegrationCheckBox.setText(TRANSLATOR.translate("ClipboardIntegration(Label)"));
        clipboardIntegrationCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if (clipboardIntegrationCheckBox.isSelected()) {
                    trayPopupCheckBox.setEnabled(true);
                } else {
                    trayPopupCheckBox.setSelected(false);
                    trayPopupCheckBox.setEnabled(false);
                }
            }
        });

        trayPopupCheckBox.setText(TRANSLATOR.translate("TrayPopup(Label)"));

        showMemoryUsageCheckBox.setText(TRANSLATOR.translate("ShowMemory(Label)"));

        alwaysOnTopCheckBox.setText(TRANSLATOR.translate("AlwaysOnTop(Label)"));

        emptyLineCheckBox.setText(TRANSLATOR.translate("EmptyLine(Label)"));

        languageComboBox.setModel(new DefaultComboBoxModel(new Language[]{Language.ENGLISH, Language.BULGARIAN}));

        generalSettingsPanel.add(new JLabel(TRANSLATOR.translate("Language(Label)")), "growx");
        generalSettingsPanel.add(languageComboBox, "growx");
        generalSettingsPanel.add(new JLabel(TRANSLATOR.translate("DefaultDictionary(Label)")), "growx");
        generalSettingsPanel.add(defaultDictionaryComboBox, "growx");
        generalSettingsPanel.add(new JLabel(TRANSLATOR.translate("LookAndFeel(Label)")), "growx");
        generalSettingsPanel.add(lookAndFeelComboBox, "growx");
        generalSettingsPanel.add(minimizeToTrayCheckBox, "growx");
        generalSettingsPanel.add(minimizeToTrayOnCloseCheckBox, "growx");
        generalSettingsPanel.add(clipboardIntegrationCheckBox, "growx");
        generalSettingsPanel.add(trayPopupCheckBox, "growx");
        generalSettingsPanel.add(showMemoryUsageCheckBox, "growx");
        generalSettingsPanel.add(alwaysOnTopCheckBox, "growx");
        generalSettingsPanel.add(emptyLineCheckBox, "growx");

        startMinimizedCheckBox = new JCheckBox(TRANSLATOR.translate("StartMinimized(CheckBox)"));
        generalSettingsPanel.add(startMinimizedCheckBox);

        return generalSettingsPanel;
    }
}
