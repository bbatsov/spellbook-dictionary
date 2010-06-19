package com.drowltd.spellbook.ui.swing.component;

import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.dialog.StandardDialog;

import javax.swing.JComponent;
import java.awt.Frame;
import java.awt.HeadlessException;

/**
 * Base dialog class in Spellbook.
 *
 * @author Bozhidar Batsov
 * @since 0.4
 */
public class BaseDialog extends StandardDialog {
    protected Frame parent;

    public BaseDialog(Frame owner, boolean modal) throws HeadlessException {
        super(owner, modal);
        this.parent = owner;
    }

    @Override
    public JComponent createBannerPanel() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public JComponent createContentPanel() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ButtonPanel createButtonPanel() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int showDialog() {
        pack();

        // this should be called after pack()!
        setLocationRelativeTo(parent);

        setVisible(true);

        return getDialogResult();
    }
}
