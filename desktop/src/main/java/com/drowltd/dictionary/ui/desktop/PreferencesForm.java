package com.drowltd.dictionary.ui.desktop;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

/**
 * User: bozhidar
 * Date: Oct 16, 2009
 * Time: 12:50:28 AM
 */
public class PreferencesForm {
    private JPanel topPanel;
    private JCheckBox minimizeToTrayCheckBox;
    private JComboBox languageBox;
    private JCheckBox clipboardIntegrationCheckBox;
    private JCheckBox minimizeToTrayOnCloseCheckBox;
    private JTextField examWordsField;
    private JComboBox lookAndFeelComboBox;

    private SupportedLanguages selectedLanguage;

    public PreferencesForm(final JFrame appFrame) {
        String[] languages = {"English", "Bulgarian"};

        languageBox.setModel(new DefaultComboBoxModel(languages));

        Preferences preferences = Preferences.userNodeForPackage(SpellbookApp.class);

        selectedLanguage = SupportedLanguages.valueOf(preferences.get("LANG", "EN"));

        // set the selected values from preferences
        languageBox.setSelectedItem(selectedLanguage == SupportedLanguages.EN ? "English" : "Bulgarian");

        minimizeToTrayCheckBox.setSelected(preferences.getBoolean("MIN_TO_TRAY", false));

        minimizeToTrayOnCloseCheckBox.setSelected(preferences.getBoolean("CLOSE_TO_TRAY", false));

        clipboardIntegrationCheckBox.setSelected(preferences.getBoolean("CLIPBOARD_INTEGRATION", false));

        languageBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (languageBox.getSelectedIndex() == 0) {
                    selectedLanguage = SupportedLanguages.EN;
                } else {
                    selectedLanguage = SupportedLanguages.BG;
                }
            }
        });

        // exam length in words
        examWordsField.setText("" + preferences.getInt("EXAM_WORDS", 10));

        // TODO implement a numeric document
        //examWordsField.setDocument();

        // build the look and feel section
        final LookAndFeelInfo[] lookAndFeelInfos = UIManager.getInstalledLookAndFeels();
        String[] lookAndFeelNames = new String[lookAndFeelInfos.length + 1];
        lookAndFeelNames[0] = "System";

        for (int i = 0; i < lookAndFeelInfos.length; i++) {
            lookAndFeelNames[i + 1] = lookAndFeelInfos[i].getName();
        }

        lookAndFeelComboBox.setModel(new DefaultComboBoxModel(lookAndFeelNames));

        lookAndFeelComboBox.setSelectedItem(preferences.get("LOOK_AND_FEEL", "System"));

        lookAndFeelComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selectedLookAndFeel = (String) lookAndFeelComboBox.getSelectedItem();

                for (LookAndFeelInfo lookAndFeelInfo : lookAndFeelInfos) {
                    if (lookAndFeelInfo.getName().equals(selectedLookAndFeel)) {
                        try {
                            UIManager.setLookAndFeel(lookAndFeelInfo.getClassName());
                        } catch (ClassNotFoundException e1) {
                            e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        } catch (InstantiationException e1) {
                            e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        } catch (IllegalAccessException e1) {
                            e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        } catch (UnsupportedLookAndFeelException e1) {
                            e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                    }
                }

                SwingUtilities.updateComponentTreeUI(topPanel);
                SwingUtilities.updateComponentTreeUI(appFrame);
            }
        });
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

    public boolean isMinimizeToTrayOnCloseEnabled() {
        return minimizeToTrayOnCloseCheckBox.isSelected();
    }

    public int getExamWords() {
        return Integer.parseInt(examWordsField.getText());
    }

    public String getSelectedLookAndFeel() {
        return (String) lookAndFeelComboBox.getSelectedItem();
    }

    public JComponent getComponent() {
        return topPanel;
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
        topPanel.setLayout(new FormLayout("fill:120dlu:noGrow,left:4dlu:noGrow,fill:80dlu:noGrow", "center:20dlu:noGrow,top:4dlu:noGrow,center:20dlu:noGrow,top:4dlu:noGrow,center:20dlu:noGrow,top:4dlu:noGrow,center:20dlu:noGrow,top:4dlu:noGrow,center:15dlu:noGrow,top:4dlu:noGrow,center:15dlu:noGrow"));
        final JLabel label1 = new JLabel();
        this.$$$loadLabelText$$$(label1, ResourceBundle.getBundle("i18n/PreferencesForm").getString("Language(Label)"));
        CellConstraints cc = new CellConstraints();
        topPanel.add(label1, cc.xy(1, 1));
        final JLabel label2 = new JLabel();
        this.$$$loadLabelText$$$(label2, ResourceBundle.getBundle("i18n/PreferencesForm").getString("MinimizeToTray(Label)"));
        topPanel.add(label2, cc.xy(1, 3));
        minimizeToTrayCheckBox = new JCheckBox();
        minimizeToTrayCheckBox.setText("");
        topPanel.add(minimizeToTrayCheckBox, cc.xy(3, 3, CellConstraints.CENTER, CellConstraints.DEFAULT));
        languageBox = new JComboBox();
        topPanel.add(languageBox, cc.xy(3, 1));
        final JLabel label3 = new JLabel();
        this.$$$loadLabelText$$$(label3, ResourceBundle.getBundle("i18n/PreferencesForm").getString("ClipboardIntegration(Label)"));
        topPanel.add(label3, cc.xy(1, 5));
        clipboardIntegrationCheckBox = new JCheckBox();
        clipboardIntegrationCheckBox.setText("");
        topPanel.add(clipboardIntegrationCheckBox, cc.xy(3, 5, CellConstraints.CENTER, CellConstraints.DEFAULT));
        final JLabel label4 = new JLabel();
        this.$$$loadLabelText$$$(label4, ResourceBundle.getBundle("i18n/PreferencesForm").getString("CloseToTray(Label)"));
        topPanel.add(label4, cc.xy(1, 7));
        minimizeToTrayOnCloseCheckBox = new JCheckBox();
        minimizeToTrayOnCloseCheckBox.setText("");
        topPanel.add(minimizeToTrayOnCloseCheckBox, cc.xy(3, 7, CellConstraints.CENTER, CellConstraints.DEFAULT));
        final JLabel label5 = new JLabel();
        this.$$$loadLabelText$$$(label5, ResourceBundle.getBundle("i18n/PreferencesForm").getString("ExamWords(Label)"));
        topPanel.add(label5, cc.xy(1, 9, CellConstraints.LEFT, CellConstraints.DEFAULT));
        examWordsField = new JTextField();
        examWordsField.setText(ResourceBundle.getBundle("i18n/PreferencesForm").getString("ExamWords(ToolTip)"));
        topPanel.add(examWordsField, cc.xy(3, 9, CellConstraints.FILL, CellConstraints.DEFAULT));
        final JLabel label6 = new JLabel();
        this.$$$loadLabelText$$$(label6, ResourceBundle.getBundle("i18n/PreferencesForm").getString("LookAndFeel(Label)"));
        topPanel.add(label6, cc.xy(1, 11));
        lookAndFeelComboBox = new JComboBox();
        topPanel.add(lookAndFeelComboBox, cc.xy(3, 11));
        label1.setLabelFor(languageBox);
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
    public JComponent $$$getRootComponent$$$() {
        return topPanel;
    }
}
