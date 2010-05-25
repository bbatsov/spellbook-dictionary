package com.drowltd.spellbook.core.spellcheck;

import com.drowltd.spellbook.core.model.Language;

import java.util.Map;

/**
 * @author ikkari
 *         Date: May 25, 2010
 *         Time: 8:56:35 PM
 */
public class DbSpellChecker implements SpellChecker{
    
    @Override
    public boolean checkWord(String word) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map<String, Integer> correct(String word) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Language getLanguage() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
