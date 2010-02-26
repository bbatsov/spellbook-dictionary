package com.drowltd.spellbook.ui.desktop.exam;

import com.drowltd.spellbook.core.i18n.Translator;
import com.drowltd.spellbook.core.preferences.PreferencesManager;
import com.drowltd.spellbook.ui.desktop.IconManager;

/**
 *
 * @author miroslava
 * @since 0.2
 *
 */
public class ExamResult extends javax.swing.JDialog {
    
    private static final PreferencesManager PM = PreferencesManager.getInstance();
    private static final Translator TRANSLATOR = Translator.getTranslator("ExamResult");
    private boolean scoreboardVisibility = false;

    /** Creates new form ExamResult */
    public ExamResult(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        TRANSLATOR.reset();
        initComponents();

        setLocationRelativeTo(parent);
        setIconImage(IconManager.getImageIcon("teacher.png", IconManager.IconSize.SIZE16).getImage());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        okResultButton = new javax.swing.JButton();
        correctWordsLabel = new javax.swing.JLabel();
        correctWrodsResultLabel = new javax.swing.JLabel();
        wrongWordsLabel = new javax.swing.JLabel();
        wrongWrodsResultLabel = new javax.swing.JLabel();
        successRateLabel = new javax.swing.JLabel();
        successRateResultLabel = new javax.swing.JLabel();
        endLabel = new javax.swing.JLabel();
        seeWrongWords = new javax.swing.JButton();
        iconLabel = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        scoreboardNameField = new javax.swing.JTextField();
        submitScoreButton = new javax.swing.JButton();
        scoreboardButton = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        scoreboarTable = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("i18n/ExamResult"); // NOI18N
        setTitle(bundle.getString("ExamResult(Title)")); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        okResultButton.setText("OK");
        okResultButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okResultButtonActionPerformed(evt);
            }
        });

        correctWordsLabel.setFont(new java.awt.Font("Tahoma", 1, 11));
        correctWordsLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        java.util.ResourceBundle bundle1 = java.util.ResourceBundle.getBundle("i18n/ExamDialog"); // NOI18N
        correctWordsLabel.setText(bundle1.getString("CorrectWords(String)")); // NOI18N

        correctWrodsResultLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        correctWrodsResultLabel.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        correctWrodsResultLabel.setVerticalTextPosition(javax.swing.SwingConstants.TOP);

        wrongWordsLabel.setFont(new java.awt.Font("Tahoma", 1, 11));
        wrongWordsLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        wrongWordsLabel.setText(bundle1.getString("WrongWords(String)")); // NOI18N

        wrongWrodsResultLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        wrongWrodsResultLabel.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        successRateLabel.setFont(new java.awt.Font("Tahoma", 1, 11));
        successRateLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        successRateLabel.setText(bundle.getString("Success(Label)")); // NOI18N

        successRateResultLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        successRateResultLabel.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        endLabel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        endLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        endLabel.setAlignmentY(0.0F);

        seeWrongWords.setText(bundle1.getString("ShowWords(Button)")); // NOI18N
        seeWrongWords.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                seeWrongWordsActionPerformed(evt);
            }
        });

        iconLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/48x48/bell2_green.png"))); // NOI18N

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 14));
        jLabel2.setText("Your Results:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(okResultButton, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(seeWrongWords, javax.swing.GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(80, 80, 80)
                .addComponent(jLabel2)
                .addGap(78, 78, 78))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(correctWordsLabel)
                    .addComponent(wrongWordsLabel)
                    .addComponent(successRateLabel))
                .addGap(28, 28, 28)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(wrongWrodsResultLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 52, Short.MAX_VALUE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(correctWrodsResultLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                        .addComponent(iconLabel)
                        .addGap(30, 30, 30))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(successRateResultLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(endLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(iconLabel))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(correctWrodsResultLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(correctWordsLabel))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(wrongWordsLabel)
                            .addComponent(wrongWrodsResultLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(9, 9, 9)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(successRateLabel)
                    .addComponent(successRateResultLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(21, 21, 21)
                .addComponent(endLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(okResultButton)
                    .addComponent(seeWrongWords))
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel1.setText("Enter your name:");

        scoreboardNameField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scoreboardNameFieldActionPerformed(evt);
            }
        });

        submitScoreButton.setText("Submit Score");
        submitScoreButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                submitScoreButtonActionPerformed(evt);
            }
        });

        scoreboardButton.setForeground(new java.awt.Color(0, 0, 255));
        scoreboardButton.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        scoreboardButton.setText("See Scoreboard");
        scoreboardButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        scoreboardButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        scoreboardButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                scoreboardButtonMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(scoreboardNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(submitScoreButton, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 62, Short.MAX_VALUE)
                        .addComponent(scoreboardButton, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1))
                    .addComponent(scoreboardButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(submitScoreButton)
                    .addComponent(scoreboardNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        scoreboarTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {" 1", null, null},
                {" 2", null, null},
                {" 3", null, null},
                {" 4", null, null},
                {" 5", null, null},
                {" 6", null, null},
                {" 7", null, null},
                {" 8", null, null},
                {" 9", null, null},
                {"10", null, null},
                {"11", null, null},
                {"12", null, null},
                {"13", null, null},
                {"14", null, null},
                {"15", null, null}
            },
            new String [] {
                "", "Name", "Result"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                true, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(scoreboarTable);
        scoreboarTable.getColumnModel().getColumn(0).setMinWidth(20);
        scoreboarTable.getColumnModel().getColumn(0).setPreferredWidth(20);
        scoreboarTable.getColumnModel().getColumn(0).setMaxWidth(20);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 231, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 267, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void okResultButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okResultButtonActionPerformed
        setVisible(false);
        ExamDialog.setFeedbackFieldDefault();
    }//GEN-LAST:event_okResultButtonActionPerformed

    private void seeWrongWordsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_seeWrongWordsActionPerformed

        WrongWordsDialog wrongWordsDialog = new WrongWordsDialog(null, true);
        wrongWordsDialog.setLocationRelativeTo(this);
        wrongWordsDialog.setVisible(true);
    }//GEN-LAST:event_seeWrongWordsActionPerformed

    private void scoreboardButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_scoreboardButtonMouseClicked
        scoreboardViewStatus();
    }//GEN-LAST:event_scoreboardButtonMouseClicked

    private void submitScoreButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_submitScoreButtonActionPerformed
       scoreboardFilling();
    }//GEN-LAST:event_submitScoreButtonActionPerformed

    private void scoreboardNameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scoreboardNameFieldActionPerformed
        scoreboardFilling();
    }//GEN-LAST:event_scoreboardNameFieldActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel correctWordsLabel;
    private javax.swing.JLabel correctWrodsResultLabel;
    private javax.swing.JLabel endLabel;
    private javax.swing.JLabel iconLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton okResultButton;
    private javax.swing.JTable scoreboarTable;
    private javax.swing.JLabel scoreboardButton;
    private javax.swing.JTextField scoreboardNameField;
    private javax.swing.JButton seeWrongWords;
    private javax.swing.JButton submitScoreButton;
    private javax.swing.JLabel successRateLabel;
    private javax.swing.JLabel successRateResultLabel;
    private javax.swing.JLabel wrongWordsLabel;
    private javax.swing.JLabel wrongWrodsResultLabel;
    // End of variables declaration//GEN-END:variables

    public void showExamResult(int correctWords, int totalWords) {

        setSize(289, 353);

        int uspeh = ((correctWords * 100) / totalWords);
        
        correctWrodsResultLabel.setText(Integer.toString(correctWords));
        wrongWrodsResultLabel.setText(Integer.toString(totalWords - correctWords));
        successRateResultLabel.setText(Integer.toString(uspeh) + "%");

        if (uspeh > 60) {
            endLabel.setText(TRANSLATOR.translate("Passed(Label)"));
            iconLabel.setIcon(IconManager.getImageIcon("bell2_green.png", IconManager.IconSize.SIZE48));
        } else {
            endLabel.setText(TRANSLATOR.translate("Failed(Label)"));
            iconLabel.setIcon(IconManager.getImageIcon("bell2_red.png", IconManager.IconSize.SIZE48));
        }
        setVisible(true);
    }

    public void scoreboardViewStatus() {

        if (scoreboardVisibility == false) {
            scoreboardButton.setText("Hide Scoreboard");
            setSize(554, 353);
            scoreboardVisibility = true;
        } else {
            scoreboardButton.setText("See Scoreboard");
            setSize(289, 353);
            scoreboardVisibility = false;
        }

    }

    public void scoreboardFilling() {

        int i = 0;
        while ((scoreboarTable.getValueAt(i, 1) != null)) {
            i++;     if (i == 14) {  break; }
        }
        scoreboarTable.setValueAt(scoreboardNameField.getText(), i, 1);
        scoreboarTable.setValueAt(successRateResultLabel.getText() + " " + "(" + ExamDialog.returnDiffLabelText() + ")", i, 2);
        scoreboardNameField.setText(null);

        if (scoreboardVisibility == false) {
            scoreboardViewStatus();
        }

    }
}
