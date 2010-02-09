package com.drowltd.dictionary.ui.desktop;

import com.drowltd.dictionary.core.i18n.Translator;
import java.awt.Desktop;
import java.io.IOException;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.HyperlinkEvent;

/**
 *
 * @author Aleksandar Vulchev
 */
public class AboutDialog extends javax.swing.JDialog {

    private static final Translator TRANSLATOR = Translator.getTranslator("Team");
    private static final Translator TRANSLATOR_TEXT = Translator.getTranslator("AboutDialog");

    /** Creates new form AboutDialog */
    public AboutDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);

        TRANSLATOR.reset();
        TRANSLATOR_TEXT.reset();

        initComponents();

        /** Checks whether desktop is supported and enable button that launch browser */
        if (Desktop.isDesktopSupported()) {
            if (Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                licenseButton.setEnabled(true);
            }
        }

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        creditsButton = new javax.swing.JButton();
        licenseButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();
        nameAndLogoPanel = new javax.swing.JPanel();
        logoLabel = new javax.swing.JLabel();
        spellbookLabel = new javax.swing.JLabel();
        dictionaryLabel = new javax.swing.JLabel();
        versionLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        infoTextPane = new javax.swing.JTextPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("i18n/AboutDialog"); // NOI18N
        setTitle(bundle.getString("About(Title)")); // NOI18N
        setResizable(false);

        creditsButton.setText(bundle.getString("Credits(Button)")); // NOI18N
        creditsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                creditsButtonActionPerformed(evt);
            }
        });

        licenseButton.setText(bundle.getString("License(Button)")); // NOI18N
        licenseButton.setEnabled(false);
        licenseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                licenseButtonActionPerformed(evt);
            }
        });

        closeButton.setText(bundle.getString("Close(Button)")); // NOI18N
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        nameAndLogoPanel.setBackground(new java.awt.Color(231, 243, 250));

        logoLabel.setBackground(new java.awt.Color(255, 255, 255));
        logoLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/logo/spellbook.png"))); // NOI18N

        spellbookLabel.setFont(new java.awt.Font("All Times New Roman", 0, 36));
        spellbookLabel.setForeground(new java.awt.Color(0, 0, 102));
        spellbookLabel.setText("Spellbook");

        dictionaryLabel.setFont(new java.awt.Font("All Times New Roman", 0, 24));
        dictionaryLabel.setForeground(new java.awt.Color(0, 0, 102));
        dictionaryLabel.setText(bundle.getString("Dictionary(Label)")); // NOI18N

        versionLabel.setFont(new java.awt.Font("All Times New Roman", 2, 14));
        versionLabel.setForeground(new java.awt.Color(0, 0, 102));
        versionLabel.setText(bundle.getString("Version(Label)")); // NOI18N

        javax.swing.GroupLayout nameAndLogoPanelLayout = new javax.swing.GroupLayout(nameAndLogoPanel);
        nameAndLogoPanel.setLayout(nameAndLogoPanelLayout);
        nameAndLogoPanelLayout.setHorizontalGroup(
            nameAndLogoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(nameAndLogoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(logoLabel)
                .addGap(18, 18, 18)
                .addGroup(nameAndLogoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(spellbookLabel)
                    .addGroup(nameAndLogoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                        .addComponent(versionLabel)
                        .addComponent(dictionaryLabel)))
                .addGap(50, 50, 50))
        );
        nameAndLogoPanelLayout.setVerticalGroup(
            nameAndLogoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(nameAndLogoPanelLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(nameAndLogoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(nameAndLogoPanelLayout.createSequentialGroup()
                        .addComponent(spellbookLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dictionaryLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(versionLabel))
                    .addComponent(logoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(28, Short.MAX_VALUE))
        );

        jScrollPane1.setBorder(null);

        infoTextPane.setBackground(new java.awt.Color(224, 223, 227));
        infoTextPane.setBorder(null);
        infoTextPane.setContentType("text/html");
        infoTextPane.setEditable(false);
        infoTextPane.setFont(new java.awt.Font("All Times New Roman", 0, 11));
        String text = String.format("<html>\n  <head>\n\n  </head>\n  <body>\n    <p style=\"margin-top: 0\"; style=\"text-align:center\">\n      \t\n\t%s<br />\n\t%s<br />\n\t%s\n    </p>\n  </body>\n</html>\n",
            TRANSLATOR_TEXT.translate("Multiplatform"),
            TRANSLATOR_TEXT.translate("License"),
            TRANSLATOR_TEXT.translate("Copyright"));
        infoTextPane.setText(text);
        infoTextPane.setCaret(null);
        infoTextPane.addHyperlinkListener(new javax.swing.event.HyperlinkListener() {
            public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {
                infoTextPaneHyperlinkUpdate(evt);
            }
        });
        jScrollPane1.setViewportView(infoTextPane);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(nameAndLogoPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 349, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(56, 56, 56)
                .addComponent(creditsButton)
                .addGap(18, 18, 18)
                .addComponent(licenseButton)
                .addGap(18, 18, 18)
                .addComponent(closeButton)
                .addContainerGap(56, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 329, Short.MAX_VALUE)
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {closeButton, creditsButton, licenseButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(nameAndLogoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(licenseButton)
                    .addComponent(creditsButton)
                    .addComponent(closeButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        setVisible(false);
    }//GEN-LAST:event_closeButtonActionPerformed

    private void licenseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_licenseButtonActionPerformed
        URL license = AboutDialog.class.getResource("/gplv3/gpl.html");

        try {
            Desktop.getDesktop().browse(license.toURI());
        } catch (URISyntaxException ex) {
            Logger.getLogger(AboutDialog.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AboutDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_licenseButtonActionPerformed

    private void creditsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_creditsButtonActionPerformed
        String team = String.format("<html>\n  <head>\n\n  </head>\n  <body>\n    <p style=\"margin-top: 0\"; style=\"text-align:center\">\n"
                + "\n\t %s <br />"
                + "\n\t <a href=\"mailto:bozhidar@drowltd.com?subject=Spellbook\">%s</a> <br />"
                + "\n\t %s <br />"
                + "\n\t <a href=\"mailto:iivalchev@gmail.com?subject=Spellbook\">%s</a> <br />"
                + "\n\t %s <br />"
                + "\n\t <a \nhref=\"mailto:frankeys89@gmail.com?subject=Spellbook\">%s</a> <br />"
                + "\n\t <a href=\"mailto:mireflame@gmail.com?subject=Spellbook\">%s</a> <br />"
                + "\n\t <a \nhref=\"mailto:george.angelow@gmail.com?subject=Spellbook\">%s</a> <br />"
                + "\n\t <a href=\"mailto:bmeshkova@gmail.com?subject=Spellbook\">%s</a> <br />"
                + "\n\t <a \nhref=\"mailto:cvetie@abv.bg?subject=Spellbook\">%s</a> <br />"
                + "\n\t <a href=\"mailto:nikolay@dimitrovi.net?subject=Spellbook\">%s</a> <br />"
                + "\n\t %s <br />"
                + "\n\t <a href=\"mailto:strannika@gmail.com?subject=Spellbook\">%s</a> <br />"
                + "\n\t   \n\t</p>\n\t\n  </body>\n</html>\n",
                TRANSLATOR.translate("ProjectLead"),
                TRANSLATOR.translate("BozhidarBatsov"),
                TRANSLATOR.translate("CoreUiSpellcheck"),
                TRANSLATOR.translate("IvanValchev"),
                TRANSLATOR.translate("ExamModul"),
                TRANSLATOR.translate("IvanSpasov"),
                TRANSLATOR.translate("MiroslavaStancheva"),
                TRANSLATOR.translate("GeorgiAngelov"),
                TRANSLATOR.translate("BilyanaMeshkova"),
                TRANSLATOR.translate("TsvetelinaNikolova"),
                TRANSLATOR.translate("NikolayDimitrov"),
                TRANSLATOR.translate("AboutDialog"),
                TRANSLATOR.translate("AlexanderValchev"));

        infoTextPane.setText(team);
    }//GEN-LAST:event_creditsButtonActionPerformed

    private void infoTextPaneHyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {//GEN-FIRST:event_infoTextPaneHyperlinkUpdate
        if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            try {
                Desktop.getDesktop().mail(evt.getURL().toURI());
            } catch (IOException ex) {
                Logger.getLogger(AboutDialog.class.getName()).log(Level.SEVERE, null, ex);
            } catch (URISyntaxException ex) {
                Logger.getLogger(AboutDialog.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_infoTextPaneHyperlinkUpdate
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JButton creditsButton;
    private javax.swing.JLabel dictionaryLabel;
    private javax.swing.JTextPane infoTextPane;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton licenseButton;
    private javax.swing.JLabel logoLabel;
    private javax.swing.JPanel nameAndLogoPanel;
    private javax.swing.JLabel spellbookLabel;
    private javax.swing.JLabel versionLabel;
    // End of variables declaration//GEN-END:variables
}
