package com.drowltd.spellbook.core.spellcheck;

import com.drowltd.spellbook.core.model.Language;

import java.util.List;
import java.util.Map;

/**
 * @author ikkari
 *         Date: May 25, 2010
 *         Time: 8:43:49 PM
 */
public interface SpellChecker {
    
    boolean misspelled(String word);

    List<String> correct(String word);

    Language getLanguage();

}
