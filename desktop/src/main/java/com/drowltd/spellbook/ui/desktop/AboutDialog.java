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
import javax.swing.JLabel;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @author Bozhidar Batsov
 * @since 0.3
 */
public class AboutDialog extends StandardDialog {
    private static final Translator TEAM_TRANSLATOR = Translator.getTranslator("Team");
    private static final Translator TRANSLATOR = Translator.getTranslator("AboutDialog");
    private JTextPane infoTextPane = new JTextPane();
    private JButton licenceButton = new JButton();
    private static final int DIALOG_WIDTH = 500;
    private static final int DIALOG_HEIGHT = 450;
    private static final int FONT_SIZE = 11;

    public AboutDialog(Frame owner, boolean modal) throws HeadlessException {
        super(owner, modal);

        TEAM_TRANSLATOR.reset();
        TRANSLATOR.reset();

        infoTextPane.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    try {
                        final URI tUri = e.getURL().toURI();
                        if (tUri.getScheme().equals("http")) {
                            Desktop.getDesktop().browse(tUri);
                        } else {
                            Desktop.getDesktop().mail(tUri);
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    } catch (URISyntaxException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        infoTextPane.setContentType("text/html");
        infoTextPane.setEditable(false);
        infoTextPane.setText(TRANSLATOR.translate("About(Message)"));

        /** Checks whether desktop is supported and enable button that launch browser */
        if (Desktop.isDesktopSupported()) {
            if (Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                licenceButton.setEnabled(true);
            }
        }

        setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
        setResizable(false);
        setTitle(TRANSLATOR.translate("About(Title)"));
    }

    @Override
    public JComponent createBannerPanel() {
        BannerPanel bannerPanel = new BannerPanel("Spellbook Dictionary 0.3",
                TRANSLATOR.translate("Banner(Message)"),
                JideIconsFactory.getImageIcon("/images/spellbook-logo.png"));
        bannerPanel.setFont(new Font("Tahoma", Font.PLAIN, FONT_SIZE));
        bannerPanel.setBackground(Color.WHITE);
        bannerPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        return bannerPanel;
    }

    @Override
    public JComponent createContentPanel() {
        JPanel panel = new JPanel(new MigLayout("wrap 1", "[grow]", "[grow][]"));

        JScrollPane jScrollPane = new JScrollPane(infoTextPane);
        panel.add(jScrollPane, "grow");

        panel.add(new JLabel(TRANSLATOR.translate("Copyleft(Label)")), "center");

        return panel;
    }

    @Override
    public ButtonPanel createButtonPanel() {
        ButtonPanel buttonPanel = new ButtonPanel(SwingConstants.CENTER);


        JButton creditsButton = new JButton();

        creditsButton.setAction(new AbstractAction(TRANSLATOR.translate("Credits(Button)")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                creditsButtonActionPerformed();
            }
        });

        licenceButton.setAction(new AbstractAction(TRANSLATOR.translate("License(Button)")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                licenseButtonActionPerformed();
            }
        });

        JButton closeButton = new JButton();
        closeButton.setAction(new AbstractAction(TRANSLATOR.translate("Close(Button)")) {
            @Override
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
        URL license = AboutDialog.class.getResource("/gplv3/gpl.html");

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
                TEAM_TRANSLATOR.translate("ProjectLead"),
                TEAM_TRANSLATOR.translate("BozhidarBatsov"),
                TEAM_TRANSLATOR.translate("CoreUiSpellcheck"),
                TEAM_TRANSLATOR.translate("IvanValchev"),
                TEAM_TRANSLATOR.translate("ExamModule"),
                TEAM_TRANSLATOR.translate("IvanSpasov"),
                TEAM_TRANSLATOR.translate("MiroslavaStancheva"),
                TEAM_TRANSLATOR.translate("GeorgiAngelov"),
                TEAM_TRANSLATOR.translate("StudyModule"),
                TEAM_TRANSLATOR.translate("AlexanderNikolov"));

        infoTextPane.setText(team);
        infoTextPane.setCaretPosition(0);
    }
}
