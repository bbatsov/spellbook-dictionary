package com.drowltd.dictionary.core.db;

import com.drowltd.dictionary.core.exam.Difficulty;
import com.drowltd.dictionary.core.exception.DictionaryDbLockedException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
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
    // simple caching mechanism to avoid db operations
    private Map<Dictionary, List<String>> dictionaryCache = new HashMap<Dictionary, List<String>>();
    private Map<Dictionary, Map<String, Integer>> spellCheckerCache = new HashMap<Dictionary, Map<String, Integer>>();
    private Map<Dictionary, Map<Difficulty, ArrayList<String>>> examCache = new HashMap<Dictionary, Map<Difficulty, ArrayList<String>>>();

    private DatabaseService(String dictDbFile) throws DictionaryDbLockedException {
        LOGGER.info("dictionary database: " + dictDbFile.replace(".data.db", ""));

        String url = "jdbc:h2:" + dictDbFile.replace(".data.db", "");
        String user = "bozhidar";
        String password = "bozhidar";

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
     * Bootstraps the database service. The method can be executed only once.
     *
     * @param dictDbFile the dictionary database file
     *
     * @throws DictionaryDbLockedException if another process is already using the db file
     */
    public static void init(String dictDbFile) throws DictionaryDbLockedException {
        if (instance == null) {
            instance = new DatabaseService(dictDbFile);
        } else {
            LOGGER.info("Database service is already initialized");
        }
    }

    public static DatabaseService getInstance() {
        return instance;
    }

    /**
     * Retrieves all words from a selected dictionary. The words from the different dictionaries
     * are cached for future invocations of the method.
     *
     * @param dictionary the target dictionary
     *
     * @return a list of all words in the selected dictionary
     */
    public List<String> getWordsFromDictionary(Dictionary dictionary) {
        LOGGER.info("Loading selected dictionary " + dictionary);

        LOGGER.info("Checking dictionary cache for " + dictionary);

        if (dictionaryCache.containsKey(dictionary)) {
            LOGGER.info("Dictionary " + dictionary + " loaded from cache");
            return dictionaryCache.get(dictionary);
        }

        final List<String> words = new ArrayList<String>();

        try {
            PreparedStatement ps = connection.prepareStatement("SELECT word FROM " + dictionary);

            final ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                final String word = rs.getString("WORD");
                words.add(word);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        LOGGER.info("Caching " + dictionary + " for future use");
        dictionaryCache.put(dictionary, words);

        return words;
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
    public String getTranslation(Dictionary dictionary, String word) {
        if (word != null && !word.isEmpty()) {

            LOGGER.info("Getting translation for " + word);

            try {
                PreparedStatement ps = connection.prepareStatement("select translation from " + dictionary + " where word='" + word.replaceAll("'", "''") + "'");

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
     * @param dictionary the source dictionary
     * @param searchKey the current search key(the content of the search text field)
     * 
     * @return the first word, starting with the search key
     */
    public String getApproximation(Dictionary dictionary, String searchKey) {
        if (searchKey != null && !searchKey.isEmpty()) {

            LOGGER.info("Getting approximation for " + searchKey);

            StringBuilder builder = new StringBuilder(searchKey);

            try {
                // we start looking for approximate matches of the full search key, but if we fail - we start looking
                // for shorter matches
                do {
                    PreparedStatement ps = connection.prepareStatement("select word from " + dictionary + " where word like '" + builder.toString().replaceAll("'", "''") + "%' order by word asc");

                    final ResultSet resultSet = ps.executeQuery();

                    if (resultSet.next()) {
                        return resultSet.getString("WORD");
                    }

                    builder.deleteCharAt(builder.length() - 1);
                } while (builder.length() > 0);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public Map<String, Integer> getRatings(Dictionary dictionary) {

        if (dictionary == null) {
            LOGGER.error("dictionary is null");
            throw new NullPointerException("dictionary is null");
        }

        if (spellCheckerCache.containsKey(dictionary)) {
            LOGGER.info("Word ratings for " + dictionary + " loaded from cache");
            return spellCheckerCache.get(dictionary);
        }

        Map<String, Integer> nWords = new HashMap<String, Integer>();

        try {
            PreparedStatement ps1 = connection.prepareStatement("SELECT word, rating FROM " + dictionary);
            LOGGER.info("SELECT WORD, RATING FROM " + dictionary);
            final ResultSet resultSet1 = ps1.executeQuery();

            while (resultSet1.next()) {
                nWords.put(resultSet1.getString("WORD"), resultSet1.getInt("RATING"));
            }

            PreparedStatement ps2 = connection.prepareStatement("SELECT word, rating FROM " + dictionary.getRatingsTable());
            LOGGER.info("SELECT WORD, RATING FROM " + dictionary.getRatingsTable());
            final ResultSet resultSet2 = ps2.executeQuery();

            while (resultSet2.next()) {
                nWords.put(resultSet2.getString("WORD"), resultSet2.getInt("RATING"));
            }

        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(DatabaseService.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (nWords.isEmpty()) {
            throw new IllegalStateException("ratings from db are not imported");
        }

        spellCheckerCache.put(dictionary, nWords);

        return nWords;
    }

    public void addMisspelled(String misspelled, Dictionary dictionary){
        if(misspelled == null || misspelled.isEmpty()){
            LOGGER.error("misspelled is null or empty");
            throw new IllegalArgumentException("misspelled is null or empty");
        }

        if(dictionary == null){
            LOGGER.error("dictionary is null");
            throw new NullPointerException("dictionary is null");
        }
        try {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO " + dictionary.getRatingsTable()+" (WORD, RATING) VALUES('"+misspelled+"',"+1+")");
            ps.execute();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }


    public ArrayList<String> getDifficultyWords(Dictionary dictionary, Difficulty difficulty) {

        if (dictionary == null) {
            LOGGER.error("dictionary is null");
            throw new NullPointerException("dictionary is null");
        }

        if (examCache.containsKey(dictionary)) {
            LOGGER.info("Word ratings for " + dictionary + " for difficulty " + difficulty + " loaded from cache");

            if (dictionary.equals(Dictionary.BG_EN)) {
                return examCache.get(dictionary).get(Difficulty.HARD);
            }

            return examCache.get(dictionary).get(difficulty);
        }

        ArrayList<String> easyWords = new ArrayList<String>();
        ArrayList<String> mediumWords = new ArrayList<String>();
        ArrayList<String> hardWords = new ArrayList<String>();
        Map<Difficulty, ArrayList<String>> difficultyWords = new HashMap<Difficulty, ArrayList<String>>();

        try {
            PreparedStatement ps = connection.prepareStatement("SELECT word, rating FROM " + dictionary);
            LOGGER.info("SELECT WORD, RATING FROM " + dictionary + " for difficulty " + difficulty);
            final ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {

                if (resultSet.getInt("RATING") > 30) {
                    easyWords.add(resultSet.getString("WORD"));
                } else if (resultSet.getInt("RATING") >= 10 && resultSet.getInt("RATING") <= 30) {
                    mediumWords.add(resultSet.getString("WORD"));
                } else {
                    hardWords.add(resultSet.getString("WORD"));
                }
            }
        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(DatabaseService.class.getName()).log(Level.SEVERE, null, ex);
        }

        difficultyWords.put(Difficulty.EASY, easyWords);
        difficultyWords.put(Difficulty.MEDIUM, mediumWords);
        difficultyWords.put(Difficulty.HARD, hardWords);

        LOGGER.info("Caching difficulty of " + dictionary + " for future use");
        examCache.put(dictionary, difficultyWords);

        if (difficulty == Difficulty.HARD || dictionary == Dictionary.BG_EN) {
            LOGGER.info("Returning hard words");
            return hardWords;
        }

        if (difficulty == Difficulty.EASY) {
            LOGGER.info("Returning easy words");
            return easyWords;
        }

        if (difficulty == Difficulty.MEDIUM) {
            LOGGER.info("Returning medium words");
            return mediumWords;
        }

        return null;
    }
}
