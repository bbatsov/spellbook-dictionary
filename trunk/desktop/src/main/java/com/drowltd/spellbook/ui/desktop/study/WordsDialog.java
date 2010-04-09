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

import com.drowltd.spellbook.core.exception.DictionaryDbLockedException;
import com.drowltd.spellbook.core.service.DictionaryService;
import com.drowltd.spellbook.core.preferences.PreferencesManager;
import com.drowltd.spellbook.core.i18n.Translator;
import com.drowltd.spellbook.core.service.study.StudyService;
import com.drowltd.spellbook.core.model.Dictionary;
import com.drowltd.spellbook.core.model.StudySet;
import com.drowltd.spellbook.ui.swing.component.AutocompletingTextField;
import com.drowltd.spellbook.ui.swing.component.DictionaryComboBox;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author Sasho
 */
public class WordsDialog extends javax.swing.JDialog {

    private long countOFTheWords;
    private DictionaryService dictionaryService;
    private List<String> wordsForStudy = new ArrayList<String>();
    private List<String> translationForStudy = new ArrayList<String>();
    private List<String> words = new ArrayList<String>();
    private List<Dictionary> dictionaries = new ArrayList<Dictionary>();
    private List<StudySet> studySets = new ArrayList<StudySet>();
    private Map<String, Dictionary> dictionariesMap = new HashMap<String, Dictionary>();
    private StudyService studyService;
    private Frame parent;
    private static final PreferencesManager PM = PreferencesManager.getInstance();
    private static final Translator TRANSLATOR = Translator.getTranslator("WordsDialog");
    StudyWordsDialog studyWordsDialog = new StudyWordsDialog(parent, true);

    /** Creates new form WordsDialog */
    public WordsDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        TRANSLATOR.reset();
        this.parent = parent;

        try {
            studyService = new StudyService();
        } catch (DictionaryDbLockedException ex) {
            Logger.getLogger(WordsDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
        studySets = studyService.getStudySets();
        dictionaryService = DictionaryService.getInstance();
        dictionaries = dictionaryService.getDictionaries();
        words = dictionaryService.getWordsFromDictionary(dictionaries.get(0));

        initComponents();

        setStudySetsInComboBox();
        //int index = PM.getInt(Preference.STUDY_SETS, studySetsComboBox.getSelectedIndex());
        //studySetsComboBox.setSelectedIndex(index);
        String dictName = null;
        for (int i = 0; i < dictionaries.size(); i++) {
            dictName = (String) dictionariesComboBox.getItemAt(i);
            dictionariesMap.put(dictName, dictionaries.get(i));
        }
        addComponentListener(new ComponentAdapter() {

            @Override
            public void componentMoved(ComponentEvent e) {
                if (wordSearchField.hasFocus()) {
                    ((AutocompletingTextField) wordSearchField).showCompletions();
                }
            }
        });

        getTable().setOpaque(true);

        ((AutocompletingTextField) wordSearchField).setCompletions(words);
        ((AutocompletingTextField) wordSearchField).setOwner(this);

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
        translationForStudy = studyService.getTranslationsForStudy(name);
        countOFTheWords = studyService.getCountOfTheWords(name);
    }

    private void updateAddButtonState() {
        addWordButton.setEnabled(words.contains(wordSearchField.getText()));

        if (addWordButton.isEnabled()) {
            wordTranslationTextPane.setText(dictionaryService.getTranslation(wordSearchField.getText(),
                    dictionaryService.getDictionary((String) dictionariesComboBox.getSelectedItem())));
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        wordsScrollPane = new javax.swing.JScrollPane();
        wordsTable = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        wordSearchField = new AutocompletingTextField();
        addWordButton = new javax.swing.JButton();
        clearButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        wordTranslationTextPane = new javax.swing.JTextPane();
        jLabel2 = new javax.swing.JLabel();
        deleteWordButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        dictionariesComboBox = new DictionaryComboBox(dictionaries);
        jLabel5 = new javax.swing.JLabel();
        addStudySetField = new javax.swing.JTextField();
        addStudySetButton = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        studySetsComboBox = new javax.swing.JComboBox();
        deleteStudySetButton = new javax.swing.JButton();
        selectAllButton = new javax.swing.JButton();
        selectNothingButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Words");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        wordsScrollPane.setViewportView(wordsTable);

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("i18n/WordsDialog"); // NOI18N
        jLabel1.setText(bundle.getString("EnterWord(Label)")); // NOI18N

        wordSearchField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wordSearchFieldActionPerformed(evt);
            }
        });

        addWordButton.setText(bundle.getString("Add(Button)")); // NOI18N
        addWordButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addWordButtonActionPerformed(evt);
            }
        });

        clearButton.setText(bundle.getString("Clear(Button)")); // NOI18N
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });

        wordTranslationTextPane.setEditable(false);
        jScrollPane1.setViewportView(wordTranslationTextPane);

        jLabel2.setText(bundle.getString("Translation(Label)")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(wordSearchField, javax.swing.GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(clearButton, javax.swing.GroupLayout.DEFAULT_SIZE, 73, Short.MAX_VALUE)
                    .addComponent(addWordButton, javax.swing.GroupLayout.DEFAULT_SIZE, 73, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(addWordButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(clearButton))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(wordSearchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        deleteWordButton.setText(bundle.getString("Delete(Button)")); // NOI18N
        deleteWordButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteWordButtonActionPerformed(evt);
            }
        });

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel3.setText(bundle.getString("SelectLangueges(Label)")); // NOI18N

        jLabel5.setText(bundle.getString("EnterName(Label)")); // NOI18N

        addStudySetButton.setText(bundle.getString("AddStudySet(Button)")); // NOI18N
        addStudySetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addStudySetButtonActionPerformed(evt);
            }
        });

        jLabel6.setText(bundle.getString("StudySets(Label)")); // NOI18N

        studySetsComboBox.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                studySetsComboBoxPopupMenuWillBecomeInvisible(evt);
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
            }
        });

        deleteStudySetButton.setText(bundle.getString("DeleteStudySet(Button)")); // NOI18N
        deleteStudySetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteStudySetButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 187, Short.MAX_VALUE)
                    .addComponent(dictionariesComboBox, 0, 187, Short.MAX_VALUE))
                .addGap(111, 111, 111)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(studySetsComboBox, 0, 134, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(addStudySetButton, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(deleteStudySetButton))
                    .addComponent(addStudySetField, javax.swing.GroupLayout.DEFAULT_SIZE, 187, Short.MAX_VALUE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(36, 36, 36))
        );

        jPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {addStudySetField, dictionariesComboBox, studySetsComboBox});

        jPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {addStudySetButton, deleteStudySetButton});

        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(studySetsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addStudySetField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(deleteStudySetButton)
                            .addComponent(addStudySetButton)))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dictionariesComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        selectAllButton.setText(bundle.getString("All(Button)")); // NOI18N
        selectAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllButtonActionPerformed(evt);
            }
        });

        selectNothingButton.setText(bundle.getString("Nothing(Button)")); // NOI18N
        selectNothingButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectNothingButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(wordsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 535, Short.MAX_VALUE)
                    .addComponent(deleteWordButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(387, 387, 387)
                        .addComponent(selectNothingButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(selectAllButton, javax.swing.GroupLayout.DEFAULT_SIZE, 71, Short.MAX_VALUE)))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {deleteWordButton, selectAllButton, selectNothingButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(selectAllButton)
                    .addComponent(selectNothingButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(wordsScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(deleteWordButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void addWordButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addWordButtonActionPerformed
        addWord();
    }//GEN-LAST:event_addWordButtonActionPerformed

    private void deleteWordButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteWordButtonActionPerformed
        //String studySetName = (String) studySetsComboBox.getSelectedItem();
        //long countOFTheRows = countOFTheWords = studyService.getCountOfTheWords(studySetName);
        //for (int i = 0; i < countOFTheRows; i++) {
        //    if ((Boolean) wordsTable.getValueAt(i, 3)) {
        //        studyService.deleteWord((String) wordsTable.getValueAt(i, 1), studySetName);
        //        countOFTheWords--;
        //    }
        //}
        //setWordsInTable(false);
    }//GEN-LAST:event_deleteWordButtonActionPerformed

    private void selectNothingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectNothingButtonActionPerformed
        boolean selectAllWords = false;
        setWordsInTable(selectAllWords);
    }//GEN-LAST:event_selectNothingButtonActionPerformed

    private void selectAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAllButtonActionPerformed
        boolean selectAllWords = true;
        setWordsInTable(selectAllWords);
    }//GEN-LAST:event_selectAllButtonActionPerformed

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
        clear();
        wordSearchField.requestFocus();
    }//GEN-LAST:event_clearButtonActionPerformed

    private void wordSearchFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wordSearchFieldActionPerformed
        wordSearchField.selectAll();
    }//GEN-LAST:event_wordSearchFieldActionPerformed

    private void addStudySetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addStudySetButtonActionPerformed

        String name = addStudySetField.getText();
        if (name != null && !name.isEmpty()) {
            studyService.addStudySet(name);
            addStudySetField.setText(null);
            setStudySetsInComboBox();
            studySetsComboBox.setSelectedItem(name);
            boolean selectAllWords = false;
            setWordsInTable(selectAllWords);
            wordSearchField.requestFocus();
        } else {
            addStudySetField.requestFocus();
        }
    }//GEN-LAST:event_addStudySetButtonActionPerformed

    private void deleteStudySetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteStudySetButtonActionPerformed
        String studySetName = (String) studySetsComboBox.getSelectedItem();
        studyService.deleteStudySet(studySetName);
        setStudySetsInComboBox();
        boolean selectAllWords = false;
        setWordsInTable(selectAllWords);
    }//GEN-LAST:event_deleteStudySetButtonActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
    }//GEN-LAST:event_formWindowClosed

    private void studySetsComboBoxPopupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_studySetsComboBoxPopupMenuWillBecomeInvisible
        boolean selectAllWords = false;
        setWordsInTable(selectAllWords);
    }//GEN-LAST:event_studySetsComboBoxPopupMenuWillBecomeInvisible
    /**
     * @param args the command line arguments
     */
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addStudySetButton;
    private javax.swing.JTextField addStudySetField;
    private javax.swing.JButton addWordButton;
    private javax.swing.JButton clearButton;
    private javax.swing.JButton deleteStudySetButton;
    private javax.swing.JButton deleteWordButton;
    private javax.swing.JComboBox dictionariesComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton selectAllButton;
    private javax.swing.JButton selectNothingButton;
    private javax.swing.JComboBox studySetsComboBox;
    private javax.swing.JTextField wordSearchField;
    private javax.swing.JTextPane wordTranslationTextPane;
    private javax.swing.JScrollPane wordsScrollPane;
    private javax.swing.JTable wordsTable;
    // End of variables declaration//GEN-END:variables

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
                String dictName = (String) dictionariesComboBox.getSelectedItem();
                studyService.addWord(word, dictionariesMap.get(dictName), studySetName);
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
        translationForStudy = studyService.getTranslationsForStudy(studySetName);

        model.setColumnIdentifiers(new String[]{TRANSLATOR.translate("ID(TableColumn)"),
                    TRANSLATOR.translate("Word(TableColumn)"), TRANSLATOR.translate("Translation(TableColumn)"),
                    TRANSLATOR.translate("Selected(TableColumn)")});

        countOFTheWords = studyService.getCountOfTheWords(studySetName);
        for (int i = 0; i < countOFTheWords; i++) {
            model.addRow(new Object[]{new Integer(i + 1), wordsForStudy.get(i), translationForStudy.get(i), select});
        }
    }
}
