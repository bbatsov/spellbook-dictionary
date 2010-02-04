package com.drowltd.dictionary.core.db;

import com.drowltd.dictionary.core.exception.NoDictionariesAvailableException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
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

    private DictionaryConfig getConfig(SDictionary dictionary) {
        assert dictionary != null : "dictionary is null";
        assert !dictConfigMap.isEmpty() : "dictConfigMap is empty";
        assert dictConfigMap.containsKey(dictionary) : "dictConfigMap doesn't contains dictionary";

        return dictConfigMap.get(dictionary);
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
                language = new Language(rsLang.getString("NAME"), rsLang.getString("ALPHABET"), new ImageIcon(inputStreamToByteArr(rsLang.getBinaryStream("FLAG_16"))));
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

    public List<SDictionary> getLoadedDictionaries() {
        return new ArrayList<SDictionary>(dictConfigMap.keySet());
    }

    public Map<String, Integer> getWordsFromDictionary(SDictionary dictionary) throws SQLException {
        if (dictionary == null) {
            throw new IllegalArgumentException("dictionary is null");
        }
        final DictionaryConfig config = getConfig(dictionary);

        final PreparedStatement ps = connection.prepareStatement("SELECT WORD, RATING FROM " + config.getTranslationTable());

        final ResultSet rs = ps.executeQuery();

        Map<String, Integer> wordMap = new HashMap<String, Integer>();

        while (rs.next()) {
            wordMap.put(rs.getString(1), rs.getInt(2));
        }

        if (wordMap.isEmpty()) {
            throw new IllegalStateException("No words loaded from db");
        }

        return wordMap;
    }

    public String getTranslation(SDictionary dictionary, String word) throws SQLException {

        if(dictionary == null){
            throw new IllegalArgumentException("dictionary is null");
        }

        if(word == null || word.isEmpty()){
            throw new IllegalArgumentException("word is null or empty");
        }
        final String translationTable = getConfig(dictionary).getTranslationTable();

        final PreparedStatement ps = connection.prepareStatement("SELECT TRANSLATION FROM " + translationTable);
        final ResultSet rs = ps.executeQuery();

        rs.next();
        return rs.getString(1);

       
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
