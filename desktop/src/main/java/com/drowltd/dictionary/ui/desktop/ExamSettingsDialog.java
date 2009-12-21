/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ExamSettingsDialog.java
 *
 * Created on 2009-11-30, 17:46:36
 */
package com.drowltd.dictionary.ui.desktop;

import java.util.prefs.Preferences;
import javax.swing.ButtonGroup;

/**
 *
 * @author Snow
 */
public class ExamSettingsDialog extends javax.swing.JDialog {

    Preferences PREFS = Preferences.userNodeForPackage(SpellbookApp.class);

    private static int seconds;
    private static int wordCount;
    private static boolean isOpen = false;
    private static String difficulty;

    /** Creates new form ExamSettingsDialog */
    public ExamSettingsDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        

        ButtonGroup difficultyGroup = new ButtonGroup();

        difficultyGroup.add(easyRadioButton);
        difficultyGroup.add(mediumRadioButton);
        difficultyGroup.add(hardRadioButton);

        

        setTitle("Spellbook Exam Settings");
       

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        wordCountField = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        easyRadioButton = new javax.swing.JRadioButton();
        mediumRadioButton = new javax.swing.JRadioButton();
        hardRadioButton = new javax.swing.JRadioButton();
        jPanel5 = new javax.swing.JPanel();
        forTimeCheckBox = new javax.swing.JCheckBox();
        jLabel5 = new javax.swing.JLabel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel2.setForeground(new java.awt.Color(204, 204, 204));

        jLabel7.setText("Words to be used by examing:");

        wordCountField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                wordCountFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                wordCountFieldFocusLost(evt);
            }
        });

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel4.setText("Choose the difficulty of the exam:");

        easyRadioButton.setText("Easy");
        easyRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                easyRadioButtonActionPerformed(evt);
            }
        });

        mediumRadioButton.setText("Medium");

        hardRadioButton.setText("Hard");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(easyRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(mediumRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(hardRadioButton))
                    .addComponent(jLabel4))
                .addContainerGap(15, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addComponent(jLabel4)
                .addGap(3, 3, 3)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mediumRadioButton)
                    .addComponent(hardRadioButton)
                    .addComponent(easyRadioButton))
                .addGap(13, 13, 13))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        forTimeCheckBox.setText("With Coundown for the answer");

        jLabel5.setText("Time depends on selected difficulty");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(forTimeCheckBox)
                    .addComponent(jLabel5))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(forTimeCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addContainerGap(17, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(wordCountField))
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(wordCountField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(33, Short.MAX_VALUE)
                .addComponent(okButton)
                .addGap(37, 37, 37)
                .addComponent(cancelButton)
                .addGap(39, 39, 39))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {cancelButton, okButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(okButton)
                    .addComponent(cancelButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
       if (forTimeCheckBox.isSelected() == true) {

            levelChoice();      //seconds != 0
            setDifficultyLabels();
            ExamDialog.timerFieldLabel("Initialized");
        } else {
           ExamDialog.timerFieldLabel("Not Initialized");
           seconds = 0;
        }

        isOpen = true;
        setWordsCount();
        prefsPutCheckbox();
        setDifficultyLabels();
        ExamDialog.diffLabelChange(difficulty);
        setVisible(false);
    }//GEN-LAST:event_okButtonActionPerformed

    private void wordCountFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_wordCountFieldFocusGained
        wordCountField.setText(null);
    }//GEN-LAST:event_wordCountFieldFocusGained

    private void wordCountFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_wordCountFieldFocusLost
        
    }//GEN-LAST:event_wordCountFieldFocusLost

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        setVisible(false);

    }//GEN-LAST:event_cancelButtonActionPerformed

    private void easyRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_easyRadioButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_easyRadioButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                ExamSettingsDialog dialog = new ExamSettingsDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {

                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JRadioButton easyRadioButton;
    private static javax.swing.JCheckBox forTimeCheckBox;
    private javax.swing.JRadioButton hardRadioButton;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private static javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JRadioButton mediumRadioButton;
    private javax.swing.JButton okButton;
    private javax.swing.JTextField wordCountField;
    // End of variables declaration//GEN-END:variables

    public void levelChoice() {
        if (easyRadioButton.isSelected()) {
            seconds = 45;
        }
        if (mediumRadioButton.isSelected()) {
            seconds = 30;
        }
        if (hardRadioButton.isSelected()) {
            seconds = 15;
        }
        
    }

    public static int returnTimeSeconds() {
        return seconds;
    }

    public static String getDifficulty() {
        return difficulty;
    }

    public void setWordsCount() {
        wordCount = Integer.parseInt(wordCountField.getText());
        PREFS.putInt("WORDS", wordCount);
        
    }

    public static int getWordsCount() {
        return wordCount;
    }
     public void setDifficultyLabels() {
        if (easyRadioButton.isSelected()) {
            difficulty = "Easy";
        } else
        if (mediumRadioButton.isSelected()) {
            difficulty = "Medium";
        } else
        if (hardRadioButton.isSelected()) {
            difficulty = "Hard";
        } 
  
    }
     public int setWordsCountUnknown() {
        return PREFS.getInt("WORDS", 10);
    }

    public void prefsPutCheckbox () {
        PREFS.putBoolean("EASY_CHECKBOX", easyRadioButton.isSelected());
        PREFS.putBoolean("MEDIUM_CHECKBOX", mediumRadioButton.isSelected());
        PREFS.putBoolean("HARD_CHECBOX", hardRadioButton.isSelected());
        PREFS.putBoolean("TIMER_CHECKBOX", forTimeCheckBox.isSelected());
    }

    public static boolean returnTimerStatus () {

       return forTimeCheckBox.isSelected();
    }

    public void showExamSettingsDialog() {              // Thanks to Kiril Kamburov (:
        wordCountField.setText("" + PREFS.getInt("WORDS", 10));
        forTimeCheckBox.setSelected(PREFS.getBoolean("TIMER_CHECKBOX", false));
        easyRadioButton.setSelected(PREFS.getBoolean("EASY_CHECKBOX", true));
        mediumRadioButton.setSelected(PREFS.getBoolean("MEDIUM_CHECKBOX", false));
        hardRadioButton.setSelected(PREFS.getBoolean("HARD_CHECBOX", false));

        setVisible(true);

    }

    public static boolean isOpen() {
        return isOpen;
    }
}



