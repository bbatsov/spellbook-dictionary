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
 * User: bozhidar
 * Date: Sep 6, 2009
 * Time: 4:43:46 PM
 */
public class DictDb {
    private static final Logger LOGGER = LoggerFactory.getLogger(DictDb.class);

    private static DictDb instance;

    private Connection connection;

    private String selectedDictionary;

    // simple caching mechanism to avoid db operations
    private Map<String, List<String>> dictionaryCache = new HashMap<String, List<String>>();

    private DictDb(String dictDbFile) throws DictionaryDbLockedException {
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

    public static void init(String dictDbFile) throws DictionaryDbLockedException {
        instance = new DictDb(dictDbFile);
    }

    public static DictDb getInstance() {
        return instance;
    }

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

    public String getTranslation(String word) {
        LOGGER.info("Getting translation for " + word);

        try {
            PreparedStatement ps = connection.prepareStatement("select translation from " + selectedDictionary + " where word='" + word.replaceAll("'", "''") + "'");

            final ResultSet resultSet = ps.executeQuery();

            resultSet.next();
            return resultSet.getString("TRANSLATION");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getSelectedDictionary() {
        return selectedDictionary;
    }

    public void setSelectedDictionary(String selectedDictionary) {
        this.selectedDictionary = selectedDictionary;
    }
}
