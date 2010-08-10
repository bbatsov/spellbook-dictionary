/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * StudySetsDialog.java
 *
 * Created on 2010-2-3, 10:21:38
 */
package bg.drow.spellbook.ui.desktop.study;

import bg.drow.spellbook.core.model.Dictionary;
import bg.drow.spellbook.core.model.Language;
import bg.drow.spellbook.core.model.StudySet;
import bg.drow.spellbook.core.preferences.PreferencesManager;
import bg.drow.spellbook.core.service.DictionaryService;
import bg.drow.spellbook.core.service.DictionaryServiceImpl;
import bg.drow.spellbook.core.service.study.StudyService;
import bg.drow.spellbook.ui.swing.component.BaseDialog;
import com.jidesoft.swing.AutoCompletion;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableColumn;
import java.awt.Dialog;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sasho
 */
public class StudySetsDialog extends BaseDialog {

    private long countOFTheWords;
    private final DictionaryService dictionaryService;
    private List<String> wordsForStudy = new ArrayList<String>();
    private List<String> translationsForStudy = new ArrayList<String>();
    private List<String> words = new ArrayList<String>();
    private List<Dictionary> dictionaries = new ArrayList<Dictionary>();
    private List<StudySet> studySets = new ArrayList<StudySet>();
    private final StudyService studyService;
    private static final PreferencesManager PM = PreferencesManager.getInstance();
    private Dictionary selectedDictionary = null;
    //components
    private JPanel studySetsPanel;
    private JTextField addStudySetField;
    private JButton addWordButton;
    private JComboBox dictionariesComboBox;
    private JPanel topPanel;
    private JComboBox studySetsComboBox;
    private JTextField wordSearchField;
    private JTextPane wordTranslationTextPane;
    private JTable wordsTable;

    public StudySetsDialog(Dialog parent, boolean modal) {
        super(parent, modal);

        dictionaryService = DictionaryServiceImpl.getInstance();
        dictionaries = dictionaryService.getDictionaries();
        words = dictionaryService.getWordsFromDictionary(dictionaries.get(2));//!

        studyService = new StudyService();
        studySets = studyService.getStudySets();

        setResizable(false);
    }

    private void updateAddButtonState() {
        addWordButton.setEnabled(words.contains(wordSearchField.getText()));

        if (addWordButton.isEnabled()) {
            Dictionary dictionary = null;
            Language lang = (Language) dictionariesComboBox.getSelectedItem();
            for (Dictionary dict : dictionaries) {
                if (!dict.isSpecial() && dict.getFromLanguage().getName().equals(lang.getName())) {
                    dictionary = dict;
                }
            }
            wordTranslationTextPane.setText(dictionaryService.getTranslation(wordSearchField.getText(),
                    dictionary));
            wordTranslationTextPane.setCaretPosition(0);
        }
    }

    public JTable getTable() {
        return wordsTable;
    }

    @Override
    public JComponent createContentPanel() {
        topPanel = new JPanel(new MigLayout("", "10[240]10[240]10", "10[]10[]0[]10"));

        initLanguagesPanel();

        initStudySetsPanel();

        initAddWordPanel();

        initWordsTablePanel();

        AutoCompletion autoCompletion = new AutoCompletion(wordSearchField, words);
        autoCompletion.setStrict(false);

        setStudySetsInComboBox();
        if (!studySets.isEmpty()) {
            int index = PM.getInt(PreferencesManager.Preference.STUDY_SETS, studySetsComboBox.getSelectedIndex());
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

        setWordsInTable(false);

        // wordsForLearning = dictDb.getWordsForLearning();
        String name = (String) studySetsComboBox.getSelectedItem();
        translationsForStudy = studyService.getTranslationsForStudy(name);
        countOFTheWords = studyService.getCountOfTheWords(name);

        return topPanel;
    }

    private void initLanguagesPanel() {
        JPanel languagesPanel = new JPanel(new MigLayout("", "25[200]25", "[][]"));
        languagesPanel.setBorder(BorderFactory.createEtchedBorder());

        JLabel jLabel3 = new JLabel();
        jLabel3.setText(getTranslator().translate("SelectLangueges(Label)")); // NOI18N
        languagesPanel.add(jLabel3, "wrap");

        dictionariesComboBox = new JComboBox();
        languagesPanel.add(dictionariesComboBox, "span,growx");

        for (Dictionary dict : dictionaries) {
            if (!dict.isSpecial() && !dict.getFromLanguage().getName().equals("Bulgarian")) {
                dictionariesComboBox.addItem(dict.getFromLanguage());
            }
        }
        dictionariesComboBox.setSelectedIndex(0);

        topPanel.add(languagesPanel, "growy");
    }

    private void initStudySetsPanel() {
        studySetsPanel = new JPanel(new MigLayout("", "25[100][100]25", "[][][][][]"));
        studySetsPanel.setBorder(BorderFactory.createEtchedBorder());

        JLabel jLabel6 = new JLabel();
        jLabel6.setText(getTranslator().translate("StudySets(Label)")); // NOI18N
        studySetsPanel.add(jLabel6, "wrap");

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
        studySetsPanel.add(studySetsComboBox, "span 2,growx,wrap");

        JLabel jLabel5 = new JLabel();
        jLabel5.setText(getTranslator().translate("EnterName(Label)")); // NOI18N
        studySetsPanel.add(jLabel5, "span 2,wrap");

        addStudySetField = new JTextField();
        studySetsPanel.add(addStudySetField, "span 2,growx,wrap");

        JButton addStudySetButton = new JButton();
        addStudySetButton.setText(getTranslator().translate("AddStudySet(Button)")); // NOI18N
        addStudySetButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                addStudySetButtonActionPerformed(evt);
            }
        });
        studySetsPanel.add(addStudySetButton, "w 81!,sg,left");

        JButton deleteStudySetButton = new JButton();
        deleteStudySetButton.setText(getTranslator().translate("DeleteStudySet(Button)")); // NOI18N
        deleteStudySetButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                deleteStudySetButtonActionPerformed(evt);
            }
        });
        studySetsPanel.add(deleteStudySetButton, "sg,right");

        topPanel.add(studySetsPanel, "growx,wrap");
    }

    private void initAddWordPanel() {
        JPanel addWordPanel = new JPanel(new MigLayout("", "[400][]", "[][][154]"));
        addWordPanel.setBorder(BorderFactory.createEtchedBorder());

        JLabel jLabel1 = new JLabel();
        jLabel1.setText(getTranslator().translate("EnterWord(Label)")); // NOI18N
        addWordPanel.add(jLabel1, "wrap");

        wordSearchField = new JTextField();
        wordSearchField.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                wordSearchFieldActionPerformed(evt);
            }
        });

        wordSearchField.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent evt) {
                wordSearchFieldFocusGained(evt);
            }
        });

        addWordPanel.add(wordSearchField, "growx");

        addWordButton = new JButton();
        addWordButton.setText(getTranslator().translate("Add(Button)")); // NOI18N
        addWordButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                addWordButtonActionPerformed(evt);
            }
        });
        addWordPanel.add(addWordButton, "w 81!,sg,right,wrap");

        JScrollPane wordTranslationScrollPane = new JScrollPane();
        wordTranslationTextPane = new JTextPane();
        wordTranslationTextPane.setEditable(false);
        wordTranslationScrollPane.add(wordTranslationTextPane);
        wordTranslationScrollPane.setViewportView(wordTranslationTextPane);
        addWordPanel.add(wordTranslationScrollPane, "grow");

        JButton clearButton = new JButton();
        clearButton.setText(getTranslator().translate("Clear(Button)")); // NOI18N
        clearButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });
        addWordPanel.add(clearButton, "sg,top");

        topPanel.add(addWordPanel, "span 2,sg,wrap");
    }

    private void initWordsTablePanel() {
        JPanel wordsTablePanel = new JPanel(new MigLayout("", "0[500]0", "[][165][]"));

        JButton selectNothingButton = new JButton();
        selectNothingButton.setText(getTranslator().translate("Nothing(Button)")); // NOI18N
        selectNothingButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                selectNothingButtonActionPerformed(evt);
            }
        });
        wordsTablePanel.add(selectNothingButton, "w 81!,right,split 2,sg");

        JButton selectAllButton = new JButton();
        selectAllButton.setText(getTranslator().translate("All(Button)")); // NOI18N
        selectAllButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                selectAllButtonActionPerformed(evt);
            }
        });
        wordsTablePanel.add(selectAllButton, "right,sg,wrap");

        JScrollPane wordsScrollPane = new JScrollPane();
        wordsTable = new JTable() {

            @Override
            public String getToolTipText(MouseEvent e) {
                String tip = null;
                Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);
                int realColumnIndex = convertColumnIndexToModel(colIndex);

                if (realColumnIndex == 1 || realColumnIndex == 2) {
                    tip = (String) getValueAt(rowIndex, colIndex);
                }
                return tip;
            }
        };

        wordsScrollPane.setViewportView(wordsTable);
        wordsTablePanel.add(wordsScrollPane, "growx,wrap");

        JButton deleteWordButton = new JButton();
        deleteWordButton.setText(getTranslator().translate("Delete(Button)"));
        deleteWordButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                deleteWordButtonActionPerformed(evt);
            }
        });
        wordsTablePanel.add(deleteWordButton, "right,sg");

        topPanel.add(wordsTablePanel, "span 2,sg,growx");
    }

    private void addWordButtonActionPerformed(ActionEvent evt) {

        if (!studySets.isEmpty()) {
            addWord();
        } else {
            JOptionPane.showMessageDialog(this, getTranslator().translate("AddStudySetFirst(Message)"), null, JOptionPane.WARNING_MESSAGE);
            clear();
            addStudySetField.requestFocus();
        }
    }

    private void deleteWordButtonActionPerformed(ActionEvent evt) {
        String studySetName = (String) studySetsComboBox.getSelectedItem();
        long countOFTheRows = countOFTheWords = studyService.getCountOfTheWords(studySetName);
        for (int i = 0; i < countOFTheRows; i++) {
            if ((Boolean) wordsTable.getValueAt(i, 3)) {
                studyService.deleteWord((String) wordsTable.getValueAt(i, 1), studySetName, selectedDictionary);
                countOFTheWords--;
            }
        }
        setWordsInTable(false);
    }

    private void selectNothingButtonActionPerformed(ActionEvent evt) {
        boolean selectAllWords = false;
        setWordsInTable(selectAllWords);
    }

    private void selectAllButtonActionPerformed(ActionEvent evt) {
        boolean selectAllWords = true;
        setWordsInTable(selectAllWords);
    }

    private void clearButtonActionPerformed(ActionEvent evt) {
        clear();
        wordSearchField.requestFocus();
    }

    private void wordSearchFieldActionPerformed(ActionEvent evt) {
        wordSearchField.selectAll();
    }

    private void wordSearchFieldFocusGained(java.awt.event.FocusEvent evt) {
        if (selectedDictionary == null) {
            setSelectedDictionary();
        }
    }

    private void addStudySetButtonActionPerformed(ActionEvent evt) {
        String name = addStudySetField.getText();
        studySets = studyService.getStudySets();
        boolean isAlreadyContainedStudySet = false;
        for (int i = 0; i < studySets.size(); i++) {
            if (studySets.get(i).getName().equals(name)) {
                isAlreadyContainedStudySet = true;
                JOptionPane.showMessageDialog(this, getTranslator().translate("AlreadyContainedStudySet(Message)"), null, JOptionPane.ERROR_MESSAGE);
            }
        }
        if (name != null && !name.isEmpty() && !isAlreadyContainedStudySet) {
            SelectLanguageDialog selectLanguageDialog = new SelectLanguageDialog(this, true);
            selectLanguageDialog.showDialog(studySetsPanel);
            boolean isSelectedDictionary = selectLanguageDialog.getDictionaryIsSelected();
            if (isSelectedDictionary) {
                studyService.addStudySet(name, selectLanguageDialog.getSelectedDictionary());
                addStudySetField.setText(null);
                setStudySetsInComboBox();
                studySetsComboBox.setSelectedItem(name);
                studySets = studyService.getStudySets();
                boolean selectAllWords = false;
                setWordsInTable(selectAllWords);
                wordSearchField.requestFocus();
            }
        } else {
            addStudySetField.requestFocus();
        }
    }

    private void deleteStudySetButtonActionPerformed(ActionEvent evt) {
        String studySetName = (String) studySetsComboBox.getSelectedItem();
        studyService.deleteStudySet(studySetName);
        setStudySetsInComboBox();
        boolean selectAllWords = false;
        setWordsInTable(selectAllWords);
        studySets = studyService.getStudySets();
    }

    private void studySetsComboBoxPopupMenuWillBecomeInvisible(PopupMenuEvent evt) {
        boolean selectAllWords = false;
        setWordsInTable(selectAllWords);
        //setSelectedDictionary();//!
    }

    private void setStudySetsInComboBox() {
        List<String> namesOfStudySets;
        namesOfStudySets = studyService.getNamesOfStudySets();
        studySetsComboBox.setModel(new DefaultComboBoxModel(namesOfStudySets.toArray()));
    }

    private void setSelectedDictionary() {
        StudySet selectedStudySet = new StudySet();
        if (studySetsComboBox.getItemAt(0) != null) {
            selectedStudySet = studyService.getStudySet((String) studySetsComboBox.getSelectedItem());
            selectedDictionary = selectedStudySet.getDictionary();
        }
    }

    private void addWord() throws HeadlessException {
        String word = wordSearchField.getText();
        String studySetName = (String) studySetsComboBox.getSelectedItem();
        wordsForStudy = studyService.getWordsForStudy(studySetName);
        if (words.contains(word)) {
            countOFTheWords = studyService.getCountOfTheWords(studySetName);
            if (wordsForStudy.contains(word)) {
                JOptionPane.showMessageDialog(this, getTranslator().translate("AlreadyContainedWord(Message)"), null, JOptionPane.ERROR_MESSAGE);
            } else {
                countOFTheWords++;
                Dictionary dictionary = null;
                Language lang = (Language) dictionariesComboBox.getSelectedItem();
                for (Dictionary dict : dictionaries) {
                    if (!dict.isSpecial() && dict.getFromLanguage().getName().equals(lang.getName())) {
                        dictionary = dict;
                    }
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
                    getTranslator().translate("Word(TableColumn)"), getTranslator().translate("Translation(TableColumn)"),
                    ""});

        countOFTheWords = studyService.getCountOfTheWords(studySetName);

        List<String> translations;
        String translationsForTheTable;

        for (int i = 0; i < countOFTheWords; i++) {
            translations = studyService.getPossiblesTranslations(translationsForStudy.get(i));
            translationsForTheTable = studyService.combinePossiblesTranslationsForTheTable(translations);

            if (translationsForTheTable.isEmpty()) {
                translationsForTheTable = translationsForStudy.get(i);
            }
            model.addRow(new Object[]{i + 1, wordsForStudy.get(i), translationsForTheTable, select});
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
