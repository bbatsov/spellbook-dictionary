/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drowltd.spellbook.ui.swing.component;

import com.drowltd.spellbook.core.model.Dictionary;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

/**
 *
 * @author bozhidar
 */
public class DictionaryComboBox extends JComboBox {
    public DictionaryComboBox(List<Dictionary> dictionaries) {
        setModel(new DefaultComboBoxModel(dictionaries.toArray()));
    }
}
