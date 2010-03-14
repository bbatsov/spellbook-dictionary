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

import com.drowltd.spellbook.core.db.DatabaseService;
import com.drowltd.spellbook.core.preferences.PreferencesManager;
import com.drowltd.spellbook.core.i18n.Translator;
import com.drowltd.spellbook.core.service.DictionaryService;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 *
 * @author Sasho
 */
public class WordsDialog extends javax.swing.JDialog {

    private long countOFTheWords;
    List<String> wordsForStudy = new ArrayList<String>();
    List<String> translationForStudy = new ArrayList<String>();
    //private DatabaseService dictDb;
    private DictionaryService dictDb;
    private Frame parent;
    private static final PreferencesManager PM = PreferencesManager.getInstance();
    private static final Translator TRANSLATOR = Translator.getTranslator("WordsDialog");
    StudyWordsDialog studyWordsDialog = new StudyWordsDialog(parent, true);

    /** Creates new form WordsDialog */
    public WordsDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        TRANSLATOR.reset();
        this.parent = parent;
        initComponents();
        getTable().setOpaque(true);
        dictDb = DictionaryService.getInstance();
        countOFTheWords = dictDb.getCountOfTheWords();
        // wordsForLearning = dictDb.getWordsForLearning();
        translationForStudy = dictDb.getTranslationForStudy();

        addWordField.requestFocus();
    }

    public JTextField getAddWordField() {
        return addWordField;
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
        addWordField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        addTranslationField = new javax.swing.JTextField();
        addButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        clearButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();
        nothingButton = new javax.swing.JButton();
        allButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Words");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        wordsScrollPane.setViewportView(wordsTable);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("i18n/WordsDialog"); // NOI18N
        jLabel1.setText(bundle.getString("EnterWord(Label)")); // NOI18N

        addWordField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addWordFieldActionPerformed(evt);
            }
        });

        jLabel2.setText(bundle.getString("EnterTranslation(Label)")); // NOI18N

        addTranslationField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addTranslationFieldActionPerformed(evt);
            }
        });

        addButton.setText(bundle.getString("Add(Button)")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        deleteButton.setText(bundle.getString("Delete(Button)")); // NOI18N
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        clearButton.setText(bundle.getString("Clear(Button)")); // NOI18N
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });

        editButton.setText(bundle.getString("Edit(Button)")); // NOI18N

        nothingButton.setText(bundle.getString("Nothing(Button)")); // NOI18N
        nothingButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nothingButtonActionPerformed(evt);
            }
        });

        allButton.setText(bundle.getString("All(Button)")); // NOI18N
        allButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(addWordField, javax.swing.GroupLayout.DEFAULT_SIZE, 182, Short.MAX_VALUE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(addTranslationField))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addButton)
                    .addComponent(editButton)
                    .addComponent(deleteButton)
                    .addComponent(clearButton))
                .addContainerGap(274, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(393, Short.MAX_VALUE)
                .addComponent(nothingButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(allButton)
                .addContainerGap())
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {addButton, clearButton, deleteButton, editButton});

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {allButton, nothingButton});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(addButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addWordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(editButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(deleteButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addTranslationField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(clearButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(allButton)
                    .addComponent(nothingButton)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(wordsScrollPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 547, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(wordsScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        String word = addWordField.getText();
        String translation = addTranslationField.getText();

        translation = translation.toLowerCase();
        List<String> words = new ArrayList<String>();

        wordsForStudy = dictDb.getWordsForStudy();
        translationForStudy = dictDb.getTranslationForStudy();
        words = StudyWordsDialog.getWords();

        if (word == null || word.isEmpty() || translation == null || translation.isEmpty()) {
            JOptionPane.showMessageDialog(this, TRANSLATOR.translate("EmptyFields(Message)"), null, JOptionPane.ERROR_MESSAGE);
        }

        if (words.contains(word)) { //da go napravq da se proverqva spored izbranite re4nici za6toto taka se zarejdata samo dumite na angliiski i se proverqva samo s tqh a ako dobavqme duma koqto e na drug ezik primerno germanski to 6te kazva postoqno 4e dumata q nqma v bazata
            //     wordsForLearning.add(word);
            countOFTheWords = dictDb.getCountOfTheWords();
            if (wordsForStudy.contains(word)) {
                JOptionPane.showMessageDialog(this, TRANSLATOR.translate("AlreadyContainedWord(Message)"), null, JOptionPane.ERROR_MESSAGE);
            }
            if (!wordsForStudy.contains(word)) {
                countOFTheWords++;
                dictDb.addWordForStudy(word, translation);

                setWordsInTable(false);
            }



        } else if (!words.contains(word) && !(word == null || word.isEmpty() || translation == null || translation.isEmpty())) {
            JOptionPane.showMessageDialog(this, TRANSLATOR.translate("NotExistWord(Message)"), null, JOptionPane.ERROR_MESSAGE);
        }

        clear();
        addWordField.requestFocus();
    }//GEN-LAST:event_addButtonActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        long countOFTheRows = countOFTheWords = dictDb.getCountOfTheWords();

        for (int i = 0; i < countOFTheRows; i++) {
            if ((Boolean) wordsTable.getValueAt(i, 3)) {
                dictDb.deleteWord((String) wordsTable.getValueAt(i, 1));
                countOFTheWords--;
            }
        }

        setWordsInTable(false);

    }//GEN-LAST:event_deleteButtonActionPerformed

    private void nothingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nothingButtonActionPerformed
        setWordsInTable(false);
    }//GEN-LAST:event_nothingButtonActionPerformed

    private void allButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_allButtonActionPerformed
        setWordsInTable(true);
    }//GEN-LAST:event_allButtonActionPerformed

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
        clear();
        addWordField.requestFocus();
    }//GEN-LAST:event_clearButtonActionPerformed

    private void addWordFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addWordFieldActionPerformed
        addTranslationField.requestFocus();
    }//GEN-LAST:event_addWordFieldActionPerformed

    private void addTranslationFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addTranslationFieldActionPerformed
        String word = addWordField.getText();
        String translation = addTranslationField.getText();
        List<String> words = new ArrayList<String>();
        wordsForStudy = dictDb.getWordsForStudy();
        translationForStudy = dictDb.getTranslationForStudy();
        words = StudyWordsDialog.getWords();

        if (word == null || word.isEmpty() || translation == null || translation.isEmpty()) {
            JOptionPane.showMessageDialog(this, TRANSLATOR.translate("EmptyFields(Message)"), null, JOptionPane.ERROR_MESSAGE);
        }

        if (words.contains(word)) {
            //     wordsForLearning.add(word);
            countOFTheWords = dictDb.getCountOfTheWords();
            if (wordsForStudy.contains(word)) {
                JOptionPane.showMessageDialog(this, TRANSLATOR.translate("AlreadyContainedWord(Message)"), null, JOptionPane.ERROR_MESSAGE);
            }
            if (!wordsForStudy.contains(word)) {
                countOFTheWords++;
                dictDb.addWordForStudy(word, translation);

                setWordsInTable(false);
            }
        } else if (!words.contains(word) && !(word == null || word.isEmpty() || translation == null || translation.isEmpty())) {
            JOptionPane.showMessageDialog(this, TRANSLATOR.translate("NotExistWord(Message)"), null, JOptionPane.ERROR_MESSAGE);
        }
        clear();
        addWordField.requestFocus();

    }//GEN-LAST:event_addTranslationFieldActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
    }//GEN-LAST:event_formWindowClosed
    /**
     * @param args the command line arguments
     */
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JTextField addTranslationField;
    private javax.swing.JTextField addWordField;
    private javax.swing.JButton allButton;
    private javax.swing.JButton clearButton;
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton editButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton nothingButton;
    private javax.swing.JScrollPane wordsScrollPane;
    private javax.swing.JTable wordsTable;
    // End of variables declaration//GEN-END:variables

    public void clear() {
        addWordField.setText(null);
        addTranslationField.setText(null);
    }

    public void setWordsInTable(Boolean select) {
        WordsTableModel model = new WordsTableModel();
        wordsTable.setModel(model);
        wordsForStudy = dictDb.getWordsForStudy();
        translationForStudy = dictDb.getTranslationForStudy();
        model.setColumnIdentifiers(new String[]{TRANSLATOR.translate("ID(TableColumn)"), TRANSLATOR.translate("Word(TableColumn)"), TRANSLATOR.translate("Translation(TableColumn)"), TRANSLATOR.translate("Selected(TableColumn)")});
        countOFTheWords = dictDb.getCountOfTheWords();
        for (int i = 0; i < countOFTheWords; i++) {
            model.addRow(new Object[]{new Integer(i + 1), wordsForStudy.get(i), translationForStudy.get(i), select});
        }
    }
}
