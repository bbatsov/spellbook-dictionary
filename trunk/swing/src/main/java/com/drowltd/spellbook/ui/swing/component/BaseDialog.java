package com.drowltd.spellbook.ui.swing.component;

import com.drowltd.spellbook.core.i18n.Translator;
import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.dialog.StandardDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JComponent;
import java.awt.Frame;
import java.awt.HeadlessException;

/**
 * Base dialog class in Spellbook.
 *
 * @author Bozhidar Batsov
 * @since 0.4
 */
public abstract class BaseDialog extends StandardDialog {
    protected Frame parent;

    public BaseDialog(Frame owner, boolean modal) throws HeadlessException {
        super(owner, modal);
        this.parent = owner;

        getTranslator().reset();
        setTitle(getTranslator().translate("Dialog(Title)"));
    }

    @Override
    public JComponent createBannerPanel() {
        return null;
    }

    @Override
    public ButtonPanel createButtonPanel() {
        return null;
    }

    public int showDialog() {
        pack();

        // this should be called after pack()!
        setLocationRelativeTo(parent);

        setVisible(true);

        return getDialogResult();
    }

    public Translator getTranslator() {
        return Translator.getTranslator(this.getClass().getSimpleName());
    }

    public Logger getLogger() {
        return LoggerFactory.getLogger(this.getClass());
    }
}
