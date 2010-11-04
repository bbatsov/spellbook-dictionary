package bg.drow.spellbook.ui.desktop.study;

import bg.drow.spellbook.core.model.Dictionary;
import bg.drow.spellbook.core.model.StudySet;
import bg.drow.spellbook.core.preferences.PreferencesManager;
import bg.drow.spellbook.core.service.DictionaryService;
import bg.drow.spellbook.core.service.study.StudyService;
import bg.drow.spellbook.ui.swing.component.BaseDialog;
import bg.drow.spellbook.ui.swing.util.IconManager;
import com.google.common.collect.Lists;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * @author Sasho
 */
public class StudySetsDialog extends BaseDialog {

    private long countOFTheWords;
    private final DictionaryService dictionaryService;
    private List<String> words = Lists.newArrayList();
    private List<StudySet> studySets = Lists.newArrayList();
    private final StudyService studyService;
    private static final PreferencesManager PM = PreferencesManager.getInstance();
    private Dictionary selectedDictionary = null;
    //components
    private JPanel studySetsPanel;
    private JButton addWordButton;
    private JPanel topPanel;
    private JComboBox studySetsComboBox;
    private JTextField wordSearchField;
    private JTextPane wordTranslationTextPane;
    private JTable wordsTable;
    private JLabel languageIconLabel;

    public StudySetsDialog(Dialog parent, boolean modal) {
        super(parent, modal);

        dictionaryService = DictionaryService.getInstance();

        studyService = new StudyService();
        studySets = studyService.getStudySets();

        setResizable(false);
    }

    private void updateAddButtonState() {
        StudySet studySet = studyService.getStudySet((String) studySetsComboBox.getSelectedItem());
        Dictionary dictionary = studySet.getDictionary();
        words = dictionaryService.getWordsFromDictionary(dictionary);

        String word = studyService.removeSpacesInTheBeginningAndEnd(wordSearchField.getText());
        addWordButton.setEnabled(words.contains(word));

        if (addWordButton.isEnabled()) {

            wordTranslationTextPane.setText(dictionaryService.getTranslation(word,
                    dictionary));
            wordTranslationTextPane.setCaretPosition(0);
        }
    }

    public JTable getTable() {
        return wordsTable;
    }

    @Override
    public JComponent createContentPanel() {
        topPanel = new JPanel(new MigLayout("", "10[490]10", "10[]10[]0[]10"));

        initStudySetsPanel();

        initAddWordPanel();

        initWordsTablePanel();


        if (!studySets.isEmpty()) {
            int index = PM.getInt(PreferencesManager.Preference.STUDY_SETS, studySetsComboBox.getSelectedIndex());
            if (index < studyService.getCountOfStudySets() && index != -1) {
                studySetsComboBox.setSelectedIndex(index);
            } else if (index >= studySets.size() || index == -1) {
                studySetsComboBox.setSelectedIndex(0);
            }
        }

        //StudySet studySet = studyService.getStudySet((String) studySetsComboBox.getSelectedItem());
        //Dictionary dictionary = studySet.getDictionary();
        //words = dictionaryService.getWordsFromDictionary(dictionary);
        //autoCompletion = new AutoCompletion(wordSearchField, words);
        //autoCompletion.setStrict(false);

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

        List<String> translationsForStudy = Lists.newArrayList();
        String studySetName = (String) studySetsComboBox.getSelectedItem();
        translationsForStudy = studyService.getTranslationsForStudy(studySetName);
        countOFTheWords = studyService.getCountOfTheWordsInStudySet(studySetName);

        return topPanel;
    }

    private void initStudySetsPanel() {
        studySetsPanel = new JPanel(new MigLayout("", "25[100][100][]25", "[][][][]"));
        studySetsPanel.setBorder(BorderFactory.createEtchedBorder());

        JLabel jLabel6 = new JLabel();
        jLabel6.setText(getTranslator().translate("StudySets(Label)")); // NOI18N
        studySetsPanel.add(jLabel6, "span 2");

        languageIconLabel = new JLabel();
        studySetsPanel.add(languageIconLabel, "span 1 4, gapleft 50, wrap");

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

        setStudySetsInComboBox();
        setSelectedDictionary();
        setLanguageIcon();

        JButton addNewStudySetButton = new JButton();
        addNewStudySetButton.setToolTipText(getTranslator().translate("AddStudySet(ToolTipText)"));
        addNewStudySetButton.setIcon(new ImageIcon(getClass().getResource("/icons/16x16/add2.png")));
        addNewStudySetButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                addNewStudySetButtonActionPerformed(evt);
            }
        });
        studySetsPanel.add(addNewStudySetButton, "w 81!,sg,left");

        JButton deleteStudySetButton = new JButton();
        deleteStudySetButton.setToolTipText(getTranslator().translate("DeleteStudySet(ToolTipText)"));
        deleteStudySetButton.setIcon(new ImageIcon(getClass().getResource("/icons/16x16/delete2.png")));
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
        addWordButton.setToolTipText(getTranslator().translate("AddWord(ToolTipText)")); // NOI18N
        addWordButton.setIcon(new ImageIcon(getClass().getResource("/icons/16x16/add2.png")));
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
        clearButton.setToolTipText(getTranslator().translate("Clear(ToolTipText)")); // NOI18N
        clearButton.setIcon(new ImageIcon(getClass().getResource("/icons/16x16/eraser.png")));
        clearButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });
        addWordPanel.add(clearButton, "sg,top");

        topPanel.add(addWordPanel, "sg,wrap");
    }

    private void initWordsTablePanel() {
        JPanel wordsTablePanel = new JPanel(new MigLayout("", "0[500]0", "[][165][]"));

        JButton selectNothingButton = new JButton();
        selectNothingButton.setToolTipText(getTranslator().translate("Nothing(ToolTipText)")); // NOI18N
        selectNothingButton.setIcon(new ImageIcon(getClass().getResource("/icons/16x16/table_sql.png")));
        selectNothingButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                selectNothingButtonActionPerformed(evt);
            }
        });
        wordsTablePanel.add(selectNothingButton, "w 81!,right,split 2,sg");

        JButton selectAllButton = new JButton();
        selectAllButton.setToolTipText(getTranslator().translate("All(ToolTipText)")); // NOI18N
        selectAllButton.setIcon(new ImageIcon(getClass().getResource("/icons/16x16/table_selection_all.png")));
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
        deleteWordButton.setToolTipText(getTranslator().translate("DeleteWord(ToolTipText)"));
        deleteWordButton.setIcon(new ImageIcon(getClass().getResource("/icons/16x16/delete2.png")));
        deleteWordButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                deleteWordButtonActionPerformed(evt);
            }
        });
        wordsTablePanel.add(deleteWordButton, "right,sg");

        topPanel.add(wordsTablePanel, "sg,growx");
    }

    private void addWordButtonActionPerformed(ActionEvent evt) {

        if (!studySets.isEmpty()) {
            addWord();
        } else {
            JOptionPane.showMessageDialog(this, getTranslator().translate("AddStudySetFirst(Message)"), null, JOptionPane.WARNING_MESSAGE);
            clear();
        }
    }

    private void deleteWordButtonActionPerformed(ActionEvent evt) {
        String studySetName = (String) studySetsComboBox.getSelectedItem();
        long countOFTheRows = countOFTheWords = studyService.getCountOfTheWordsInStudySet(studySetName);

        setSelectedDictionary();

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

    private void addNewStudySetButtonActionPerformed(ActionEvent evt) {

        AddStudySetDialog addStudySetDialog = new AddStudySetDialog(this, true);
        addStudySetDialog.showDialog(studySetsComboBox);

        String name = addStudySetDialog.getStudySetName();

        if (name != null && !name.isEmpty()) {

            studyService.addStudySet(name, addStudySetDialog.getSelectedDictionary());
            setStudySetsInComboBox();
            studySetsComboBox.setSelectedItem(name);
            selectedDictionary = addStudySetDialog.getSelectedDictionary();
            setLanguageIcon();
            studySets = studyService.getStudySets();
            boolean selectAllWords = false;
            setWordsInTable(selectAllWords);
            wordSearchField.requestFocus();

        }
    }

    private void deleteStudySetButtonActionPerformed(ActionEvent evt) {
        String studySetName = (String) studySetsComboBox.getSelectedItem();
        int studySetIndex = studySetsComboBox.getSelectedIndex();
        studyService.deleteStudySet(studySetName);
        setStudySetsInComboBox();
        studySetsComboBox.setSelectedIndex(studySetIndex - 1);
        setSelectedDictionary();
        setLanguageIcon();
        boolean selectAllWords = false;
        setWordsInTable(selectAllWords);
        studySets = studyService.getStudySets();
    }

    private void studySetsComboBoxPopupMenuWillBecomeInvisible(PopupMenuEvent evt) {
        boolean selectAllWords = false;
        setWordsInTable(selectAllWords);
        
        Dictionary oldDictionary = selectedDictionary;
        setSelectedDictionary();
        setLanguageIcon();
        if (!oldDictionary.equals(selectedDictionary)) {
            clear();
        }
    }

    private void setLanguageIcon() {
        String languageName = selectedDictionary.getFromLanguage().getName();
        languageIconLabel.setIcon(new ImageIcon(getClass().getResource("/icons/48x48/" + languageName + "128.png")));
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
        String word = studyService.removeSpacesInTheBeginningAndEnd(wordSearchField.getText());
        String studySetName = (String) studySetsComboBox.getSelectedItem();
        List<String> wordsForStudy = Lists.newArrayList();
        wordsForStudy = studyService.getWordsForStudy(studySetName);

        if (words.contains(word)) {
            countOFTheWords = studyService.getCountOfTheWordsInStudySet(studySetName);
            if (wordsForStudy.contains(word)) {
                JOptionPane.showMessageDialog(this, getTranslator().translate("AlreadyContainedWord(Message)"), null, JOptionPane.ERROR_MESSAGE);
            } else {
                countOFTheWords++;
                StudySet studySet = studyService.getStudySet(studySetName);
                studyService.addWord(word, studySet.getDictionary(), studySetName);

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

        List<String> wordsForStudy = Lists.newArrayList();
        wordsForStudy = studyService.getWordsForStudy(studySetName);

        List<String> translationsForStudy = Lists.newArrayList();
        translationsForStudy = studyService.getTranslationsForStudy(studySetName);

        model.setColumnIdentifiers(new String[]{"",
                    getTranslator().translate("Word(TableColumn)"), getTranslator().translate("Translation(TableColumn)"),
                    ""});

        countOFTheWords = studyService.getCountOfTheWordsInStudySet(studySetName);

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
