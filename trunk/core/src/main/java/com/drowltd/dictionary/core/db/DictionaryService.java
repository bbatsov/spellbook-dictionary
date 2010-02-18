package com.drowltd.dictionary.core.db;

import com.drowltd.dictionary.core.exam.Difficulty;
import com.drowltd.dictionary.core.exception.NoDictionariesAvailableException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author iivalchev
 */
public class DictionaryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DictionaryService.class);
    private static final String schemaTable = "DICTIONARY_SCHEMA";
    private static final String languagesTable = "LANGUAGES";
    private final Connection connection;
    private final Map<SDictionary, DictionaryConfig> dictConfigMap;

    public DictionaryService(Connection connection) throws SQLException, NoDictionariesAvailableException {
        if (connection == null) {
            throw new IllegalArgumentException("connection is null");
        }
        this.connection = connection;

        dictConfigMap = new HashMap<SDictionary, DictionaryConfig>();
        populate();
    }

    private void populate() throws SQLException, NoDictionariesAvailableException {
        final Map<Integer, Language> languageMap = new HashMap<Integer, Language>();

        populateLanguages(languageMap);
        populateDictionaries(languageMap);
    }

    private void populateLanguages(final Map<Integer, Language> languageMap) throws SQLException {
        assert languageMap != null : "languageMap is null";

        PreparedStatement psLang = connection.prepareStatement("SELECT * FROM " + languagesTable);
        final ResultSet rsLang = psLang.executeQuery();
        while (rsLang.next()) {
            final Language language;

            try {
                final ImageIcon imageIcon = new ImageIcon(inputStreamToByteArr(rsLang.getBinaryStream("FLAG_16")));
                language = new Language(rsLang.getString("NAME"), rsLang.getString("ALPHABET"), imageIcon);
            } catch (IOException e) {
                throw new SQLException("Can't load Language BLOB images", e);
            }

            LOGGER.info("Language created: " + language.getName() + " " + language.getAlphabet());

            languageMap.put(rsLang.getInt("ID"), language);
        }
    }

    private void populateDictionaries(final Map<Integer, Language> languageMap) throws SQLException, NoDictionariesAvailableException {
        assert languageMap != null : "languageMap is null";

        PreparedStatement psDict = connection.prepareStatement("SELECT * FROM " + schemaTable);
        final ResultSet rsDict = psDict.executeQuery();
        while (rsDict.next()) {

            final Language languageFrom = languageMap.get(rsDict.getInt("LANGUAGE_FROM"));
            final Language languageTo = languageMap.get(rsDict.getInt("LANGUAGE_TO"));

            assert languageFrom != null : "languageMap doesn't have LANGUAGE_FROM";
            assert languageTo != null : "languageMap doesn't have LANGUAGE_TO";

            String name = languageFrom.getName() + "-" + languageTo.getName();

            ImageIcon flag16 = null;
            ImageIcon flag24 = null;

            try {
                flag16 = new ImageIcon(inputStreamToByteArr(rsDict.getBinaryStream("FLAG_16")));
                flag24 = new ImageIcon(inputStreamToByteArr(rsDict.getBinaryStream("FLAG_24")));
            } catch (IOException e) {
                throw new SQLException("Can't load Dictionary BLOB images", e);
            }

            assert flag16 != null && flag24 != null : "flags16 or flag24 are null";

            SDictionary dictionary = new SDictionary(name, languageFrom, languageTo, flag16, flag24);
            LOGGER.info("SDictionary created: " + dictionary.getName());

            DictionaryConfig config = new DictionaryConfig(dictionary, rsDict.getString("TRANSLATIONS_TABLE"), rsDict.getString("RATINGS_TABLE"));
            LOGGER.info("DictionaryConfig created: " + config.getTranslationTable() + " " + config.getRatingsTable());

            dictConfigMap.put(dictionary, config);
        }
        if (dictConfigMap.isEmpty()) {
            throw new NoDictionariesAvailableException();
        }

    }

    //Tests only BEGIN
    protected Map<SDictionary, DictionaryConfig> getDictConfigMap() {
        return new HashMap<SDictionary, DictionaryConfig>(dictConfigMap);
    }
    //Tests only END

    public List<SDictionary> getAvailableDictionaries() {
        return new ArrayList<SDictionary>(dictConfigMap.keySet());
    }

    public Map<String, Integer> getWordsFromDictionary(SDictionary dictionary) throws SQLException {
        if (dictionary == null) {
            throw new IllegalArgumentException("dictionary is null");
        }

        final PreparedStatement ps = connection.prepareStatement("SELECT WORD, RATING FROM " + getTranslationTable(dictionary));

        final ResultSet rs = ps.executeQuery();

        Map<String, Integer> wordMap = new LinkedHashMap<String, Integer>();

        while (rs.next()) {
            wordMap.put(rs.getString(1), rs.getInt(2));
        }

        return wordMap;
    }

    public String getTranslation(SDictionary dictionary, String word) throws SQLException {

        if (dictionary == null) {
            throw new IllegalArgumentException("dictionary is null");
        }

        if (word == null || word.isEmpty()) {
            throw new IllegalArgumentException("word is null or empty");
        }

        final ResultSet rs = connection.prepareStatement("SELECT TRANSLATION FROM " + getTranslationTable(dictionary)).executeQuery();

        rs.next();
        final String translation = rs.getString(1);
        return translation == null ? "" : translation;
    }

    public boolean addWord(SDictionary dictionary, String word, String translation) throws SQLException {
        return addWord(dictionary, word, translation, 1);
    }

    public boolean addWord(SDictionary dictionary, String word, String translation, int rating) throws SQLException {
        if (dictionary == null) {
            throw new IllegalArgumentException("dictionary is null");
        }

        if (word == null || word.isEmpty()) {
            throw new IllegalArgumentException("word is null or empty");
        }

        if (translation == null || translation.isEmpty()) {
            throw new IllegalArgumentException("translation is null or empty");
        }

        if (rating < 1) {
            throw new IllegalArgumentException("rating < 1");
        }

        final PreparedStatement ps = connection.prepareStatement("INSERT INTO " + getTranslationTable(dictionary)
                + "(WORD, TRANSLATION, RATING) VALUES('" + word + "','" + translation + "','" + rating + "')");

        return ps.executeUpdate() > 0;
    }

    public Map<String, Integer> getRatings(Language language) throws SQLException {
        if (language == null) {
            throw new IllegalArgumentException("language is null");
        }
        final DictionaryConfig config = getConfig(language);

        Map<String, Integer> ratingsMap = new HashMap<String, Integer>();

        final String translationTable = config.getTranslationTable();
        final ResultSet rs0 = connection.prepareStatement("SELECT WORD, RATING FROM " + translationTable).executeQuery();

        while (rs0.next()) {
            ratingsMap.put(rs0.getString(1), rs0.getInt(2));
        }

        final String ratingsTable = config.getRatingsTable();
        final ResultSet rs1 = connection.prepareStatement("SELECT WORD, RATING FROM " + ratingsTable).executeQuery();

        while (rs1.next()) {
            ratingsMap.put(rs1.getString(1), rs1.getInt(2));
        }

        return ratingsMap;
    }

    public void updateTranslation(SDictionary dictionary, String word, String translation) throws SQLException {
        if (dictionary == null) {
            throw new IllegalArgumentException("dictionary is null");
        }

        if (word == null || word.isEmpty()) {
            throw new IllegalArgumentException("word is null or empty");
        }

        if (translation == null || translation.isEmpty()) {
            throw new IllegalArgumentException("translation is null or empty");
        }

        connection.prepareStatement("UPDATE " + getTranslationTable(dictionary) + " SET TRANSLATION = \'" + translation + "\' "
                + "WHERE WORD = \'" + word + "\'").executeUpdate();

    }

    public void updateWord(SDictionary dictionary, String oldWord, String newWord) throws SQLException {
        if (dictionary == null) {
            throw new IllegalArgumentException("dictionary is null");
        }

        if (oldWord == null || oldWord.isEmpty()) {
            throw new IllegalArgumentException("oldWord is null or empty");
        }

        if (newWord == null || newWord.isEmpty()) {
            throw new IllegalArgumentException("newWord is null or empty");
        }

        connection.prepareStatement("UPDATE " + getTranslationTable(dictionary) + " SET WORD = \'" + newWord + "\' "
                + "WHERE WORD = \'" + oldWord + "\'").executeUpdate();


    }

    public SDictionary getDictionary(Language languageFrom, Language languageTo) {
        if (languageFrom == null) {
            throw new IllegalArgumentException("languageFrom is null");
        }
        if (languageTo == null) {
            throw new IllegalArgumentException("languageTo is null");
        }

        for (SDictionary dict : dictConfigMap.keySet()) {
            if (dict.getLanguageFrom().equals(languageFrom) && dict.getLanguageTo().equals(languageTo)) {
                return dict;
            }
        }

        throw new IllegalStateException("No dictionary found");
    }

    public List<Language> getLanguagesTo(Language languageFrom) {

        if (languageFrom == null) {
            throw new IllegalArgumentException("languageFrom is null");
        }

        List<Language> languageList = new ArrayList<Language>(dictConfigMap.size());

        for (SDictionary dict : dictConfigMap.keySet()) {
            if (dict.getLanguageFrom().equals(languageFrom)) {
                languageList.add(dict.getLanguageTo());
            }
        }

        return languageList;
    }

    public List<String> getDifficultyWords(SDictionary dictionary, Difficulty difficulty) throws SQLException {

        if (dictionary == null) {
            throw new IllegalArgumentException("dictionary is null");
        }

        if (difficulty == null) {
            throw new IllegalArgumentException("difficulty is null");
        }

        final ResultSet rs0 = connection.prepareStatement("SELECT COUNT(1) FROM EN_BG WHERE RATING > "
                + difficulty.getLow() + " AND RATING <= " + difficulty.getHigh()).executeQuery();

        rs0.next();
        int rows = 0;
        if ((rows = rs0.getInt(1)) == 0) {
            return Collections.emptyList();
        }

        final ResultSet rs = connection.prepareStatement("SELECT WORD FROM " + getTranslationTable(dictionary)
                + " WHERE RATING > " + difficulty.getLow() + " AND RATING <= " + difficulty.getHigh()).executeQuery();


        List<String> words = new ArrayList<String>(rows);
        while (rs.next()) {
            words.add(rs.getString(1));
        }

        return words;
    }

    public boolean addMisspelled(SDictionary dictionary, String misspelled) throws SQLException {
        if (dictionary == null) {
            throw new IllegalArgumentException("dictionary is null");
        }

        if (misspelled == null || misspelled.isEmpty()) {
            throw new IllegalArgumentException("misspelled is null or empty");
        }

        return connection.prepareStatement("INSERT INTO " + getRatingsTable(dictionary)
                + " (WORD, RATING) VALUES('" + misspelled + "',1)").executeUpdate() > 0;
    }

    private DictionaryConfig getConfig(SDictionary dictionary) {
        assert dictionary != null : "dictionary is null";
        assert !dictConfigMap.isEmpty() : "dictConfigMap is empty";
        assert dictConfigMap.containsKey(dictionary) : "dictConfigMap doesn't contains dictionary";

        return dictConfigMap.get(dictionary);
    }

    private DictionaryConfig getConfig(Language language) {
        assert language != null : "language is null";
        assert !dictConfigMap.isEmpty() : "dictConfigMap is empty";

        for (SDictionary dict : dictConfigMap.keySet()) {
            if (dict.getLanguageFrom().equals(language)) {
                return dictConfigMap.get(dict);
            }
        }

        throw new IllegalStateException("No DictionaryConfig for " + language.getName());
    }

    private String getTranslationTable(SDictionary dictionary) {
        assert dictionary != null : "dictionary is null";
        assert !dictConfigMap.isEmpty() : "dictConfigMap is empty";
        assert dictConfigMap.containsKey(dictionary) : "dictConfigMap doesn't contains dictionary";

        return dictConfigMap.get(dictionary).getTranslationTable();

    }

    private String getRatingsTable(SDictionary dictionary) {
        assert dictionary != null : "dictionary is null";
        assert !dictConfigMap.isEmpty() : "dictConfigMap is empty";
        assert dictConfigMap.containsKey(dictionary) : "dictConfigMap doesn't contains dictionary";

        return dictConfigMap.get(dictionary).getRatingsTable();

    }

    private byte[] inputStreamToByteArr(InputStream stream) throws IOException {
        assert stream != null : "stream is null";

        List<Integer> bytesList = new LinkedList<Integer>();
        try {
            int next = -1;
            for (;;) {
                next = stream.read();
                if (next == -1) {
                    break;
                }
                bytesList.add(next);
            }
        } finally {
            stream.close();
        }

        byte[] bytes = new byte[bytesList.size()];
        int index = 0;
        for (Integer i : bytesList) {
            bytes[index] = i.byteValue();
            ++index;
        }
        return bytes;
    }

    protected static class DictionaryConfig {

        private final SDictionary dictionary;
        private final String translationTable;
        private final String ratingsTable;

        public DictionaryConfig(SDictionary dictionary, String translationTable, String ratingsTable) {
            if (dictionary == null) {
                throw new IllegalArgumentException("dictionary is null");
            }

            if (translationTable == null || translationTable.isEmpty()) {
                throw new IllegalArgumentException("translationTable is null or empty");
            }

            if (ratingsTable == null || ratingsTable.isEmpty()) {
                throw new IllegalArgumentException("ratingsTable is null or empty");
            }

            this.dictionary = dictionary;
            this.translationTable = translationTable;
            this.ratingsTable = ratingsTable;
        }

        public SDictionary getDictionary() {
            return dictionary;
        }

        public String getRatingsTable() {
            return ratingsTable;
        }

        public String getTranslationTable() {
            return translationTable;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }

            if (o == this) {
                return true;
            }

            if (o.getClass() != this.getClass()) {
                return false;
            }

            DictionaryConfig other = (DictionaryConfig) o;
            if (dictionary.equals(other.dictionary)
                    && ratingsTable.equals(other.ratingsTable)
                    && translationTable.equals(other.translationTable)) {
                return true;
            }

            return false;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 59 * hash + (this.dictionary != null ? this.dictionary.hashCode() : 0);
            hash = 59 * hash + (this.translationTable != null ? this.translationTable.hashCode() : 0);
            hash = 59 * hash + (this.ratingsTable != null ? this.ratingsTable.hashCode() : 0);
            return hash;
        }
    }
}
