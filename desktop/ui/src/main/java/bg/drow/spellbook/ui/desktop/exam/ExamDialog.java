package bg.drow.spellbook.ui.desktop.exam;

import bg.drow.spellbook.core.model.Dictionary;
import bg.drow.spellbook.core.model.Difficulty;
import bg.drow.spellbook.core.model.Language;
import bg.drow.spellbook.core.preferences.PreferencesManager;
import bg.drow.spellbook.core.service.DictionaryService;
import bg.drow.spellbook.core.service.exam.ExamService;
import bg.drow.spellbook.ui.desktop.PreferencesDialog;
import bg.drow.spellbook.ui.desktop.PreferencesExtractor;
import bg.drow.spellbook.ui.desktop.SpellbookFrame;
import bg.drow.spellbook.ui.swing.component.BaseDialog;
import bg.drow.spellbook.ui.swing.component.DifficultyComboBox;
import bg.drow.spellbook.ui.swing.util.IconManager;
import com.jidesoft.dialog.ButtonPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:bozhidar@drow.bg">Bozhidar Batsov</a>
 * @since 0.2
 */
public class ExamDialog extends BaseDialog {
    private ExamService examService = ExamService.getInstance();
    private final DictionaryService dictionaryService = DictionaryService.getInstance();
    private static final PreferencesManager PM = PreferencesManager.getInstance();

    private int examWords;
    private Difficulty difficulty;
    private Dictionary selectedDictionary;
    private ExamStats examStats;

    private Frame parent;
    private JButton settingsButton;

    private static enum TimerStatus {
        PAUSED, STARTED, STOPPED, DISABLED
    }

    private TimerStatus timerStatus;
    private JComboBox fromLanguageComboBox;
    private JComboBox toLanguageComboBox;
    private DifficultyComboBox difficultyComboBox;
    private JLabel timerIconLabel;
    private JLabel answerIconLabel;
    private JLabel feedbackField;
    private JButton startButton;
    private JButton stopButton;
    private JButton answerButton;
    private JButton pauseButton;
    private JTextField translateField;
    private JTextField answerField;
    private JProgressBar timerProgressBar;
    private JProgressBar wordsProgressBar;

    public ExamDialog(Frame parent, boolean modal) {
        super(parent, modal);

        this.parent = parent;

        fromLanguageComboBox = new JComboBox();
        toLanguageComboBox = new JComboBox();
        difficultyComboBox = new DifficultyComboBox();
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
        readExamPreferences();

        initLanguages();
    }

    private void readExamPreferences() {
        // set the default difficulty
        difficultyComboBox.setSelectedItem(Difficulty.valueOf(PM.get(PreferencesManager.Preference.EXAM_DIFFICULTY, Difficulty.EASY.name())));

        boolean timerEnabled = PM.getBoolean(PreferencesManager.Preference.EXAM_TIMER, false);
        timerProgressBar.setVisible(timerEnabled);

        timerIconLabel.setVisible(timerEnabled);

        timerStatus = timerEnabled ? TimerStatus.STOPPED : TimerStatus.DISABLED;
        examWords = PM.getInt(PreferencesManager.Preference.EXAM_WORDS, 10);
    }

    @Override
    public JComponent createContentPanel() {
        JPanel contentPanel = new JPanel(new MigLayout("wrap 5", "[grow][grow][grow][grow][grow]", "[grow][][][][grow][grow][grow][grow][][grow][grow][]"));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        contentPanel.add(new JLabel(getTranslator().translate("Languages(Label)")), "span, left");
        contentPanel.add(new JLabel(getTranslator().translate("From(Label)")), "left");
        contentPanel.add(fromLanguageComboBox, "growx");

        fromLanguageComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                final List<Language> languagesTo = examService.getToLanguages((Language) fromLanguageComboBox.getSelectedItem());

                toLanguageComboBox.removeAllItems();

                for (Language language : languagesTo) {
                    toLanguageComboBox.addItem(language);
                }
            }
        });

        contentPanel.add(new JLabel(getTranslator().translate("To(Label)")), "right");
        contentPanel.add(toLanguageComboBox, "right, growx");
        contentPanel.add(new JLabel(IconManager.getImageIcon("dictionary.png", IconManager.IconSize.SIZE48)), "center");

        contentPanel.add(new JLabel(getTranslator().translate("Difficulty(Label)")), "left");
        contentPanel.add(difficultyComboBox, "growx");

        contentPanel.add(startButton, "growx");
        startButton.setIcon(IconManager.getMenuIcon("media_play_green.png"));
        startButton.setText(getTranslator().translate("Start(Button)"));
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                startExam();
            }
        });

        contentPanel.add(pauseButton, "growx");
        pauseButton.setIcon(IconManager.getMenuIcon("media_pause.png"));
        pauseButton.setText(getTranslator().translate("Pause(Button)"));
        pauseButton.setEnabled(false);
        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                pauseExam();
            }
        });

        contentPanel.add(stopButton, "growx");
        stopButton.setIcon(IconManager.getMenuIcon("media_stop_red.png"));
        stopButton.setText(getTranslator().translate("Stop(Button)"));
        stopButton.setEnabled(false);
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                stopExam();
                pauseButton.setText(getTranslator().translate("Pause(Button)"));
            }
        });

        contentPanel.add(new JLabel(getTranslator().translate("OverTranslateField(Label)")), "span, left");

        contentPanel.add(translateField, "span 5, left, growx");
        translateField.setEditable(false);

        contentPanel.add(new JLabel(getTranslator().translate("OverAnswerField(Label)")), "span, left");

        contentPanel.add(answerField, "span 5, left, growx");
        answerField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                answered();
            }
        });

        contentPanel.add(answerIconLabel, "span 4, right");
        answerIconLabel.setIcon(IconManager.getImageIcon("bell2_grey.png", IconManager.IconSize.SIZE24));
        contentPanel.add(answerButton, "left, span, growx");
        answerButton.setIcon(IconManager.getMenuIcon("check2.png"));
        answerButton.setText(getTranslator().translate("Answer(Button)"));
        answerButton.setEnabled(false);
        answerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                answered();
                answerField.requestFocus();
            }
        });

        contentPanel.add(wordsProgressBar, "span, grow");
        wordsProgressBar.setForeground(Color.GREEN);
        wordsProgressBar.setString("0/0");
        wordsProgressBar.setStringPainted(true);

        contentPanel.add(feedbackField, "span, left");
        feedbackField.setText(getTranslator().translate("Feedback(Field)"));

        contentPanel.add(timerIconLabel, "");
        timerIconLabel.setIcon(IconManager.getImageIcon("stopwatch.png", IconManager.IconSize.SIZE48));
        contentPanel.add(timerProgressBar, "span, growx");
        timerProgressBar.setForeground(Color.GREEN);
        timerProgressBar.setString(getTranslator().translate("NotActive(Message)"));
        timerProgressBar.setStringPainted(true);

        return contentPanel;
    }

    private void pauseExam() {
        if (timerStatus == TimerStatus.STARTED) {
            swingTimer.stop();
            pauseButton.setText(getTranslator().translate("Continue(Button)"));
            answerButton.setEnabled(false);
            timerStatus = TimerStatus.PAUSED;
        } else if (timerStatus == TimerStatus.PAUSED) {
            swingTimer.start();
            pauseButton.setText(getTranslator().translate("Pause(Button)"));
            answerButton.setEnabled(true);
            timerStatus = TimerStatus.STARTED;
        }
    }

    private void startExam() {
        settingsButton.getAction().setEnabled(false);

        selectedDictionary = dictionaryService.getDictionary((Language) fromLanguageComboBox.getSelectedItem(),
                (Language) toLanguageComboBox.getSelectedItem());

        Language selectedLanguage = (Language) fromLanguageComboBox.getSelectedItem();

        difficulty = (Difficulty) difficultyComboBox.getSelectedItem();

        getLogger().info("Selected difficulty " + difficulty);
        getLogger().info("Timer is " + timerStatus);
        getLogger().info("Selected language is " + selectedLanguage);

        examService.getDifficultyWords(selectedDictionary, selectedLanguage, difficulty);

        examStats = new ExamStats();
        examStats.setDifficulty(difficulty);
        examStats.setDictionary(selectedDictionary);

        nextWord();

        enableComponents(false);

        examWords = PM.getInt(PreferencesManager.Preference.EXAM_WORDS, 10);

        if (timerStatus == TimerStatus.STARTED || timerStatus == TimerStatus.STOPPED) {
            timerRunButton();
            timerStatus = TimerStatus.STARTED;
            timerIconLabel.setIcon(IconManager.getImageIcon("stopwatch_run.png", IconManager.IconSize.SIZE48));
        } else {
            timerStatus = TimerStatus.DISABLED;
            timerIconLabel.setIcon(IconManager.getImageIcon("stopwatch_stop.png", IconManager.IconSize.SIZE48));
            pauseButton.setEnabled(false);
        }

        wordsProgressBar.setMaximum(examWords);
        wordsProgressBar.setString("0/" + examStats.getTotalWords());
        wordsProgressBar.setValue(1);
        feedbackField.setText(getTranslator().translate("ExamStarted(Label)"));
        answerField.requestFocus();
    }

    private void stopExam() {
        settingsButton.getAction().setEnabled(true);

        swingTimer.stop();
        timerProgressBar.setValue(0);
        examStats.setEndTime(new Date());

        if (timerStatus != TimerStatus.DISABLED) {
            timerStatus = TimerStatus.STOPPED;
            timerIconLabel.setIcon(IconManager.getImageIcon("stopwatch_stop.png", IconManager.IconSize.SIZE48));
        }

        // clear state
        timerProgressBar.setString(getTranslator().translate("NotActive(Message)"));
        wordsProgressBar.setValue(0);
        answerIconLabel.setIcon(IconManager.getImageIcon("bell2_grey.png", IconManager.IconSize.SIZE24));
        feedbackField.setText(getTranslator().translate("EndOfExam(Message)"));

        enableComponents(true);
        translateField.setText(null);
        answerField.setText(null);

        // reread config
        examWords = PM.getInt(PreferencesManager.Preference.EXAM_WORDS, examWords);

        // don't show results for empty exam sessions
        if (examStats.getTotalWords() > 0) {
            showExamResult();
        }
    }

    private void nextWord() {
        examService.getExamWord(selectedDictionary);
        translateField.setText(examService.examWord());
    }

    private void answered() {
        displayTranslation();

        // reset the timer on answer
        if (timerStatus == TimerStatus.STARTED) {
            swingTimer.restart();
            timerProgressBar.setValue(0);
        }

        if (examStats.getTotalWords() >= examWords) {
            stopExam();
        } else {
            nextWord();
        }

        answerField.setText(null);
        answerField.requestFocusInWindow();
    }

    private void displayTranslation() {
        examService.possibleAnswers();

        if (examService.isCorrect(answerField.getText())) {
            wordsProgressBar.setForeground(Color.GREEN);
            feedbackField.setText(getTranslator().translate("CorrectAnswer(String)"));
            answerIconLabel.setIcon(IconManager.getImageIcon("bell2_green.png", IconManager.IconSize.SIZE24));
            examStats.getCorrectWords().add(translateField.getText());
        } else {
            wordsProgressBar.setForeground(Color.RED);
            feedbackField.setText(getTranslator().translate("WrongAnswer(String)"));
            answerIconLabel.setIcon(IconManager.getImageIcon("bell2_red.png", IconManager.IconSize.SIZE24));
            examStats.getIncorrectWords().add(translateField.getText());
        }

        wordsProgressBar.setString(examStats.getCorrectWords().size() + "/" + examStats.getTotalWords());
        wordsProgressBar.setValue(examStats.getTotalWords() + 1);
    }

    private Timer swingTimer = new Timer(1000, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            timerProgressBar.setValue(timerProgressBar.getValue() + 1);
            int remainingTime = difficulty.getTime() - timerProgressBar.getValue();

            // if the time expires continue with the next question
            if (remainingTime == 0) {
                answered();
            }

            // if the remaining time is little make it stand out
            if (remainingTime < 6) {
                timerProgressBar.setForeground(Color.RED);
            }

            timerProgressBar.setString("Remaining time: " + remainingTime);
        }
    });

    private void enableComponents(Boolean enable) {
        fromLanguageComboBox.setEnabled(enable);
        toLanguageComboBox.setEnabled(enable);
        startButton.setEnabled(enable);
        stopButton.setEnabled(!enable);
        answerButton.setEnabled(!enable);
        pauseButton.setEnabled(!enable);
        answerField.setEnabled(!enable);
    }

    private void timerRunButton() {
        swingTimer.start();
        pauseButton.setEnabled(true);
        timerProgressBar.setMaximum(difficulty.getTime());
    }

    private void showExamResult() {
        ExamSummaryDialog examSummaryDialog = new ExamSummaryDialog(this, examStats);
        examSummaryDialog.showExamResult();
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

    @Override
    public ButtonPanel createButtonPanel() {
        ButtonPanel buttonPanel = new ButtonPanel();

        settingsButton = new JButton();

        buttonPanel.add(settingsButton, ButtonPanel.OTHER_BUTTON);
        buttonPanel.add(createCloseButton(), ButtonPanel.CANCEL_BUTTON);

        settingsButton.setAction(new AbstractAction(getBaseTranslator().translate("Settings(Button)")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                PreferencesDialog preferencesDialog = new PreferencesDialog(null, true);
                preferencesDialog.getTabbedPane().setSelectedIndex(2);
                preferencesDialog.setLocationRelativeTo(null);

                PreferencesExtractor.extract((SpellbookFrame) parent, preferencesDialog);

                //Reload config
                readExamPreferences();
            }
        });

        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        return buttonPanel;
    }
}

