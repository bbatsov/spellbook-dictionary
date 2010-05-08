/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * WordsDialog.java
 *
 * Created on 2010-2-3, 10:21:38
 */
package com.drowltd.spellbook.ui.desktop.study;

import com.drowltd.spellbook.core.service.DictionaryService;
import com.drowltd.spellbook.core.preferences.PreferencesManager;
import com.drowltd.spellbook.core.preferences.PreferencesManager.Preference;
import com.drowltd.spellbook.core.i18n.Translator;
import com.drowltd.spellbook.core.service.study.StudyService;
import com.drowltd.spellbook.core.model.Dictionary;
import com.drowltd.spellbook.core.model.StudySet;
import com.drowltd.spellbook.ui.swing.component.DictionaryComboBox;
import com.jidesoft.hints.ListDataIntelliHints;
import com.jidesoft.swing.AutoCompletion;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableColumn;

import net.miginfocom.swing.MigLayout;

/**
 *
 * @author Sasho
 */
public class WordsDialog extends javax.swing.JDialog {

    private long countOFTheWords;
    private DictionaryService dictionaryService;
    private List<String> wordsForStudy = new ArrayList<String>();
    private List<String> translationsForStudy = new ArrayList<String>();
    private List<String> words = new ArrayList<String>();
    private List<Dictionary> dictionaries = new ArrayList<Dictionary>();
    private List<StudySet> studySets = new ArrayList<StudySet>();
    private StudyService studyService;
    private Frame parent;
    private static final PreferencesManager PM = PreferencesManager.getInstance();
    private static final Translator TRANSLATOR = Translator.getTranslator("WordsDialog");
    StudyWordsDialog studyWordsDialog = new StudyWordsDialog(parent, true);

    //componets
    private javax.swing.JButton addStudySetButton;
    private JTextField addStudySetField;
    private JButton addWordButton;
    private JButton clearButton;
    private JButton deleteStudySetButton;
    private JButton deleteWordButton;
    private JComboBox dictionariesComboBox;
    private JLabel jLabel1;
    private JLabel jLabel3;
    private JLabel jLabel5;
    private JLabel jLabel6;
    private JPanel topPanel;
    private JPanel addWordPanel;
    private JPanel studySetsPanel;
    private JPanel languagesPanel;
    private JScrollPane wordTranslationScrollPane;
    private JButton selectAllButton;
    private JButton selectNothingButton;
    private JComboBox studySetsComboBox;
    private JTextField wordSearchField;
    private JTextPane wordTranslationTextPane;
    private JScrollPane wordsScrollPane;
    private JTable wordsTable;

    /** Creates new form WordsDialog */
    public WordsDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        TRANSLATOR.reset();
        this.parent = parent;

        dictionaryService = DictionaryService.getInstance();
        dictionaries = dictionaryService.getDictionaries();
        words = dictionaryService.getWordsFromDictionary(dictionaries.get(0));

        initComponents();

        AutoCompletion autoCompletion = new AutoCompletion(wordSearchField, words);
        autoCompletion.setStrict(false);

        studyService = new StudyService();
        studySets = studyService.getStudySets();
        setStudySetsInComboBox();
        if (!studySets.isEmpty()) {
            int index = PM.getInt(Preference.STUDY_SETS, studySetsComboBox.getSelectedIndex());
            studySetsComboBox.setSelectedIndex(index);
        }

        getTable().setOpaque(true);

        //ListDataIntelliHints intellihints = new ListDataIntelliHints(wordSearchField, words);
        //intellihints.setCaseSensitive(false);

        wordSearchField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateAddButtonState();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateAddButtonState();
                if (wordSearchField.getText().isEmpty()) {
                    wordTranslationTextPane.setText(null);
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateAddButtonState();
            }
        });

        // wordsForLearning = dictDb.getWordsForLearning();
        String name = (String) studySetsComboBox.getSelectedItem();
        translationsForStudy = studyService.getTranslationsForStudy(name);
        countOFTheWords = studyService.getCountOfTheWords(name);
    }

    private void updateAddButtonState() {
        addWordButton.setEnabled(words.contains(wordSearchField.getText()));

        if (addWordButton.isEnabled()) {
            Dictionary dictionary = null;
            if (dictionariesComboBox.getSelectedItem().equals(TRANSLATOR.translate("EnglishItem(ComboBox)"))) {
                dictionary = dictionaries.get(0);
            }
            wordTranslationTextPane.setText(dictionaryService.getTranslation(wordSearchField.getText(),
                    dictionary));
            wordTranslationTextPane.setCaretPosition(0);
        }
    }

    public JTextField getAddWordField() {
        return wordSearchField;
    }

    public JTable getTable() {
        return wordsTable;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {

        topPanel = new JPanel(new MigLayout("", "10[240]10[240]10", "10[]10[]0[]10"));
        setContentPane(topPanel);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("i18n/WordsDialog"); // NOI18N
        setTitle("Words");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {

            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        initLanguagesPanel(bundle);

        initStudySetsPanel(bundle);

        initAddWordPanel(bundle);

        initWordsTablePanel(bundle);

        pack();
    }

    private void initLanguagesPanel(ResourceBundle bundle) {
        languagesPanel = new javax.swing.JPanel(new MigLayout("", "25[200]25", "[][]"));
        languagesPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel3 = new javax.swing.JLabel();
        jLabel3.setText(bundle.getString("SelectLangueges(Label)")); // NOI18N
        languagesPanel.add(jLabel3, "wrap");

        dictionariesComboBox = new javax.swing.JComboBox();
        languagesPanel.add(dictionariesComboBox, "span,growx");
        dictionariesComboBox.addItem(bundle.getString("EnglishItem(ComboBox)"));
        dictionariesComboBox.setSelectedIndex(0);

        topPanel.add(languagesPanel, "growy");
    }

     private void initStudySetsPanel(ResourceBundle bundle) {
        studySetsPanel = new javax.swing.JPanel(new MigLayout("", "25[100][100]25", "[][][][][]"));
        studySetsPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel6 = new javax.swing.JLabel();
        jLabel6.setText(bundle.getString("StudySets(Label)")); // NOI18N
        studySetsPanel.add(jLabel6, "wrap");

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
        studySetsPanel.add(studySetsComboBox, "span 2,growx,wrap");

        jLabel5 = new javax.swing.JLabel();
        jLabel5.setText(bundle.getString("EnterName(Label)")); // NOI18N
        studySetsPanel.add(jLabel5, "span 2,wrap");

        addStudySetField = new javax.swing.JTextField();
        studySetsPanel.add(addStudySetField, "span 2,growx,wrap");

        addStudySetButton = new javax.swing.JButton();
        addStudySetButton.setText(bundle.getString("AddStudySet(Button)")); // NOI18N
        addStudySetButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addStudySetButtonActionPerformed(evt);
            }
        });
        studySetsPanel.add(addStudySetButton, "w 74!,sg,left");

        deleteStudySetButton = new javax.swing.JButton();
        deleteStudySetButton.setText(bundle.getString("DeleteStudySet(Button)")); // NOI18N
        deleteStudySetButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteStudySetButtonActionPerformed(evt);
            }
        });
        studySetsPanel.add(deleteStudySetButton, "sg,right");

        topPanel.add(studySetsPanel, "growx,wrap");
    }

    private void initAddWordPanel(ResourceBundle bundle) {
        addWordPanel = new javax.swing.JPanel(new MigLayout("", "[400][]", "[][][154]"));
        addWordPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel1 = new javax.swing.JLabel();
        jLabel1.setText(bundle.getString("EnterWord(Label)")); // NOI18N
        addWordPanel.add(jLabel1, "wrap");

        wordSearchField = new javax.swing.JTextField();
        wordSearchField.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wordSearchFieldActionPerformed(evt);
            }
        });
        addWordPanel.add(wordSearchField, "growx");

        addWordButton = new javax.swing.JButton();
        addWordButton.setText(bundle.getString("Add(Button)")); // NOI18N
        addWordButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addWordButtonActionPerformed(evt);
            }
        });
        addWordPanel.add(addWordButton, "w 74!,sg,right,wrap");

        wordTranslationScrollPane = new javax.swing.JScrollPane();
        wordTranslationTextPane = new javax.swing.JTextPane();
        wordTranslationTextPane.setEditable(false);
        wordTranslationScrollPane.add(wordTranslationTextPane);
        wordTranslationScrollPane.setViewportView(wordTranslationTextPane);
        addWordPanel.add(wordTranslationScrollPane, "grow");

        clearButton = new javax.swing.JButton();
        clearButton.setText(bundle.getString("Clear(Button)")); // NOI18N
        clearButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });
        addWordPanel.add(clearButton, "sg,top");

        topPanel.add(addWordPanel, "span 2,sg,wrap");
    }

    private void initWordsTablePanel(ResourceBundle bundle) {
        JPanel wordsTablePanel = new JPanel(new MigLayout("", "0[500]0", "[][165][]"));

        selectNothingButton = new javax.swing.JButton();
        selectNothingButton.setText(bundle.getString("Nothing(Button)")); // NOI18N
        selectNothingButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectNothingButtonActionPerformed(evt);
            }
        });
        wordsTablePanel.add(selectNothingButton, "w 74!,right,split 2,sg");

        selectAllButton = new javax.swing.JButton();
        selectAllButton.setText(bundle.getString("All(Button)")); // NOI18N
        selectAllButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllButtonActionPerformed(evt);
            }
        });
        wordsTablePanel.add(selectAllButton, "right,sg,wrap");

        wordsScrollPane = new javax.swing.JScrollPane();
        wordsTable = new javax.swing.JTable();
        wordsScrollPane.setViewportView(wordsTable);
        wordsTablePanel.add(wordsScrollPane, "growx,wrap");

        deleteWordButton = new javax.swing.JButton();
        deleteWordButton.setText(bundle.getString("Delete(Button)"));
        deleteWordButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteWordButtonActionPerformed(evt);
            }
        });
        wordsTablePanel.add(deleteWordButton, "right,sg");

        topPanel.add(wordsTablePanel, "span 2,sg,growx");
    }

    private void addWordButtonActionPerformed(java.awt.event.ActionEvent evt) {

        if (!studySets.isEmpty()) {
            addWord();
        } else {
            JOptionPane.showMessageDialog(this, TRANSLATOR.translate("AddStudySetFirst(Message)"), null, JOptionPane.WARNING_MESSAGE);
            clear();
            addStudySetField.requestFocus();
        }
    }

    private void deleteWordButtonActionPerformed(java.awt.event.ActionEvent evt) {
        String studySetName = (String) studySetsComboBox.getSelectedItem();
        long countOFTheRows = countOFTheWords = studyService.getCountOfTheWords(studySetName);
        for (int i = 0; i < countOFTheRows; i++) {
            if ((Boolean) wordsTable.getValueAt(i, 3)) {
                studyService.deleteWord((String) wordsTable.getValueAt(i, 1), studySetName);
                countOFTheWords--;
            }
        }
        setWordsInTable(false);
    }

    private void selectNothingButtonActionPerformed(java.awt.event.ActionEvent evt) {
        boolean selectAllWords = false;
        setWordsInTable(selectAllWords);
    }

    private void selectAllButtonActionPerformed(java.awt.event.ActionEvent evt) {
        boolean selectAllWords = true;
        setWordsInTable(selectAllWords);
    }

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {
        clear();
        wordSearchField.requestFocus();
    }

    private void wordSearchFieldActionPerformed(java.awt.event.ActionEvent evt) {
        wordSearchField.selectAll();
    }

    private void addStudySetButtonActionPerformed(java.awt.event.ActionEvent evt) {

        String name = addStudySetField.getText();
        if (name != null && !name.isEmpty()) {
            studyService.addStudySet(name);
            addStudySetField.setText(null);
            setStudySetsInComboBox();
            studySetsComboBox.setSelectedItem(name);
            studySets = studyService.getStudySets();
            boolean selectAllWords = false;
            setWordsInTable(selectAllWords);
            wordSearchField.requestFocus();
        } else {
            addStudySetField.requestFocus();
        }
    }

    private void deleteStudySetButtonActionPerformed(java.awt.event.ActionEvent evt) {
        String studySetName = (String) studySetsComboBox.getSelectedItem();
        studyService.deleteStudySet(studySetName);
        setStudySetsInComboBox();
        boolean selectAllWords = false;
        setWordsInTable(selectAllWords);
        studySets = studyService.getStudySets();
    }

    private void formWindowClosed(java.awt.event.WindowEvent evt) {
    }

    private void studySetsComboBoxPopupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
        boolean selectAllWords = false;
        setWordsInTable(selectAllWords);
    }

    private void setStudySetsInComboBox() {
        List<String> namesOfStudySets = new ArrayList<String>();
        namesOfStudySets = studyService.getNamesOfStudySets();
        studySetsComboBox.setModel(new DefaultComboBoxModel(namesOfStudySets.toArray()));
    }

    private void addWord() throws HeadlessException {
        String word = wordSearchField.getText();
        String studySetName = (String) studySetsComboBox.getSelectedItem();
        wordsForStudy = studyService.getWordsForStudy(studySetName);
        if (words.contains(word)) {
            countOFTheWords = studyService.getCountOfTheWords(studySetName);
            if (wordsForStudy.contains(word)) {
                JOptionPane.showMessageDialog(this, TRANSLATOR.translate("AlreadyContainedWord(Message)"), null, JOptionPane.ERROR_MESSAGE);
            } else {
                countOFTheWords++;
                Dictionary dictionary = null;
                if (dictionariesComboBox.getSelectedItem().equals(TRANSLATOR.translate("EnglishItem(ComboBox)"))) {
                    dictionary = dictionaries.get(0);
                }
                studyService.addWord(word, dictionary, studySetName);
                boolean selectAllWords = false;
                setWordsInTable(selectAllWords);
            }
        }
        clear();
        wordSearchField.requestFocus();
    }

    public void clear() {
        wordSearchField.setText(null);
        wordTranslationTextPane.setText(null);
    }

    public void setWordsInTable(Boolean select) {
        WordsTableModel model = new WordsTableModel();
        wordsTable.setModel(model);

        String studySetName = (String) studySetsComboBox.getSelectedItem();
        wordsForStudy = studyService.getWordsForStudy(studySetName);
        translationsForStudy = studyService.getTranslationsForStudy(studySetName);

        model.setColumnIdentifiers(new String[]{"",
                    TRANSLATOR.translate("Word(TableColumn)"), TRANSLATOR.translate("Translation(TableColumn)"),
                    ""});

        countOFTheWords = studyService.getCountOfTheWords(studySetName);

        List<String> translations = new ArrayList<String>();
        String translationsForTheTable = null;

        for (int i = 0; i < countOFTheWords; i++) {
            translations = studyService.getPossiblesTranslations(translationsForStudy.get(i));
            translationsForTheTable = studyService.combinePossiblesTranslationsForTheTable(translations);

            if (translationsForTheTable.isEmpty()) {
                translationsForTheTable = translationsForStudy.get(i);
            }
            model.addRow(new Object[]{new Integer(i + 1), wordsForStudy.get(i), translationsForTheTable, select});
        }
        setPreferredColumnWidth();
    }

    private void setPreferredColumnWidth() {
        TableColumn column;
        for (int i = 0; i < 4; i++) {
            column = wordsTable.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setMinWidth(1);
                column.setMaxWidth(35);
                column.setPreferredWidth(31);
            }
            if (i == 1) {
                column.setMinWidth(1);
                column.setMaxWidth(150);
                column.setPreferredWidth(100);
            }
            if (i == 3) {
                column.setMinWidth(1);
                column.setMaxWidth(25);
                column.setPreferredWidth(16);
            }
        }
    }
}
