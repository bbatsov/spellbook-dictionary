package bg.drow.spellbook.ui.desktop.study;

import bg.drow.spellbook.core.model.Dictionary;
import bg.drow.spellbook.core.model.StudySet;
import bg.drow.spellbook.core.service.DictionaryService;
import bg.drow.spellbook.core.service.study.StudyService;
import bg.drow.spellbook.ui.swing.component.BaseDialog;
import com.google.common.collect.Lists;
import javax.swing.event.PopupMenuEvent;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.event.PopupMenuListener;

/**
 *
 * @author Sasho
 */
public class AddStudySetDialog extends BaseDialog {

    private final DictionaryService dictionaryService;
    private final StudyService studyService;
    private String name;
    private List<Dictionary> dictionaries = Lists.newArrayList();
    private Dictionary selectedDictionary = new Dictionary();
    //components
    private JComboBox selectLanguageComboBox;
    private JLabel dictionaryIconLabel;
    private JTextField enterStudySetNameField;

    /** Creates new form SelectLanguageDialog */
    public AddStudySetDialog(Dialog parent, boolean modal) {
        super(parent, modal);

        dictionaryService = DictionaryService.getInstance();
        dictionaries = dictionaryService.getDictionaries();

        studyService = new StudyService();

        setResizable(false);
    }

    @Override
    public JComponent createContentPanel() {

        JPanel topPanel = new JPanel(new MigLayout("", "10[200]10[]10", "[15][30][][]"));

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        JLabel enterStudySetNameLabel = new JLabel();
        enterStudySetNameLabel.setText(getTranslator().translate("EnterStudySetName(Label)"));
        topPanel.add(enterStudySetNameLabel, "span, wrap");

        enterStudySetNameField = new JTextField();
        topPanel.add(enterStudySetNameField, "growx");

        JButton addStudySetButton = new JButton();
        addStudySetButton.setToolTipText(getTranslator().translate("AddStudySet(ToolTipText)"));
        addStudySetButton.setIcon(new ImageIcon(getClass().getResource("/icons/16x16/add2.png")));
        addStudySetButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                addStudySetButtonActionPerformed(evt);
            }
        });
        topPanel.add(addStudySetButton, "w 81!, wrap");

        JLabel selectLanguageLabel = new JLabel();
        selectLanguageLabel.setText(getTranslator().translate("SelectLanguage(Label)"));
        topPanel.add(selectLanguageLabel, "w 200!");

        dictionaryIconLabel = new JLabel();
        topPanel.add(dictionaryIconLabel, "span 1 2, gapleft 15, wrap");

        selectLanguageComboBox = new JComboBox();
        selectLanguageComboBox.addPopupMenuListener(new PopupMenuListener() {

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent evt) {
                selectLanguageComboBoxPopupMenuWillBecomeInvisible(evt);
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });

        for (Dictionary dict : dictionaries) {
            if (!dict.isSpecial() && !dict.getFromLanguage().getName().equals("Bulgarian")) {
                selectLanguageComboBox.addItem(dict.getFromLanguage().getName());
            }
        }
        topPanel.add(selectLanguageComboBox, "growx");

        setLanguageIconInDictionaryIconLabel();

        return topPanel;
    }

    public void addStudySetButtonActionPerformed(ActionEvent evt) {
        name = enterStudySetNameField.getText();

        if (name.isEmpty() || name == null) {
            enterStudySetNameField.requestFocus();
        } else {
            List<StudySet> studySets = studyService.getStudySets();
            boolean isAlreadyContainedStudySet = false;
            for (int i = 0; i < studySets.size(); i++) {
                if (studySets.get(i).getName().equals(name)) {
                    isAlreadyContainedStudySet = true;
                    JOptionPane.showMessageDialog(this, getTranslator().translate("AlreadyContainedStudySet(Message)"), null, JOptionPane.ERROR_MESSAGE);
                }
            }

            if (name != null && !name.isEmpty() && !isAlreadyContainedStudySet) {
                String language = (String) selectLanguageComboBox.getSelectedItem();
                for (Dictionary dict : dictionaries) {
                    if (!dict.isSpecial() && dict.getFromLanguage().getName().equals(language)) {
                        selectedDictionary = dict;
                    }
                }
                setVisible(false);
            }
        }
    }

    private void selectLanguageComboBoxPopupMenuWillBecomeInvisible(PopupMenuEvent evt) {
        setLanguageIconInDictionaryIconLabel();
        enterStudySetNameField.requestFocus();
    }

    private void setLanguageIconInDictionaryIconLabel() {
        String iconName = (String) selectLanguageComboBox.getSelectedItem();
        dictionaryIconLabel.setIcon(new ImageIcon(getClass().getResource("/icons/48x48/" + iconName + ".png")));
    }

    public String getStudySetName() {
        return name;
    }

    public Dictionary getSelectedDictionary() {
        return selectedDictionary;
    }
}
