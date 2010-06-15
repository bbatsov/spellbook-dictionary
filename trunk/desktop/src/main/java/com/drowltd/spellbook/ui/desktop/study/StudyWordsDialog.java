/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * LearningWordsDialog.java
 *
 * Created on 2010-1-27, 17:01:30
 */
package com.drowltd.spellbook.ui.desktop.study;

import com.drowltd.spellbook.core.i18n.Translator;
import com.drowltd.spellbook.core.model.Dictionary;
import com.drowltd.spellbook.core.model.StudySet;
import com.drowltd.spellbook.core.model.StudySetEntry;
import com.drowltd.spellbook.core.preferences.PreferencesManager;
import com.drowltd.spellbook.core.service.DictionaryService;
import com.drowltd.spellbook.core.service.study.StudyService;
import com.drowltd.spellbook.ui.swing.component.DictionaryComboBox;
import com.drowltd.spellbook.ui.swing.util.IconManager;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Set;

import static com.drowltd.spellbook.core.preferences.PreferencesManager.Preference;

/**
 * @author Sasho
 */
public class StudyWordsDialog extends JDialog {

    private final StudyService studyService;
    private final DictionaryService dictionaryService;
    private static final PreferencesManager PM = PreferencesManager.getInstance();
    private static final Translator TRANSLATOR = Translator.getTranslator("StudyWordsDialog");
    private SelectedDictionary selectedDictionary = SelectedDictionary.EN_BG;
    private WordsDialog wordsDialog;
    private List<String> wordsForLearning = new ArrayList<String>();
    private List<String> translationForLearning = new ArrayList<String>();
    private List shuffleWordsForLearning = new ArrayList<String>();
    private List shuffleTranslationForLearning = new ArrayList<String>();
    private List<Dictionary> dictionaries = new ArrayList<Dictionary>();
    private List<StudySet> studySets = new ArrayList<StudySet>();
    private int wordIndex = 0;
    private long countOfWords;
    private Integer correctAnswer;
    private Integer wrongAnswer;
    private Integer answerSeen;
    private boolean isStopedLearn = true;
    private HowToEnumerate howToEnumerate = HowToEnumerate.IN_ORDER_OF_INPUT;
    private final Frame parent;
    //components
    private JButton answerButton;
    private JTextField answerField;
    private JLabel answerSeenLabel;
    private JLabel answerStatutLabel;
    private JLabel countOfTheCorrectWordsLabel;
    private JLabel countOfTheWrongWordsLabel;
    private JComboBox dictionariesComboBox;
    private JLabel addWordsLabel;
    private JLabel imoticonLabel;
    private JRadioButton inOrderOfInputRadioButton;
    private JRadioButton inReverseOrderOfInputRadioButton;
    private JPanel topPanel;
    private JRadioButton randomRadioButton;
    private JCheckBox repeatMisspelledWordsCheckBox;
    private JCheckBox repeatWordCheckBox;
    private JButton seeAnswerButton;
    private JButton startButton;
    private JButton stopButton;
    private JComboBox studySetsComboBox;
    private JLabel transcriptionLabel;
    private JTextField translateField;
    private JLabel warningIconLabel;
    private JButton wordsButton;

    private enum SelectedDictionary {

        EN_BG, BG_EN
    }

    private enum HowToEnumerate {

        IN_ORDER_OF_INPUT, IN_REVERSE_ORDER_OF_INPUT, RANDOM
    }

    public StudyWordsDialog(Frame parent, boolean modal) {
        super(parent, modal);
        TRANSLATOR.reset();
        this.parent = parent;
        dictionaryService = DictionaryService.getInstance();
        dictionaries = dictionaryService.getDictionaries();
        initComponents();

        studyService = new StudyService();
        studySets = studyService.getStudySets();
        setStudySetsInComboBox();
        ButtonGroup enumerateGroup = new ButtonGroup();
        enumerateGroup.add(inReverseOrderOfInputRadioButton);
        enumerateGroup.add(inOrderOfInputRadioButton);
        enumerateGroup.add(randomRadioButton);
        inOrderOfInputRadioButton.setSelected(true);
        answerButton.setEnabled(false);
        seeAnswerButton.setEnabled(false);
        stopButton.setEnabled(false);
        correctAnswer = 0;
        wrongAnswer = 0;
        answerSeen = 0;
        int index = PM.getInt(Preference.DICTIONARIES, dictionariesComboBox.getSelectedIndex());
        dictionariesComboBox.setSelectedIndex(index);
        String studySetName = (String) studySetsComboBox.getSelectedItem();
        countOfWords = studyService.getCountOfTheWords(studySetName);
        checkingTheDatabase();
    }

    private void initComponents() {

        topPanel = new JPanel(new MigLayout("", "10[]10[]10", "10[179][][]10"));
        setContentPane(topPanel);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        ResourceBundle bundle = ResourceBundle.getBundle("i18n/StudyWordsDialog"); // NOI18N
        setTitle(bundle.getString("LearningWordsDialog(Title)")); // NOI18N
        setResizable(false);
        addWindowFocusListener(new WindowFocusListener() {

            @Override
            public void windowGainedFocus(WindowEvent evt) {
                formWindowGainedFocus(evt);
            }

            @Override
            public void windowLostFocus(WindowEvent evt) {
            }
        });
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosed(WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        initWordsPanel(bundle);

        initHowToEnumeratePanel(bundle);

        repeatWordCheckBox = new JCheckBox();
        repeatWordCheckBox.setText(bundle.getString("RepeatWords(CheckBox)")); // NOI18N
        repeatWordCheckBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                repeatWordCheckBoxActionPerformed(evt);
            }
        });
        topPanel.add(repeatWordCheckBox);

        repeatMisspelledWordsCheckBox = new JCheckBox();
        repeatMisspelledWordsCheckBox.setText(bundle.getString("RepeatMisspelledWords(CheckBox)")); // NOI18N
        repeatMisspelledWordsCheckBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                repeatMisspelledWordsCheckBoxActionPerformed(evt);
            }
        });
        topPanel.add(repeatMisspelledWordsCheckBox, "left,wrap");

        initStudyPanel(bundle);

        pack();
    }

    private void initWordsPanel(ResourceBundle bundle) {
        JPanel wordsPanel = new JPanel(new MigLayout("", "20[200]20", "[][][][][]0[21]"));
        wordsPanel.setBorder(BorderFactory.createEtchedBorder());
        wordsPanel.setMaximumSize(new Dimension(240, 179)); //224

        JLabel jLabel1 = new JLabel();
        jLabel1.setText(bundle.getString("Languages(Label)")); // NOI18N
        wordsPanel.add(jLabel1, "left,wrap");

        dictionariesComboBox = new DictionaryComboBox(dictionaries);
        wordsPanel.add(dictionariesComboBox, "span,growx,wrap");

        JLabel jLabel3 = new JLabel();
        jLabel3.setText(bundle.getString("StudySet(Label)")); // NOI18N
        wordsPanel.add(jLabel3, "wrap");

        studySetsComboBox = new JComboBox();
        studySetsComboBox.addPopupMenuListener(new PopupMenuListener() {

            @Override
            public void popupMenuCanceled(PopupMenuEvent evt) {
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent evt) {
                studySetsComboBoxPopupMenuWillBecomeInvisible(evt);
            }

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent evt) {
            }
        });
        wordsPanel.add(studySetsComboBox, "span,growx,wrap");

        wordsButton = new JButton();
        wordsButton.setIcon(new ImageIcon(getClass().getResource("/icons/16x16/dictionary.png"))); // NOI18N
        wordsButton.setText(bundle.getString("Words(Button)")); // NOI18N
        wordsButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                wordsButtonActionPerformed(evt);
            }
        });
        wordsPanel.add(wordsButton, "center,w 105!,wrap");

        warningIconLabel = new JLabel();
        warningIconLabel.setIcon(new ImageIcon(getClass().getResource("/icons/24x24/warning.png"))); // NOI18N
        wordsPanel.add(warningIconLabel, "split 2,left,w 25!,h 22!");

        addWordsLabel = new JLabel();
        addWordsLabel.setText(bundle.getString("AddWordsLabel(Message)")); // NOI18N
        wordsPanel.add(addWordsLabel, "wrap");

        topPanel.add(wordsPanel, "w 240!,h 183!,sg");
    }

    private void initHowToEnumeratePanel(ResourceBundle bundle) {
        JPanel howToEnumeratePanel = new JPanel(new MigLayout("wrap 1", "53[]", "[]15[]20[]20[]"));
        howToEnumeratePanel.setBorder(BorderFactory.createEtchedBorder());

        JLabel jLabel5 = new JLabel();
        jLabel5.setText(bundle.getString("Enumerate(Label)")); // NOI18N
        howToEnumeratePanel.add(jLabel5);

        inOrderOfInputRadioButton = new JRadioButton();
        inOrderOfInputRadioButton.setText(bundle.getString("InOrderOfInput(Label)")); // NOI18N
        howToEnumeratePanel.add(inOrderOfInputRadioButton);

        inReverseOrderOfInputRadioButton = new JRadioButton();
        inReverseOrderOfInputRadioButton.setText(bundle.getString("InReverseOrderOfInput(Label)")); // NOI18N
        howToEnumeratePanel.add(inReverseOrderOfInputRadioButton);

        randomRadioButton = new JRadioButton();
        randomRadioButton.setText(bundle.getString("Random(Label)")); // NOI18N
        howToEnumeratePanel.add(randomRadioButton);

        topPanel.add(howToEnumeratePanel, "sg,wrap");
    }

    private void initStudyPanel(ResourceBundle bundle) {
        JPanel studyPanel = new JPanel(new MigLayout("", "[left,105][right,105][][30]", "[][][][47][][][]"));
        studyPanel.setBorder(BorderFactory.createEtchedBorder());

        startButton = new JButton();
        startButton.setIcon(new ImageIcon(getClass().getResource("/icons/16x16/media_play_green.png"))); // NOI18N
        startButton.setText(bundle.getString("Start(Button)")); // NOI18N
        startButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });
        studyPanel.add(startButton, "w 118!,sg");

        stopButton = new JButton();
        stopButton.setIcon(new ImageIcon(getClass().getResource("/icons/16x16/media_stop_red.png"))); // NOI18N
        stopButton.setText(bundle.getString("Stop(Button)")); // NOI18N
        stopButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                stopButtonActionPerformed(evt);
            }
        });
        studyPanel.add(stopButton, "sg");

        JLabel jLabel7 = new JLabel();
        jLabel7.setText(bundle.getString("Correct(Label)")); // NOI18N
        studyPanel.add(jLabel7, "gapleft 80,right");

        countOfTheCorrectWordsLabel = new JLabel();
        studyPanel.add(countOfTheCorrectWordsLabel, "left,wrap");

        JLabel jLabel4 = new JLabel();
        jLabel4.setText(bundle.getString("OverTranslateField(Label)")); // NOI18N
        studyPanel.add(jLabel4, "span 2,left");

        JLabel jLabel8 = new JLabel();
        jLabel8.setText(bundle.getString("Wrong(Label)")); // NOI18N
        studyPanel.add(jLabel8, "gapleft 80,right");

        countOfTheWrongWordsLabel = new JLabel();
        studyPanel.add(countOfTheWrongWordsLabel, "left,wrap");

        translateField = new JTextField();
        translateField.setEditable(false);
        studyPanel.add(translateField, "span 2,w 246!");

        JLabel jLabel9 = new JLabel();
        jLabel9.setText(bundle.getString("AnswerSeen(Label)")); // NOI18N
        studyPanel.add(jLabel9, "right");

        answerSeenLabel = new JLabel();
        studyPanel.add(answerSeenLabel, "left,wrap");

        transcriptionLabel = new JLabel();
        transcriptionLabel.setText(" ");
        studyPanel.add(transcriptionLabel, "span 2,growx,top");

        imoticonLabel = new JLabel();
        studyPanel.add(imoticonLabel, "w 53!,h 47!,gapleft 90,wrap");

        JLabel jLabel6 = new JLabel();
        jLabel6.setText(bundle.getString("OverAnswerField(Label)")); // NOI18N
        studyPanel.add(jLabel6, "span 2,wrap");

        answerField = new JTextField();
        answerField.setEditable(false);
        answerField.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                answerFieldActionPerformed(evt);
            }
        });
        studyPanel.add(answerField, "span 2,w 246!");

        answerStatutLabel = new JLabel();
        studyPanel.add(answerStatutLabel, "span 2,gapleft 60,wrap");

        answerButton = new JButton();
        answerButton.setIcon(new ImageIcon(getClass().getResource("/icons/16x16/check.png"))); // NOI18N
        answerButton.setText(bundle.getString("Answer(Button)")); // NOI18N
        answerButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                answerButtonActionPerformed(evt);
            }
        });
        studyPanel.add(answerButton, "sg");

        seeAnswerButton = new JButton();
        //seeAnswerButton.setIcon(new ImageIcon(getClass().getResource("/icons/16x16/lightbulb_on.png"))); // NOI18N
        seeAnswerButton.setText(bundle.getString("SeeAnswer(Button)")); // NOI18N
        seeAnswerButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                seeAnswerButtonActionPerformed(evt);
            }
        });
        studyPanel.add(seeAnswerButton, "sg");

        topPanel.add(studyPanel, "span 2,growx");
    }

    private void startButtonActionPerformed(ActionEvent evt) {
        startLearning();
    }

    private void wordsButtonActionPerformed(ActionEvent evt) {

        wordsDialog = new WordsDialog(parent, true);
        wordsDialog.clear();
        wordsDialog.setWordsInTable(false);
        wordsDialog.setLocationRelativeTo(this);
        wordsDialog.setVisible(true);

    }

    private void answerButtonActionPerformed(ActionEvent evt) {

        if (selectedDictionary == SelectedDictionary.EN_BG) {
            if (howToEnumerate == HowToEnumerate.RANDOM) {
                getAnswer(shuffleWordsForLearning, shuffleTranslationForLearning);
            } else {
                getAnswer(wordsForLearning, translationForLearning);
            }
        }
        if (selectedDictionary == SelectedDictionary.BG_EN) {
            if (howToEnumerate == HowToEnumerate.RANDOM) {
                getAnswer(shuffleTranslationForLearning, shuffleWordsForLearning);
            } else {
                getAnswer(translationForLearning, wordsForLearning);
            }
        }
    }

    private void seeAnswerButtonActionPerformed(ActionEvent evt) {
        String answer;
        if (selectedDictionary == SelectedDictionary.EN_BG) {
            if (howToEnumerate == HowToEnumerate.RANDOM) {
                answer = shuffleWordsForLearning.get(wordIndex) + "\n\n" + (String) shuffleTranslationForLearning.get(wordIndex);
            } else {
                answer = wordsForLearning.get(wordIndex) + "\n\n" + translationForLearning.get(wordIndex);
            }
            //JOptionPane.showMessageDialog(this, message, TRANSLATOR.translate("SeeAnswerPaneTittle(Title)"), JOptionPane.INFORMATION_MESSAGE);
            SeeAnswerDialog seeAnswerDialog = new SeeAnswerDialog(parent, true);
            seeAnswerDialog.setAnswer(answer);
            seeAnswerDialog.setLocationRelativeTo(this);
            seeAnswerDialog.setVisible(true);
        }
        if (selectedDictionary == SelectedDictionary.BG_EN) {
            if (howToEnumerate == HowToEnumerate.RANDOM) {
                answer = (String) shuffleWordsForLearning.get(wordIndex);
            } else {
                answer = wordsForLearning.get(wordIndex);
            }
            JOptionPane.showMessageDialog(this, answer, TRANSLATOR.translate("SeeAnswerPaneTittle(Title)"), JOptionPane.INFORMATION_MESSAGE);
        }
        answerSeen++;
        answerSeenLabel.setText(answerSeen.toString());
        answerField.requestFocus();
    }

    private void stopButtonActionPerformed(ActionEvent evt) {

        setComponentsEnable(true);
        //answerField.setText(null);

    }

    private void formWindowClosed(WindowEvent evt) {
        PM.putInt(Preference.DICTIONARIES, dictionariesComboBox.getSelectedIndex());
        PM.putBoolean(Preference.LEARNING_IN_ORDER, inOrderOfInputRadioButton.isSelected());
        PM.putBoolean(Preference.LEARNING_IN_REVERSE_ORDER, inReverseOrderOfInputRadioButton.isSelected());
        PM.putBoolean(Preference.LEARNING_RANDOM, randomRadioButton.isSelected());
        PM.putBoolean(Preference.REPEAT_MISSPELLED_WORDS, repeatMisspelledWordsCheckBox.isSelected());
        PM.putBoolean(Preference.REPEAT_WORDS, repeatWordCheckBox.isSelected());
    }

    private void answerFieldActionPerformed(ActionEvent evt) {
        if (!isStopedLearn) {
            if (selectedDictionary == SelectedDictionary.EN_BG) {
                if (howToEnumerate == HowToEnumerate.RANDOM) {
                    getAnswer(shuffleWordsForLearning, shuffleTranslationForLearning);
                } else {
                    getAnswer(wordsForLearning, translationForLearning);
                }
            }
            if (selectedDictionary == SelectedDictionary.BG_EN) {
                if (howToEnumerate == HowToEnumerate.RANDOM) {
                    getAnswer(shuffleTranslationForLearning, shuffleWordsForLearning);
                } else {
                    getAnswer(translationForLearning, wordsForLearning);
                }
            }
        }
    }

    private void repeatWordCheckBoxActionPerformed(ActionEvent evt) {
        if (repeatWordCheckBox.isSelected()) {
            repeatMisspelledWordsCheckBox.setSelected(false);
        }
        if (!isStopedLearn) {
            answerField.requestFocus();
        }
    }

    private void repeatMisspelledWordsCheckBoxActionPerformed(ActionEvent evt) {
        if (repeatMisspelledWordsCheckBox.isSelected()) {
            repeatWordCheckBox.setSelected(false);
        }
        if (!isStopedLearn) {
            answerField.requestFocus();
        }
    }

    private void studySetsComboBoxPopupMenuWillBecomeInvisible(PopupMenuEvent evt) {
        checkingTheDatabase();
        PM.putInt(Preference.STUDY_SETS, studySetsComboBox.getSelectedIndex());
    }

    private void formWindowGainedFocus(WindowEvent evt) {
        studySets = studyService.getStudySets();
        setStudySetsInComboBox();
        if (!studySets.isEmpty()) {
            int index = PM.getInt(Preference.STUDY_SETS, studySetsComboBox.getSelectedIndex());
            if (studySets.size() >= index) {
                studySetsComboBox.setSelectedIndex(index);
            } else {
                studySetsComboBox.setSelectedIndex(0);
            }
        }
        String studySetName = (String) studySetsComboBox.getSelectedItem();
        countOfWords = studyService.getCountOfTheWords(studySetName);
        checkingTheDatabase();
        PM.putInt(Preference.STUDY_SETS, studySetsComboBox.getSelectedIndex());
    }

    private void startLearning() {
        if (dictionariesComboBox.getSelectedIndex() == 0) {
            selectedDictionary = SelectedDictionary.EN_BG;
        }
        if (dictionariesComboBox.getSelectedIndex() == 1) {
            selectedDictionary = SelectedDictionary.BG_EN;
        }

        String studySetName = (String) studySetsComboBox.getSelectedItem();
        wordsForLearning = studyService.getWordsForStudy(studySetName);
        translationForLearning = studyService.getTranslationsForStudy(studySetName);

        setComponentsEnable(false);

        answerField.requestFocus();
        resetToZeroCounters();

        if (inOrderOfInputRadioButton.isSelected()) {
            howToEnumerate = HowToEnumerate.IN_ORDER_OF_INPUT;
            wordIndex = 0;
            showWordWhenStartTheStudy(wordsForLearning, translationForLearning);
        }
        if (inReverseOrderOfInputRadioButton.isSelected()) {
            howToEnumerate = HowToEnumerate.IN_REVERSE_ORDER_OF_INPUT;

            long wordIndex1 = studyService.getCountOfTheWords(studySetName) - 1;
            wordIndex = (int) wordIndex1;

            showWordWhenStartTheStudy(wordsForLearning, translationForLearning);
        }
        if (randomRadioButton.isSelected()) {
            shuffleWordsForLearning = new ArrayList();
            shuffleTranslationForLearning = new ArrayList();
            shuffle(wordsForLearning, translationForLearning);
            howToEnumerate = HowToEnumerate.RANDOM;
            wordIndex = 0;
            showWordWhenStartTheStudy(shuffleWordsForLearning, shuffleTranslationForLearning);
        }
    }

    private void resetToZeroCounters() {
        answerStatutLabel.setText(null);
        countOfTheCorrectWordsLabel.setText("0");
        countOfTheWrongWordsLabel.setText("0");
        answerSeenLabel.setText("0");
        correctAnswer = 0;
        wrongAnswer = 0;
        answerSeen = 0;
    }

    private void showWordWhenStartTheStudy(List<String> words, List<String> translations) {
        String translation, word;
        if (selectedDictionary == SelectedDictionary.EN_BG) {
            word = words.get(wordIndex);
            translateField.setText(word);
            translateField.setToolTipText(word);
            String transcription = getTranscription(word);
            transcriptionLabel.setText(" " + transcription);
        } else {
            translation = translations.get(wordIndex);
            List<String> possiblesTranslations;
            possiblesTranslations = studyService.getPossiblesTranslations(translation);
            translation = studyService.combinePossiblesTranslationsForTheTable(possiblesTranslations);
            translateField.setText(translation);
            translateField.setToolTipText(translation);
            translateField.setCaretPosition(0);
        }
    }

    private void shuffle(List words, List translations) {
        if (words.size() > 0 && translations.size() > 0) {

            List copyWords = new ArrayList();
            List copyTranslations = new ArrayList();

            for (Object object : words) {
                copyWords.add(object);
            }
            for (Object object : translations) {
                copyTranslations.add(object);
            }

            Random generator = new Random();

            do {
                int index = (int) (generator.nextDouble() * (double) copyWords.size());
                shuffleWordsForLearning.add(copyWords.remove(index));
                shuffleTranslationForLearning.add(copyTranslations.remove(index));
            } while (copyWords.size() > 0);
        }
    }

    private void getAnswer(List<String> words, List<String> translations) {
        String wordTranslation = answerField.getText();
        wordTranslation = wordTranslation.toLowerCase();

        String[] ourAnswers;
        ourAnswers = wordTranslation.split("[,]+");

        if (wordTranslation.isEmpty()) {
            JOptionPane.showMessageDialog(this, TRANSLATOR.translate("AnswerFeild(Message)"), null, JOptionPane.ERROR_MESSAGE);
            answerField.requestFocus();
        }

        List<String> possibleAnswers;
        String translation = translations.get(wordIndex);
        possibleAnswers = studyService.getPossiblesTranslations(translation);
        studyService.possibleAnswers(translation);
        List<String> anotherPossibleAnswers;
        anotherPossibleAnswers = studyService.getAnothersPossiblesAnswers();

        if (repeatWordCheckBox.isSelected() && !wordTranslation.isEmpty()) {
            repeatWordIndex();
        }
        checkingWhetherAnswerIsCorrect(ourAnswers, possibleAnswers, anotherPossibleAnswers, wordTranslation);

        if (howToEnumerate == HowToEnumerate.IN_ORDER_OF_INPUT) {
            if (!wordTranslation.isEmpty()) {
                wordIndex++;
            }
            if (wordIndex < countOfWords) {
                showNextWord(words);
            }
            if (wordIndex == countOfWords) {
                //stop study
                setComponentsEnable(true);
            }

        } else if (howToEnumerate == HowToEnumerate.IN_REVERSE_ORDER_OF_INPUT) {
            if (!wordTranslation.isEmpty()) {
                wordIndex--;
            }
            if (wordIndex >= 0) {
                showNextWord(words);
            }
            if (wordIndex == -1) {
                //stop study
                setComponentsEnable(true);
            }

        } else {
            if (!wordTranslation.isEmpty()) {
                wordIndex++;
            }
            if (wordIndex < countOfWords) {
                showNextWord(words);
            }
            if (wordIndex == countOfWords) {
                //stop study
                setComponentsEnable(true);
            }
        }
        answerField.requestFocus();
    }

    private void repeatWordIndex() {
        if (howToEnumerate == HowToEnumerate.IN_ORDER_OF_INPUT) {
            wordIndex--;
        }
        if (howToEnumerate == HowToEnumerate.IN_REVERSE_ORDER_OF_INPUT) {
            wordIndex++;
        }
        if (howToEnumerate == HowToEnumerate.RANDOM) {
            wordIndex--;
        }
    }

    private void checkingWhetherAnswerIsCorrect(String[] ourAnswers, List<String> possibleAnswers, List<String> anotherPossibleAnswers, String wordTranslation) {
        boolean isCorrectAnswer = false;
        for (String answer : ourAnswers) {
            answer = studyService.removeSpacesInTheBeginningAndEnd(answer);
            if (possibleAnswers.contains(answer)) {
                isCorrectAnswer = true;
            } else if (anotherPossibleAnswers.contains(answer)) {
                isCorrectAnswer = true;
            } else {
                isCorrectAnswer = false;
                break;
            }
        }
        if (isCorrectAnswer) {
            answerStatutLabel.setText(TRANSLATOR.translate("CorrectAnswer(Message)"));
            correctAnswer++;
            countOfTheCorrectWordsLabel.setText(correctAnswer.toString());
            imoticonLabel.setIcon(IconManager.getImageIcon("laugh.gif", IconManager.IconSize.SIZE48));
        } else {
            if (!wordTranslation.isEmpty()) {
                answerStatutLabel.setText(TRANSLATOR.translate("WrongAnswer(Message)"));
                imoticonLabel.setIcon(IconManager.getImageIcon("shy.gif", IconManager.IconSize.SIZE48));
                answerField.setText(null);
                wrongAnswer++;
            }
            if (wordTranslation.isEmpty()) {
                answerStatutLabel.setText(null);
            }
            countOfTheWrongWordsLabel.setText(wrongAnswer.toString());
            if (repeatMisspelledWordsCheckBox.isSelected() && !wordTranslation.isEmpty()) {
                repeatWordIndex();
            }
        }
    }

    private void showNextWord(List<String> words) {

        String word = words.get(wordIndex);
        if (selectedDictionary == SelectedDictionary.BG_EN) {
            List<String> possiblesTranslations;
            possiblesTranslations = studyService.getPossiblesTranslations(word);
            word = studyService.combinePossiblesTranslationsForTheTable(possiblesTranslations);
        }
        translateField.setText(word);
        translateField.setToolTipText(word);
        translateField.setCaretPosition(0);

        String transcription = getTranscription(word);
        transcriptionLabel.setText(" " + transcription);
        answerField.setText(null);
    }

    private String getTranscription(String word) {
        String translation = null;

        if (selectedDictionary == SelectedDictionary.EN_BG) {
            translation = dictionaryService.getTranslation(word, dictionaries.get(0));
        }
        if (selectedDictionary == SelectedDictionary.BG_EN) {
            return " ";
        }

        int beginIndex = translation.indexOf('[');
        int endIndex = translation.indexOf(']') + 1;

        if (beginIndex == -1 && endIndex == 0) {
            return " ";
        }

        return translation.substring(beginIndex, endIndex);
    }

    private void setComponentsEnable(boolean enable) {

        isStopedLearn = enable;
        answerButton.setEnabled(!enable);
        seeAnswerButton.setEnabled(!enable);
        stopButton.setEnabled(!enable);
        wordsButton.setEnabled(enable);
        startButton.setEnabled(enable);
        dictionariesComboBox.setEnabled(enable);
        studySetsComboBox.setEnabled(enable);
        inOrderOfInputRadioButton.setEnabled(enable);
        inReverseOrderOfInputRadioButton.setEnabled(enable);
        randomRadioButton.setEnabled(enable);
        transcriptionLabel.setText(" ");
        translateField.setText(null);
        translateField.setToolTipText(null);
        answerField.setText(null);
        answerField.setEditable(!enable);
        startButton.requestFocus();
    }

    private void checkingTheDatabase() {

        if (studySets.isEmpty()) {
            reportThatDatabaseIsEmpty();
        } else {
            String studySetName = (String) studySetsComboBox.getSelectedItem();
            if (studySetName == null) {
                studySetsComboBox.setSelectedIndex(0);
                studySetName = (String) studySetsComboBox.getSelectedItem();
            }
            StudySet studySet = studyService.getStudySet(studySetName);
            Set<StudySetEntry> studySetEntry = studySet.getStudySetEntries();
            if (studySetEntry.isEmpty()) {
                reportThatDatabaseIsEmpty();
            } else {
                startButton.setEnabled(isStopedLearn);
                warningIconLabel.setIcon(null);
                addWordsLabel.setText(null);
            }
        }
    }

    private void reportThatDatabaseIsEmpty() {
        startButton.setEnabled(false);
        warningIconLabel.setIcon(IconManager.getImageIcon("warning.png", IconManager.IconSize.SIZE24));
        addWordsLabel.setText(TRANSLATOR.translate("AddWordsLabel(Message)"));
    }

    public void showDialog() {

        inOrderOfInputRadioButton.setSelected(PM.getBoolean(Preference.LEARNING_IN_ORDER, true));
        inReverseOrderOfInputRadioButton.setSelected(PM.getBoolean(Preference.LEARNING_IN_REVERSE_ORDER, false));
        randomRadioButton.setSelected(PM.getBoolean(Preference.LEARNING_RANDOM, false));
        repeatMisspelledWordsCheckBox.setSelected(PM.getBoolean(Preference.REPEAT_MISSPELLED_WORDS, true));
        repeatWordCheckBox.setSelected(PM.getBoolean(Preference.REPEAT_WORDS, false));

        setVisible(true);

    }

    private void setStudySetsInComboBox() {
        List<String> namesOfStudySets;
        namesOfStudySets = studyService.getNamesOfStudySets();
        studySetsComboBox.setModel(new DefaultComboBoxModel(namesOfStudySets.toArray()));
        //int index = PM.getInt(Preference.STUDY_SETS, studySetsComboBox.getSelectedIndex());
        //studySetsComboBox.setSelectedIndex(index);
    }
}
