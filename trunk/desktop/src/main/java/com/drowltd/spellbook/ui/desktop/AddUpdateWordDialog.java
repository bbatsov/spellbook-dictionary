/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * AddWordToDb.java
 *
 * Created on Jan 8, 2010, 7:54:01 PM
 */
package com.drowltd.spellbook.ui.desktop;

import com.drowltd.spellbook.core.i18n.Translator;
import com.drowltd.spellbook.core.model.Dictionary;
import com.drowltd.spellbook.core.service.DictionaryService;
import com.drowltd.spellbook.ui.swing.component.BaseDialog;
import com.drowltd.spellbook.ui.swing.validation.ButtonControllingDocumentListener;
import com.jidesoft.dialog.BannerPanel;
import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.dialog.ButtonResources;
import com.jidesoft.icons.JideIconsFactory;
import com.jidesoft.swing.DefaultOverlayable;
import com.jidesoft.swing.OverlayTextField;
import com.jidesoft.swing.OverlayableIconsFactory;
import com.jidesoft.swing.OverlayableUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author bozhidar
 */
public class AddUpdateWordDialog extends BaseDialog {

    private static final Translator TRANSLATOR = Translator.getTranslator("AddUpdateWordDialog");
    private static final Translator STANDARD_DIALOG_TRANSLATOR = Translator.getTranslator("StandardDialog");
    private List<String> translationRows = new ArrayList<String>();
    private boolean whetherAddWord = false;
    private JButton addButton;
    private JTextField newMeaningTextField;
    private JTextField wordTextField;
    private JLabel wordTextFieldValidationLabel = new JLabel();
    private JTextPane translationPane;
    private Dictionary dictionary;
    private JButton okButton;
    private static final int FONT_SIZE = 11;

    private static final DictionaryService DICTIONARY_SERVICE = DictionaryService.getInstance();

    public AddUpdateWordDialog(Frame parent, boolean modal, boolean add) {
        super(parent, modal);

        TRANSLATOR.reset();

        addButton = new JButton(TRANSLATOR.translate("Add(JButton)"));

        addButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!translationRows.contains(newMeaningTextField.getText())) {
                    translationRows.add(newMeaningTextField.getText());
                    if (translationPane.getText().isEmpty()) {
                        translationPane.setText(translationPane.getText() + newMeaningTextField.getText());
                    } else {
                        translationPane.setText(translationPane.getText() + "\n" + newMeaningTextField.getText());
                    }
                }
                newMeaningTextField.selectAll();
                newMeaningTextField.requestFocus();
            }
        });

        wordTextField = new OverlayTextField();

        okButton = new JButton();

        wordTextField.getDocument().addDocumentListener(new ButtonControllingDocumentListener(wordTextField, okButton));

        // on add we add some special notifications for existing words
        if (add) {
            wordTextField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    checkForWordExistence();
                    //TODO check language as well
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    checkForWordExistence();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    //To change body of implemented methods use File | Settings | File Templates.
                }
            });
        } else {
            // on update only the translations can be updated
            wordTextField.setEditable(false);
        }

        newMeaningTextField = new JTextField();
        translationPane = new JTextPane();

        Action doNothing = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                //do nothing
            }
        };
        translationPane.getInputMap().put(KeyStroke.getKeyStroke("ENTER"),
                "doNothing");
        translationPane.getActionMap().put("doNothing",
                doNothing);

        setLocationRelativeTo(parent);

        setSize(500, 500);

    }

    private void checkForWordExistence() {
        if (DICTIONARY_SERVICE.containsWord(wordTextField.getText(), dictionary)) {
            wordTextFieldValidationLabel.setIcon(OverlayableUtils.getPredefinedOverlayIcon(OverlayableIconsFactory.ERROR));
            okButton.setEnabled(false);
        } else {
            wordTextFieldValidationLabel.setIcon(OverlayableUtils.getPredefinedOverlayIcon(OverlayableIconsFactory.CORRECT));
            okButton.setEnabled(true);
        }
    }

    @Override
    public JComponent createBannerPanel() {
        BannerPanel bannerPanel = new BannerPanel(TRANSLATOR.translate("BannerTitle(Message)"),
                TRANSLATOR.translate("Banner(Message)"),
                JideIconsFactory.getImageIcon("/icons/48x48/pencil.png"));
        bannerPanel.setFont(new Font("Tahoma", Font.PLAIN, FONT_SIZE));
        bannerPanel.setBackground(Color.WHITE);
        return bannerPanel;
    }

    @Override
    public JComponent createContentPanel() {
        JPanel panel = new JPanel(new MigLayout("wrap 2", "[grow][]", "[][][][][][grow]"));
        if (whetherAddWord) {
            panel.add(new JLabel(TRANSLATOR.translate("AddWord(TextFieldBorder)")), "span 2, left");
            setTitle(TRANSLATOR.translate("AddDialogTitle(Title)"));
        } else {
            panel.add(new JLabel(TRANSLATOR.translate("EditWord(TextFieldBorder)")), "span 2, left");
            setTitle(TRANSLATOR.translate("UpdateDialogTitle(Title)"));
        }
        panel.add(new DefaultOverlayable(wordTextField, wordTextFieldValidationLabel, DefaultOverlayable.SOUTH_EAST), "span 2, growx, top");
        panel.add(new JLabel(TRANSLATOR.translate("AddMeaning(TextFieldBorder)")), "span 2, left");
        panel.add(newMeaningTextField, "growx, top");
        panel.add(addButton, "w 73::,gapright 2,top");
        panel.add(new JLabel(TRANSLATOR.translate("EditMeaning(TextFieldBorde)")), "span 2, left");
        panel.add(new JScrollPane(translationPane), "span 2,grow");

        return panel;
    }

    @Override
    public ButtonPanel createButtonPanel() {
        ButtonPanel buttonPanel = new ButtonPanel();
        JButton cancelButton = new JButton();
        JButton helpButton = new JButton();
        okButton.setName(OK);
        cancelButton.setName(CANCEL);
        cancelButton.setText("sasho");
        helpButton.setName(HELP);
        buttonPanel.addButton(okButton, ButtonPanel.AFFIRMATIVE_BUTTON);
        buttonPanel.addButton(cancelButton, ButtonPanel.CANCEL_BUTTON);
        buttonPanel.addButton(helpButton, ButtonPanel.HELP_BUTTON);

        okButton.setAction(new AbstractAction(STANDARD_DIALOG_TRANSLATOR.translate("OK(JButton)")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (translationPane.getText().isEmpty()) {
                    newMeaningTextField.requestFocus();
                } else {
                    setDialogResult(RESULT_AFFIRMED);
                    setVisible(false);
                    dispose();
                }
            }
        });
        cancelButton.setAction(new AbstractAction(STANDARD_DIALOG_TRANSLATOR.translate("Cancel(JButton)")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                setDialogResult(RESULT_CANCELLED);
                setVisible(false);
                dispose();
            }
        });
        final ResourceBundle resourceBundle = ButtonResources.getResourceBundle(Locale.getDefault());
        helpButton.setAction(new AbstractAction(STANDARD_DIALOG_TRANSLATOR.translate("Help(JButton)")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                // do something
            }
        });
        helpButton.setMnemonic(resourceBundle.getString("Button.help.mnemonic").charAt(0));

        setDefaultCancelAction(cancelButton.getAction());
        setDefaultAction(okButton.getAction());
        getRootPane().setDefaultButton(okButton);
        if (wordTextField.getText().isEmpty()) {
            okButton.setEnabled(false);
        }
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return buttonPanel;
    }

    public void setWhetherAddWord(boolean whetherAddWord) {
        this.whetherAddWord = whetherAddWord;
    }

    public void setDictionary(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    public Dictionary getDictionary() {
        return dictionary;
    }

    public void setWord(String word) {
        wordTextField.setText(word);
    }

    public String getWord() {
        return wordTextField.getText();
    }

    public void setTranslation(String translation) {
        if (translation.subSequence(translation.length() - 2, translation.length() - 1).equals("\n")) {
            translation = translation.substring(0, translation.length() - 3); //delete last "\n"
        }
        translationPane.setText(translation);
        translationRows = splitTranslationOfRows(translation);
    }

    private List<String> splitTranslationOfRows(String translation) {
        List<String> rows = new ArrayList<String>();
        int endIndex = 0;
        while (translation.contains("\n")) {
            endIndex = translation.indexOf("\n");
            rows.add(translation.substring(0, endIndex));
            translation = translation.substring(endIndex + 1);
        }

        return rows;
    }

    public String getTranslation() {
        return translationPane.getText();
    }
}
