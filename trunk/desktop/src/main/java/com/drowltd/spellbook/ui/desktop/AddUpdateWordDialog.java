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
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author bozhidar
 */
public class AddUpdateWordDialog extends StandardDialog {
    private static final Translator TRANSLATOR = Translator.getTranslator("AddUpdateWordDialog");

    private JButton addButton;
    private JButton editButton;
    private JTextField newMeaningTextField;
    private JTextField wordTextField;
    private JTextPane translationPane;
    private Dictionary dictionary;
    private String toBeEdited;
    private Action selectLine;

    public AddUpdateWordDialog(Frame parent, boolean modal) {
        super(parent, modal);

        TRANSLATOR.reset();

        addButton = new JButton(TRANSLATOR.translate("Add(JButton)"));

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                translationPane.setText(translationPane.getText() + newMeaningTextField.getText() + "\n");
            }
        });

        editButton = new JButton(TRANSLATOR.translate("Edit(JButton)"));

        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (toBeEdited == null) {
                    return;
                }
                String editedText = translationPane.getText().replaceAll(toBeEdited, newMeaningTextField.getText());
                toBeEdited = newMeaningTextField.getText();
                translationPane.setText(editedText);
            }
        });

        wordTextField = new JTextField();
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
            }
        });

        setLocationRelativeTo(parent);

        setSize(400, 400);

        selectLine = getSelectLineAction();
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
        panel.add(addButton, "top");
        panel.add(new JLabel(TRANSLATOR.translate("Preview(TextFieldBorde)")), "span 2, left");
        panel.add(new JScrollPane(translationPane), "growx, growy");
        panel.add(editButton, "top");

        return panel;
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
