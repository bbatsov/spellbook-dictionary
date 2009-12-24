package com.drowltd.dictionary.ui.desktop.exam;

import com.drowltd.dictionary.core.exam.ExamService;
import com.drowltd.dictionary.core.db.*;
import com.drowltd.dictionary.core.i18n.Translator;
import com.drowltd.dictionary.core.preferences.PreferencesManager;
import com.drowltd.dictionary.ui.desktop.IconManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.Timer;

/**
 *
 * @author Franky
 */
public class ExamDialog extends javax.swing.JDialog {

    private ExamService answer;
    private static final PreferencesManager PM = PreferencesManager.getInstance();
    private int seconds = 0;
    private int secondsBackup = 0;
    private int examWords;
    private int examWordsCopy;
    private int maximumSecondsProgressBar = 0;
    private int maximumWordsProgressBar = 0;
    private boolean answerPressed = false;
    private Dictionary selectedDictionary = Dictionary.BG_EN;
    private ExamSettingsDialog examSettingsDialog = new ExamSettingsDialog(null, rootPaneCheckingEnabled);
    private int totalWords;
    private int correctWords;
    private int fromWordsIndex;
    private int toWordsIndex;
    private boolean timerUsed;
    private static final Translator TRANSLATOR = Translator.getTranslator("ExamDialog");
    private static ArrayList<String> wrongWords = new ArrayList<String>();
    private static ArrayList<String> correctTranslation = new ArrayList<String>();

    /** Creates new form ExamDialog */
    public ExamDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);

        TRANSLATOR.reset();
        initComponents();
        showWrongWordsButton.setVisible(false);

        setIconImage(IconManager.getImageIcon("dictionary.png", IconManager.IconSize.SIZE16).getImage());
        setLocationRelativeTo(parent);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        settingsPanel = new javax.swing.JPanel();
        fromLanguageComboBox = new javax.swing.JComboBox();
        toLanguageComboBox = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        settingsButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        startButton = new javax.swing.JButton();
        translateField = new javax.swing.JTextField();
        answerField = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        stopButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        feedbackField = new javax.swing.JTextArea();
        timerProgressBar = new javax.swing.JProgressBar();
        difficultyLabel = new javax.swing.JLabel();
        answerButton = new javax.swing.JButton();
        wordsProgressBar = new javax.swing.JProgressBar();
        timerStatusLabel = new javax.swing.JLabel();
        timerIconLabel = new javax.swing.JLabel();
        answerIconLabel = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        showWrongWordsButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("i18n/ExamDialog"); // NOI18N
        setTitle(bundle.getString("Exam(Title)")); // NOI18N
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        settingsPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        fromLanguageComboBox.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                fromLanguageComboBoxPopupMenuWillBecomeInvisible(evt);
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                fromLanguageComboBoxPopupMenuWillBecomeVisible(evt);
            }
        });

        toLanguageComboBox.addItem(bundle.getString("English(Item)"));
        toLanguageComboBox.addItem(bundle.getString("Bulgarian(Item)"));
        toLanguageComboBox.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                toLanguageComboBoxPopupMenuWillBecomeInvisible(evt);
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                toLanguageComboBoxPopupMenuWillBecomeVisible(evt);
            }
        });

        jLabel1.setText(bundle.getString("Languages(Label)")); // NOI18N

        jLabel2.setText(bundle.getString("From(Label)")); // NOI18N

        jLabel3.setText(bundle.getString("To(Label)")); // NOI18N

        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/48x48/preferences.png"))); // NOI18N
        jLabel6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel6MouseClicked(evt);
            }
        });

        settingsButton.setText(bundle.getString("Settings(Button)")); // NOI18N
        settingsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                settingsButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout settingsPanelLayout = new javax.swing.GroupLayout(settingsPanel);
        settingsPanel.setLayout(settingsPanelLayout);
        settingsPanelLayout.setHorizontalGroup(
            settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 227, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(settingsPanelLayout.createSequentialGroup()
                        .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(toLanguageComboBox, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(fromLanguageComboBox, javax.swing.GroupLayout.Alignment.LEADING, 0, 130, Short.MAX_VALUE))
                        .addGap(64, 64, 64)
                        .addComponent(settingsButton)
                        .addGap(58, 58, 58)
                        .addComponent(jLabel6)))
                .addGap(54, 54, 54))
        );
        settingsPanelLayout.setVerticalGroup(
            settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jLabel6)
                        .addGroup(settingsPanelLayout.createSequentialGroup()
                            .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel2)
                                .addComponent(fromLanguageComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel3)
                                .addComponent(toLanguageComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(settingsButton))
                .addGap(18, 18, 18))
        );

        fromLanguageComboBox.addItem(bundle.getString("English(Item)"));
        fromLanguageComboBox.addItem(bundle.getString("Bulgarian(Item)"));
        fromLanguageComboBox.setSelectedIndex(1);

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        startButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/16x16/media_play_green.png"))); // NOI18N
        startButton.setText(bundle.getString("Start(Button)")); // NOI18N
        startButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });

        translateField.setEditable(false);

        answerField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                answerFieldActionPerformed(evt);
            }
        });

        jLabel8.setText(bundle.getString("OverAnswerField(Label)")); // NOI18N

        jLabel9.setText(bundle.getString("OverTranslateField(Label)")); // NOI18N

        stopButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/16x16/media_stop_red.png"))); // NOI18N
        stopButton.setText(bundle.getString("Stop(Button)")); // NOI18N
        stopButton.setEnabled(false);
        stopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopButtonActionPerformed(evt);
            }
        });

        feedbackField.setBackground(new java.awt.Color(240, 240, 240));
        feedbackField.setColumns(20);
        feedbackField.setEditable(false);
        feedbackField.setFont(new java.awt.Font("Tahoma", 0, 11));
        feedbackField.setRows(4);
        feedbackField.setText(bundle.getString("Feedback(Field)")); // NOI18N
        jScrollPane1.setViewportView(feedbackField);

        timerProgressBar.setForeground(new java.awt.Color(51, 255, 51));
        timerProgressBar.setToolTipText(bundle.getString("Timer(String)")); // NOI18N
        timerProgressBar.setString(bundle.getString("Timer(String)")); // NOI18N
        timerProgressBar.setStringPainted(true);

        difficultyLabel.setText("k");

        answerButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/16x16/check2.png"))); // NOI18N
        answerButton.setText(bundle.getString("Answer(Button)")); // NOI18N
        answerButton.setEnabled(false);
        answerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                answerButtonActionPerformed(evt);
            }
        });

        wordsProgressBar.setForeground(new java.awt.Color(102, 102, 255));
        wordsProgressBar.setToolTipText(bundle.getString("Words(String)")); // NOI18N
        wordsProgressBar.setString(bundle.getString("Words(String)")); // NOI18N
        wordsProgressBar.setStringPainted(true);

        timerIconLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/48x48/stopwatch.png"))); // NOI18N

        answerIconLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/24x24/bell2_grey.png"))); // NOI18N

        jLabel4.setText(bundle.getString("Difficulty(Label)")); // NOI18N

        jLabel5.setText(bundle.getString("Timer(Label)")); // NOI18N

        showWrongWordsButton.setText(bundle.getString("ShowWords(Button)")); // NOI18N
        showWrongWordsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showWrongWordsButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel9)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(answerField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 232, Short.MAX_VALUE)
                                .addComponent(translateField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 232, Short.MAX_VALUE)
                                .addGroup(jPanel3Layout.createSequentialGroup()
                                    .addGap(35, 35, 35)
                                    .addComponent(startButton)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 31, Short.MAX_VALUE)
                                    .addComponent(stopButton, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(12, 12, 12))
                                .addGroup(jPanel3Layout.createSequentialGroup()
                                    .addComponent(answerIconLabel)
                                    .addGap(36, 36, 36)
                                    .addComponent(answerButton, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(34, 34, 34)))
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel3Layout.createSequentialGroup()
                                    .addGap(15, 15, 15)
                                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel3Layout.createSequentialGroup()
                                    .addGap(19, 19, 19)
                                    .addComponent(timerIconLabel)
                                    .addGap(18, 18, 18)
                                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel4)
                                        .addComponent(jLabel5))
                                    .addGap(18, 18, 18)
                                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(timerStatusLabel)
                                        .addComponent(difficultyLabel))))
                            .addContainerGap())
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addComponent(timerProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 447, Short.MAX_VALUE)
                            .addContainerGap())
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addComponent(wordsProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 447, Short.MAX_VALUE)
                            .addContainerGap()))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(showWrongWordsButton)
                        .addGap(58, 58, 58))))
        );

        jPanel3Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {startButton, stopButton});

        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(stopButton)
                                .addComponent(startButton))
                            .addComponent(timerIconLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel9))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(difficultyLabel)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(timerStatusLabel)
                            .addComponent(jLabel5))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(translateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel8)
                        .addGap(5, 5, 5)
                        .addComponent(answerField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(answerIconLabel)
                            .addComponent(answerButton))
                        .addGap(1, 1, 1))
                    .addComponent(jScrollPane1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(showWrongWordsButton)
                .addGap(5, 5, 5)
                .addComponent(timerProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(wordsProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(settingsPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 471, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(settingsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startButtonActionPerformed
        if ((fromLanguageComboBox.getSelectedIndex() == 0) && (toLanguageComboBox.getSelectedIndex() == 1)) {   //English Index = 0; Bulgarian = 1;
            selectedDictionary = Dictionary.EN_BG;
        }
        if ((fromLanguageComboBox.getSelectedIndex() == 1) && (toLanguageComboBox.getSelectedIndex() == 0)) {
            selectedDictionary = Dictionary.BG_EN;
        }
        wrongWords.clear();
        correctTranslation.clear();
        answer = new ExamService(selectedDictionary);
        totalWords = 0;
        correctWords = 0;
        dbCalling();

        editability(false);

        if (ExamSettingsDialog.isOpen()) {
            examWords = ExamSettingsDialog.getWordsCount();
            examWordsCopy = ExamSettingsDialog.getWordsCount();
            PM.putInt("EXAM_WORDS", examWords);
        }


        if (timerStatusLabel.getText().equals(TRANSLATOR.translate("Initialized(Label)")) || timerStatusLabel.getText().equals(TRANSLATOR.translate("Stopped(Label)"))) {
            timerRunButton();
            timerUsed = true;
            timerStatusLabel.setText(TRANSLATOR.translate("Initialized(Label)"));
            timerIconLabel.setIcon(IconManager.getImageIcon("stopwatch_run.png", IconManager.IconSize.SIZE48));
        } else {
            timerStatusLabel.setText(TRANSLATOR.translate("NotInitialized(Label)"));
            timerIconLabel.setIcon(IconManager.getImageIcon("stopwatch_stop.png", IconManager.IconSize.SIZE48));
        }

        secondsBackup = seconds;
        maximumWordsProgressBar = examWords;
        wordsProgressBar.setMaximum(maximumWordsProgressBar - 1);
        wordsProgressBar.setString("1/" + examWords);
        showWrongWordsButton.setVisible(false);
    }//GEN-LAST:event_startButtonActionPerformed

    private void stopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopButtonActionPerformed
        stopExam();
    }//GEN-LAST:event_stopButtonActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        swingTimer.stop();
        PM.put("DIFF_LABEL", difficultyLabel.getText());
        PM.putBoolean("TIMER_USED", timerUsed);
        if (timerStatusLabel.getText().contains(TRANSLATOR.translate("Stopped(Label)"))) {
            if (ExamSettingsDialog.returnTimerStatus() || PM.getBoolean("TIMER_USED", timerUsed)) {
                PM.put("TIMER_STATUS", TRANSLATOR.translate("Initialized(Label)"));
            } else {
                PM.put("TIMER_STATUS", TRANSLATOR.translate("NotInitialized(Label)"));
            }
        } else {
            PM.put("TIMER_STATUS", timerStatusLabel.getText());
        }
    }//GEN-LAST:event_formWindowClosed

    private void settingsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingsButtonActionPerformed
        examSettingsDialog.showExamSettingsDialog();
    }//GEN-LAST:event_settingsButtonActionPerformed

    private void answerFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_answerFieldActionPerformed
        answered();
        seconds = secondsBackup;
        answerPressed = true;
    }//GEN-LAST:event_answerFieldActionPerformed

    private void answerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_answerButtonActionPerformed
        answered();
        seconds = secondsBackup;
        answerPressed = true;
    }//GEN-LAST:event_answerButtonActionPerformed

    private void jLabel6MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel6MouseClicked
        examSettingsDialog.showExamSettingsDialog();
    }//GEN-LAST:event_jLabel6MouseClicked

    private void fromLanguageComboBoxPopupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_fromLanguageComboBoxPopupMenuWillBecomeInvisible
        if (fromLanguageComboBox.getSelectedItem() == toLanguageComboBox.getSelectedItem()) {
            toLanguageComboBox.setSelectedIndex(fromWordsIndex);
        }

    }//GEN-LAST:event_fromLanguageComboBoxPopupMenuWillBecomeInvisible

    private void showWrongWordsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showWrongWordsButtonActionPerformed
        WrongWordsDialog wrongWordsDialog = new WrongWordsDialog(null, rootPaneCheckingEnabled);
        wrongWordsDialog.setVisible(true);
    }//GEN-LAST:event_showWrongWordsButtonActionPerformed

    private void fromLanguageComboBoxPopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_fromLanguageComboBoxPopupMenuWillBecomeVisible
        fromWordsIndex = fromLanguageComboBox.getSelectedIndex();
    }//GEN-LAST:event_fromLanguageComboBoxPopupMenuWillBecomeVisible

    private void toLanguageComboBoxPopupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_toLanguageComboBoxPopupMenuWillBecomeInvisible
        if (toLanguageComboBox.getSelectedItem() == fromLanguageComboBox.getSelectedItem()) {
            fromLanguageComboBox.setSelectedIndex(toWordsIndex);
        }
    }//GEN-LAST:event_toLanguageComboBoxPopupMenuWillBecomeInvisible

    private void toLanguageComboBoxPopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_toLanguageComboBoxPopupMenuWillBecomeVisible
        toWordsIndex = toLanguageComboBox.getSelectedIndex();
    }//GEN-LAST:event_toLanguageComboBoxPopupMenuWillBecomeVisible
    /**
     * @param args the command line arguments
     */
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton answerButton;
    private javax.swing.JTextField answerField;
    private javax.swing.JLabel answerIconLabel;
    private static javax.swing.JLabel difficultyLabel;
    private javax.swing.JTextArea feedbackField;
    private javax.swing.JComboBox fromLanguageComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton settingsButton;
    private javax.swing.JPanel settingsPanel;
    private javax.swing.JButton showWrongWordsButton;
    private static javax.swing.JButton startButton;
    private static javax.swing.JButton stopButton;
    private javax.swing.JLabel timerIconLabel;
    private javax.swing.JProgressBar timerProgressBar;
    private static javax.swing.JLabel timerStatusLabel;
    private javax.swing.JComboBox toLanguageComboBox;
    private javax.swing.JTextField translateField;
    private javax.swing.JProgressBar wordsProgressBar;
    // End of variables declaration//GEN-END:variables

    private void dbCalling() {
        answer.getExamWord(selectedDictionary);
        translateField.setText(answer.examWord());
        totalWords++;
    }

    private void answered() {
        examWords--;

        displayTranslation();

        if (examWords == 1) {
            totalWords += 1;
            stopExam();

            examWords = examWordsCopy;
        } else {
            dbCalling();
        }

        answerField.setText(null);

    }

    private void displayTranslation() {
        answer.possibleAnswers();

        String str = (examWordsCopy - examWords + 1) + "/ " + examWordsCopy;
        wordsProgressBar.setString(str);
        wordsProgressBar.setValue(maximumWordsProgressBar - examWords);

        if (answer.isCorrect(answerField.getText())) {

            wordsProgressBar.setForeground(new java.awt.Color(51, 255, 51));

            feedbackField.setText(TRANSLATOR.translate("CorrectAnswer(String)"));
            answerIconLabel.setIcon(IconManager.getImageIcon("bell2_green.png", IconManager.IconSize.SIZE24));
            correctWords++;
        } else {
            wordsProgressBar.setForeground(new java.awt.Color(204, 0, 0));
            feedbackField.setText(TRANSLATOR.translate("WrongAnser(String)"));
            answerIconLabel.setIcon(IconManager.getImageIcon("bell2_red.png", IconManager.IconSize.SIZE24));
            wrongWords.add(answer.examWord());
            correctTranslation.add(answer.getTranslation());
        }

    }
    private boolean flagLast = false;
    private Timer swingTimer = new javax.swing.Timer(1 * 1000, new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {

            if (examWords != 0) {
                if (flagLast) {
                    seconds = secondsBackup;
                    flagLast = false;

                }

                if (seconds >= 10) {

                    timerProgressBar.setString("Time left: 00:" + seconds);

                    seconds--;
                    timerProgressBar.setValue(maximumSecondsProgressBar - seconds);



                } else if (seconds < 10 && seconds >= 0) {
                    if (seconds < 6) {
                        wordsProgressBar.setForeground(new java.awt.Color(204, 0, 0));
                    } else {
                        wordsProgressBar.setForeground(new java.awt.Color(204, 0, 0));
                    }

                    timerProgressBar.setString("Time left: 00:0" + seconds);

                    seconds--;
                    timerProgressBar.setValue(maximumSecondsProgressBar - seconds);


                    if (seconds == -1) {
                        if (!answerPressed) {
                            answered();
                        }
                        flagLast = true;
                        answerPressed = false;
                        //examWords--;
                    }
                }
            } else {
                stopExam();
                wordsProgressBar.setValue(maximumWordsProgressBar);

            }

        }
    });

    public static void diffLabelChange(String diff) {
        difficultyLabel.setText(diff);
    }

    public static void timerFieldLabel(String status) {
        timerStatusLabel.setText(status);
        if (status.matches(TRANSLATOR.translate("NotInitialized(Label)"))) {
            stopButton.setEnabled(false);
        } else {
            stopButton.setEnabled(true);
        }
    }

    public void showExamDialog() {
        answer = new ExamService(selectedDictionary);
        if (PM.getBoolean("RETURN_TIMER_STATUS", false)) {
            seconds = PM.getInt("SECONDS", WIDTH);

        }

        difficultyLabel.setText(PM.get("DIFF_LABEL", TRANSLATOR.translate("Easy(Label)")));

        timerStatusLabel.setText(PM.get("TIMER_STATUS", TRANSLATOR.translate("NotInitialized(Label)")));

        examWords = PM.getInt("EXAM_WORDS", 10);
        examWordsCopy = examWords;
        setVisible(true);
    }

    private void editability(Boolean a) {

        fromLanguageComboBox.setEnabled(a);
        toLanguageComboBox.setEnabled(a);
        settingsButton.setEnabled(a);
        startButton.setEnabled(a);
        stopButton.setEnabled(!a);
        answerButton.setEnabled(!a);
    }

    private void timerRunButton() {
        seconds = ExamSettingsDialog.returnTimeSeconds();
        if (seconds == 0) {
            seconds = PM.getInt("SECONDS", WIDTH);
        } else {
            PM.putInt("SECONDS", seconds);
        }
        swingTimer.start();
        maximumSecondsProgressBar = seconds;
        timerProgressBar.setMaximum(maximumSecondsProgressBar);

    }

    private void stopExam() {
        swingTimer.stop();
        JOptionPane.showMessageDialog(rootPane, TRANSLATOR.translate("EndOfExam(Message)"));        
        timerProgressBar.setValue(0);
        if (!timerStatusLabel.getText().equalsIgnoreCase(TRANSLATOR.translate("NotInitialized(Label)"))) {
            timerStatusLabel.setText(TRANSLATOR.translate("Stopped(Label)"));
            timerIconLabel.setIcon(IconManager.getImageIcon("stopwatch_stop.png", IconManager.IconSize.SIZE48));
        }
        timerProgressBar.setString(TRANSLATOR.translate("Timer(String)"));
        wordsProgressBar.setValue(0);
        wordsProgressBar.setString(TRANSLATOR.translate("Words(String)"));
        answerIconLabel.setIcon(IconManager.getImageIcon("bell2_grey.png", IconManager.IconSize.SIZE24));
        editability(true);
        translateField.setText(null);
        answerField.setText(null);
        // startButton.setText(TRANSLATOR.translate("Start(Button)"));
        examWords = PM.getInt("EXAM_WORDS", examWords);
        wrongWords.add(answer.examWord());
        correctTranslation.add(answer.getTranslation());
        examResult();
    }

    private void examResult() {

        feedbackField.setText(TRANSLATOR.translate("YourScore(String)") + correctWords + "/" + totalWords + "\n" + TRANSLATOR.translate("CorrectWords(String)") + correctWords + "\n" + TRANSLATOR.translate("WrongWords(String)") + (totalWords - correctWords));
        showWrongWordsButton.setVisible(true);       
        
    }

    public static ArrayList<String> getWrongWords() {
        return wrongWords;
    }

    public static ArrayList<String> getCorrectTranslation() {
        return correctTranslation;
    }
}




