package bg.drow.spellbook.ui.desktop;

import bg.drow.spellbook.ui.swing.component.BaseDialog;
import com.jidesoft.dialog.ButtonPanel;
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
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.Desktop;
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
public class AboutDialog extends BaseDialog {
    private JTextPane infoTextPane = new JTextPane();
    private JButton licenceButton = new JButton();
    private static final int DIALOG_WIDTH = 500;
    private static final int DIALOG_HEIGHT = 500;

    public AboutDialog(Frame owner, boolean modal) throws HeadlessException {
        super(owner, modal);

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
        infoTextPane.setText(getTranslator().translate("About(Message)"));

        /** Checks whether desktop is supported and enable button that launch browser */
        if (Desktop.isDesktopSupported()) {
            if (Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                licenceButton.setEnabled(true);
            }
        }

        setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
        setResizable(false);
    }

    @Override
    public JComponent createContentPanel() {
        JPanel panel = new JPanel(new MigLayout("wrap 1", "[grow]", "[grow][]"));

        JScrollPane jScrollPane = new JScrollPane(infoTextPane);
        panel.add(jScrollPane, "grow");

        panel.add(new JLabel("Java Version: " + System.getProperty("java.version")));

        panel.add(new JLabel("JVM: " + System.getProperty("java.vm.name")));

        panel.add(new JLabel("Vendor: " + System.getProperty("java.vendor")));

        panel.add(new JLabel(getTranslator().translate("Copyleft(Label)")), "center");

        return panel;
    }

    @Override
    public ButtonPanel createButtonPanel() {
        ButtonPanel buttonPanel = new ButtonPanel(SwingConstants.CENTER);


        JButton creditsButton = new JButton();

        creditsButton.setAction(new AbstractAction(getTranslator().translate("Credits(Button)")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                creditsButtonActionPerformed();
            }
        });

        licenceButton.setAction(new AbstractAction(getTranslator().translate("License(Button)")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                licenseButtonActionPerformed();
            }
        });

        JButton closeButton = createCloseButton();

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
                + "\n\t <a href=\"mailto:bozhidar@drow.com?subject=Spellbook\">%s</a> <br /><br/>"
                + "\n\t <b>%s</b> <br />"
                + "\n\t <a href=\"mailto:iivalchev@gmail.com?subject=Spellbook\">%s</a> <br />"
                + "\n\t <a \nhref=\"mailto:ivan.hantov@gmail.com?subject=Spellbook\">%s</a> <br />"
                + "\n\t <a href=\"mailto:vasilsakarov@gmail.com?subject=Spellbook\">%s</a> <br />"
                + "\n\t <a href=\"mailto:AlexanderNikolovNikolov@gmail.com?subject=Spellbook\">%s</a> <br />"
                + "\n\t   \n\t</p>\n\t\n  </body>\n</html>\n",
                getTranslator().translate("ProjectLead(Label)"),
                getTranslator().translate("BozhidarBatsov(TeamMember)"),
                getTranslator().translate("Team(Label)"),
                getTranslator().translate("IvanValchev(TeamMember)"),
                getTranslator().translate("IvanHantov(TeamMember)"),
                getTranslator().translate("VasilSakarov(TeamMember)"),
                getTranslator().translate("AlexanderNikolov(TeamMember)"));

        infoTextPane.setText(team);
        infoTextPane.setCaretPosition(0);
    }
}
