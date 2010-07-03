package com.drowltd.spellbook.ui.desktop;

import com.drowltd.spellbook.ui.swing.component.BaseDialog;
import com.jidesoft.dialog.ButtonPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;

/**
 * Import dictionary dialog.
 *
 * @author Bozhidar Batsov
 * @since 0.4
 */
public class ImportDialog extends BaseDialog {
    public ImportDialog(Frame owner, boolean modal) throws HeadlessException {
        super(owner, modal);
    }

    @Override
    public JComponent createContentPanel() {
        JPanel mainPanel = new JPanel(new MigLayout("wrap 3", "[grow][grow][grow]", "[][][]"));

        mainPanel.add(new JLabel(getTranslator().translate("DictionaryFile(Label)")), "grow");

        JTextField dictionaryFile = new JTextField();

        mainPanel.add(dictionaryFile, "growx");

        JButton selectDictionaryFileButton = new JButton(getTranslator().translate("SelectFile(Button)"));

        mainPanel.add(selectDictionaryFileButton, "growx");

        mainPanel.add(new JLabel(getTranslator().translate("DictionaryName(Label)")), "growx");

        JTextField dictionaryName = new JTextField();

        mainPanel.add(dictionaryName, "span 2, growx");

        mainPanel.add(new JLabel(getTranslator().translate("DictionaryIcon(Label)")), "growx");

        JTextField dictionaryIcon = new JTextField();

        mainPanel.add(dictionaryIcon, "growx");

        JButton selectIconButton = new JButton(getTranslator().translate("SelectIcon(Button)"));

        mainPanel.add(selectIconButton, "growx");

        return mainPanel;
    }

    @Override
    public ButtonPanel createButtonPanel() {
        ButtonPanel buttonPanel = new ButtonPanel(SwingConstants.RIGHT);

        JButton importButton = new JButton();

        importButton.setAction(new AbstractAction(getTranslator().translate("Import(Button)")) {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

        JButton closeButton = new JButton();
        closeButton.setAction(new AbstractAction(getTranslator().translate("Close(Button)")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                setDialogResult(RESULT_CANCELLED);
                setVisible(false);
            }
        });

        buttonPanel.addButton(importButton);
        buttonPanel.addButton(closeButton);

        setDefaultCancelAction(closeButton.getAction());
        setDefaultAction(importButton.getAction());
        getRootPane().setDefaultButton(importButton);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return buttonPanel;
    }
}
