package bg.drow.spellbook.ui.swing.component;

import bg.drow.spellbook.core.model.Language;

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
