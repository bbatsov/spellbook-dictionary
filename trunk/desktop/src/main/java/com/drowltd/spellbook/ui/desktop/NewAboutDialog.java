package com.drowltd.spellbook.ui.desktop;

import com.drowltd.spellbook.core.i18n.Translator;
import com.jidesoft.dialog.BannerPanel;
import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.dialog.StandardDialog;
import com.jidesoft.icons.JideIconsFactory;
import net.miginfocom.swing.MigLayout;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @author Bozhidar Batsov
 * @since 0.3
 */
public class NewAboutDialog extends StandardDialog {
    private static final Translator TRANSLATOR = Translator.getTranslator("Team");
    private static final Translator TRANSLATOR_TEXT = Translator.getTranslator("AboutDialog");
    private JTextPane infoTextPane = new JTextPane();
    private JButton licenceButton = new JButton();

    public NewAboutDialog(Frame owner, boolean modal) throws HeadlessException {
        super(owner, modal);

        TRANSLATOR.reset();
        TRANSLATOR_TEXT.reset();

        infoTextPane.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    try {
                        Desktop.getDesktop().mail(e.getURL().toURI());
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    } catch (URISyntaxException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        infoTextPane.setContentType("text/html");
        infoTextPane.setText("Spellbook is a multiplatform dictionary application written in Java. Spellbook is open source and is developed by a team committed to the" +
                        "goal of creating a high quality application that will serve the user community as best as possible.");

        /** Checks whether desktop is supported and enable button that launch browser */
        if (Desktop.isDesktopSupported()) {
            if (Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                licenceButton.setEnabled(true);
            }
        }

        setSize(400, 500);
    }

    @Override
    public JComponent createBannerPanel() {
        BannerPanel headerPanel1 = new BannerPanel("Spellbook Dictionary 0.3",
                "Spellbook is a multiplatform dictionary application written in Java. Spellbook is open source and is developed by a team committed to the" +
                        "goal of creating a high quality application that will serve the user community as best as possible.",
                JideIconsFactory.getImageIcon("/images/spellbook-logo.png"));
        headerPanel1.setFont(new Font("Tahoma", Font.PLAIN, 11));
        headerPanel1.setBackground(Color.WHITE);
        headerPanel1.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        return headerPanel1;
    }

    @Override
    public JComponent createContentPanel() {
        JPanel panel = new JPanel(new MigLayout("wrap 1", "[grow]", "[grow]"));

        JScrollPane jScrollPane = new JScrollPane(infoTextPane);
        panel.add(jScrollPane, "growx");

        return panel;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ButtonPanel createButtonPanel() {
        ButtonPanel buttonPanel = new ButtonPanel(SwingConstants.CENTER);


        JButton creditsButton = new JButton();

        creditsButton.setAction(new AbstractAction("Credits") {
            public void actionPerformed(ActionEvent e) {
                creditsButtonActionPerformed();
            }
        });

        licenceButton.setAction(new AbstractAction("Licence") {
            public void actionPerformed(ActionEvent e) {
                licenseButtonActionPerformed();
            }
        });

        JButton closeButton = new JButton();
        closeButton.setAction(new AbstractAction("Close") {
            public void actionPerformed(ActionEvent e) {
                setDialogResult(RESULT_CANCELLED);
                setVisible(false);
                dispose();
            }
        });

        buttonPanel.addButton(creditsButton);
        buttonPanel.addButton(licenceButton);
        buttonPanel.addButton(closeButton);

        setDefaultCancelAction(closeButton.getAction());
        setDefaultAction(creditsButton.getAction());
        getRootPane().setDefaultButton(creditsButton);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return buttonPanel;
    }

    private void licenseButtonActionPerformed() {
        URL license = NewAboutDialog.class.getResource("/gplv3/gpl.html");

        try {
            Desktop.getDesktop().browse(license.toURI());
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void creditsButtonActionPerformed() {
        String team = String.format("<html>\n  <head>\n\n  </head>\n  <body>\n    <p style=\"margin-top: 0\"; style=\"text-align:center\">\n"
                + "\n\t <b>%s</b> <br />"
                + "\n\t <a href=\"mailto:bozhidar@drowltd.com?subject=Spellbook\">%s</a> <br />"
                + "\n\t <b>%s</b> <br />"
                + "\n\t <a href=\"mailto:iivalchev@gmail.com?subject=Spellbook\">%s</a> <br />"
                + "\n\t <b>%s</b> <br />"
                + "\n\t <a \nhref=\"mailto:frankeys89@gmail.com?subject=Spellbook\">%s</a> <br />"
                + "\n\t <a href=\"mailto:mireflame@gmail.com?subject=Spellbook\">%s</a> <br />"
                + "\n\t <a \nhref=\"mailto:george.angelow@gmail.com?subject=Spellbook\">%s</a> <br />"
                + "\n\t <b>%s</b> <br /> "
                + "\n\t <a href=\"mailto:AlexanderNikolovNikolov@gmail.com?subject=Spellbook\">%s</a> <br />"
                + "\n\t   \n\t</p>\n\t\n  </body>\n</html>\n",
                TRANSLATOR.translate("ProjectLead"),
                TRANSLATOR.translate("BozhidarBatsov"),
                TRANSLATOR.translate("CoreUiSpellcheck"),
                TRANSLATOR.translate("IvanValchev"),
                TRANSLATOR.translate("ExamModul"),
                TRANSLATOR.translate("IvanSpasov"),
                TRANSLATOR.translate("MiroslavaStancheva"),
                TRANSLATOR.translate("GeorgiAngelov"),
                TRANSLATOR.translate("StudyModule"),
                TRANSLATOR.translate("AlexanderNikolov"));

        infoTextPane.setText(team);
        infoTextPane.setCaretPosition(0);
    }

    public static void main(String[] args) {
        NewAboutDialog newAboutDialog = new NewAboutDialog(null, true);

        newAboutDialog.setVisible(true);
    }
}
