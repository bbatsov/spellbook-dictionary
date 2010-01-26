package com.drowltd.dictionary.ui.desktop.exam;

import com.drowltd.dictionary.core.exam.Difficulty;
import com.drowltd.dictionary.core.i18n.Translator;
import com.drowltd.dictionary.core.preferences.PreferencesManager;
import com.drowltd.dictionary.ui.desktop.*;
import javax.swing.ButtonGroup;

/**
 *
 * @author Georgi Angelov
 * @since 0.2
 */
public class ExamSettingsDialog extends javax.swing.JDialog {

    private static final PreferencesManager PM = PreferencesManager.getInstance();
    private static int seconds;
    private static int wordCount;
    private static boolean isOpen = false;
    private static String difficultyLabelText;
    private static Difficulty difficulty;
    private static final Translator TRANSLATOR = Translator.getTranslator("ExamSettingsDialog");

    /** Creates new form ExamSettingsDialog */
    public ExamSettingsDialog(com.drowltd.dictionary.ui.desktop.exam.ExamDialog parent, boolean modal) {
        super(parent, modal);
        TRANSLATOR.reset();



        initComponents();

        // accept only numbers
        wordCountField.setDocument(new NumberDocument());

        ButtonGroup difficultyGroup = new ButtonGroup();

        difficultyGroup.add(easyRadioButton);
        difficultyGroup.add(mediumRadioButton);
        difficultyGroup.add(hardRadioButton);


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
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("i18n/ExamSettingsDialog"); // NOI18N
        setTitle(bundle.getString("Title")); // NOI18N
        setResizable(false);

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel2.setForeground(new java.awt.Color(204, 204, 204));

        jLabel7.setText(bundle.getString("WordsByExam(Label)")); // NOI18N

        wordCountField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                wordCountFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                wordCountFieldFocusLost(evt);
            }
        });

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel4.setText(bundle.getString("ChooseDifficulty(Label)")); // NOI18N

        easyRadioButton.setText(bundle.getString("Easy(Label)")); // NOI18N
        easyRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                easyRadioButtonActionPerformed(evt);
            }
        });

        mediumRadioButton.setText(bundle.getString("Medium(Label)")); // NOI18N

        hardRadioButton.setText(bundle.getString("Hard(Label)")); // NOI18N

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
                .addContainerGap(20, Short.MAX_VALUE))
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

        forTimeCheckBox.setText(bundle.getString("Countdown(Label)")); // NOI18N

        jLabel5.setText(bundle.getString("TimeDependancy(Label)")); // NOI18N

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(forTimeCheckBox)
                    .addComponent(jLabel5))
                .addContainerGap(11, Short.MAX_VALUE))
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
                .addContainerGap(38, Short.MAX_VALUE)
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
        if (forTimeCheckBox.isSelected()) {

            levelChoice();      //seconds != 0
            ExamDialog.setTimerProgressbarVisible();
            ExamDialog.setEnumTimerStatus(ExamDialog.TimerStatus.STARTED);
            ExamDialog.setFeedbackFieldDefault();
        } else {
            ExamDialog.setEnumTimerStatus(ExamDialog.TimerStatus.DISABLED);
            ExamDialog.setFeedbackFieldDefault();
            levelChoice();
            seconds = 0;
            ExamDialog.setTimerProgressbarInvisible();
        }

        isOpen = true;
        setWordsCount();
        PMPutCheckbox();
        setDifficultyLabels();
        ExamDialog.diffLabelChange(difficultyLabelText);
        ExamDialog.setDifficulty(difficulty);
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
            difficulty = Difficulty.EASY;
        }

        if (mediumRadioButton.isSelected()) {
            seconds = 30;
            difficulty = Difficulty.MEDIUM;
        }

        if (hardRadioButton.isSelected()) {
            seconds = 15;
            difficulty = Difficulty.HARD;
        }

    }

    public static int returnTimeSeconds() {
        return seconds;
    }

    public static Difficulty getDifficulty() {
        return difficulty;
    }

    public void setWordsCount() {
        wordCount = Integer.parseInt(wordCountField.getText());
        PM.putInt("WORDS", wordCount);

    }

    public static int getWordsCount() {
        return wordCount;
    }

    public void setDifficultyLabels() {

        if (easyRadioButton.isSelected()) {
            difficultyLabelText = Difficulty.EASY.toString();
        } else if (mediumRadioButton.isSelected()) {
            difficultyLabelText = Difficulty.MEDIUM.toString();
        } else if (hardRadioButton.isSelected()) {
            difficultyLabelText = Difficulty.HARD.toString();
        }

    }

    public int setWordsCountUnknown() {
        return PM.getInt("WORDS", 10);
    }

    public void PMPutCheckbox() {
        PM.putBoolean("EASY_CHECKBOX", easyRadioButton.isSelected());
        PM.putBoolean("MEDIUM_CHECKBOX", mediumRadioButton.isSelected());
        PM.putBoolean("HARD_CHECBOX", hardRadioButton.isSelected());
        PM.putBoolean("TIMER_CHECKBOX", forTimeCheckBox.isSelected());
    }

    public static boolean returnTimerStatus() {

        return forTimeCheckBox.isSelected();
    }

    public void showExamSettingsDialog() {              // Thanks to Kiril Kamburov (:
        wordCountField.setText("" + PM.getInt("WORDS", 10));
        forTimeCheckBox.setSelected(PM.getBoolean("TIMER_CHECKBOX", false));
        easyRadioButton.setSelected(PM.getBoolean("EASY_CHECKBOX", true));
        mediumRadioButton.setSelected(PM.getBoolean("MEDIUM_CHECKBOX", false));
        hardRadioButton.setSelected(PM.getBoolean("HARD_CHECBOX", false));


        setVisible(true);

    }

    public static boolean isOpen() {
        return isOpen;
    }
}


