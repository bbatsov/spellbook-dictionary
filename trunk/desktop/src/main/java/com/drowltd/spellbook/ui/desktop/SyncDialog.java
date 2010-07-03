package com.drowltd.spellbook.ui.desktop;

import com.drowltd.spellbook.core.service.SynchronizeService;
import com.drowltd.spellbook.ui.swing.component.BaseDialog;
import com.drowltd.spellbook.ui.swing.util.IconManager;
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
import java.awt.event.ActionListener;

/**
 * Database synchronization dialog.
 *
 * @author Bozhidar Batsov
 * @since 0.4
 */
public class SyncDialog extends BaseDialog {
    private static final SynchronizeService SYNCHRONIZE_SERVICE = SynchronizeService.getInstance();

    public SyncDialog(Frame owner, boolean modal) throws HeadlessException {
        super(owner, modal);

        setTitle(getTranslator().translate("Dialog(Title)"));
        setIconImage(IconManager.getMenuIcon("replace2.png").getImage());
    }

    @Override
    public JComponent createContentPanel() {
        JPanel mainPanel = new JPanel(new MigLayout("wrap 3", "[grow][grow][grow]", "[][][]"));

        mainPanel.add(new JLabel(getTranslator().translate("LastSync(Label)")), "grow");

        JTextField lastSync = new JTextField();
        lastSync.setText(SYNCHRONIZE_SERVICE.getLastSyncDate() == null ? "n/a" : SYNCHRONIZE_SERVICE.getLastSyncDate().toString());
        lastSync.setEditable(false);

        mainPanel.add(lastSync, "grow");

        JButton remotePullButton = new JButton(getTranslator().translate("RemotePull(Button)"));
        remotePullButton.setIcon(IconManager.getImageIcon("download.png", IconManager.IconSize.SIZE24));

        remotePullButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SYNCHRONIZE_SERVICE.pullUpdates();
            }
        });

        mainPanel.add(remotePullButton, "grow");

        mainPanel.add(new JLabel(getTranslator().translate("LocalChanges(Label)")), "growx");

        JTextField localChanges = new JTextField();
        localChanges.setText(SYNCHRONIZE_SERVICE.getNumberOfLocalChanges() + "");
        localChanges.setEditable(false);

        mainPanel.add(localChanges, "grow");

        JButton submitLocalButton = new JButton(getTranslator().translate("SubmitLocal(Button)"));
        submitLocalButton.setIcon(IconManager.getImageIcon("upload.png", IconManager.IconSize.SIZE24));


        submitLocalButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SYNCHRONIZE_SERVICE.pushUpdates();
            }
        });

        mainPanel.add(submitLocalButton, "grow");

        return mainPanel;
    }

    @Override
    public ButtonPanel createButtonPanel() {
        ButtonPanel buttonPanel = new ButtonPanel(SwingConstants.RIGHT);

        JButton closeButton = new JButton();
        closeButton.setAction(new AbstractAction(getTranslator().translate("Close(Button)")) {
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
