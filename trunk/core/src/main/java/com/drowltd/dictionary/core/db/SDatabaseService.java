
package com.drowltd.dictionary.core.db;

import com.drowltd.dictionary.core.exam.Difficulty;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author iivalchev
 */
public class SDatabaseService {

    public void addMisspelled(SDictionary dictionary, String misspelled){
    }

    /**
     * Adds a new word into a dictionary.
     *
     * @param word to be added
     * @param translation of the word
     * @param dictionary in which the word will be added
     * @return true if the word was added, false if it already existed in the
     * dictionary
     */
    public boolean addWord(SDictionary dictionary, String word, String translation){
        return false;
    }


    public List<String> getDifficultyWords(SDictionary dictionary, Difficulty difficulty, int quantity){
        return Collections.emptyList();
    }

    public Map<String, Integer> getRatings(Language language){
        return Collections.emptyMap();
    }

    /**
     * Retrieve the translation for a word. Since the word to be translated is assumed to be in the
     * selected dictionary an error will occur is the method is passed an non-existing word.
     *
     * @param dictionary the source dictionary
     * @param word the word to be translated
     *
     * @return the translation of the word
     */
    public String getTranslation(SDictionary dictionary, String word){
        return "";
    }

    /**
     * Retrieves all words from a selected dictionary. The words from the different dictionaries
     * are cached for future invocations of the method.
     *
     * @param dictionary the target dictionary
     *
     * @return a list of all words in the selected dictionary
     */
    public List<String> getWordsFromDictionary(SDictionary dictionary){
        return Collections.emptyList();
    }

    /**
     * Updates a word into a dictionary.
     *
     * @param word
     * @param translation
     * @param dictionary
     */
    public void updateTranslation(SDictionary dictionary, String word, String translation){
    }

    public void updateWord(SDictionary dictionary, String oldWord, String newWord){
    }

    public List<SDictionary> getAvailableDictionaries(){
        return Collections.emptyList();
    }

    public SDictionary getDictionary(Language languageFrom, Language languageTo){
        return null;
    }

}
