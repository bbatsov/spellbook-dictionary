package com.drowltd.spellbook.ui.swing.component;

import com.drowltd.spellbook.core.i18n.Translator;
import com.idrsolutions.pdf.pdfhelp.PdfHelpPanel;

import javax.swing.JComponent;
import java.awt.Frame;
import java.awt.HeadlessException;

/**
 * Help contents dialog
 *
 * @author Bozhidar Batsov
 * @since 0.4
 */
public class HelpDialog extends BaseDialog {
    private static final Translator TRANSLATOR = Translator.getTranslator("HelpDialog");

    public HelpDialog(Frame owner, boolean modal) throws HeadlessException {
        super(owner, modal);

        setTitle(TRANSLATOR.translate("Dialog(Title)"));
    }

    @Override
    public JComponent createContentPanel() {
        String[] locations = new String[]{
                "jar:/docs/user_manual.pdf"
        };

        return new PdfHelpPanel(locations, false);
    }
}
