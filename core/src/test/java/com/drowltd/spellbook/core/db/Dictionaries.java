package com.drowltd.spellbook.core.db;

import com.drowltd.spellbook.core.db.SDictionary;
import com.drowltd.spellbook.core.db.Language;
import javax.swing.ImageIcon;

/**
 *
 * @author iivalchev
 */
public class Dictionaries {

    final static Language english;
    final static Language bulgarian;
    final static SDictionary dictionaryEN_BG;
    final static SDictionary dictionaryBG_EN;

    static {
        final ImageIcon imageIcon = new ImageIcon("");

        english = new Language("English", "abcdefghijklmnopqrstuvwxyz", imageIcon);
        bulgarian = new Language("Bulgarian", "абвгдежзийклмнопрстуфхцчшщъьюя", imageIcon);

        dictionaryEN_BG = new SDictionary("English-Bulgarian", english, bulgarian, imageIcon, imageIcon);
        dictionaryBG_EN = new SDictionary("Bulgarian-English", bulgarian, english, imageIcon, imageIcon);
    }
}
