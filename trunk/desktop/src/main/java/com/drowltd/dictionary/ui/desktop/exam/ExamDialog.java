package com.drowltd.dictionary.ui.desktop.exam;

import com.drowltd.dictionary.core.exam.ExamService;
import com.drowltd.dictionary.core.db.*;
import com.drowltd.dictionary.core.exam.Difficulty;
import com.drowltd.dictionary.core.i18n.Translator;
import com.drowltd.dictionary.core.preferences.PreferencesManager;
import com.drowltd.dictionary.ui.desktop.IconManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.Timer;

/**
 *
 * @author Franky, Snow, miroslava
 * @since 0.2
 * 
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
    private Dictionary selectedDictionary = Dictionary.EN_BG;
    private static Difficulty difficulty = Difficulty.EASY;
    private ExamSettingsDialog examSettingsDialog = new ExamSettingsDialog(this, rootPaneCheckingEnabled);
    
    private int totalWords;
    private int correctWords;
    private int fromWordsIndex;
    private int toWordsIndex;
    private boolean timerUsed;
    private String diffLabelText;
    private static Difficulty enumDiff = Difficulty.EASY;
    private static final Translator TRANSLATOR = Translator.getTranslator("ExamDialog");
    private static ArrayList<String> wrongWords = new ArrayList<String>();
    private static ArrayList<String> correctTranslation = new ArrayList<String>();

    public enum TimerStatus {

        PAUSED, STARTED, STOPPED, DISABLED
    }
    private static TimerStatus enumTimerStatus = TimerStatus.DISABLED;

    /** Creates new form ExamDialog */
    public ExamDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);

        TRANSLATOR.reset();
        initComponents();
        pauseButton.setEnabled(false);

        setIconImage(IconManager.getImageIcon("dictionary.png", IconManager.IconSize.SIZE16).getImage());
        setLocationRelativeTo(parent);

        //   Next two lines can be used directly even with more languages
        fromLanguageComboBox.setSelectedIndex(PM.getInt("FROM_LANGUAGE_LAST_SELECTED", fromLanguageComboBox.getSelectedIndex()));
        toLanguageComboBox.setSelectedIndex(PM.getInt("TO_LANGUAGE_LAST_SELECTED", toLanguageComboBox.getSelectedIndex()));


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
        settingsIconLabel = new javax.swing.JLabel();
        settingsButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        startButton = new javax.swing.JButton();
        translateField = new javax.swing.JTextField();
        answerField = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        stopButton = new javax.swing.JButton();
        timerProgressBar = new javax.swing.JProgressBar();
        difficultyLabel = new javax.swing.JLabel();
        answerButton = new javax.swing.JButton();
        wordsProgressBar = new javax.swing.JProgressBar();
        timerIconLabel = new javax.swing.JLabel();
        answerIconLabel = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        pauseButton = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        feedbackField = new javax.swing.JLabel();

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

        settingsIconLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/48x48/preferences.png"))); // NOI18N
        settingsIconLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                settingsIconLabelMouseClicked(evt);
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
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, settingsPanelLayout.createSequentialGroup()
                        .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(toLanguageComboBox, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(fromLanguageComboBox, javax.swing.GroupLayout.Alignment.LEADING, 0, 130, Short.MAX_VALUE))
                        .addGap(64, 64, 64)
                        .addComponent(settingsButton)))
                .addGap(58, 58, 58)
                .addComponent(settingsIconLabel)
                .addGap(38, 38, 38))
        );
        settingsPanelLayout.setVerticalGroup(
            settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(settingsIconLabel)
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
        jPanel3.setMinimumSize(new java.awt.Dimension(32767, 32767));

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

        timerProgressBar.setForeground(new java.awt.Color(51, 255, 51));
        timerProgressBar.setToolTipText(bundle.getString("Timer(String)")); // NOI18N
        timerProgressBar.setString(bundle.getString("Timer(String)")); // NOI18N
        timerProgressBar.setStringPainted(true);

        answerButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/16x16/check2.png"))); // NOI18N
        answerButton.setText(bundle.getString("Answer(Button)")); // NOI18N
        answerButton.setEnabled(false);
        answerButton.setMaximumSize(new java.awt.Dimension(75, 25));
        answerButton.setMinimumSize(new java.awt.Dimension(75, 25));
        answerButton.setPreferredSize(new java.awt.Dimension(75, 25));
        answerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                answerButtonActionPerformed(evt);
            }
        });

        wordsProgressBar.setForeground(new java.awt.Color(102, 102, 255));
        wordsProgressBar.setToolTipText(bundle.getString("Words(String)")); // NOI18N
        wordsProgressBar.setMinimumSize(new java.awt.Dimension(32767, 17));
        wordsProgressBar.setString(bundle.getString("Words(String)")); // NOI18N
        wordsProgressBar.setStringPainted(true);

        timerIconLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/48x48/stopwatch.png"))); // NOI18N

        answerIconLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/24x24/bell2_grey.png"))); // NOI18N

        jLabel4.setText(bundle.getString("Difficulty(Label)")); // NOI18N

        pauseButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/16x16/media_pause.png"))); // NOI18N
        pauseButton.setText(bundle.getString("Pause(Button)")); // NOI18N
        pauseButton.setEnabled(false);
        pauseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pauseButtonActionPerformed(evt);
            }
        });

        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/48x48/dictionary.png"))); // NOI18N

        feedbackField.setText(bundle.getString("Feedback(Field)")); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(feedbackField, javax.swing.GroupLayout.PREFERRED_SIZE, 331, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(timerIconLabel)
                        .addGap(42, 42, 42)
                        .addComponent(timerProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 340, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel8)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                                        .addGap(93, 93, 93)
                                        .addComponent(answerButton, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(answerField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 292, Short.MAX_VALUE)
                                    .addComponent(translateField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 292, Short.MAX_VALUE)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                                        .addComponent(startButton, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(pauseButton, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(stopButton, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.LEADING)))
                            .addGap(39, 39, 39)
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel4)
                                .addComponent(jLabel7)
                                .addComponent(answerIconLabel))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(difficultyLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(160, 160, 160))
                        .addComponent(wordsProgressBar, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 430, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );

        jPanel3Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {startButton, stopButton});

        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(startButton)
                    .addComponent(stopButton)
                    .addComponent(jLabel4)
                    .addComponent(pauseButton)
                    .addComponent(difficultyLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(translateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel8)
                        .addGap(5, 5, 5)
                        .addComponent(answerField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(answerIconLabel)
                    .addComponent(answerButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addComponent(wordsProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(feedbackField)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(timerProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(timerIconLabel))
                .addGap(22, 22, 22))
        );

        jPanel3Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {answerButton, pauseButton});

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 468, Short.MAX_VALUE)
                    .addComponent(settingsPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 468, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(settingsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 344, Short.MAX_VALUE)
                .addContainerGap())
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
        answer = new ExamService(selectedDictionary, difficulty);
        totalWords = 0;
        correctWords = 0;
        dbCalling();

        editability(false);

        if (ExamSettingsDialog.isOpen()) {
            examWords = ExamSettingsDialog.getWordsCount();
            examWordsCopy = ExamSettingsDialog.getWordsCount();
            PM.putInt("EXAM_WORDS", examWords);
        }


        if (enumTimerStatus == TimerStatus.STARTED || enumTimerStatus == TimerStatus.STOPPED) {
            timerRunButton();
            timerUsed = true;
            enumTimerStatus = TimerStatus.STARTED;
            timerIconLabel.setIcon(IconManager.getImageIcon("stopwatch_run.png", IconManager.IconSize.SIZE48));
        } else {
            enumTimerStatus = TimerStatus.DISABLED;
            timerIconLabel.setIcon(IconManager.getImageIcon("stopwatch_stop.png", IconManager.IconSize.SIZE48));
            pauseButton.setEnabled(false);
        }

        secondsBackup = seconds;
        maximumWordsProgressBar = examWords;
        wordsProgressBar.setMaximum(maximumWordsProgressBar);
        wordsProgressBar.setString("1/" + examWords);
        wordsProgressBar.setValue(1);
        feedbackField.setText(TRANSLATOR.translate("ExamStarted(Label)"));

    }//GEN-LAST:event_startButtonActionPerformed

    private void stopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopButtonActionPerformed
        wrongWords.add(answer.examWord());
        correctTranslation.add(answer.getTranslation());
        stopExam();
        pauseButton.setText(TRANSLATOR.translate("Pause(Button)"));

    }//GEN-LAST:event_stopButtonActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        swingTimer.stop();
        PM.put("A_DIFFICULTY", enumDiff.name());
        PM.put("DIFF_LABEL", difficultyLabel.getText());
        PM.put("DIFFICULTY", difficulty.name());
        PM.putBoolean("TIMER_USED", timerUsed);
        PM.put("A_TIMER_STATUS", enumTimerStatus.name());
        PM.putBoolean("TIMER_PROGRESSBAR_VISIBILITY", timerProgressBar.isVisible());
        PM.putBoolean("TIMER_ICON_VISIBILITY", timerIconLabel.isVisible());
        PM.putInt("FROM_LANGUAGE_LAST_SELECTED", fromLanguageComboBox.getSelectedIndex());
        PM.putInt("TO_LANGUAGE_LAST_SELECTED", toLanguageComboBox.getSelectedIndex());
    }//GEN-LAST:event_formWindowClosed

    private void settingsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingsButtonActionPerformed
        examSettingsDialog.setLocationRelativeTo(this);
        examSettingsDialog.showExamSettingsDialog();
        
    }//GEN-LAST:event_settingsButtonActionPerformed

    private void answerFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_answerFieldActionPerformed
        answered();
        seconds = secondsBackup;
    }//GEN-LAST:event_answerFieldActionPerformed

    private void answerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_answerButtonActionPerformed
        answered();
        seconds = secondsBackup;
    }//GEN-LAST:event_answerButtonActionPerformed

    private void settingsIconLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_settingsIconLabelMouseClicked
        examSettingsDialog.setLocationRelativeTo(this);
        examSettingsDialog.showExamSettingsDialog();
    }//GEN-LAST:event_settingsIconLabelMouseClicked

    private void fromLanguageComboBoxPopupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_fromLanguageComboBoxPopupMenuWillBecomeInvisible
        if (fromLanguageComboBox.getSelectedItem() == toLanguageComboBox.getSelectedItem()) {
            toLanguageComboBox.setSelectedIndex(fromWordsIndex);
        }

    }//GEN-LAST:event_fromLanguageComboBoxPopupMenuWillBecomeInvisible

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

    private void pauseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pauseButtonActionPerformed
        if (pauseButton.getText().equals(TRANSLATOR.translate("Pause(Button)"))) {
            swingTimer.stop();
            pauseButton.setText(TRANSLATOR.translate("Continue(Button)"));
            answerButton.setEnabled(false);
        } else if (pauseButton.getText().equals(TRANSLATOR.translate("Continue(Button)"))) {
            swingTimer.start();
            pauseButton.setText(TRANSLATOR.translate("Pause(Button)"));
            answerButton.setEnabled(true);
        }


    }//GEN-LAST:event_pauseButtonActionPerformed
    /**
     * @param args the command line arguments
     */
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton answerButton;
    private javax.swing.JTextField answerField;
    private javax.swing.JLabel answerIconLabel;
    private static javax.swing.JLabel difficultyLabel;
    private static javax.swing.JLabel feedbackField;
    private javax.swing.JComboBox fromLanguageComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel3;
    private static javax.swing.JButton pauseButton;
    private javax.swing.JButton settingsButton;
    private javax.swing.JLabel settingsIconLabel;
    private javax.swing.JPanel settingsPanel;
    private static javax.swing.JButton startButton;
    private static javax.swing.JButton stopButton;
    private static javax.swing.JLabel timerIconLabel;
    private static javax.swing.JProgressBar timerProgressBar;
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

        if (examWords == 0) {
            stopExam();

            examWords = examWordsCopy;
        } else {
            dbCalling();
        }

        answerField.setText(null);

    }

    private void displayTranslation() {
        answer.possibleAnswers();
        String str;
        if (examWords == 0) {
            str = (examWordsCopy - examWords) + "/ " + examWordsCopy;
        } else {
            str = (examWordsCopy - examWords + 1) + "/ " + examWordsCopy;
        }
        wordsProgressBar.setString(str);
        wordsProgressBar.setValue(maximumWordsProgressBar - examWords + 1);
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

                    timerProgressBar.setString(TRANSLATOR.translate("TimeSecond(ProgressBar)") + seconds);

                    seconds--;
                    timerProgressBar.setValue(maximumSecondsProgressBar - seconds);



                } else if (seconds < 10 && seconds >= 0) {
                    if (seconds < 6) {
                        wordsProgressBar.setForeground(new java.awt.Color(204, 0, 0));
                    } else {
                        wordsProgressBar.setForeground(new java.awt.Color(204, 0, 0));
                    }

                    timerProgressBar.setString(TRANSLATOR.translate("TimeFirst(ProgressBar)") + seconds);

                    seconds--;
                    timerProgressBar.setValue(maximumSecondsProgressBar - seconds);


                    if (seconds == -1) {

                        answered();

                        flagLast = true;

                    }
                }
            } else {
                wrongWords.add(answer.examWord());
                correctTranslation.add(answer.getTranslation());
                stopExam();
                wordsProgressBar.setValue(maximumWordsProgressBar);

            }

        }
    });

   

    public void showExamDialog() {
        difficulty = difficulty.valueOf(PM.get("DIFFICULTY", difficulty.name()));
        if (PM.getBoolean("RETURN_TIMER_STATUS", false)) {
            seconds = PM.getInt("SECONDS", WIDTH);

        }

        timerProgressBar.setVisible(PM.getBoolean("TIMER_PROGRESSBAR_VISIBILITY", true));

        timerIconLabel.setVisible(PM.getBoolean("TIMER_ICON_VISIBILITY", false));

        diffLabelText = PM.get("A_DIFFICULTY", enumDiff.name());
        diffLabelChange(diffLabelText);

        enumTimerStatus = enumTimerStatus.valueOf(PM.get("A_TIMER_STATUS", enumTimerStatus.DISABLED.toString()));
        examWords = PM.getInt("EXAM_WORDS", 10);
        examWordsCopy = examWords;
        setVisible(true);
    }

     /* This method handels the text in GUI, which is related to difficulty
      * Maybe using String isn't the best way for handling the problem, but
      * there going to be made some optimization
      */
     public static void diffLabelChange(String diff) {

        if (diff.equals("EASY")) {
            difficultyLabel.setText(TRANSLATOR.translate("Easy(Label)"));
            enumDiff = Difficulty.EASY;
        }
        if (diff.equals("MEDIUM")){
            difficultyLabel.setText(TRANSLATOR.translate("Medium(Label)"));
            enumDiff = Difficulty.MEDIUM;
        }
        if (diff.equals("HARD")) {
            difficultyLabel.setText(TRANSLATOR.translate("Hard(Label)"));
            enumDiff = Difficulty.HARD;
        }
    }

    private void editability(Boolean a) {

        fromLanguageComboBox.setEnabled(a);
        toLanguageComboBox.setEnabled(a);
        settingsButton.setEnabled(a);
        startButton.setEnabled(a);
        stopButton.setEnabled(!a);
        answerButton.setEnabled(!a);
        pauseButton.setEnabled(!a);
    }

    private void timerRunButton() {
        seconds = ExamSettingsDialog.returnTimeSeconds();
        if (seconds == 0) {
            seconds = PM.getInt("SECONDS", WIDTH);
        } else {
            PM.putInt("SECONDS", seconds);
        }
        swingTimer.start();
        pauseButton.setEnabled(true);
        maximumSecondsProgressBar = seconds;
        timerProgressBar.setMaximum(maximumSecondsProgressBar);

    }

    private void stopExam() {
        swingTimer.stop();
        timerProgressBar.setValue(0);
        if (enumTimerStatus != TimerStatus.DISABLED) {
            enumTimerStatus = TimerStatus.STOPPED;
            timerIconLabel.setIcon(IconManager.getImageIcon("stopwatch_stop.png", IconManager.IconSize.SIZE48));
        }
        timerProgressBar.setString(TRANSLATOR.translate("Timer(String)"));
        wordsProgressBar.setValue(0);
        wordsProgressBar.setString(TRANSLATOR.translate("Words(String)"));
        answerIconLabel.setIcon(IconManager.getImageIcon("bell2_grey.png", IconManager.IconSize.SIZE24));
        editability(true);
        translateField.setText(null);
        answerField.setText(null);
        examWords = PM.getInt("EXAM_WORDS", examWords);
        examResult();

    }

    private void examResult() {

        ExamResult examResultDialog = new ExamResult(null, rootPaneCheckingEnabled);
        examResultDialog.setLocationRelativeTo(this);
        examResultDialog.showExamResult(correctWords, totalWords);

    }

    public static ArrayList<String> getWrongWords() {
        return wrongWords;
    }

    public static ArrayList<String> getCorrectTranslation() {
        return correctTranslation;
    }

    public static void setTimerProgressbarVisible() {
        timerProgressBar.setVisible(true);
        stopButton.setEnabled(false);
        timerIconLabel.setVisible(true);
       // feedbackField.setText(TRANSLATOR.translate("Feedback(Field)"));
    }

    public static void setTimerProgressbarInvisible() {
        timerProgressBar.setVisible(false);
        pauseButton.setEnabled(false);
        timerIconLabel.setVisible(false);
      //  feedbackField.setText(TRANSLATOR.translate("Feedback(Field)"));
    }

    public static void setDifficulty(Difficulty d) {
        difficulty = d;
    }

    public static void setEnumTimerStatus(TimerStatus timerStatus) {
        enumTimerStatus = timerStatus;
    }

    public static void setFeedbackFieldDefault() {

        feedbackField.setText(TRANSLATOR.translate("Feedback(Field)"));
    }
}




