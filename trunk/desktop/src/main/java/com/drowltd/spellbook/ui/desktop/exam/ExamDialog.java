package com.drowltd.spellbook.ui.desktop.exam;

import com.drowltd.spellbook.core.i18n.Translator;
import com.drowltd.spellbook.core.model.Dictionary;
import com.drowltd.spellbook.core.model.Difficulty;
import com.drowltd.spellbook.core.model.Language;
import com.drowltd.spellbook.core.preferences.PreferencesManager;
import com.drowltd.spellbook.core.preferences.PreferencesManager.Preference;
import com.drowltd.spellbook.core.service.DictionaryService;
import com.drowltd.spellbook.core.service.exam.ExamService;
import com.drowltd.spellbook.ui.desktop.PreferencesDialog;
import com.drowltd.spellbook.ui.desktop.PreferencesExtractor;
import com.drowltd.spellbook.ui.swing.util.IconManager;
import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.dialog.StandardDialog;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * @author Ivan Spasov
 * @author Georgi Angelov
 * @author Miroslava Stancheva
 * @since 0.2
 */
public class ExamDialog extends StandardDialog {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExamDialog.class);
    private ExamService examService;
    private static final Translator TRANSLATOR = Translator.getTranslator("ExamDialog");
    private static final PreferencesManager PM = PreferencesManager.getInstance();

    private int seconds = 0;
    private int secondsBackup = 0;
    private int examWords;
    private int examWordsCopy;
    private int maximumSecondsProgressBar = 0;
    private int maximumWordsProgressBar = 0;
    private static Difficulty difficulty = Difficulty.EASY;
    private Dictionary selectedDictionary;
    private final DictionaryService dictionaryService = DictionaryService.getInstance();
    private int totalWords;
    private int correctWords;
    private List<String> wrongWords = new ArrayList<String>();
    private boolean timerEnabled = PM.getBoolean(Preference.EXAM_TIMER, false);

    public enum TimerStatus {
        PAUSED, STARTED, STOPPED, DISABLED
    }

    private static TimerStatus enumTimerStatus = TimerStatus.DISABLED;
    private JComboBox fromLanguageComboBox;
    private JComboBox toLanguageComboBox;
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

        fromLanguageComboBox = new JComboBox();
        toLanguageComboBox = new JComboBox();
        difficultyLabel = new JLabel();
        startButton = new JButton();
        translateField = new JTextField();
        answerField = new JTextField();
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

        setSize(425, 490);
        initLanguages();
    }

    @Override
    public JComponent createBannerPanel() {
        return null;
    }

    @Override
    public JComponent createContentPanel() {
        JPanel contentPanel = new JPanel(new MigLayout("wrap 5", "[grow][grow][grow][grow][grow]", "[grow][][][][grow][grow][grow][grow][][grow][grow][]"));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        contentPanel.add(new JLabel(TRANSLATOR.translate("Languages(Label)")), "span, left");
        contentPanel.add(new JLabel(TRANSLATOR.translate("From(Label)")), "left");
        contentPanel.add(fromLanguageComboBox, "right, growx");

        fromLanguageComboBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                fromLanguageComboBoxActionPerformed(evt);
            }
        });

        contentPanel.add(new JLabel(TRANSLATOR.translate("To(Label)")), "left");
        contentPanel.add(toLanguageComboBox, "right, growx");
        contentPanel.add(new JLabel(IconManager.getImageIcon("dictionary.png", IconManager.IconSize.SIZE48)), "right");

        contentPanel.add(new JLabel(TRANSLATOR.translate("Difficulty(Label)")), "left");
        contentPanel.add(difficultyLabel, "left");

        contentPanel.add(startButton, "right");
        startButton.setIcon(IconManager.getMenuIcon("media_play_green.png"));
        startButton.setText(TRANSLATOR.translate("Start(Button)"));
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });

        contentPanel.add(pauseButton, "center");
        pauseButton.setIcon(IconManager.getMenuIcon("media_pause.png"));
        pauseButton.setText(TRANSLATOR.translate("Pause(Button)"));
        pauseButton.setEnabled(false);
        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                pauseButtonActionPerformed(evt);
            }
        });

        contentPanel.add(stopButton, "span, wrap");
        stopButton.setIcon(IconManager.getMenuIcon("media_stop_red.png"));
        stopButton.setText(TRANSLATOR.translate("Stop(Button)"));
        stopButton.setEnabled(false);
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                stopButtonActionPerformed(evt);
            }
        });

        contentPanel.add(new JLabel(TRANSLATOR.translate("OverTranslateField(Label)")), "span, left");

        contentPanel.add(translateField, "span 5, left, growx");
        translateField.setEditable(false);

        contentPanel.add(new JLabel(TRANSLATOR.translate("OverAnswerField(Label)")), "span, left");

        contentPanel.add(answerField, "span 5, left, growx");
        answerField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                answerFieldActionPerformed(evt);
            }
        });

        contentPanel.add(answerIconLabel, "span 4, right");
        answerIconLabel.setIcon(new ImageIcon(getClass().getResource("/icons/24x24/bell2_grey.png")));
        contentPanel.add(answerButton, "left, span, growx");
        answerButton.setIcon(new ImageIcon(getClass().getResource("/icons/16x16/check2.png")));
        answerButton.setText(TRANSLATOR.translate("Answer(Button)"));
        answerButton.setEnabled(false);
        answerButton.setMaximumSize(new Dimension(75, 25));
        answerButton.setMinimumSize(new Dimension(75, 25));
        answerButton.setPreferredSize(new Dimension(75, 25));
        answerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                answerButtonActionPerformed(evt);
            }
        });

        contentPanel.add(wordsProgressBar, "span, grow");
        wordsProgressBar.setForeground(new Color(102, 102, 255));
        wordsProgressBar.setToolTipText(TRANSLATOR.translate("Words(String)"));
        wordsProgressBar.setString(TRANSLATOR.translate("Words(String)"));
        wordsProgressBar.setStringPainted(true);

        contentPanel.add(feedbackField, "span, left");
        feedbackField.setText(TRANSLATOR.translate("Feedback(Field)"));

        contentPanel.add(timerIconLabel, "");
        timerIconLabel.setIcon(new ImageIcon(getClass().getResource("/icons/48x48/stopwatch.png")));
        contentPanel.add(timerProgressBar, "span, grow");
        timerProgressBar.setForeground(new Color(51, 255, 51));
        timerProgressBar.setToolTipText(TRANSLATOR.translate("Timer(String)"));
        timerProgressBar.setString(TRANSLATOR.translate("Timer(String)"));
        timerProgressBar.setStringPainted(true);

        return contentPanel;
    }

    private void answerButtonActionPerformed(ActionEvent evt) {
        answered();
        seconds = secondsBackup;
        answerField.requestFocus();
    }

    private void answerFieldActionPerformed(ActionEvent evt) {
        answered();
        seconds = secondsBackup;
    }

    private void stopButtonActionPerformed(ActionEvent evt) {
        wrongWords.add(examService.examWord());
        stopExam();
        pauseButton.setText(TRANSLATOR.translate("Pause(Button)"));

    }

    private void pauseButtonActionPerformed(ActionEvent evt) {
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

    private void startButtonActionPerformed(ActionEvent evt) {
        selectedDictionary = dictionaryService.getDictionary((Language) fromLanguageComboBox.getSelectedItem(),
                (Language) toLanguageComboBox.getSelectedItem());
        assert selectedDictionary != null;

        Language selectedLanguage = (Language) fromLanguageComboBox.getSelectedItem();
        assert selectedLanguage != null;

        LOGGER.info("Selected difficulty " + difficulty);
        LOGGER.info("Timer is " + enumTimerStatus);
        LOGGER.info("Selected language is " + selectedLanguage);

        wrongWords.clear();
        examService.getDifficultyWords(selectedDictionary, selectedLanguage, difficulty);
        totalWords = 0;
        correctWords = 0;

        callAnswerService();

        enableComponents(false);

        examWords = PM.getInt(Preference.EXAM_WORDS, 10);
        examWordsCopy = examWords;

        if (enumTimerStatus == TimerStatus.STARTED || enumTimerStatus == TimerStatus.STOPPED) {

            if (timerEnabled) {
                seconds = difficulty.getTime();
            }
            timerRunButton();
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

    private void fromLanguageComboBoxActionPerformed(ActionEvent evt) {
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
        ResourceBundle bundle = ResourceBundle.getBundle("i18n/ExamDialog");

        quitButton.setAction(new AbstractAction(bundle.getString("Quit(Button)")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                setDialogResult(RESULT_CANCELLED);
                setVisible(false);
            }
        });

        settingsButton.setAction(new AbstractAction(bundle.getString("Settings(Button)")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                PreferencesDialog preferencesDialog = new PreferencesDialog(null, true);
                preferencesDialog.getTabbedPane().setSelectedIndex(2);

                preferencesDialog.setLocationRelativeTo(null);
                PreferencesExtractor.extract(null, preferencesDialog);
                preferencesDialog.refreshNewSettingsToExam();
            }
        });

        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

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
            wordsProgressBar.setForeground(new Color(51, 255, 51));

            feedbackField.setText(TRANSLATOR.translate("CorrectAnswer(String)"));
            answerIconLabel.setIcon(IconManager.getImageIcon("bell2_green.png", IconManager.IconSize.SIZE24));
            correctWords++;
        } else {
            wordsProgressBar.setForeground(new Color(204, 0, 0));
            feedbackField.setText(TRANSLATOR.translate("WrongAnswer(String)"));
            answerIconLabel.setIcon(IconManager.getImageIcon("bell2_red.png", IconManager.IconSize.SIZE24));
            wrongWords.add(examService.examWord());
        }

    }

    private boolean flagLast = false;
    private Timer swingTimer = new Timer(1000, new ActionListener() {

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
                        wordsProgressBar.setForeground(new Color(204, 0, 0));
                    } else {
                        wordsProgressBar.setForeground(new Color(204, 0, 0));
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
                stopExam();
                wordsProgressBar.setValue(maximumWordsProgressBar);
            }
        }
    });

    public void showExamDialog() {

        difficulty = Difficulty.valueOf(PM.get(Preference.EXAM_DIFFICULTY, difficulty.name()));

        timerProgressBar.setVisible(timerEnabled);

        timerIconLabel.setVisible(timerEnabled);

        String diffLabelText = PM.get(Preference.EXAM_DIFFICULTY, difficulty.name());
        diffLabelChange(diffLabelText);

        enumTimerStatus = timerEnabled ? TimerStatus.STOPPED : TimerStatus.DISABLED;
        examWords = PM.getInt(Preference.EXAM_WORDS, 10);
        examWordsCopy = examWords;
        setVisible(true);
    }

    /* This method handles the text in GUI, which is related to difficulty
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

    private void enableComponents(Boolean a) {
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
        enableComponents(true);
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

    public static void setTimerProgressBarVisible() {
        timerProgressBar.setVisible(true);
        stopButton.setEnabled(false);
        timerIconLabel.setVisible(true);
    }

    public static void setTimerProgressBarInvisible() {
        timerProgressBar.setVisible(false);
        pauseButton.setEnabled(false);
        timerIconLabel.setVisible(false);
    }

    public static void setEnumTimerStatus(TimerStatus timerStatus) {
        enumTimerStatus = timerStatus;
    }

    public static void setFeedbackFieldDefault() {
        feedbackField.setText(TRANSLATOR.translate("Feedback(Field)"));
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

        final List<Language> languagesTo = examService.getToLanguages((Language) fromLanguageComboBox.getSelectedItem());
        toLanguageComboBox.removeAllItems();
        for (Language language : languagesTo) {
            toLanguageComboBox.addItem(language);
        }
    }
}

