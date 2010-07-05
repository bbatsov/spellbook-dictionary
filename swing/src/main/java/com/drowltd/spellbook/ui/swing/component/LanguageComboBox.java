package com.drowltd.spellbook.ui.swing.component;

import com.drowltd.spellbook.core.model.Language;

import javax.swing.JComboBox;

/**
 * @author Bozhidar Batsov
 * @since 0.4
 */
public class LanguageComboBox extends JComboBox {
    public LanguageComboBox() {
        super(Language.values());
    }
}
