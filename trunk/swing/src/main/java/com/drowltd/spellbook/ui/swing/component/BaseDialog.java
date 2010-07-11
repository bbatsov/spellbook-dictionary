package com.drowltd.spellbook.ui.swing.component;

import com.drowltd.spellbook.core.i18n.Translator;
import com.drowltd.spellbook.ui.swing.util.IconManager;
import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.dialog.StandardDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.event.ActionEvent;

/**
 * Base dialog class in Spellbook.
 *
 * @author Bozhidar Batsov
 * @since 0.4
 */
public abstract class BaseDialog extends StandardDialog {
    public BaseDialog(Frame owner, boolean modal) throws HeadlessException {
        super(owner, modal);

        init();
    }

    public BaseDialog(Dialog owner, boolean modal) throws HeadlessException {
        super(owner, modal);

        init();
    }

    private void init() {
        // reset translators on dialog creation
        getTranslator().reset();

        // init title and icon from resource bundle via translator
        setTitle(getTranslator().translate("Dialog(Title)"));
        final Image image = IconManager.getImageIcon(getTranslator().translate("Dialog(Icon)"), IconManager.IconSize.SIZE16).getImage();

        if (image != null) {
            setIconImage(image);
        } else {
            setIconImage(IconManager.getMenuIcon("dictionary.png").getImage());
        }
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

    public Translator getBaseTranslator() {
        return Translator.getTranslator(BaseDialog.class.getSimpleName());
    }

    public Logger getLogger() {
        return LoggerFactory.getLogger(this.getClass());
    }

    protected JButton createOkButton() {
        JButton okButton = new JButton();

        okButton.setAction(new AbstractAction(getBaseTranslator().translate("OK(Button)")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                setDialogResult(RESULT_AFFIRMED);
                setVisible(false);
            }
        });

        return okButton;
    }

    protected JButton createCancelButton() {
        JButton closeButton = new JButton();

        closeButton.setAction(new AbstractAction(getBaseTranslator().translate("Cancel(Button)")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                setDialogResult(RESULT_CANCELLED);
                setVisible(false);
            }
        });

        return closeButton;
    }

    protected JButton createCloseButton() {
        JButton closeButton = new JButton();

        closeButton.setAction(new AbstractAction(getBaseTranslator().translate("Close(Button)")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                setDialogResult(RESULT_CANCELLED);
                setVisible(false);
            }
        });

        return closeButton;
    }

    protected JButton createHelpButton() {
        JButton helpButton = new JButton();

        helpButton.setAction(new AbstractAction(getBaseTranslator().translate("Help(Button)")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                // do something
            }
        });

        return helpButton;
    }
}
