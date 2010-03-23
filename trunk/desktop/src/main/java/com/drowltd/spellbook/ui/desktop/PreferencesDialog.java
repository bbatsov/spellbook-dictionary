package com.drowltd.spellbook.ui.desktop;

import com.drowltd.spellbook.core.model.Difficulty;
import com.drowltd.spellbook.core.i18n.Translator;
import com.drowltd.spellbook.core.model.Language;
import com.drowltd.spellbook.core.preferences.PreferencesManager;
import com.drowltd.spellbook.core.service.DictionaryService;
import com.drowltd.spellbook.ui.desktop.exam.ExamDialog;
import com.drowltd.spellbook.ui.swing.component.DictionaryComboBox;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import static com.drowltd.spellbook.core.preferences.PreferencesManager.Preference;

/**
 * Spellbook's preferences dialog.
 *
 * @author Bozhidar Batsov
 * @since 0.2
 */
public class PreferencesDialog extends javax.swing.JDialog {

    private static final Translator TRANSLATOR = Translator.getTranslator("PreferencesForm");
    private static final PreferencesManager PM = PreferencesManager.getInstance();
    private Language selectedLanguage;
    private Font selectedFont;
    private boolean ok;
    private static final DictionaryService DICTIONARY_SERVICE = DictionaryService.getInstance();

    /** Creates new form PreferencesDialog */
    public PreferencesDialog(final java.awt.Frame parent, boolean modal) {
        super(parent, modal);

        TRANSLATOR.reset();

        initComponents();

        initGeneralTab(parent);

        initFontTab();

        initExamTab();
    }

    public JTabbedPane getTabbedTane() {
        return tabbedPane;
    }

    private void initGeneralTab(final java.awt.Frame parent) {


        selectedLanguage = Language.valueOf(PM.get(Preference.UI_LANG, "ENGLISH"));

        // set the selected values from preferences
        languageComboBox.setSelectedIndex(selectedLanguage == Language.ENGLISH ? 0 : 1);

        defaultDictionaryComboBox.setSelectedItem(PM.get(Preference.DEFAULT_DICTIONARY, "English-Bulgarian"));

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

                SwingUtilities.updateComponentTreeUI(tabbedPane);
                SwingUtilities.updateComponentTreeUI(jPanel4);
                SwingUtilities.updateComponentTreeUI(parent);
            }
        });
    }

    private void initFontTab() {
        emptyLineCheckBox.setSelected(PM.getBoolean(Preference.EMPTY_LINE, false));

        String[] availableFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        fontList.setListData(availableFonts);

        // select the current font
        String selectedFontName = PM.get(Preference.FONT_NAME, "Serif");
        fontList.setSelectedValue(selectedFontName, true);

        fontList.addListSelectionListener(new SelectionUpdater());

        fontSizeList.setListData(new Integer[]{8, 10, 12, 14, 16, 18});

        int currentFontSize = PM.getInt(Preference.FONT_SIZE, 12);
        fontSizeList.setSelectedValue(currentFontSize, true);

        fontSizeList.addListSelectionListener(new SelectionUpdater());

        fontStyleList.setListData(new String[]{"Regular", "Bold", "Italic"});
        fontStyleList.addListSelectionListener(new SelectionUpdater());
        fontStyleList.setSelectedIndex(PM.getInt(Preference.FONT_STYLE, 0));

        previewText.setFont(generateFont());

        currentFontField.setEnabled(false);
        currentFontField.setText(PM.get(Preference.FONT_NAME, selectedFont.getFontName()));
        fontList.setSelectedValue((PM.get(Preference.FONT_NAME, selectedFont.getFontName())).replace("bold", ""), ok);
        //bottom line needs a little rework do be done by Franky(Ivan Spasov) and the new design is ready and fully functional
        currentStyleField.setEnabled(false);
        currentStyleField.setText(fontStyleList.getSelectedValue().toString());
        currentFontSizeField.setEnabled(false);
        currentFontSizeField.setText(fontSizeList.getSelectedValue().toString());
    }

    private Font generateFont() {
        String fontName = (String) fontList.getSelectedValue();

        Integer sizeInt = (Integer) fontSizeList.getSelectedValue();

        selectedFont = new Font(fontName,
                (fontStyleList.isSelectedIndex(2) ? Font.ITALIC : Font.PLAIN)
                | (fontStyleList.isSelectedIndex(1) ? Font.BOLD : Font.PLAIN)
                | (fontStyleList.isSelectedIndex(0) ? Font.PLAIN : Font.PLAIN),
                sizeInt);

        return selectedFont;
    }

    public Font getSelectedFont() {
        return selectedFont;
    }

    private class SelectionUpdater implements ChangeListener, ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            previewText.setFont(generateFont());
            currentFontField.setText(generateFont().getFontName().toString());
            currentStyleField.setText(fontStyleList.getSelectedValue().toString());
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            previewText.setFont(generateFont());

        }
    }

    private void initExamTab() {
        switch (Difficulty.valueOf(PM.get(Preference.EXAM_DIFFICULTY, Difficulty.EASY.toString()))) {
            case EASY:
                easyRadioButton.setSelected(true);
                break;
            case MEDIUM:
                mediumRadioButton.setSelected(true);
                break;
            case HARD:
                hardRadioButton.setSelected(true);
                break;
            default:
                throw new IllegalStateException("Unknown difficulty");
        }

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
        if (easyRadioButton.isSelected()) {
            return Difficulty.EASY;
        } else if (mediumRadioButton.isSelected()) {
            return Difficulty.MEDIUM;
        } else {
            return Difficulty.HARD;
        }
    }

    /* This method sets the new settings to exam module after
     * changing them into setting dialog.
     */
    public void refreshNewSettingsToExam() {
        ExamDialog.diffLabelChange(getExamDifficulty().toString());

        if (isExamTimerEnabled()) {
            ExamDialog.setTimerProgressbarVisible();
            ExamDialog.setEnumTimerStatus(ExamDialog.TimerStatus.STARTED);
        } else {
            ExamDialog.setTimerProgressbarInvisible();
            ExamDialog.setEnumTimerStatus(ExamDialog.TimerStatus.DISABLED);
        }

    }

    public Language getSelectedLanguage() {
        return selectedLanguage;
    }

    public String getDefaultDictionary() {
        return (String) defaultDictionaryComboBox.getSelectedItem();
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

        return ok;
    }

    public void disableTrayOptions() {
        minimizeToTrayCheckBox.setSelected(false);
        minimizeToTrayCheckBox.setEnabled(false);

        minimizeToTrayOnCloseCheckBox.setSelected(false);
        minimizeToTrayOnCloseCheckBox.setEnabled(false);

        trayPopupCheckBox.setSelected(false);
        trayPopupCheckBox.setEnabled(false);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        difficultyButtonGroup = new javax.swing.ButtonGroup();
        tabbedPane = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        minimizeToTrayCheckBox = new javax.swing.JCheckBox();
        minimizeToTrayOnCloseCheckBox = new javax.swing.JCheckBox();
        clipboardIntegrationCheckBox = new javax.swing.JCheckBox();
        trayPopupCheckBox = new javax.swing.JCheckBox();
        showMemoryUsageCheckBox = new javax.swing.JCheckBox();
        alwaysOnTopCheckBox = new javax.swing.JCheckBox();
        emptyLineCheckBox = new javax.swing.JCheckBox();
        jPanel10 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        languageComboBox = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        lookAndFeelComboBox = new javax.swing.JComboBox();
        defaultDictionaryComboBox = new DictionaryComboBox(DICTIONARY_SERVICE.getDictionaries());
        jPanel2 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        fontList = new javax.swing.JList();
        jScrollPane2 = new javax.swing.JScrollPane();
        fontSizeList = new javax.swing.JList();
        jScrollPane3 = new javax.swing.JScrollPane();
        fontStyleList = new javax.swing.JList();
        jLabel13 = new javax.swing.JLabel();
        fontPreviewPanel = new javax.swing.JPanel();
        previewText = new javax.swing.JLabel();
        currentFontField = new javax.swing.JTextField();
        currentStyleField = new javax.swing.JTextField();
        currentFontSizeField = new javax.swing.JTextField();
        jPanel6 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        easyRadioButton = new javax.swing.JRadioButton();
        mediumRadioButton = new javax.swing.JRadioButton();
        hardRadioButton = new javax.swing.JRadioButton();
        jLabel5 = new javax.swing.JLabel();
        timerCheckBox = new javax.swing.JCheckBox();
        jLabel7 = new javax.swing.JLabel();
        wordCountField = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("i18n/PreferencesForm"); // NOI18N
        setTitle(bundle.getString("Preferences(Title)")); // NOI18N
        setResizable(false);

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("Functionality(JPanelBorder)"))); // NOI18N

        minimizeToTrayCheckBox.setText(bundle.getString("MinimizeToTray(Label)")); // NOI18N

        minimizeToTrayOnCloseCheckBox.setText(bundle.getString("CloseToTray(Label)")); // NOI18N

        clipboardIntegrationCheckBox.setText(bundle.getString("ClipboardIntegration(Label)")); // NOI18N
        clipboardIntegrationCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clipboardIntegrationCheckBoxActionPerformed(evt);
            }
        });

        trayPopupCheckBox.setText(bundle.getString("TrayPopup(Label)")); // NOI18N
        trayPopupCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trayPopupCheckBoxActionPerformed(evt);
            }
        });

        showMemoryUsageCheckBox.setText(bundle.getString("ShowMemory(Label)")); // NOI18N

        alwaysOnTopCheckBox.setText(bundle.getString("AlwaysOnTop(Label)")); // NOI18N

        emptyLineCheckBox.setText("Empty line after each meaning");

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(minimizeToTrayCheckBox)
                            .addComponent(minimizeToTrayOnCloseCheckBox)
                            .addComponent(clipboardIntegrationCheckBox))
                        .addGap(44, 44, 44)
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(trayPopupCheckBox)
                            .addComponent(showMemoryUsageCheckBox)
                            .addComponent(alwaysOnTopCheckBox)))
                    .addComponent(emptyLineCheckBox))
                .addContainerGap(136, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minimizeToTrayCheckBox)
                    .addComponent(trayPopupCheckBox))
                .addGap(18, 18, 18)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minimizeToTrayOnCloseCheckBox)
                    .addComponent(showMemoryUsageCheckBox))
                .addGap(18, 18, 18)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(clipboardIntegrationCheckBox)
                    .addComponent(alwaysOnTopCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                .addComponent(emptyLineCheckBox)
                .addContainerGap())
        );

        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("Main(JPanelBorder)"))); // NOI18N

        jLabel1.setText(bundle.getString("Language(Label)")); // NOI18N

        languageComboBox.setModel(new DefaultComboBoxModel(new String[] {TRANSLATOR.translate("English(Item)"), TRANSLATOR.translate("Bulgarian(Item)")}));
        languageComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                languageComboBoxActionPerformed(evt);
            }
        });

        jLabel2.setText(bundle.getString("LookAndFeel(Label)")); // NOI18N

        jLabel3.setText(bundle.getString("DefaultDictionary(Label)")); // NOI18N

        lookAndFeelComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        lookAndFeelComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lookAndFeelComboBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addGap(237, 237, 237)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(defaultDictionaryComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(languageComboBox, javax.swing.GroupLayout.Alignment.LEADING, 0, 103, Short.MAX_VALUE)
                    .addComponent(lookAndFeelComboBox, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(35, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(languageComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lookAndFeelComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(defaultDictionaryComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(18, Short.MAX_VALUE))
        );

        tabbedPane.addTab(bundle.getString("GeneralSettings(Title)"), new javax.swing.ImageIcon(getClass().getResource("/icons/16x16/preferences.png")), jPanel1); // NOI18N

        java.util.ResourceBundle bundle1 = java.util.ResourceBundle.getBundle("i18n/FontChooserForm"); // NOI18N
        jLabel9.setText(bundle1.getString("Font(Label)")); // NOI18N

        jLabel10.setText(bundle1.getString("Size(Label)")); // NOI18N

        fontList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(fontList);

        fontSizeList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane2.setViewportView(fontSizeList);

        fontStyleList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane3.setViewportView(fontStyleList);

        jLabel13.setText(bundle1.getString("Style(Label)")); // NOI18N

        fontPreviewPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Preview", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.DEFAULT_POSITION));

        previewText.setText("The quick fox jumps over the lazy dog.");

        javax.swing.GroupLayout fontPreviewPanelLayout = new javax.swing.GroupLayout(fontPreviewPanel);
        fontPreviewPanel.setLayout(fontPreviewPanelLayout);
        fontPreviewPanelLayout.setHorizontalGroup(
            fontPreviewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fontPreviewPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(previewText)
                .addContainerGap(274, Short.MAX_VALUE))
        );
        fontPreviewPanelLayout.setVerticalGroup(
            fontPreviewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fontPreviewPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(previewText)
                .addContainerGap(23, Short.MAX_VALUE))
        );

        currentFontField.setEditable(false);
        currentFontField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                currentFontFieldActionPerformed(evt);
            }
        });

        currentStyleField.setEditable(false);

        currentFontSizeField.setOpaque(false);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(fontPreviewPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel9)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE)
                            .addComponent(currentFontField))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel13)
                            .addComponent(currentStyleField, javax.swing.GroupLayout.DEFAULT_SIZE, 148, Short.MAX_VALUE)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 148, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel10)
                            .addComponent(currentFontSizeField)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(currentFontField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 171, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(currentFontSizeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 171, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(currentStyleField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 171, Short.MAX_VALUE)))
                .addGap(11, 11, 11)
                .addComponent(fontPreviewPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabbedPane.addTab(bundle1.getString("FontTab(Label)"), new javax.swing.ImageIcon(getClass().getResource("/icons/16x16/font.png")), jPanel2); // NOI18N

        jPanel6.setForeground(new java.awt.Color(204, 204, 204));

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("Main(JPanelBorder)"))); // NOI18N

        java.util.ResourceBundle bundle2 = java.util.ResourceBundle.getBundle("i18n/ExamSettingsDialog"); // NOI18N
        jLabel4.setText(bundle2.getString("ChooseDifficulty(Label)")); // NOI18N

        difficultyButtonGroup.add(easyRadioButton);
        easyRadioButton.setText(bundle2.getString("Easy(Label)")); // NOI18N
        easyRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                easyRadioButtonActionPerformed(evt);
            }
        });

        difficultyButtonGroup.add(mediumRadioButton);
        mediumRadioButton.setText(bundle2.getString("Medium(Label)")); // NOI18N

        difficultyButtonGroup.add(hardRadioButton);
        hardRadioButton.setText(bundle2.getString("Hard(Label)")); // NOI18N

        jLabel5.setText(bundle2.getString("TimeDependancy(Label)")); // NOI18N

        timerCheckBox.setText(bundle2.getString("Countdown(Label)")); // NOI18N

        jLabel7.setText(bundle2.getString("WordsByExam(Label)")); // NOI18N

        wordCountField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                wordCountFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                wordCountFieldFocusLost(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                        .addComponent(timerCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 107, Short.MAX_VALUE)
                        .addComponent(jLabel5))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 123, Short.MAX_VALUE)
                        .addComponent(easyRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(mediumRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(hardRadioButton))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 233, Short.MAX_VALUE)
                        .addComponent(wordCountField, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(easyRadioButton)
                    .addComponent(mediumRadioButton)
                    .addComponent(hardRadioButton))
                .addGap(18, 18, 18)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(timerCheckBox))
                .addGap(18, 18, 18)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(wordCountField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(178, Short.MAX_VALUE))
        );

        tabbedPane.addTab("Exam", new javax.swing.ImageIcon(getClass().getResource("/icons/16x16/blackboard.png")), jPanel6); // NOI18N

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(267, Short.MAX_VALUE)
                .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel4Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {cancelButton, okButton});

        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(okButton)
                .addComponent(cancelButton))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 507, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 366, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void languageComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_languageComboBoxActionPerformed
        if (languageComboBox.getSelectedIndex() == 0) {
            selectedLanguage = Language.ENGLISH;
        } else {
            selectedLanguage = Language.BULGARIAN;
        }
    }//GEN-LAST:event_languageComboBoxActionPerformed

    private void clipboardIntegrationCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clipboardIntegrationCheckBoxActionPerformed
        if (clipboardIntegrationCheckBox.isSelected()) {
            trayPopupCheckBox.setEnabled(true);
        } else {
            trayPopupCheckBox.setSelected(false);
            trayPopupCheckBox.setEnabled(false);
        }
    }//GEN-LAST:event_clipboardIntegrationCheckBoxActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        ok = true;
        setVisible(false);
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        ok = false;
        initExamTab();
        setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void wordCountFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_wordCountFieldFocusGained
        wordCountField.setText(null);
}//GEN-LAST:event_wordCountFieldFocusGained

    private void wordCountFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_wordCountFieldFocusLost
}//GEN-LAST:event_wordCountFieldFocusLost

    private void easyRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_easyRadioButtonActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_easyRadioButtonActionPerformed

    private void lookAndFeelComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lookAndFeelComboBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_lookAndFeelComboBoxActionPerformed

    private void trayPopupCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trayPopupCheckBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_trayPopupCheckBoxActionPerformed

    private void currentFontFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_currentFontFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_currentFontFieldActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox alwaysOnTopCheckBox;
    private javax.swing.JButton cancelButton;
    private javax.swing.JCheckBox clipboardIntegrationCheckBox;
    private javax.swing.JTextField currentFontField;
    private javax.swing.JTextField currentFontSizeField;
    private javax.swing.JTextField currentStyleField;
    private javax.swing.JComboBox defaultDictionaryComboBox;
    private javax.swing.ButtonGroup difficultyButtonGroup;
    private javax.swing.JRadioButton easyRadioButton;
    private javax.swing.JCheckBox emptyLineCheckBox;
    private javax.swing.JList fontList;
    private javax.swing.JPanel fontPreviewPanel;
    private javax.swing.JList fontSizeList;
    private javax.swing.JList fontStyleList;
    private javax.swing.JRadioButton hardRadioButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private static javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JComboBox languageComboBox;
    private javax.swing.JComboBox lookAndFeelComboBox;
    private javax.swing.JRadioButton mediumRadioButton;
    private javax.swing.JCheckBox minimizeToTrayCheckBox;
    private javax.swing.JCheckBox minimizeToTrayOnCloseCheckBox;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel previewText;
    private javax.swing.JCheckBox showMemoryUsageCheckBox;
    private javax.swing.JTabbedPane tabbedPane;
    private static javax.swing.JCheckBox timerCheckBox;
    private javax.swing.JCheckBox trayPopupCheckBox;
    private javax.swing.JTextField wordCountField;
    // End of variables declaration//GEN-END:variables
}
