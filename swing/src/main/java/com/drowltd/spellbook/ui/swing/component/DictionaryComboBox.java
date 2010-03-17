/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drowltd.spellbook.ui.swing.component;

import com.drowltd.spellbook.core.i18n.Translator;
import com.drowltd.spellbook.core.model.Dictionary;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

/**
 *
 * @author bozhidar
 */
public class DictionaryComboBox extends JComboBox {
    private static final Translator TRANSLATOR = Translator.getTranslator("Model");

    public DictionaryComboBox(List<Dictionary> dictionaries) {
        String[] dictionaryNames = new String[dictionaries.size()];

        for (int i = 0; i < dictionaries.size(); i++) {
            dictionaryNames[i] = dictionaries.get(i).getName();
        }

        setModel(new DefaultComboBoxModel(dictionaryNames));
    }
}
