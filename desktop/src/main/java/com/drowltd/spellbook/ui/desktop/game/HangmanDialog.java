package com.drowltd.spellbook.ui.desktop.game;

import com.drowltd.spellbook.core.i18n.Translator;
import com.drowltd.spellbook.core.model.Dictionary;
import com.drowltd.spellbook.core.model.Difficulty;
import com.drowltd.spellbook.core.model.Language;
import com.drowltd.spellbook.core.preferences.PreferencesManager;
import com.drowltd.spellbook.core.service.DictionaryService;
import com.drowltd.spellbook.core.service.exam.ExamService;
import com.drowltd.spellbook.ui.swing.component.BaseDialog;
import com.drowltd.spellbook.ui.swing.component.DifficultyComboBox;
import com.drowltd.spellbook.ui.swing.util.IconManager;
import com.jidesoft.dialog.ButtonPanel;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The classic Hangman game.
 *
 * @author Bozhidar Batsov
 * @since 0.4
 */
public class HangmanDialog extends BaseDialog {
    private static final Logger LOGGER = LoggerFactory.getLogger(HangmanDialog.class);
    private ExamService examService = ExamService.getInstance();
    private final DictionaryService dictionaryService = DictionaryService.getInstance();
    private static final Translator TRANSLATOR = Translator.getTranslator("HangmanDialog");
    private static final PreferencesManager PM = PreferencesManager.getInstance();

    private Difficulty difficulty;
    private Dictionary selectedDictionary;

    private Set<Character> guessedChars = new HashSet<Character>();
    private int attempts = 0;

    private static final int MAX_ATTEMPTS = 7;

    private JComboBox fromLanguageComboBox;
    private JComboBox toLanguageComboBox;
    private DifficultyComboBox difficultyComboBox;
    private JLabel answerIconLabel;
    private JLabel feedbackField;
    private JButton startButton;
    private JButton stopButton;
    private JButton answerButton;
    private JTextField guessWordField;
    private JTextField answerField;

    private String currentWord;

    public HangmanDialog(Frame parent, boolean modal) {
        super(parent, modal);

        TRANSLATOR.reset();

        fromLanguageComboBox = new JComboBox();
        toLanguageComboBox = new JComboBox();
        difficultyComboBox = new DifficultyComboBox();
        startButton = new JButton();
        guessWordField = new JTextField();
        answerField = new JTextField();
        stopButton = new JButton();
        answerButton = new JButton();
        answerIconLabel = new JLabel();
        feedbackField = new JLabel();

        setIconImage(IconManager.getImageIcon("dictionary.png", IconManager.IconSize.SIZE16).getImage());
        setTitle(TRANSLATOR.translate("Hangman(Title)"));

        initLanguages();
    }

    @Override
    public JComponent createContentPanel() {
        JPanel contentPanel = new JPanel(new MigLayout("wrap 5", "[grow][grow][grow][grow][grow]", "[grow][][][][grow][grow][grow][grow][][grow][grow][]"));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        contentPanel.add(new JLabel(TRANSLATOR.translate("Languages(Label)")), "span, left");
        contentPanel.add(new JLabel(TRANSLATOR.translate("From(Label)")), "left");
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

        contentPanel.add(new JLabel(TRANSLATOR.translate("To(Label)")), "right");
        contentPanel.add(toLanguageComboBox, "right, growx");
        contentPanel.add(new JLabel(IconManager.getImageIcon("dictionary.png", IconManager.IconSize.SIZE48)), "center");

        contentPanel.add(new JLabel(TRANSLATOR.translate("Difficulty(Label)")), "left");
        contentPanel.add(difficultyComboBox, "growx");

        contentPanel.add(startButton, "growx");
        startButton.setIcon(IconManager.getMenuIcon("media_play_green.png"));
        startButton.setText(TRANSLATOR.translate("Start(Button)"));
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                startGame();
            }
        });

        contentPanel.add(stopButton, "growx");
        stopButton.setIcon(IconManager.getMenuIcon("media_stop_red.png"));
        stopButton.setText(TRANSLATOR.translate("Stop(Button)"));
        stopButton.setEnabled(false);
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                stopGame();
            }
        });

        contentPanel.add(new JLabel(TRANSLATOR.translate("OverTranslateField(Label)")), "span, left");

        contentPanel.add(guessWordField, "span 5, left, growx");
        guessWordField.setEditable(false);

        contentPanel.add(new JLabel(TRANSLATOR.translate("OverAnswerField(Label)")), "span, left");

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
        answerButton.setText(TRANSLATOR.translate("Answer(Button)"));
        answerButton.setEnabled(false);
        answerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                answered();
                answerField.requestFocus();
            }
        });

        contentPanel.add(feedbackField, "span, left");
        feedbackField.setText(TRANSLATOR.translate("Feedback(Field)"));

        return contentPanel;
    }

    private void startGame() {
        selectedDictionary = dictionaryService.getDictionary((Language) fromLanguageComboBox.getSelectedItem(),
                (Language) toLanguageComboBox.getSelectedItem());
        assert selectedDictionary != null;

        Language selectedLanguage = (Language) fromLanguageComboBox.getSelectedItem();
        assert selectedLanguage != null;

        difficulty = (Difficulty) difficultyComboBox.getSelectedItem();
        assert difficulty != null;

        LOGGER.info("Selected difficulty " + difficulty);
        LOGGER.info("Selected language is " + selectedLanguage);

        examService.getDifficultyWords(selectedDictionary, selectedLanguage, difficulty);

        currentWord = examService.examWord();

        nextGuess();

        enableComponents(false);

        feedbackField.setText(TRANSLATOR.translate("GameStarted(Label)"));
        answerField.requestFocus();
    }

    private void stopGame() {
        answerIconLabel.setIcon(IconManager.getImageIcon("bell2_grey.png", IconManager.IconSize.SIZE24));
        feedbackField.setText(TRANSLATOR.translate("EndOfGame(Message)"));

        enableComponents(true);
        guessWordField.setText(null);
        answerField.setText(null);
    }

    private void nextGuess() {
        guessWordField.setText(maskWord(currentWord));
    }

    private void answered() {
        displayTranslation();

        if (attempts >= MAX_ATTEMPTS) {
            stopGame();
        } else {
            nextGuess();
        }

        answerField.setText(null);
        answerField.requestFocusInWindow();
    }

    private void displayTranslation() {
        examService.possibleAnswers();

        if (examService.isCorrect(answerField.getText())) {
            feedbackField.setText(TRANSLATOR.translate("CorrectAnswer(String)"));
            answerIconLabel.setIcon(IconManager.getImageIcon("bell2_green.png", IconManager.IconSize.SIZE24));
        } else {
            feedbackField.setText(TRANSLATOR.translate("WrongAnswer(String)"));
            answerIconLabel.setIcon(IconManager.getImageIcon("bell2_red.png", IconManager.IconSize.SIZE24));
        }
    }

    private void enableComponents(Boolean enable) {
        fromLanguageComboBox.setEnabled(enable);
        toLanguageComboBox.setEnabled(enable);
        startButton.setEnabled(enable);
        stopButton.setEnabled(!enable);
        answerButton.setEnabled(!enable);
        answerField.setEnabled(!enable);
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

        JButton quitButton = new JButton();

        quitButton.setName(TRANSLATOR.translate("Quit(Button)"));
        buttonPanel.add(quitButton, ButtonPanel.CANCEL_BUTTON);

        quitButton.setAction(new AbstractAction(TRANSLATOR.translate("Quit(Button)")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                setDialogResult(RESULT_CANCELLED);
                setVisible(false);
            }
        });
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        return buttonPanel;
    }

    private String maskWord(String word) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < word.length(); i++) {
            if (guessedChars.contains(word.charAt(i))) {
                result.append(word.charAt(i));
            } else {
                result.append("_");
            }
        }

        return result.toString();
    }
}
