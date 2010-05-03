
package com.drowltd.spellbook.ui.desktop.exam;

import com.drowltd.spellbook.core.preferences.PreferencesManager.Preference;
import com.drowltd.spellbook.core.model.Dictionary;
import com.drowltd.spellbook.core.service.exam.ExamService;
import com.drowltd.spellbook.core.model.Difficulty;
import com.drowltd.spellbook.core.i18n.Translator;
import com.drowltd.spellbook.core.preferences.PreferencesManager;
import com.drowltd.spellbook.core.service.DictionaryService;
import com.drowltd.spellbook.core.model.Language;
import com.drowltd.spellbook.ui.desktop.PreferencesDialog;
import com.drowltd.spellbook.ui.desktop.PreferencesExtractor;
import com.drowltd.spellbook.ui.swing.util.IconManager;

import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.dialog.StandardDialog;
import com.jidesoft.plaf.UIDefaultsLookup;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.Timer;
import net.miginfocom.swing.MigLayout;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Ivan Spasov
 * @author Georgi Angelov
 * @author Miroslava Stancheva
 * @since 0.2
 */
public class ExamDialog extends StandardDialog {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ExamDialog.class);
    private ExamService examService;
    private static final PreferencesManager PM = PreferencesManager.getInstance();
    private int seconds = 0;
    private int secondsBackup = 0;
    private int examWords;
    private int examWordsCopy;
    private int maximumSecondsProgressBar = 0;
    private int maximumWordsProgressBar = 0;
    private static Difficulty difficulty = Difficulty.EASY;
    private static Language selectedLanguage;
    private static Dictionary selectedDictionary;
    private final DictionaryService dictionaryService = DictionaryService.getInstance();
    private int totalWords;
    private int correctWords;
    private int fromWordsIndex;
    private int toWordsIndex;
    private boolean timerUsed;
    private String diffLabelText;
    private static final Translator TRANSLATOR = Translator.getTranslator("ExamDialog");
    private static List<String> wrongWords = new ArrayList<String>();
    private static List<String> correctTranslation = new ArrayList<String>();
    private boolean timerEnabled = PM.getBoolean(Preference.EXAM_TIMER, false);

    public enum TimerStatus {

        PAUSED, STARTED, STOPPED, DISABLED
    }
    private static TimerStatus enumTimerStatus = TimerStatus.DISABLED;
//    private JPanel settingsPanel;
//    private JPanel jPanel3;
    private JComboBox fromLanguageComboBox;
    private JComboBox toLanguageComboBox;
    private JLabel jLabel4;
    private JLabel jLabel7;
    private JLabel jLabel8;
    private JLabel jLabel9;
    private static JLabel difficultyLabel;
    private static JLabel timerIconLabel;
    private JLabel answerIconLabel;
    private static JLabel feedbackField;
    private static JButton startButton;
    private static JButton stopButton;
    private JButton answerButton;
    private static JButton pauseButton;
    private JTextField translateField;
    private JTextField answerField;
    private static JProgressBar timerProgressBar;
    private JProgressBar wordsProgressBar;

    public ExamDialog(Frame parent, boolean modal) {
        super(parent, modal);

        TRANSLATOR.reset();

        examService = new ExamService();

        

//        settingsPanel = new JPanel();
        fromLanguageComboBox = new JComboBox();
        toLanguageComboBox = new JComboBox();
        jLabel4 = new JLabel();
        difficultyLabel = new JLabel();
        jLabel7 = new JLabel();
//        jPanel3 = new JPanel();
        startButton = new JButton();
        translateField = new JTextField();
        answerField = new JTextField();
        jLabel8 = new JLabel();
        jLabel9 = new JLabel();
        stopButton = new JButton();
        timerProgressBar = new JProgressBar();
        answerButton = new JButton();
        wordsProgressBar = new JProgressBar();
        timerIconLabel = new JLabel();
        answerIconLabel = new JLabel();
        pauseButton = new JButton();
        feedbackField = new JLabel();


        setIconImage(IconManager.getImageIcon("dictionary.png", IconManager.IconSize.SIZE16).getImage());
        setLocationRelativeTo(parent);

        setSize(400, 500);
        initLanguages();
    }

    @Override
    public JComponent createBannerPanel() {
        return null;
    }

    @Override
    public JComponent createContentPanel() {
        JPanel panel = new JPanel(new MigLayout("wrap 5", "[][][][][]", "[grow][][][][grow][grow][grow][grow][][grow][grow][]"));
        panel.add(new JLabel(TRANSLATOR.translate("Languages(Label)")), "span 5, left");

        panel.add(new JLabel(TRANSLATOR.translate("From(Label)")), "");
        panel.add(fromLanguageComboBox, "");
        fromLanguageComboBox.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {

            @Override
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }

            @Override
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                fromLanguageComboBoxPopupMenuWillBecomeInvisible(evt);
            }

            @Override
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                fromLanguageComboBoxPopupMenuWillBecomeVisible(evt);
            }
        });
        fromLanguageComboBox.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fromLanguageComboBoxActionPerformed(evt);
            }
        });

        panel.add(new JLabel(TRANSLATOR.translate("To(Label)")), "");
        panel.add(toLanguageComboBox, "growx");
        toLanguageComboBox.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {

            @Override
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }

            @Override
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                toLanguageComboBoxPopupMenuWillBecomeInvisible(evt);
            }

            @Override
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                toLanguageComboBoxPopupMenuWillBecomeVisible(evt);
            }
        });
        panel.add(jLabel7, "");
        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/48x48/dictionary.png")));

        panel.add(jLabel4, "");
        jLabel4.setText(TRANSLATOR.translate("Difficulty(Label)"));
        panel.add(difficultyLabel, "span 4, left");
        
        panel.add(startButton, "");
        startButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/16x16/media_play_green.png"))); // NOI18N
        startButton.setText(TRANSLATOR.translate("Start(Button)"));
        startButton.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });
        panel.add(pauseButton, "split 2");
        pauseButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/16x16/media_pause.png"))); // NOI18N
        pauseButton.setText(TRANSLATOR.translate("Pause(Button)"));
        pauseButton.setEnabled(false);
        pauseButton.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pauseButtonActionPerformed(evt);
            }
        });
        panel.add(stopButton, "span, wrap");
        stopButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/16x16/media_stop_red.png"))); // NOI18N
        stopButton.setText(TRANSLATOR.translate("Stop(Button)"));
        stopButton.setEnabled(false);
        stopButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopButtonActionPerformed(evt);
            }
        });

        panel.add(jLabel9, "span 5, left");
        jLabel9.setText(TRANSLATOR.translate("OverTranslateField(Label)"));

        panel.add(translateField, "span 5, left, growx");
        translateField.setEditable(false);

        panel.add(jLabel8, "span 5, left");
        jLabel8.setText(TRANSLATOR.translate("OverAnswerField(Label)"));

        panel.add(answerField, "span 5, left, growx");
        answerField.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                answerFieldActionPerformed(evt);
            }
        });
        panel.add(answerIconLabel, "center");
        answerIconLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/24x24/bell2_grey.png")));
        panel.add(answerButton, "span 4, left, growx");
        answerButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/16x16/check2.png"))); // NOI18N
        answerButton.setText(TRANSLATOR.translate("Answer(Button)")); // NOI18N
        answerButton.setEnabled(false);
        answerButton.setMaximumSize(new java.awt.Dimension(75, 25));
        answerButton.setMinimumSize(new java.awt.Dimension(75, 25));
        answerButton.setPreferredSize(new java.awt.Dimension(75, 25));
        answerButton.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                answerButtonActionPerformed(evt);
            }
        });

        panel.add(wordsProgressBar, "span 5, left");
        wordsProgressBar.setForeground(new java.awt.Color(102, 102, 255));
        wordsProgressBar.setToolTipText(TRANSLATOR.translate("Words(String)"));
        wordsProgressBar.setString(TRANSLATOR.translate("Words(String)"));
        wordsProgressBar.setStringPainted(true);

        panel.add(feedbackField, "span 5, left");
        feedbackField.setText(TRANSLATOR.translate("Feedback(Field)"));

        panel.add(timerIconLabel, "");
        timerIconLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/48x48/stopwatch.png")));
        panel.add(timerProgressBar, "");
        timerProgressBar.setForeground(new java.awt.Color(51, 255, 51));
        timerProgressBar.setToolTipText(TRANSLATOR.translate("Timer(String)"));
        timerProgressBar.setString(TRANSLATOR.translate("Timer(String)"));
        timerProgressBar.setStringPainted(true);

        return panel;
    }

    private void answerButtonActionPerformed(java.awt.event.ActionEvent evt) {
        answered();
        seconds = secondsBackup;
        answerField.requestFocus();
    }

    private void answerFieldActionPerformed(java.awt.event.ActionEvent evt) {
        answered();
        seconds = secondsBackup;
    }

    private void stopButtonActionPerformed(java.awt.event.ActionEvent evt) {
        wrongWords.add(examService.examWord());
        correctTranslation.add(examService.getTranslation());
        stopExam();
        pauseButton.setText(TRANSLATOR.translate("Pause(Button)"));

    }

    private void pauseButtonActionPerformed(java.awt.event.ActionEvent evt) {
        if (pauseButton.getText().equals(TRANSLATOR.translate("Pause(Button)"))) {
            swingTimer.stop();
            pauseButton.setText(TRANSLATOR.translate("Continue(Button)"));
            answerButton.setEnabled(false);
        } else if (pauseButton.getText().equals(TRANSLATOR.translate("Continue(Button)"))) {
            swingTimer.start();
            pauseButton.setText(TRANSLATOR.translate("Pause(Button)"));
            answerButton.setEnabled(true);
        }


    }

    private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {
        selectedDictionary = dictionaryService.getDictionary((Language) fromLanguageComboBox.getSelectedItem(), (Language) toLanguageComboBox.getSelectedItem());
        assert selectedDictionary != null;

        selectedLanguage = (Language) fromLanguageComboBox.getSelectedItem();
        assert selectedLanguage != null;

        LOGGER.info("Selected difficulty " + difficulty);
        LOGGER.info("Timer is " + enumTimerStatus);
        LOGGER.info("Selected language is " + selectedLanguage);

        wrongWords.clear();
        correctTranslation.clear();
        examService.getDifficultyWords(selectedDictionary, selectedLanguage, difficulty);
        totalWords = 0;
        correctWords = 0;

        callAnswerService();

        editability(false);

        examWords = PM.getInt(Preference.EXAM_WORDS, 10);
        examWordsCopy = examWords;

        if (enumTimerStatus == TimerStatus.STARTED || enumTimerStatus == TimerStatus.STOPPED) {

            if (timerEnabled) {
                seconds = difficulty.getTime();
            }
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
        answerField.requestFocus();
    }

    private void toLanguageComboBoxPopupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
        if (toLanguageComboBox.getSelectedItem() == fromLanguageComboBox.getSelectedItem()) {
            fromLanguageComboBox.setSelectedIndex(toWordsIndex);
        }
    }

    private void toLanguageComboBoxPopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
        toWordsIndex = toLanguageComboBox.getSelectedIndex();
    }

    private void fromLanguageComboBoxPopupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
        if (fromLanguageComboBox.getSelectedItem() == toLanguageComboBox.getSelectedItem()) {
            toLanguageComboBox.setSelectedIndex(fromWordsIndex);
        }
    }

    private void fromLanguageComboBoxPopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
        fromWordsIndex = fromLanguageComboBox.getSelectedIndex();
    }

    private void fromLanguageComboBoxActionPerformed(java.awt.event.ActionEvent evt) {
        final List<Language> languagesTo = examService.getToLanguages((Language) fromLanguageComboBox.getSelectedItem());
        toLanguageComboBox.removeAllItems();
        for (Language language : languagesTo) {
            toLanguageComboBox.addItem(language);
        }
    }

    @Override
    public ButtonPanel createButtonPanel() {
        ButtonPanel buttonPanel = new ButtonPanel();

        JButton quitButton = new JButton();
        JButton settingsButton = new JButton();

        quitButton.setName(TRANSLATOR.translate("Quit(Button)"));
        settingsButton.setName(TRANSLATOR.translate("Settings(Button)"));

        buttonPanel.add(settingsButton, ButtonPanel.OTHER_BUTTON);
        buttonPanel.add(quitButton, ButtonPanel.CANCEL_BUTTON);

        quitButton.setAction(new AbstractAction(UIDefaultsLookup.getString("OptionPane.cancelButtonText")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                setDialogResult(RESULT_CANCELLED);
                setVisible(false);
            }
        });

        settingsButton.setAction(new AbstractAction(UIDefaultsLookup.getString("OptionPane.settingsButtonText")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                PreferencesDialog preferencesDialog = new PreferencesDialog(null, true);
         //       preferencesDialog.getTabbedTane().setSelectedIndex(2);


                preferencesDialog.setLocationRelativeTo(null);
                PreferencesExtractor.extract(null, preferencesDialog);
                preferencesDialog.refreshNewSettingsToExam();
            }
        });
        return buttonPanel;
    }

    private void callAnswerService() {
        examService.getExamWord(selectedDictionary);
        translateField.setText(examService.examWord());
        totalWords++;
    }

    private void answered() {
        examWords--;
        displayTranslation();

        if (examWords == 0) {
            stopExam();

            examWords = examWordsCopy;
        } else {
            callAnswerService();
        }

        answerField.setText(null);
        answerField.requestFocusInWindow();
    }

    private void displayTranslation() {
        examService.possibleAnswers();
        String str;

        if (examWords == 0) {
            str = (examWordsCopy - examWords) + "/ " + examWordsCopy;
        } else {
            str = (examWordsCopy - examWords + 1) + "/ " + examWordsCopy;
        }

        wordsProgressBar.setString(str);
        wordsProgressBar.setValue(maximumWordsProgressBar - examWords + 1);

        if (examService.isCorrect(answerField.getText())) {
            wordsProgressBar.setForeground(new java.awt.Color(51, 255, 51));

            feedbackField.setText(TRANSLATOR.translate("CorrectAnswer(String)"));
            answerIconLabel.setIcon(IconManager.getImageIcon("bell2_green.png", IconManager.IconSize.SIZE24));
            correctWords++;
        } else {
            wordsProgressBar.setForeground(new java.awt.Color(204, 0, 0));
            feedbackField.setText(TRANSLATOR.translate("WrongAnser(String)"));
            answerIconLabel.setIcon(IconManager.getImageIcon("bell2_red.png", IconManager.IconSize.SIZE24));
            wrongWords.add(examService.examWord());
            correctTranslation.add(examService.getTranslation());
        }

    }
    private boolean flagLast = false;
    private Timer swingTimer = new Timer(1 * 1000, new ActionListener() {

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
                wrongWords.add(examService.examWord());
                correctTranslation.add(examService.getTranslation());
                stopExam();
                wordsProgressBar.setValue(maximumWordsProgressBar);
            }
        }
    });

    public void showExamDialog() {
        difficulty = difficulty.valueOf(PM.get(Preference.EXAM_DIFFICULTY, difficulty.name()));

        timerProgressBar.setVisible(timerEnabled);

        timerIconLabel.setVisible(timerEnabled);

        diffLabelText = PM.get(Preference.EXAM_DIFFICULTY, difficulty.name());
        diffLabelChange(diffLabelText);

        enumTimerStatus = timerEnabled ? TimerStatus.STOPPED : TimerStatus.DISABLED;
        examWords = PM.getInt(Preference.EXAM_WORDS, 10);
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
            difficulty = Difficulty.EASY;
        }
        if (diff.equals("MEDIUM")) {
            difficultyLabel.setText(TRANSLATOR.translate("Medium(Label)"));
            difficulty = Difficulty.MEDIUM;
        }
        if (diff.equals("HARD")) {
            difficultyLabel.setText(TRANSLATOR.translate("Hard(Label)"));
            difficulty = Difficulty.HARD;
        }
    }

    private void editability(Boolean a) {

        fromLanguageComboBox.setEnabled(a);
        toLanguageComboBox.setEnabled(a);
        // settingsButton.setEnabled(a);
        startButton.setEnabled(a);
        stopButton.setEnabled(!a);
        answerButton.setEnabled(!a);
        pauseButton.setEnabled(!a);
        answerField.setEnabled(!a);

    }

    private void timerRunButton() {
        seconds = difficulty.getTime();

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
        examWords = PM.getInt(Preference.EXAM_WORDS, examWords);
        examResult();

    }

    private void examResult() {

        ExamResult examResultDialog = new ExamResult(null, true);
        examResultDialog.setLocationRelativeTo(this);
        examResultDialog.showExamResult(correctWords, totalWords);

    }

    public static List<String> getWrongWords() {
        return wrongWords;
    }

    public static List<String> getCorrectTranslation() {
        return correctTranslation;
    }

    public static void setTimerProgressbarVisible() {
        timerProgressBar.setVisible(true);
        stopButton.setEnabled(false);
        timerIconLabel.setVisible(true);
    }

    public static void setTimerProgressbarInvisible() {
        timerProgressBar.setVisible(false);
        pauseButton.setEnabled(false);
        timerIconLabel.setVisible(false);
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

    public static String returnDiffLabelText() {
        return difficultyLabel.getText();
    }

    private void initLanguages() {
        final List<Dictionary> availableDictionaries = dictionaryService.getDictionaries();
        Set<Language> addedLanguages = new HashSet<Language>();
        for (Dictionary dictionary : availableDictionaries) {
            final Language languageFrom = dictionary.getFromLanguage();
            if (!addedLanguages.contains(languageFrom)) {
                addedLanguages.add(languageFrom);
                fromLanguageComboBox.addItem(languageFrom);
            }
        }
    }

//    public static void main(String[] args) {
//        ExamDialog examDialog = new ExamDialog(null, true);
//        examDialog.setVisible(true);
//    }
}