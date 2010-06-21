package com.drowltd.spellbook.ui.desktop;

import com.drowltd.spellbook.core.i18n.Translator;
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
 * Database synchronization dialog.
 *
 * @author Bozhidar Batsov
 * @since 0.4
 */
public class SyncDialog extends BaseDialog {
    private static final Translator TRANSLATOR = Translator.getTranslator("SyncDialog");

    public SyncDialog(Frame owner, boolean modal) throws HeadlessException {
        super(owner, modal);
    }

    @Override
    public JComponent createContentPanel() {
        JPanel mainPanel = new JPanel(new MigLayout("wrap 3", "[grow][grow][grow]", "[][][]"));

        mainPanel.add(new JLabel(TRANSLATOR.translate("LastSync(Label)")), "grow");

        JTextField lastSync = new JTextField();

        mainPanel.add(lastSync, "grow");

        JButton remotePullButton = new JButton(TRANSLATOR.translate("RemotePull(Button)"));

        mainPanel.add(remotePullButton, "grow");

        mainPanel.add(new JLabel(TRANSLATOR.translate("LocalChanges(Label)")), "growx");

        JTextField localChanges = new JTextField();

        mainPanel.add(localChanges, "grow");

        JButton submitLocalButton = new JButton(TRANSLATOR.translate("SubmitLocal(Button)"));

        mainPanel.add(submitLocalButton, "grow");

        return mainPanel;
    }

    @Override
    public ButtonPanel createButtonPanel() {
        ButtonPanel buttonPanel = new ButtonPanel(SwingConstants.RIGHT);

        JButton closeButton = new JButton();
        closeButton.setAction(new AbstractAction(TRANSLATOR.translate("Close(Button)")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                setDialogResult(RESULT_CANCELLED);
                setVisible(false);
            }
        });

        buttonPanel.addButton(closeButton);

        setDefaultCancelAction(closeButton.getAction());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return buttonPanel;
    }
}