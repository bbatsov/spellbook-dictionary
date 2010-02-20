package com.drowltd.dictionary.ui.desktop;

import com.drowltd.dictionary.core.exam.Difficulty;
import com.drowltd.dictionary.core.i18n.Translator;
import com.drowltd.dictionary.core.preferences.PreferencesManager;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import static com.drowltd.dictionary.core.preferences.PreferencesManager.Preference;

/**
 * Spellbook's preferences dialog.
 *
 * @author Bozhidar Batsov
 * @since 0.2
 */
public class PreferencesDialog extends javax.swing.JDialog {
    private static final Translator TRANSLATOR = Translator.getTranslator("PreferencesForm");
    private static final PreferencesManager PM = PreferencesManager.getInstance();

    private SupportedLanguages selectedLanguage;

    private Font selectedFont;
    
    private boolean ok;

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
        

        selectedLanguage = SupportedLanguages.valueOf(PM.get(Preference.LANG, "EN"));

        // set the selected values from preferences
        languageComboBox.setSelectedIndex(selectedLanguage == SupportedLanguages.EN ? 0 : 1);

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
        String[] lookAndFeelNames = new String[lookAndFeelInfos.length + 1];
        lookAndFeelNames[0] = "System";

        for (int i = 0; i < lookAndFeelInfos.length; i++) {
            lookAndFeelNames[i + 1] = lookAndFeelInfos[i].getName();
        }

        lookAndFeelComboBox.setModel(new DefaultComboBoxModel(lookAndFeelNames));

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

        previewText.setFont(generateFont());
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
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            previewText.setFont(generateFont());
        }
    }

    private void initExamTab() {
        switch (Difficulty.valueOf(PM.get(Preference.EXAM_DIFFICULTY, Difficulty.EASY.toString()))) {
            case EASY: easyRadioButton.setSelected(true); break;
            case MEDIUM: mediumRadioButton.setSelected(true); break;
            case HARD: hardRadioButton.setSelected(true); break;
            default: throw new IllegalStateException("Unknown difficulty");
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

    public SupportedLanguages getSelectedLanguage() {
        return selectedLanguage;
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
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        languageComboBox = new javax.swing.JComboBox();
        lookAndFeelComboBox = new javax.swing.JComboBox();
        defaultDictionaryComboBox = new javax.swing.JComboBox();
        minimizeToTrayCheckBox = new javax.swing.JCheckBox();
        minimizeToTrayOnCloseCheckBox = new javax.swing.JCheckBox();
        clipboardIntegrationCheckBox = new javax.swing.JCheckBox();
        trayPopupCheckBox = new javax.swing.JCheckBox();
        showMemoryUsageCheckBox = new javax.swing.JCheckBox();
        alwaysOnTopCheckBox = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        fontList = new javax.swing.JList();
        jScrollPane2 = new javax.swing.JScrollPane();
        fontSizeList = new javax.swing.JList();
        jLabel11 = new javax.swing.JLabel();
        previewText = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        fontStyleList = new javax.swing.JList();
        jLabel13 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        wordCountField = new javax.swing.JTextField();
        jPanel7 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        easyRadioButton = new javax.swing.JRadioButton();
        mediumRadioButton = new javax.swing.JRadioButton();
        hardRadioButton = new javax.swing.JRadioButton();
        jPanel8 = new javax.swing.JPanel();
        timerCheckBox = new javax.swing.JCheckBox();
        jLabel5 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("i18n/PreferencesForm"); // NOI18N
        setTitle(bundle.getString("Preferences(Title)")); // NOI18N

        jLabel1.setText(bundle.getString("Language(Label)")); // NOI18N

        jLabel2.setText(bundle.getString("LookAndFeel(Label)")); // NOI18N

        jLabel3.setText(bundle.getString("DefaultDictionary(Label)")); // NOI18N

        languageComboBox.setModel(new DefaultComboBoxModel(new String[] {TRANSLATOR.translate("English(Item)"), TRANSLATOR.translate("Bulgarian(Item)")}));
        languageComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                languageComboBoxActionPerformed(evt);
            }
        });

        lookAndFeelComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        defaultDictionaryComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        minimizeToTrayCheckBox.setText(bundle.getString("MinimizeToTray(Label)")); // NOI18N

        minimizeToTrayOnCloseCheckBox.setText(bundle.getString("CloseToTray(Label)")); // NOI18N

        clipboardIntegrationCheckBox.setText(bundle.getString("ClipboardIntegration(Label)")); // NOI18N
        clipboardIntegrationCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clipboardIntegrationCheckBoxActionPerformed(evt);
            }
        });

        trayPopupCheckBox.setText(bundle.getString("TrayPopup(Label)")); // NOI18N

        showMemoryUsageCheckBox.setText(bundle.getString("ShowMemory(Label)")); // NOI18N

        alwaysOnTopCheckBox.setText(bundle.getString("AlwaysOnTop(Label)")); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 432, Short.MAX_VALUE)
                        .addComponent(languageComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 421, Short.MAX_VALUE)
                        .addComponent(lookAndFeelComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 384, Short.MAX_VALUE)
                        .addComponent(defaultDictionaryComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(minimizeToTrayCheckBox)
                    .addComponent(minimizeToTrayOnCloseCheckBox)
                    .addComponent(clipboardIntegrationCheckBox)
                    .addComponent(trayPopupCheckBox)
                    .addComponent(showMemoryUsageCheckBox)
                    .addComponent(alwaysOnTopCheckBox))
                .addContainerGap())
        );

        jPanel3Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {defaultDictionaryComboBox, languageComboBox, lookAndFeelComboBox});

        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(languageComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(lookAndFeelComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(defaultDictionaryComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(minimizeToTrayCheckBox)
                .addGap(18, 18, 18)
                .addComponent(minimizeToTrayOnCloseCheckBox)
                .addGap(18, 18, 18)
                .addComponent(clipboardIntegrationCheckBox)
                .addGap(18, 18, 18)
                .addComponent(trayPopupCheckBox)
                .addGap(18, 18, 18)
                .addComponent(showMemoryUsageCheckBox)
                .addGap(18, 18, 18)
                .addComponent(alwaysOnTopCheckBox)
                .addContainerGap(29, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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

        jLabel11.setText(bundle1.getString("Preview(Label)")); // NOI18N

        previewText.setText(bundle1.getString("PreviewText(Label)")); // NOI18N

        jLabel12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/48x48/font.png"))); // NOI18N

        fontStyleList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane3.setViewportView(fontStyleList);

        jLabel13.setText(bundle1.getString("Style(Label)")); // NOI18N

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(previewText))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel9)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(24, 24, 24)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel13))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel10))))
                .addGap(18, 18, 18)
                .addComponent(jLabel12)
                .addContainerGap(109, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel12)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel9)
                            .addComponent(jLabel10)
                            .addComponent(jLabel13))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 227, Short.MAX_VALUE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 227, Short.MAX_VALUE)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 227, Short.MAX_VALUE))
                        .addGap(30, 30, 30)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel11)
                            .addComponent(previewText, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(65, 65, 65))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        tabbedPane.addTab(bundle1.getString("Font(Label)"), new javax.swing.ImageIcon(getClass().getResource("/icons/16x16/font.png")), jPanel2); // NOI18N

        jPanel6.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel6.setForeground(new java.awt.Color(204, 204, 204));

        java.util.ResourceBundle bundle2 = java.util.ResourceBundle.getBundle("i18n/ExamSettingsDialog"); // NOI18N
        jLabel7.setText(bundle2.getString("WordsByExam(Label)")); // NOI18N

        wordCountField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                wordCountFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                wordCountFieldFocusLost(evt);
            }
        });

        jPanel7.setBorder(javax.swing.BorderFactory.createEtchedBorder());

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

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(easyRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(mediumRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(hardRadioButton))
                    .addComponent(jLabel4))
                .addContainerGap(24, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel4)
                .addGap(3, 3, 3)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mediumRadioButton)
                    .addComponent(hardRadioButton)
                    .addComponent(easyRadioButton))
                .addGap(13, 13, 13))
        );

        jPanel8.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        timerCheckBox.setText(bundle2.getString("Countdown(Label)")); // NOI18N

        jLabel5.setText(bundle2.getString("TimeDependancy(Label)")); // NOI18N

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(timerCheckBox)
                    .addComponent(jLabel5))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(timerCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addContainerGap(17, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(wordCountField))
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(327, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(wordCountField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(182, Short.MAX_VALUE))
        );

        tabbedPane.addTab("Exam", new javax.swing.ImageIcon(getClass().getResource("/icons/16x16/blackboard.png")), jPanel6); // NOI18N

        okButton.setText("Ok");
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
                .addContainerGap(394, Short.MAX_VALUE)
                .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel4Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {cancelButton, okButton});

        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(okButton)
                    .addComponent(cancelButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 604, Short.MAX_VALUE)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 434, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void languageComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_languageComboBoxActionPerformed
        if (languageComboBox.getSelectedIndex() == 0) {
            selectedLanguage = SupportedLanguages.EN;
        } else {
            selectedLanguage = SupportedLanguages.BG;
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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox alwaysOnTopCheckBox;
    private javax.swing.JButton cancelButton;
    private javax.swing.JCheckBox clipboardIntegrationCheckBox;
    private javax.swing.JComboBox defaultDictionaryComboBox;
    private javax.swing.ButtonGroup difficultyButtonGroup;
    private javax.swing.JRadioButton easyRadioButton;
    private javax.swing.JList fontList;
    private javax.swing.JList fontSizeList;
    private javax.swing.JList fontStyleList;
    private javax.swing.JRadioButton hardRadioButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private static javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JComboBox languageComboBox;
    private javax.swing.JComboBox lookAndFeelComboBox;
    private javax.swing.JRadioButton mediumRadioButton;
    private javax.swing.JCheckBox minimizeToTrayCheckBox;
    private javax.swing.JCheckBox minimizeToTrayOnCloseCheckBox;
    private javax.swing.JButton okButton;
    private javax.swing.JTextField previewText;
    private javax.swing.JCheckBox showMemoryUsageCheckBox;
    private javax.swing.JTabbedPane tabbedPane;
    private static javax.swing.JCheckBox timerCheckBox;
    private javax.swing.JCheckBox trayPopupCheckBox;
    private javax.swing.JTextField wordCountField;
    // End of variables declaration//GEN-END:variables
}
