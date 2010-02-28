package com.drowltd.spellbook.core.db;

import com.drowltd.spellbook.core.exam.Difficulty;
import com.drowltd.spellbook.core.exception.DictionaryDbLockedException;
import com.drowltd.spellbook.core.exception.NoDictionariesAvailableException;
import com.drowltd.spellbook.core.exception.SDatabaseServiceException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author iivalchev
 */
public class SDatabaseService {

    private final DictionaryService dictionaryService;
    

    public static SDatabaseService getLocalDatabaseService(String dictDbFile) throws DictionaryDbLockedException, NoDictionariesAvailableException {
        if (dictDbFile == null || dictDbFile.isEmpty()) {
            throw new IllegalArgumentException("dictDbFile is null or empty");
        }

        String url = "jdbc:h2:" + dictDbFile.replace(".data.db", "");
        String user = "bozhidar";
        String password = "bozhidar";
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            if (e.getMessage() != null) {
                if (e.getMessage().contains("Database may be already in use: Locked by another process.")) {
                    throw new DictionaryDbLockedException();
                }
            }
            e.printStackTrace();
        }

        return new SDatabaseService(connection);
    }

    public static SDatabaseService getRemoteDatabaseService() {
        return null;
    }

    private SDatabaseService(Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("connectionis null");
        }

        try {
            dictionaryService = new DictionaryService(connection);
        } catch (SQLException ex) {
            throw new IllegalStateException(ex.getMessage());
        }
    }

    public void setCacheSize(int size){
       dictionaryService.setCacheSize(size);
    }

    public void addMisspelled(SDictionary dictionary, String misspelled) {
        try {
            dictionaryService.addMisspelled(dictionary, misspelled);
        } catch (SQLException ex) {
            throw new SDatabaseServiceException(misspelled + " could not be added to the database.");
        }
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
    public boolean addWord(SDictionary dictionary, String word, String translation)  {
        return addWord(dictionary, word, translation, 0);
    }

    public boolean addWord(SDictionary dictionary, String word, String translation, int rating)  {
        boolean added = false;
        try {
            added = dictionaryService.addWord(dictionary, word, translation, rating);
        } catch (SQLException ex) {
            throw new SDatabaseServiceException(word + " could not be added to the database.");
        }

        return added;
    }

    public List<String> getDifficultyWords(SDictionary dictionary, Difficulty difficulty)  {
        List<String> words = null;

        try {
            words = dictionaryService.getDifficultyWords(dictionary, difficulty);
        } catch (SQLException ex) {
            throw new SDatabaseServiceException("Words could not be fetched from database.");
        }

        return words;
    }

    public Map<String, Integer> getRatings(Language language)  {
        Map<String, Integer> ratings = null;
        try {
            ratings = dictionaryService.getRatings(language);
        } catch (SQLException ex) {
            throw new SDatabaseServiceException("Ratings could not be fetched from database.");
        }

        return ratings;
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
    public String getTranslation(SDictionary dictionary, String word)  {
        String translation = null;
        try {
            translation = dictionaryService.getTranslation(dictionary, word);
        } catch (SQLException ex) {
            throw new SDatabaseServiceException("Translation could not be fetched from database.");
        }
        return translation;
    }

    /**
     * Retrieves all words from a selected dictionary. The words from the different dictionaries
     * are cached for future invocations of the method.
     *
     * @param dictionary the target dictionary
     *
     * @return a list of all words in the selected dictionary
     */
    public List<String> getWordsFromDictionary(SDictionary dictionary)  {
        return new ArrayList<String>(getWordsMapFromDictionary(dictionary).keySet());
    }

    public Map<String, Integer> getWordsMapFromDictionary(SDictionary dictionary)  {
        Map<String, Integer> words = null;
        try {
            words = dictionaryService.getWordsFromDictionary(dictionary);
        } catch (SQLException ex) {
            throw new SDatabaseServiceException("Words could not be fetched from database.");
        }
        return words;
    }

    /**
     * Updates a word into a dictionary.
     *
     * @param word
     * @param translation
     * @param dictionary
     */
    public void updateTranslation(SDictionary dictionary, String word, String translation)  {
        try {
            dictionaryService.updateTranslation(dictionary, word, translation);
        } catch (SQLException ex) {
            throw new SDatabaseServiceException("Translation could not be in database.");
        }
    }

    public void updateWord(SDictionary dictionary, String oldWord, String newWord)  {
        try {
            dictionaryService.updateWord(dictionary, oldWord, newWord);
        } catch (SQLException ex) {
            throw new SDatabaseServiceException("Words could not be updated in database.");
        }
    }

    public List<SDictionary> getAvailableDictionaries() {
        return dictionaryService.getAvailableDictionaries();
    }

    public SDictionary getDictionary(Language languageFrom, Language languageTo) {
        return dictionaryService.getDictionary(languageFrom, languageTo);
    }

    public List<Language> getLanguagesTo(Language language) {
        return dictionaryService.getLanguagesTo(language);
    }

    public String getApproximation(SDictionary dictionary, String searchKey){
        return dictionaryService.getApproximation(dictionary, searchKey);
    }
}
