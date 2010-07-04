package com.drowltd.spellbook.ui.swing.component;

import com.drowltd.spellbook.core.i18n.Translator;
import com.drowltd.spellbook.ui.swing.util.IconManager;
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
    public BaseDialog(Frame owner, boolean modal) throws HeadlessException {
        super(owner, modal);

        // reset translators on dialog creation
        getTranslator().reset();

        // init title and icon from resource bundle via translator
        setTitle(getTranslator().translate("Dialog(Title)"));
        setIconImage(IconManager.getImageIcon(getTranslator().translate("Dialog(Icon)"), IconManager.IconSize.SIZE16).getImage());
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
        setLocationRelativeTo(getParent());

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
