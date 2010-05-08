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

import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Set;
import javax.swing.*;

import net.miginfocom.swing.MigLayout;

import static com.drowltd.spellbook.core.preferences.PreferencesManager.Preference;

/**
 *
 * @author Sasho
 */
public class StudyWordsDialog extends javax.swing.JDialog {

    private StudyService studyService;
    private DictionaryService dictionaryService;
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
    private Frame parent;
    //components
    private JButton answerButton;
    private JTextField answerField;
    private JLabel answerSeenLabel;
    private JLabel answerStatutLabel;
    private JLabel countOfTheCorrectWordsLabel;
    private JLabel countOfTheWrongWordsLabel;
    private JComboBox dictionariesComboBox;
    private JLabel firstRowLabel;
    private JLabel imoticonLabel;
    private JRadioButton inOrderOfInputRadioButton;
    private JRadioButton inReverseOrderOfInputRadioButton;
    private JPanel topPanel;
    private JRadioButton randomRadioButton;
    private JCheckBox repeatMisspelledWordsCheckBox;
    private JCheckBox repeatWordCheckBox;
    private JLabel secondRowLabel;
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

    /** Creates new form LearningWordsDialog */
    public StudyWordsDialog(java.awt.Frame parent, boolean modal) {
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
        correctAnswer = new Integer(0);
        wrongAnswer = new Integer(0);
        answerSeen = new Integer(0);
        int index = PM.getInt(Preference.DICTIONARIES, dictionariesComboBox.getSelectedIndex());
        dictionariesComboBox.setSelectedIndex(index);
        String studySetName = (String) studySetsComboBox.getSelectedItem();
        countOfWords = studyService.getCountOfTheWords(studySetName);
        checkingTheDatabase();
    }

    private void initComponents() {

        topPanel = new JPanel(new MigLayout("", "10[]10[]10", "10[179][][]10"));
        setContentPane(topPanel);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("i18n/StudyWordsDialog"); // NOI18N
        setTitle(bundle.getString("LearningWordsDialog(Title)")); // NOI18N
        setResizable(false);
        addWindowFocusListener(new java.awt.event.WindowFocusListener() {

            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
                formWindowGainedFocus(evt);
            }

            public void windowLostFocus(java.awt.event.WindowEvent evt) {
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {

            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        initWordsPanel(bundle);

        initHowToEnumeratePanel(bundle);

        repeatWordCheckBox = new javax.swing.JCheckBox();
        repeatWordCheckBox.setText(bundle.getString("RepeatWords(CheckBox)")); // NOI18N
        repeatWordCheckBox.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                repeatWordCheckBoxActionPerformed(evt);
            }
        });
        topPanel.add(repeatWordCheckBox);

        repeatMisspelledWordsCheckBox = new javax.swing.JCheckBox();
        repeatMisspelledWordsCheckBox.setText(bundle.getString("RepeatMisspelledWords(CheckBox)")); // NOI18N
        repeatMisspelledWordsCheckBox.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                repeatMisspelledWordsCheckBoxActionPerformed(evt);
            }
        });
        topPanel.add(repeatMisspelledWordsCheckBox, "left,wrap");

        initStudyPanel(bundle);

        /*
        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(studyPanel);
        studyPanel.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
        jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(jPanel3Layout.createSequentialGroup().addContainerGap().addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false).addComponent(translateField, javax.swing.GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE).addGroup(jPanel3Layout.createSequentialGroup().addComponent(answerButton, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(18, 18, 18).addComponent(seeAnswerButton)).addComponent(answerField, javax.swing.GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE).addComponent(transcriptionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE).addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 236, javax.swing.GroupLayout.PREFERRED_SIZE).addGroup(jPanel3Layout.createSequentialGroup().addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false).addComponent(startButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addGap(18, 18, 18).addComponent(stopButton, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE))).addGap(50, 50, 50).addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(jPanel3Layout.createSequentialGroup().addGap(8, 8, 8).addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jLabel9, javax.swing.GroupLayout.Alignment.TRAILING).addComponent(jLabel8, javax.swing.GroupLayout.Alignment.TRAILING).addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(answerSeenLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE).addComponent(countOfTheWrongWordsLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE).addComponent(countOfTheCorrectWordsLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE)).addGap(10, 10, 10)).addGroup(jPanel3Layout.createSequentialGroup().addGap(31, 31, 31).addComponent(imoticonLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)).addGroup(jPanel3Layout.createSequentialGroup().addGap(8, 8, 8).addComponent(answerStatutLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE))).addGap(142, 142, 142)));

        jPanel3Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[]{answerButton, seeAnswerButton, startButton, stopButton});

        jPanel3Layout.setVerticalGroup(
        jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(jPanel3Layout.createSequentialGroup().addContainerGap().addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER).addComponent(startButton).addComponent(stopButton).addComponent(jLabel7).addComponent(countOfTheCorrectWordsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jLabel4).addComponent(jLabel8).addComponent(countOfTheWrongWordsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER).addComponent(translateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jLabel9).addComponent(answerSeenLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup().addComponent(transcriptionLabel).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 35, Short.MAX_VALUE).addComponent(jLabel6)).addComponent(imoticonLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER).addComponent(answerField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(answerStatutLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(answerButton).addComponent(seeAnswerButton)).addContainerGap()));

        jPanel3Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[]{answerButton, seeAnswerButton, startButton, stopButton});
         */

        /*
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(studyPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 513, Short.MAX_VALUE).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false).addComponent(repeatWordCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(wordsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 39, Short.MAX_VALUE).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false).addComponent(repeatMisspelledWordsCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(howToEnumeratePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))).addContainerGap()));

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[]{howToEnumeratePanel, wordsPanel});

        layout.setVerticalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false).addComponent(howToEnumeratePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(wordsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(repeatWordCheckBox).addComponent(repeatMisspelledWordsCheckBox)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(studyPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addContainerGap()));
         */
        pack();
    }

    private void initWordsPanel(ResourceBundle bundle) {
        JPanel wordsPanel = new javax.swing.JPanel(new MigLayout("", "20[]20", "[][][][][][]0[]"));
        wordsPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        wordsPanel.setMaximumSize(new java.awt.Dimension(224, 179));

        JLabel jLabel1 = new javax.swing.JLabel();
        jLabel1.setText(bundle.getString("Languages(Label)")); // NOI18N
        wordsPanel.add(jLabel1, "left,wrap");

        dictionariesComboBox = new DictionaryComboBox(dictionaries);
        wordsPanel.add(dictionariesComboBox, "w 184!,wrap");

        JLabel jLabel3 = new javax.swing.JLabel();
        jLabel3.setText(bundle.getString("StudySet(Label)")); // NOI18N
        wordsPanel.add(jLabel3, "wrap");

        studySetsComboBox = new javax.swing.JComboBox();
        studySetsComboBox.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {

            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }

            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                studySetsComboBoxPopupMenuWillBecomeInvisible(evt);
            }

            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
            }
        });
        wordsPanel.add(studySetsComboBox, "w 184!,wrap");

        wordsButton = new javax.swing.JButton();
        wordsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/16x16/dictionary.png"))); // NOI18N
        wordsButton.setText(bundle.getString("Words(Button)")); // NOI18N
        wordsButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wordsButtonActionPerformed(evt);
            }
        });
        wordsPanel.add(wordsButton, "center,w 105!,wrap");

        warningIconLabel = new javax.swing.JLabel();
        warningIconLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/24x24/warning.png"))); // NOI18N
        wordsPanel.add(warningIconLabel, "split 2,left,w 25!,h 22!");

        firstRowLabel = new javax.swing.JLabel();
        firstRowLabel.setText(bundle.getString("AddWordsFirstLabel(Message)")); // NOI18N
        wordsPanel.add(firstRowLabel, "center,h 12!,wrap");

        secondRowLabel = new javax.swing.JLabel();
        secondRowLabel.setText(bundle.getString("AddWordsSecondLabel(Message)"));
        wordsPanel.add(secondRowLabel, "center,h 12!");

        topPanel.add(wordsPanel, "sg");
    }

    private void initHowToEnumeratePanel(ResourceBundle bundle) {
        JPanel howToEnumeratePanel = new javax.swing.JPanel(new MigLayout("wrap 1", "37[]", "[]20[]20[]20[]"));
        howToEnumeratePanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        //howToEnumeratePanel.setMaximumSize(new java.awt.Dimension(224, 159));
        // howToEnumeratePanel.setMinimumSize(new java.awt.Dimension(224, 159));

        JLabel jLabel5 = new javax.swing.JLabel();
        jLabel5.setText(bundle.getString("Enumerate(Label)")); // NOI18N
        howToEnumeratePanel.add(jLabel5);

        inOrderOfInputRadioButton = new javax.swing.JRadioButton();
        inOrderOfInputRadioButton.setText(bundle.getString("InOrderOfInput(Label)")); // NOI18N
        howToEnumeratePanel.add(inOrderOfInputRadioButton);

        inReverseOrderOfInputRadioButton = new javax.swing.JRadioButton();
        inReverseOrderOfInputRadioButton.setText(bundle.getString("InReverseOrderOfInput(Label)")); // NOI18N
        howToEnumeratePanel.add(inReverseOrderOfInputRadioButton);

        randomRadioButton = new javax.swing.JRadioButton();
        randomRadioButton.setText(bundle.getString("Random(Label)")); // NOI18N
        howToEnumeratePanel.add(randomRadioButton);

        topPanel.add(howToEnumeratePanel, "sg,wrap");
    }

    private void initStudyPanel(ResourceBundle bundle) {
        JPanel studyPanel = new javax.swing.JPanel(new MigLayout("", "[left,105][right,105][][30]", "[][][][47][][][][]"));
        studyPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        //studyPanel.setMaximumSize(new java.awt.Dimension(492, 238));
        //studyPanel.setMinimumSize(new java.awt.Dimension(492, 238));

        startButton = new javax.swing.JButton();
        startButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/16x16/media_play_green.png"))); // NOI18N
        startButton.setText(bundle.getString("Start(Button)")); // NOI18N
        startButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });
        studyPanel.add(startButton, "growx,sg");

        stopButton = new javax.swing.JButton();
        stopButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/16x16/media_stop_red.png"))); // NOI18N
        stopButton.setText(bundle.getString("Stop(Button)")); // NOI18N
        stopButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopButtonActionPerformed(evt);
            }
        });
        studyPanel.add(stopButton, "growx,sg");

        JLabel jLabel7 = new javax.swing.JLabel();
        jLabel7.setText(bundle.getString("Correct(Label)")); // NOI18N
        studyPanel.add(jLabel7, "gapleft 80,right");

        countOfTheCorrectWordsLabel = new javax.swing.JLabel();
        studyPanel.add(countOfTheCorrectWordsLabel, "left,wrap");

        JLabel jLabel4 = new javax.swing.JLabel();
        jLabel4.setText(bundle.getString("OverTranslateField(Label)")); // NOI18N
        studyPanel.add(jLabel4, "span 2,left");

        JLabel jLabel8 = new javax.swing.JLabel();
        jLabel8.setText(bundle.getString("Wrong(Label)")); // NOI18N
        studyPanel.add(jLabel8, "gapleft 80,right");

        countOfTheWrongWordsLabel = new javax.swing.JLabel();
        studyPanel.add(countOfTheWrongWordsLabel, "left,wrap");

        translateField = new javax.swing.JTextField();
        translateField.setEditable(false);
        studyPanel.add(translateField, "span 2,growx");

        JLabel jLabel9 = new javax.swing.JLabel();
        jLabel9.setText(bundle.getString("AnswerSeen(Label)")); // NOI18N
        studyPanel.add(jLabel9, "right");

        answerSeenLabel = new javax.swing.JLabel();
        studyPanel.add(answerSeenLabel, "left,wrap");

        transcriptionLabel = new javax.swing.JLabel();
        transcriptionLabel.setText(" ");
        studyPanel.add(transcriptionLabel, "span 2,growx,top");

        imoticonLabel = new javax.swing.JLabel();
        studyPanel.add(imoticonLabel, "w 53!,h 47!,gapleft 90,wrap");

        JLabel jLabel6 = new javax.swing.JLabel();
        jLabel6.setText(bundle.getString("OverAnswerField(Label)")); // NOI18N
        studyPanel.add(jLabel6, "span 2,wrap");

        answerField = new javax.swing.JTextField();
        answerField.setEditable(false);
        answerField.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                answerFieldActionPerformed(evt);
            }
        });
        studyPanel.add(answerField, "span 2,growx");

        answerStatutLabel = new javax.swing.JLabel();
        studyPanel.add(answerStatutLabel, "span 2,gapleft 60,wrap");

        answerButton = new javax.swing.JButton();
        answerButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/16x16/check.png"))); // NOI18N
        answerButton.setText(bundle.getString("Answer(Button)")); // NOI18N
        answerButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                answerButtonActionPerformed(evt);
            }
        });
        studyPanel.add(answerButton, "growx,sg");

        seeAnswerButton = new javax.swing.JButton();
        seeAnswerButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/16x16/lightbulb_on.png"))); // NOI18N
        seeAnswerButton.setText(bundle.getString("SeeAnswer(Button)")); // NOI18N
        seeAnswerButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                seeAnswerButtonActionPerformed(evt);
            }
        });
        studyPanel.add(seeAnswerButton, "growx,sg");

        topPanel.add(studyPanel, "span 2,growx");
    }

    private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {
        imoticonLabel.setIcon(IconManager.getImageIcon("laugh.gif", IconManager.IconSize.SIZE48));//for test
        startLearning();
    }

    private void wordsButtonActionPerformed(java.awt.event.ActionEvent evt) {

        wordsDialog = new WordsDialog(parent, true);
        wordsDialog.clear();
        wordsDialog.setWordsInTable(false);
        wordsDialog.setLocationRelativeTo(this);
        wordsDialog.setVisible(true);

    }

    private void answerButtonActionPerformed(java.awt.event.ActionEvent evt) {

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

    private void seeAnswerButtonActionPerformed(java.awt.event.ActionEvent evt) {
        String word = null;
        if (selectedDictionary == SelectedDictionary.EN_BG) {
            if (howToEnumerate == HowToEnumerate.RANDOM) {
                word = (String) shuffleTranslationForLearning.get(wordIndex);
            } else {
                word = translationForLearning.get(wordIndex);
            }
            String message = TRANSLATOR.translate("SeeAnswerMessege(Message)") + " \"" + word + "\"";
            JOptionPane.showMessageDialog(this, message, TRANSLATOR.translate("SeeAnswerPaneTittle(Title)"), JOptionPane.INFORMATION_MESSAGE);
        }
        if (selectedDictionary == SelectedDictionary.BG_EN) {
            if (howToEnumerate == HowToEnumerate.RANDOM) {
                word = (String) shuffleWordsForLearning.get(wordIndex);
            } else {
                word = wordsForLearning.get(wordIndex);
            }
            String message = TRANSLATOR.translate("SeeAnswerMessege(Message)") + " \"" + word + "\"";
            JOptionPane.showMessageDialog(this, message, TRANSLATOR.translate("SeeAnswerPaneTittle(Title)"), JOptionPane.INFORMATION_MESSAGE);
        }
        answerSeen++;
        answerSeenLabel.setText(answerSeen.toString());
        answerField.requestFocus();
    }

    private void stopButtonActionPerformed(java.awt.event.ActionEvent evt) {

        setComponentsEnable(true);
        //answerField.setText(null);

    }

    private void formWindowClosed(java.awt.event.WindowEvent evt) {
        PM.putInt(Preference.DICTIONARIES, dictionariesComboBox.getSelectedIndex());
        PM.putBoolean(Preference.LEARNING_IN_ORDER, inOrderOfInputRadioButton.isSelected());
        PM.putBoolean(Preference.LEARNING_IN_REVERSE_ORDER, inReverseOrderOfInputRadioButton.isSelected());
        PM.putBoolean(Preference.LEARNING_RANDOM, randomRadioButton.isSelected());
        PM.putBoolean(Preference.REPEAT_MISSPELLED_WORDS, repeatMisspelledWordsCheckBox.isSelected());
        PM.putBoolean(Preference.REPEAT_WORDS, repeatWordCheckBox.isSelected());
    }

    private void answerFieldActionPerformed(java.awt.event.ActionEvent evt) {
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

    private void repeatWordCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {
        if (repeatWordCheckBox.isSelected()) {
            repeatMisspelledWordsCheckBox.setSelected(false);
        }
        if (!isStopedLearn) {
            answerField.requestFocus();
        }
    }

    private void repeatMisspelledWordsCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {
        if (repeatMisspelledWordsCheckBox.isSelected()) {
            repeatWordCheckBox.setSelected(false);
        }
        if (!isStopedLearn) {
            answerField.requestFocus();
        }
    }

    private void studySetsComboBoxPopupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
        checkingTheDatabase();
        PM.putInt(Preference.STUDY_SETS, studySetsComboBox.getSelectedIndex());
    }

    private void formWindowGainedFocus(java.awt.event.WindowEvent evt) {
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
        correctAnswer = new Integer(0);
        wrongAnswer = new Integer(0);
        answerSeen = new Integer(0);
    }

    private void showWordWhenStartTheStudy(List<String> words, List<String> translations) {
        String translation = null, word = null;
        if (selectedDictionary == SelectedDictionary.EN_BG) {
            word = words.get(wordIndex);
            translateField.setText(word);
            String transcription = getTranscription(word);
            transcriptionLabel.setText(" " + transcription);
        } else {
            translation = translations.get(wordIndex);
            List<String> possiblesTranslations = new ArrayList<String>();
            possiblesTranslations = studyService.getPossiblesTranslations(translation);
            translation = studyService.combinePossiblesTranslationsForTheTable(possiblesTranslations);
            translateField.setText(translation);
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

        String[] ourAnswers = null;
        ourAnswers = wordTranslation.split("[,]+");

        if (wordTranslation == null || wordTranslation.isEmpty()) {
            JOptionPane.showMessageDialog(this, TRANSLATOR.translate("AnswerFeild(Message)"), null, JOptionPane.ERROR_MESSAGE);
            answerField.requestFocus();
        }

        List<String> possibleAnswers = new ArrayList<String>();
        String translation = translations.get(wordIndex);
        possibleAnswers = studyService.getPossiblesTranslations(translation);
        studyService.possibleAnswers(translation);
        List<String> anotherPossibleAnswers = new ArrayList<String>();
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
                continue;
            } else if (anotherPossibleAnswers.contains(answer)) {
                isCorrectAnswer = true;
                continue;
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
            List<String> possiblesTranslations = new ArrayList<String>();
            possiblesTranslations = studyService.getPossiblesTranslations(word);
            word = studyService.combinePossiblesTranslationsForTheTable(possiblesTranslations);
        }
        translateField.setText(word);
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

        String transcription = translation.substring(beginIndex, endIndex);
        return transcription;
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
                firstRowLabel.setText(null);
                secondRowLabel.setText(null);
            }
        }
    }

    private void reportThatDatabaseIsEmpty() {
        startButton.setEnabled(false);
        warningIconLabel.setIcon(IconManager.getImageIcon("warning.png", IconManager.IconSize.SIZE24));
        firstRowLabel.setText(TRANSLATOR.translate("AddWordsFirstLabel(Message)"));
        secondRowLabel.setText(TRANSLATOR.translate("AddWordsSecondLabel(Message)"));
    }

    public void showLearningWordsDialog() {

        inOrderOfInputRadioButton.setSelected(PM.getBoolean(Preference.LEARNING_IN_ORDER, true));
        inReverseOrderOfInputRadioButton.setSelected(PM.getBoolean(Preference.LEARNING_IN_REVERSE_ORDER, false));
        randomRadioButton.setSelected(PM.getBoolean(Preference.LEARNING_RANDOM, false));
        repeatMisspelledWordsCheckBox.setSelected(PM.getBoolean(Preference.REPEAT_MISSPELLED_WORDS, true));
        repeatWordCheckBox.setSelected(PM.getBoolean(Preference.REPEAT_WORDS, false));

        setVisible(true);

    }

    private void setStudySetsInComboBox() {
        List<String> namesOfStudySets = new ArrayList<String>();
        namesOfStudySets = studyService.getNamesOfStudySets();
        studySetsComboBox.setModel(new DefaultComboBoxModel(namesOfStudySets.toArray()));
        //int index = PM.getInt(Preference.STUDY_SETS, studySetsComboBox.getSelectedIndex());
        //studySetsComboBox.setSelectedIndex(index);
    }
}
