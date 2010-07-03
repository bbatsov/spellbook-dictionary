package com.drowltd.spellbook.ui.desktop;

import com.drowltd.spellbook.core.model.Dictionary;
import com.drowltd.spellbook.core.model.Difficulty;
import com.drowltd.spellbook.core.model.Language;
import com.drowltd.spellbook.core.preferences.PreferencesManager;
import com.drowltd.spellbook.core.service.DictionaryService;
import com.drowltd.spellbook.ui.swing.component.BaseDialog;
import com.drowltd.spellbook.ui.swing.component.DictionaryComboBox;
import com.drowltd.spellbook.ui.swing.component.DifficultyComboBox;
import com.drowltd.spellbook.ui.swing.util.IconManager;
import com.drowltd.spellbook.ui.swing.util.LafUtil;
import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.dialog.ButtonResources;
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
public class PreferencesDialog extends BaseDialog {

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
    private JCheckBox wordOfTheDay;
    private JCheckBox checkForUpdates;
    private JLabel previewText;
    private JCheckBox showMemoryUsageCheckBox;
    private JTabbedPane tabbedPane;
    private JCheckBox timerCheckBox;
    private JCheckBox trayPopupCheckBox;
    private JTextField wordCountField;
    private DifficultyComboBox difficultyComboBox;
    private JCheckBox checkJavaVersionCheckBox;

    public PreferencesDialog(final Frame parent, boolean modal) {
        super(parent, modal);

        getTranslator().reset();

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

        defaultDictionaryComboBox.setSelectedItem(DICTIONARY_SERVICE.getDictionary(PM.get(Preference.DEFAULT_DICTIONARY, "English-Bulgarian")));

        minimizeToTrayCheckBox.setSelected(PM.getBoolean(Preference.MIN_TO_TRAY, false));

        minimizeToTrayOnCloseCheckBox.setSelected(PM.getBoolean(Preference.CLOSE_TO_TRAY, false));

        clipboardIntegrationCheckBox.setSelected(PM.getBoolean(Preference.CLIPBOARD_INTEGRATION, true));

        if (!clipboardIntegrationCheckBox.isSelected()) {
            trayPopupCheckBox.setEnabled(false);
            trayPopupCheckBox.setSelected(false);
        } else {
            trayPopupCheckBox.setSelected(PM.getBoolean(Preference.TRAY_POPUP, true));
        }

        showMemoryUsageCheckBox.setSelected(PM.getBoolean(Preference.SHOW_MEMORY_USAGE, false));

        alwaysOnTopCheckBox.setSelected(PM.getBoolean(Preference.ALWAYS_ON_TOP, false));

        emptyLineCheckBox.setSelected(PM.getBoolean(Preference.EMPTY_LINE, true));

        startMinimizedCheckBox.setSelected(PM.getBoolean(Preference.START_IN_TRAY, false));

        wordOfTheDay.setSelected(PM.getBoolean(Preference.WORD_OF_THE_DAY, true));

        checkForUpdates.setSelected(PM.getBoolean(Preference.CHECK_FOR_UPDATES, true));

        checkJavaVersionCheckBox.setSelected(PM.getBoolean(Preference.CHECK_JAVA_VERSION, true));

        final List<LookAndFeelInfo> lookAndFeelInfos = LafUtil.getAvailableLookAndFeels();

        List<String> lookAndFeelNames = new ArrayList<String>();
        lookAndFeelNames.add("System");

        for (UIManager.LookAndFeelInfo lookAndFeelInfo : lookAndFeelInfos) {
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

                // the new look and feel may displace components so we need to pack the dialog again
                pack();
            }
        });
    }

    private void initFontTab() {
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

    public boolean showWordOfTheDay() {
        return wordOfTheDay.isSelected();
    }

    public boolean isCheckForUpdatesEnabled() {
        return checkForUpdates.isSelected();
    }

    public boolean isCheckJavaVersionEnabled() {
        return checkJavaVersionCheckBox.isSelected();
    }

    public String getSelectedLookAndFeel() {
        return (String) lookAndFeelComboBox.getSelectedItem();
    }

    public void disableTrayOptions() {
        minimizeToTrayCheckBox.setSelected(false);
        minimizeToTrayCheckBox.setEnabled(false);

        minimizeToTrayOnCloseCheckBox.setSelected(false);
        minimizeToTrayOnCloseCheckBox.setEnabled(false);

        trayPopupCheckBox.setSelected(false);
        trayPopupCheckBox.setEnabled(false);

        startMinimizedCheckBox.setSelected(false);
        startMinimizedCheckBox.setEnabled(false);
    }

    private void initGuiComponents() {
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab(getTranslator().translate("GeneralSettings(Title)"), IconManager.getMenuIcon("preferences.png"), createGeneralPreferencesPanel());
        tabbedPane.addTab(getTranslator().translate("FontTab(Label)"), IconManager.getMenuIcon("font.png"), createFontPreferencesPanel());
        tabbedPane.addTab(getTranslator().translate("Exam(Title)"), IconManager.getMenuIcon("blackboard.png"), createExamPreferencesPanel());

        pack();
    }

    private JPanel createExamPreferencesPanel() {
        JPanel examSettingsPanel = new JPanel(new MigLayout("wrap 2", "[grow][grow]"));

        timerCheckBox = new JCheckBox();
        wordCountField = new JTextField();
        // accept only numbers
        wordCountField.setDocument(new NumberDocument());
        difficultyComboBox = new DifficultyComboBox();

        examSettingsPanel.add(new JLabel(getTranslator().translate("DefaultExamDifficulty(Label)")), "growx");
        examSettingsPanel.add(difficultyComboBox);
        examSettingsPanel.add(new JLabel(getTranslator().translate("ExamSize(Label)")), "growx");
        examSettingsPanel.add(wordCountField, "wrap, width 100");
        examSettingsPanel.add(timerCheckBox);

        timerCheckBox.setText(getTranslator().translate("TimeBasedExam(Label)"));
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

        fontSettingsPanel.add(new JLabel(getTranslator().translate("Font(Label)")), "span 2, growx");
        fontSettingsPanel.add(new JLabel(getTranslator().translate("Style(Label)")), "growx");
        fontSettingsPanel.add(new JLabel(getTranslator().translate("Size(Label)")), "growx");
        fontSettingsPanel.add(currentFontField, "span 2, growx");
        fontSettingsPanel.add(currentStyleField, "growx");
        fontSettingsPanel.add(currentFontSizeField, "growx");
        fontSettingsPanel.add(new JScrollPane(fontList), "span 2, grow");
        fontSettingsPanel.add(new JScrollPane(fontStyleList), "grow");
        fontSettingsPanel.add(new JScrollPane(fontSizeList), "grow");
        fontSettingsPanel.add(new JLabel(getTranslator().translate("Preview(Label)")), "span 4, gaptop 10, gapbottom 10");
        fontSettingsPanel.add(previewText, "span 4, grow");

        previewText.setText(getTranslator().translate("PreviewText(Label)"));

        return fontSettingsPanel;
    }

    private JPanel createGeneralPreferencesPanel() {
        JPanel generalSettingsPanel = new JPanel(new MigLayout("wrap 2", "[grow][grow]", "[][][]20[]10[]10[]10[]10[]"));
        minimizeToTrayCheckBox = new JCheckBox(getTranslator().translate("MinimizeToTray(Label)"));
        minimizeToTrayOnCloseCheckBox = new JCheckBox(getTranslator().translate("CloseToTray(Label)"));
        clipboardIntegrationCheckBox = new JCheckBox(getTranslator().translate("ClipboardIntegration(Label)"));
        trayPopupCheckBox = new JCheckBox(getTranslator().translate("TrayPopup(Label)"));
        showMemoryUsageCheckBox = new JCheckBox(getTranslator().translate("ShowMemory(Label)"));
        alwaysOnTopCheckBox = new JCheckBox(getTranslator().translate("AlwaysOnTop(Label)"));
        emptyLineCheckBox = new JCheckBox(getTranslator().translate("EmptyLine(Label)"));
        startMinimizedCheckBox = new JCheckBox(getTranslator().translate("StartMinimized(CheckBox)"));
        checkForUpdates = new JCheckBox(getTranslator().translate("CheckForUpdates(CheckBox)"));
        wordOfTheDay = new JCheckBox(getTranslator().translate("ShowWordOfTheDay(CheckBox)"));
        checkJavaVersionCheckBox = new JCheckBox(getTranslator().translate("CheckJavaVersion(CheckBox)"));
        languageComboBox = new JComboBox();
        lookAndFeelComboBox = new JComboBox();
        defaultDictionaryComboBox = new DictionaryComboBox(DICTIONARY_SERVICE.getDictionaries());

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

        languageComboBox.setModel(new DefaultComboBoxModel(new Language[]{Language.ENGLISH, Language.BULGARIAN}));

        generalSettingsPanel.add(new JLabel(getTranslator().translate("Language(Label)")));
        generalSettingsPanel.add(languageComboBox, "growx");
        generalSettingsPanel.add(new JLabel(getTranslator().translate("DefaultDictionary(Label)")));
        generalSettingsPanel.add(defaultDictionaryComboBox, "growx");
        generalSettingsPanel.add(new JLabel(getTranslator().translate("LookAndFeel(Label)")));
        generalSettingsPanel.add(lookAndFeelComboBox, "growx");
        generalSettingsPanel.add(minimizeToTrayCheckBox);
        generalSettingsPanel.add(minimizeToTrayOnCloseCheckBox);
        generalSettingsPanel.add(clipboardIntegrationCheckBox);
        generalSettingsPanel.add(trayPopupCheckBox);
        generalSettingsPanel.add(showMemoryUsageCheckBox);
        generalSettingsPanel.add(alwaysOnTopCheckBox);
        generalSettingsPanel.add(emptyLineCheckBox);
        generalSettingsPanel.add(wordOfTheDay);
        generalSettingsPanel.add(checkForUpdates);
        generalSettingsPanel.add(startMinimizedCheckBox);
        generalSettingsPanel.add(checkJavaVersionCheckBox);

        return generalSettingsPanel;
    }
}
