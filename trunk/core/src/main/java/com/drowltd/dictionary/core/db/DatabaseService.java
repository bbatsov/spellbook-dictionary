package com.drowltd.dictionary.core.db;

import com.drowltd.dictionary.core.exception.DictionaryDbLockedException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A thin layer of abstraction on top of the database, that provides the basic
 * database access services, like dictionary selection,
 * word translation and word approximation.
 *
 * @author Bozhidar Batsov
 * @since  0.1
 */
public class DatabaseService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseService.class);
    private static DatabaseService instance;
    private Connection connection;
    private String selectedDictionary;
    // simple caching mechanism to avoid db operations
    private Map<String, List<String>> dictionaryCache = new HashMap<String, List<String>>();

    private DatabaseService(String dictDbFile) throws DictionaryDbLockedException {
        LOGGER.info("dictionary database: " + dictDbFile.replace(".data.db", ""));

        String url = "jdbc:h2:" + dictDbFile.replace(".data.db", "");
        String user = "bozhidar";
        String password = "bozhidar";

        // by default use english-bulgarian dictionary
        selectedDictionary = "EN_BG";

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
    }

    /**
     * Bootstraps the database service.
     *
     * @param dictDbFile the dictionary database file
     *
     * @throws DictionaryDbLockedException if another process is already using the db file
     */
    public static void init(String dictDbFile) throws DictionaryDbLockedException {
        instance = new DatabaseService(dictDbFile);
    }

    public static DatabaseService getInstance() {
        return instance;
    }

    /**
     * Retrieves all words from a selected dictionary. The words from the different dictionaries
     * are cached for future invocations of the method.
     *
     * @return a list of all words in the selected dictionary
     */
    public List<String> getWordsFromSelectedDictionary() {
        LOGGER.info("Loading selected dictionary " + selectedDictionary);

        LOGGER.info("Checking dictionary cache for " + selectedDictionary);

        if (dictionaryCache.containsKey(selectedDictionary)) {
            LOGGER.info("Dictionary " + selectedDictionary + " loaded from cache");
            return dictionaryCache.get(selectedDictionary);
        }

        final List<String> words = new ArrayList<String>();

        try {
            PreparedStatement ps = connection.prepareStatement("SELECT word FROM " + selectedDictionary);

            final ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                final String word = rs.getString("WORD");
                words.add(word);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        LOGGER.info("Caching " + selectedDictionary + " for future use");
        dictionaryCache.put(selectedDictionary, words);

        return words;
    }

    /**
     * Retrieve the translation for a word. Since the word to be translated is assumed to be in the
     * selected dictionary an error will occur is the method is passed an non-existing word.
     *
     * @param word the word to be translated
     *
     * @return the translation of the word
     */
    public String getTranslation(String word) {
        if (word != null && !word.isEmpty()) {

            LOGGER.info("Getting translation for " + word);

            try {
                PreparedStatement ps = connection.prepareStatement("select translation from " + selectedDictionary + " where word='" + word.replaceAll("'", "''") + "'");

                final ResultSet resultSet = ps.executeQuery();

                // this method is only sensible for existing words
                if (resultSet.next()) {
                    return resultSet.getString("TRANSLATION");
                } else {
                    throw new IllegalArgumentException("Selected word for translation " + word + " is not present in the database");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * An auxilliary method to aid the exact search. It looks for the first word, that
     * starts with the current search key
     *
     * @param searchKey the current search key(the content of the search text field)
     * 
     * @return the first word, starting with the search key
     */
    public String getApproximation(String searchKey) {
        if (searchKey != null && !searchKey.isEmpty()) {

            LOGGER.info("Getting approximation for " + searchKey);

            try {
                PreparedStatement ps = connection.prepareStatement("select word from " + selectedDictionary + " where word like '" + searchKey.replaceAll("'", "''") + "%' order by word asc");

                final ResultSet resultSet = ps.executeQuery();

                if (resultSet.next()) {
                    return resultSet.getString("WORD");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }

        return null;
    }

    /**
     * Retrieves the name of the currently selected dictionary.
     *
     * @return the name of the currently selected dictionary
     */
    public String getSelectedDictionary() {
        return selectedDictionary;
    }

    /**
     * Changes the selected dictionary.
     *
     * @param selectedDictionary the name of the dictionary to be selected
     */
    public void setSelectedDictionary(String selectedDictionary) {
        this.selectedDictionary = selectedDictionary;
    }
}
