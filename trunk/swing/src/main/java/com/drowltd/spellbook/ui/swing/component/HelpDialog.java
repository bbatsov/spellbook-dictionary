package com.drowltd.spellbook.ui.swing.component;

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

    public HelpDialog(Frame owner, boolean modal) throws HeadlessException {
        super(owner, modal);

        setTitle("Dialog(Title)");
    }

    @Override
    public JComponent createContentPanel() {
        String[] locations = new String[]{
                "jar:/docs/user_manual.pdf"
        };

        return new PdfHelpPanel(locations, false);
    }
}
