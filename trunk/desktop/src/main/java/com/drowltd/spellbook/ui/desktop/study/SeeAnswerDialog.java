/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * seeAnswerDialog.java
 *
 * Created on 2010-5-9, 22:44:57
 */
package com.drowltd.spellbook.ui.desktop.study;

import com.drowltd.spellbook.core.i18n.Translator;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author Sasho
 */
public class SeeAnswerDialog extends javax.swing.JDialog {

    private JPanel topPanel;
    private JScrollPane seeAnswerScrollPane;
    private JTextPane seeAnswerTextPane;
    private JButton closeButton;
    private static final Translator TRANSLATOR = Translator.getTranslator("StudyWordsDialog");

    /** Creates new form seeAnswerDialog */
    public SeeAnswerDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }

    private void initComponents() {

        topPanel = new JPanel(new MigLayout("wrap 1", "[550]", "[250][30]"));
        setContentPane(topPanel);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(TRANSLATOR.translate("SeeAnswerDialog(Title)"));
        setResizable(false);

        seeAnswerScrollPane = new javax.swing.JScrollPane();
        seeAnswerTextPane = new javax.swing.JTextPane();
        seeAnswerTextPane.setEditable(false);
        seeAnswerScrollPane.add(seeAnswerTextPane);
        seeAnswerScrollPane.setViewportView(seeAnswerTextPane);
        topPanel.add(seeAnswerScrollPane,"w 550!, h 250!");

        closeButton = new javax.swing.JButton();
        closeButton.setText("close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });
        topPanel.add(closeButton, "w 80!,right");

        pack();
    }

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {
        setVisible(false);
    }

    public void setAnswer(String answer) {
        seeAnswerTextPane.setText(answer);
        seeAnswerTextPane.setCaretPosition(0);
    }
}
