package com.drowltd.dictionary.core.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
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

    public DictionaryService(Connection connection) throws SQLException {
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

    private void populate() throws SQLException {
        final Map<Integer, Language> languageMap = new HashMap<Integer, Language>();

        PreparedStatement psLang = connection.prepareStatement("SELECT * FROM "+languagesTable);
        final ResultSet rsLang = psLang.executeQuery();
        while(rsLang.next()){
            final Language language = new Language(rsLang.getString("NAME"), rsLang.getString("ALPHABET"));
            LOGGER.info("Language created: "+language.getName()+" "+language.getAlphabet());
            
            languageMap.put(rsLang.getInt("ID"), language);
        }

        PreparedStatement psDict = connection.prepareStatement("SELECT * FROM "+schemaTable);
        final ResultSet rsDict = psDict.executeQuery();
        while(rsDict.next()){

            final Language languageFrom = languageMap.get(rsDict.getInt("LANGUAGE_FROM"));
            final Language languageTo = languageMap.get(rsDict.getInt("LANGUAGE_TO"));

            assert languageFrom != null: "languageMap doesn't have LANGUAGE_FROM";
            assert languageTo != null: "languageMap doesn't have LANGUAGE_TO";

            String name = languageFrom.getName()+"-"+languageTo.getName();
            SDictionary dictionary = new SDictionary(name, languageFrom, languageTo);
            LOGGER.info("SDictionary created: "+dictionary.getName());

            DictionaryConfig config = new DictionaryConfig(dictionary, rsDict.getString("TRANSLATIONS_TABLE"), rsDict.getString("RATINGS_TABLE"));
            LOGGER.info("DictionaryConfig created: "+config.getTranslationTable()+" "+config.getRatingsTable());

            dictConfigMap.put(dictionary, config);
        }

    }

    //Tests only BEGIN
    protected Map<SDictionary, DictionaryConfig> getDictConfigMap() {
        return new HashMap<SDictionary, DictionaryConfig>(dictConfigMap);
    }
    //Tests only END

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
