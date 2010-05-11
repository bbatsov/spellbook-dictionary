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
import com.drowltd.spellbook.ui.swing.validation.ButtonControllingDocumentListener;
import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.dialog.ButtonResources;
import com.jidesoft.dialog.StandardDialog;
import com.jidesoft.plaf.UIDefaultsLookup;
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
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultEditorKit;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author bozhidar
 */
public class AddUpdateWordDialog extends StandardDialog {

    private static final Translator TRANSLATOR = Translator.getTranslator("AddUpdateWordDialog");
    private boolean wetherToChange = false;
    private JButton saveButton;
    private JTextField newMeaningTextField;
    private JTextField wordTextField;
    private JTextPane translationPane;
    private Dictionary dictionary;
    private String toBeEdited;
    private Action selectLine;
    private JButton okButton;

    public AddUpdateWordDialog(Frame parent, boolean modal) {
        super(parent, modal);

        TRANSLATOR.reset();

        saveButton = new JButton(TRANSLATOR.translate("Save(JButton)"));

        saveButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                boolean isNullTextField = newMeaningTextField.getText().isEmpty();
                if (wetherToChange) {
                    String editedText = translationPane.getText().replaceAll(toBeEdited, newMeaningTextField.getText());
                    toBeEdited = newMeaningTextField.getText();
                    translationPane.setText(editedText);

                    if (isNullTextField) {  //enter in this block if we deleted a row
                        deleteEmptyRows();
                    }
                } else {
                    translationPane.setText(translationPane.getText() + newMeaningTextField.getText() + "\n");
                }
                wetherToChange = false;
            }
        });

        wordTextField = new JTextField();

        okButton = new JButton();

        wordTextField.getDocument().addDocumentListener(new ButtonControllingDocumentListener(wordTextField, okButton));

        newMeaningTextField = new JTextField();
        translationPane = new JTextPane();

        translationPane.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1) {
                    selectLine.actionPerformed(null);
                }

                toBeEdited = translationPane.getSelectedText();
                newMeaningTextField.setText(toBeEdited);
                wetherToChange = true;
            }
        });

        translationPane.setEditable(false);

        setLocationRelativeTo(parent);

        setSize(400, 400);

        selectLine = getSelectLineAction();
    }

    private void deleteEmptyRows() {
        //enter in this block if we deleted a row
        String translation = translationPane.getText();
        String row = null;
        int endIndex = 0;
        List<String> rows = new ArrayList<String>();
        while (translation.contains("\n")) {
            endIndex = translation.indexOf("\n");
            row = translation.substring(0, endIndex);
            if (!row.isEmpty()) {
                rows.add(row);
            }
            translation = translation.substring(endIndex + 1);
        }
        StringBuilder newTranslation = new StringBuilder();
        for (String tRow : rows) {
            newTranslation.append(tRow + "\n");
        }
        translationPane.setText(newTranslation.toString());
    }

    @Override
    public JComponent createBannerPanel() {
        return null;
    }

    @Override
    public JComponent createContentPanel() {
        JPanel panel = new JPanel(new MigLayout("wrap 2", "[grow][]", "[][][][][][grow]"));

        panel.add(new JLabel(TRANSLATOR.translate("Word(TextFieldBorder)")), "span 2, left");
        panel.add(wordTextField, "span 2, growx, top");
        panel.add(new JLabel(TRANSLATOR.translate("Add/Edit(TextFieldBorder)")), "span 2, left");
        panel.add(newMeaningTextField, "growx, top");
        panel.add(saveButton, "w 73::,top");
        panel.add(new JLabel(TRANSLATOR.translate("Preview(TextFieldBorde)")), "span 2, left");
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
        helpButton.setName(HELP);
        buttonPanel.addButton(okButton, ButtonPanel.AFFIRMATIVE_BUTTON);
        buttonPanel.addButton(cancelButton, ButtonPanel.CANCEL_BUTTON);
        buttonPanel.addButton(helpButton, ButtonPanel.HELP_BUTTON);

        okButton.setAction(new AbstractAction(UIDefaultsLookup.getString("OptionPane.okButtonText")) {

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
        if (wordTextField.getText().isEmpty()) {
            okButton.setEnabled(false);
        }
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return buttonPanel;
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
        translationPane.setText(translation);
    }

    public String getTranslation() {
        return translationPane.getText();
    }

    private Action getSelectLineAction() {
        Action[] action = translationPane.getActions();

        for (int i = 0; i < action.length; i++) {

            if (action[i].getValue(Action.NAME).equals(DefaultEditorKit.selectLineAction)) {
                selectLine = action[i];
            }

        }

        return selectLine;
    }

    public static void main(String[] args) {
        AddUpdateWordDialog addUpdateWordDialog = new AddUpdateWordDialog(null, true);

        addUpdateWordDialog.setVisible(true);
    }
}
