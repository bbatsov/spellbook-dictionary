package com.drowltd.spellbook.core.spellcheck;

import com.drowltd.spellbook.core.model.Language;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author ikkari
 *         Date: May 25, 2010
 *         Time: 8:43:49 PM
 */
public interface SpellChecker {
    
    boolean checkWord(String word);

    Map<String, Integer> correct(String word);

    Language getLanguage();

}
