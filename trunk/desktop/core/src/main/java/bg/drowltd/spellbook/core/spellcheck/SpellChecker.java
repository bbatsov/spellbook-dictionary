package bg.drowltd.spellbook.core.spellcheck;

import bg.drowltd.spellbook.core.model.Language;

import java.util.List;

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
