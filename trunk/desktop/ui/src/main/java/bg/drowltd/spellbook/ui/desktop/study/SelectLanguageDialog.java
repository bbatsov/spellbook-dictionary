/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * SelectLanguageDialog.java
 *
 * Created on 2010-7-11, 14:20:44
 */
package bg.drowltd.spellbook.ui.desktop.study;

import bg.drowltd.spellbook.core.model.Dictionary;
import bg.drowltd.spellbook.core.service.DictionaryService;
import bg.drowltd.spellbook.core.service.DictionaryServiceImpl;
import bg.drowltd.spellbook.ui.swing.component.BaseDialog;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author Sasho
 */
public class SelectLanguageDialog extends BaseDialog {

    private final DictionaryService dictionaryService;
    private List<Dictionary> dictionaries = new ArrayList<Dictionary>();
    private Dictionary selectedDictionary = new Dictionary();
    private boolean dictionaryIsSelected = false;
    JComboBox selectLanguageComboBox = new JComboBox();

    /** Creates new form SelectLanguageDialog */
    public SelectLanguageDialog(Dialog parent, boolean modal) {
        super(parent, modal);

        dictionaryService = DictionaryServiceImpl.getInstance();
        dictionaries = dictionaryService.getDictionaries();

        setResizable(false);
    }

    @Override
    public JComponent createContentPanel() {

        JPanel topPanel = new JPanel(new MigLayout("", "10[200]10[]10", "[15][30]"));

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        JLabel selectLanguageLabel = new JLabel();
        selectLanguageLabel.setText(getTranslator().translate("SelectLanguage(Label)"));
        topPanel.add(selectLanguageLabel, "span, wrap");


        for (Dictionary dict : dictionaries) {
            if (!dict.isSpecial() && !dict.getFromLanguage().getName().equals("Bulgarian")) {
                selectLanguageComboBox.addItem(dict.getFromLanguage().getName());
            }
        }
        topPanel.add(selectLanguageComboBox, "growx");

        JButton okButton = new JButton();
        okButton.setText(getTranslator().translate("ok(Button)"));
        okButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        topPanel.add(okButton, "w 81!");

        return topPanel;
    }

    public void okButtonActionPerformed(ActionEvent evt) {
        dictionaryIsSelected = true;
        String language = (String) selectLanguageComboBox.getSelectedItem();
        for (Dictionary dict : dictionaries) {
            if (!dict.isSpecial() && dict.getFromLanguage().getName().equals(language)) {
                selectedDictionary = dict;
            }
        }
        setVisible(false);
    }

    public Dictionary getSelectedDictionary() {
        return selectedDictionary;
    }

    public boolean getDictionaryIsSelected() {
        return dictionaryIsSelected;
    }

    public void setDictionaryIsSelected(boolean isSelected) {
        dictionaryIsSelected = isSelected;
    }
}
