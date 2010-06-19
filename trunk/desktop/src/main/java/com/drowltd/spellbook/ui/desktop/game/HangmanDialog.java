package com.drowltd.spellbook.ui.desktop.game;

import com.drowltd.spellbook.core.i18n.Translator;
import com.drowltd.spellbook.core.model.Dictionary;
import com.drowltd.spellbook.core.model.Difficulty;
import com.drowltd.spellbook.core.model.Language;
import com.drowltd.spellbook.core.service.DictionaryService;
import com.drowltd.spellbook.core.service.exam.ExamService;
import com.drowltd.spellbook.ui.swing.component.BaseDialog;
import com.drowltd.spellbook.ui.swing.component.DifficultyComboBox;
import com.drowltd.spellbook.ui.swing.util.IconManager;
import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.swing.DefaultOverlayable;
import com.jidesoft.swing.OverlayTextField;
import com.jidesoft.swing.OverlayableIconsFactory;
import com.jidesoft.swing.OverlayableUtils;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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

    private Set<Character> guessedChars = new HashSet<Character>();
    private int attempts = 0;

    private static final int MAX_ATTEMPTS = 7;

    private JComboBox fromLanguageComboBox;
    private JComboBox toLanguageComboBox;
    private DifficultyComboBox difficultyComboBox;
    private JLabel guessIconLabel;
    private JLabel feedbackField;
    private JButton startButton;
    private JButton stopButton;
    private JButton guessButton;
    private JTextField wordField;
    private JTextField translationField;
    private JTextField guessField;
    private JLabel guessFieldOverlayLabel;
    private JLabel remainingGuessesLabel;

    private HangmanDrawing hangmanDrawing = new HangmanDrawing();

    private String currentWord;

    public HangmanDialog(Frame parent, boolean modal) {
        super(parent, modal);

        TRANSLATOR.reset();

        fromLanguageComboBox = new JComboBox();
        toLanguageComboBox = new JComboBox();
        difficultyComboBox = new DifficultyComboBox();
        startButton = new JButton();
        wordField = new JTextField();
        translationField = new JTextField();
        guessField = new OverlayTextField();
        guessFieldOverlayLabel = new JLabel();
        stopButton = new JButton();
        guessButton = new JButton();
        guessIconLabel = new JLabel();
        feedbackField = new JLabel();

        setIconImage(IconManager.getImageIcon("brain.png", IconManager.IconSize.SIZE16).getImage());
        setTitle(TRANSLATOR.translate("Hangman(Title)"));

        initLanguages();
    }

    @Override
    public JComponent createContentPanel() {
        JPanel contentPanel = new JPanel(new MigLayout("wrap 5", "[grow][grow][grow][grow][grow]", "[grow][][][][grow][grow][grow][grow][][grow][grow][][][]"));
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

        contentPanel.add(stopButton, "growx, wrap");
        stopButton.setIcon(IconManager.getMenuIcon("media_stop_red.png"));
        stopButton.setText(TRANSLATOR.translate("Stop(Button)"));
        stopButton.setEnabled(false);
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                stopGame();
            }
        });

        remainingGuessesLabel = new JLabel();
        remainingGuessesLabel.setText(TRANSLATOR.translate("RemainingGuesses(Label)", MAX_ATTEMPTS - attempts));
        contentPanel.add(remainingGuessesLabel, "span");

        contentPanel.add(hangmanDrawing, "center, span, width 150::, height 150::");

        contentPanel.add(new JLabel(TRANSLATOR.translate("WordField(Label)")), "span, left");

        contentPanel.add(wordField, "span, left, growx");
        wordField.setEditable(false);

        contentPanel.add(new JLabel(TRANSLATOR.translate("TranslationField(Label)")), "span, left");

        contentPanel.add(translationField, "span, left, growx");
        translationField.setEditable(false);

        contentPanel.add(new JLabel(TRANSLATOR.translate("GuessField(Label)")), "span, left");

        contentPanel.add(new DefaultOverlayable(guessField, guessFieldOverlayLabel, DefaultOverlayable.SOUTH_EAST), "span 5, left, growx");
        guessField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                guessed();
            }
        });

        guessField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (guessField.getText().length() == 1 && guessedChars.contains(guessField.getText().charAt(0))) {
                    guessFieldOverlayLabel.setIcon(OverlayableUtils.getPredefinedOverlayIcon(OverlayableIconsFactory.ERROR));
                    guessButton.setEnabled(false);
                } else {
                    guessFieldOverlayLabel.setIcon(OverlayableUtils.getPredefinedOverlayIcon(OverlayableIconsFactory.CORRECT));
                    guessButton.setEnabled(true);
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (guessField.getText().isEmpty()) {
                    guessFieldOverlayLabel.setIcon(OverlayableUtils.getPredefinedOverlayIcon(OverlayableIconsFactory.ERROR));
                    guessButton.setEnabled(false);
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });

        contentPanel.add(guessIconLabel, "span 4, right");
        guessIconLabel.setIcon(IconManager.getImageIcon("bell2_grey.png", IconManager.IconSize.SIZE24));
        contentPanel.add(guessButton, "left, span, growx");
        guessButton.setIcon(IconManager.getMenuIcon("check2.png"));
        guessButton.setText(TRANSLATOR.translate("Guess(Button)"));
        guessButton.setEnabled(false);
        guessButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                guessed();
                guessField.requestFocus();
            }
        });

        // the default the field is empty
        guessFieldOverlayLabel.setIcon(OverlayableUtils.getPredefinedOverlayIcon(OverlayableIconsFactory.ERROR));
        guessButton.setEnabled(false);

        contentPanel.add(feedbackField, "span, left");
        feedbackField.setText(TRANSLATOR.translate("Feedback(Field)"));

        return contentPanel;
    }

    private void startGame() {
        Dictionary selectedDictionary = dictionaryService.getDictionary((Language) fromLanguageComboBox.getSelectedItem(),
                (Language) toLanguageComboBox.getSelectedItem());
        assert selectedDictionary != null;

        Language selectedLanguage = (Language) fromLanguageComboBox.getSelectedItem();
        assert selectedLanguage != null;

        Difficulty difficulty = (Difficulty) difficultyComboBox.getSelectedItem();
        assert difficulty != null;

        LOGGER.info("Selected difficulty " + difficulty);
        LOGGER.info("Selected language is " + selectedLanguage);

        examService.getDifficultyWords(selectedDictionary, selectedLanguage, difficulty);

        hangmanDrawing.setStage(0);
        hangmanDrawing.repaint();

        // get the current word
        examService.getExamWord(selectedDictionary);
        currentWord = examService.examWord();
        translationField.setText(dictionaryService.getTranslation(currentWord, selectedDictionary).split("\n")[selectedDictionary.getFromLanguage() == Language.ENGLISH ? 1 : 0]);

        nextGuess();

        enableComponents(false);

        feedbackField.setText(TRANSLATOR.translate("GameStarted(Label)"));
        guessField.requestFocus();
    }

    private void stopGame() {
        guessIconLabel.setIcon(IconManager.getImageIcon("bell2_grey.png", IconManager.IconSize.SIZE24));
        feedbackField.setText(TRANSLATOR.translate("EndOfGame(Message)"));

        enableComponents(true);
        wordField.setText(null);
        guessField.setText(null);
        translationField.setText(null);

        attempts = 0;
        guessedChars.clear();
    }

    private void nextGuess() {

        final String maskedWord = maskWord(currentWord);
        wordField.setText(maskedWord);

        if (!maskedWord.contains("_")) {
            JOptionPane.showMessageDialog(this, TRANSLATOR.translate("Success(Message)"));
            stopGame();
        }
    }

    private void guessed() {
        final String currentGuess = guessField.getText();

        if (currentWord.equals(currentGuess)) {
            JOptionPane.showMessageDialog(this, TRANSLATOR.translate("Success(Message)"));
            stopGame();
        } else {
            // we take only the first character
            guessedChars.add(currentGuess.charAt(0));

            if (currentWord.indexOf(currentGuess) != -1) {
                feedbackField.setText(TRANSLATOR.translate("CorrectGuess(String)"));
                guessIconLabel.setIcon(IconManager.getImageIcon("bell2_green.png", IconManager.IconSize.SIZE24));
            } else {
                feedbackField.setText(TRANSLATOR.translate("WrongGuess(String)"));
                guessIconLabel.setIcon(IconManager.getImageIcon("bell2_red.png", IconManager.IconSize.SIZE24));

                // the attempts count is increased only for wrong attempts
                if (++attempts >= MAX_ATTEMPTS) {
                    JOptionPane.showMessageDialog(this, TRANSLATOR.translate("Failure(Message)"));
                    stopGame();
                }
            }

            nextGuess();

            hangmanDrawing.setStage(attempts);
            hangmanDrawing.repaint();
            remainingGuessesLabel.setText(TRANSLATOR.translate("RemainingGuesses(Label)", MAX_ATTEMPTS - attempts));

            guessField.setText(null);
            guessField.requestFocusInWindow();
        }
    }

    private void enableComponents(Boolean enable) {
        fromLanguageComboBox.setEnabled(enable);
        toLanguageComboBox.setEnabled(enable);
        startButton.setEnabled(enable);
        stopButton.setEnabled(!enable);
        guessButton.setEnabled(!enable);
        guessField.setEnabled(!enable);
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
        StringBuilder result = new StringBuilder(word.length() * 3);

        for (int i = 0; i < word.length(); i++) {
            if (guessedChars.contains(word.charAt(i))) {
                result.append(word.charAt(i));
            } else {
                result.append("_");
            }

            result.append(" ");
        }

        return result.toString();
    }
}
