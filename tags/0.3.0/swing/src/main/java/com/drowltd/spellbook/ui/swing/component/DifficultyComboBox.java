package com.drowltd.spellbook.ui.swing.component;

import com.drowltd.spellbook.core.model.Difficulty;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

/**
 * @author Bozhidar Batsov
 * @since 0.3
 */
public class DifficultyComboBox extends JComboBox {
    public DifficultyComboBox() {
        setModel(new DefaultComboBoxModel(Difficulty.values()));
    }
}