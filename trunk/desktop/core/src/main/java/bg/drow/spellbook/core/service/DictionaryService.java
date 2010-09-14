package bg.drow.spellbook.core.service;

import bg.drow.spellbook.core.model.Dictionary;
import bg.drow.spellbook.core.model.DictionaryEntry;
import bg.drow.spellbook.core.model.Language;
import bg.drow.spellbook.core.model.RankEntry;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides Spellbook's basic dictionary related functionality like looking for dictionaries, words, adding/updating/
 * deleting dictionary entries.
 *
 * @author <a href="mailto:bozhidar@drow.bg">Bozhidar Batsov</a>
 */
public class DictionaryService extends AbstractPersistenceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DictionaryService.class);

    private static final DictionaryService INSTANCE = new DictionaryService();

    private static final Map<String, List<String>> DICTIONARY_CACHE = Maps.newHashMap();

    public DictionaryService() {
        super();
    }

    /**
     * Obtains the service single instance.
     *
     * @return service instance
     */
    public static DictionaryService getInstance() {
        return INSTANCE;
    }

    /**
     * Retrieve a list of all available dictionaries.
     *
     * @return a list of available dictionaries, empty list if none are available
     */
    public List<Dictionary> getDictionaries() {
        List<Dictionary> result = Lists.newArrayList();

        try {
            PreparedStatement ps = dbConnection.prepareStatement("select * from DICTIONARIES");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Dictionary dictionary = new Dictionary(rs);

                result.add(dictionary);
            }

            return result;
        } catch (SQLException e) {
            e.printStackTrace();

            System.exit(-1);
        }

        return result;
    }

    /**
     * Retrieves a dictionary by its name.
     *
     * @param dictionaryName the name of the dictionary we wish to obtain
     * @return the dictionary corresponding to the name
     */
    public Dictionary getDictionary(String dictionaryName) {
        List<Dictionary> dictionaries = getDictionaries();

        if (dictionaries.size() < 1) {
            throw new IllegalStateException("No dictionaries!");
        }

        for (Dictionary dictionary : dictionaries) {
            if (dictionary.getName().equals(dictionaryName)) {
                return dictionary;
            }
        }

        return null;
    }

    /**
     * Retrieves all words from the target dictionary. The words are cached for subsequent
     * invocations of the method
     *
     * @param d the target dictionary
     * @return a list of the words in the dictionary
     */
    public List<String> getWordsFromDictionary(Dictionary d) {
        if (!DICTIONARY_CACHE.containsKey(d.getName())) {
            LOGGER.info("Caching dictionary " + d.getName());

            List<String> words = Lists.newArrayList();

            try {
                PreparedStatement ps = dbConnection.prepareStatement("select word from Dictionary_Entries "
                                + "where dictionary_id = " + d.getId() + " order by LOWER(word) asc");

                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    words.add(rs.getString("WORD"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            DICTIONARY_CACHE.put(d.getName(), words);
        } else {
            LOGGER.info("Loading from cache dictionary " + d.getName());
        }

        return DICTIONARY_CACHE.get(d.getName());
    }

    /**
     * Retrieves the translation of a word from the specified dictionary.
     *
     * @param word the target word
     * @param d    the target dictionary
     * @return the word's translation
     */
    public String getTranslation(String word, Dictionary d) {
        try {
            PreparedStatement ps = dbConnection.prepareStatement("SELECT word_translation from DICTIONARY_ENTRIES where word='" + word + "' and dictionary_id=" + d.getId());

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("word_TRANSLATION");
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();

            return null;
        }
    }

    /**
     * Adds a new word to a dictionary
     *
     * @param word        the word to add
     * @param translation the word's translation
     * @param d           the dictionary in which the word will be added
     */
    public void addWord(String word, String translation, Dictionary d) {
        if (word == null || word.trim().isEmpty()) {
            LOGGER.error("word == null || word.isEmpty()");
            throw new IllegalArgumentException("word == null || word.isEmpty()");
        }

        if (translation == null || translation.isEmpty()) {
            LOGGER.error("translation == null || translation.isEmpty()");
            throw new IllegalArgumentException("word == null || word.isEmpty()");
        }
        if (d == null) {
            LOGGER.error("d == null");
            throw new IllegalArgumentException("d == null");
        }

        if (containsWord(word, d)) {
            LOGGER.warn("word already exists: " + word);
            return;
        }

        final DictionaryEntry de = new DictionaryEntry();
        de.setDictionary(d);
        de.setWord(word);
        de.setTranslation(translation);
        de.setUpdatedByUser(true);

        RankEntry re = new RankEntry();
        re.setWord(word);
        re.setRank(1);
        re.setLanguage(d.getFromLanguage());

        de.persist();
        re.persist();
    }

    /**
     * Updates a dictionary entry. Only its translation can be updated.
     *
     * @param word        the word definition to update(needed to find the entry to update)
     * @param translation the new translation
     * @param d           the dictionary containing the word
     */
    public void updateWord(String word, String translation, Dictionary d) {
        if (word == null || word.trim().isEmpty()) {
            LOGGER.error("word == null || word.isEmpty()");
            throw new IllegalArgumentException("word == null || word.isEmpty()");
        }

        if (translation == null || translation.trim().isEmpty()) {
            LOGGER.error("translation == null || translation.isEmpty()");
            throw new IllegalArgumentException("word == null || word.isEmpty()");
        }
        if (d == null) {
            LOGGER.error("d == null");
            throw new IllegalArgumentException("d == null");
        }

        if (!containsWord(word, d)) {
            return;
        }

        //DictionaryEntry de = EM.createQuery("select de from DictionaryEntry de "
        //        + "where de.dictionary = :dictionary and de.word = :word", DictionaryEntry.class).setParameter("dictionary", d).setParameter("word", word).getSingleResult();

        //de.setTranslation(translation);

        //update
    }

    /**
     * Deletes a word from the specified dictionary.
     *
     * @param word       word to delete
     * @param dictionary the dictionary to remove the word from
     */
    public void deleteWord(String word, Dictionary dictionary) {
        try {
            PreparedStatement ps = dbConnection.prepareStatement("DELETE FROM ? WHERE word=? and dictionary_id=?");

            ps.setString(1, DictionaryEntry.TABLE_NAME);
            ps.setString(2, word);
            ps.setLong(3, dictionary.getId());
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    /**
     * Checks whether a dictionary contains a word.
     *
     * @param word the word for which to check
     * @param d    the dictionary in which to check
     * @return true if the word is present, false otherwise
     */
    public boolean containsWord(String word, Dictionary d) {
        if (d == null) {
            LOGGER.error("d == null");
            throw new IllegalArgumentException("d == null");
        }

        try {
            PreparedStatement ps = dbConnection.prepareStatement("SELECT * from DICTIONARY_ENTRIES WHERE word = '" + word + "' and dictionary_id = " + d.getId());

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();

            return false;
        }
    }

    public Map<String, Integer> getRatings(Language language) {
        if (language == null) {
            LOGGER.error("language == null");
            throw new IllegalArgumentException("language == null");
        }

//        final List<RankEntry> ratingslist = EM.createQuery("select re from RankEntry re "
//                + " where re.language = :language", RankEntry.class).setParameter("language", language).getResultList();

        Map<String, Integer> ratingsMap = new HashMap<String, Integer>();
//        for (RankEntry re : ratingslist) {
//            ratingsMap.put(re.getWord(), re.getRank());
//        }

        return ratingsMap;
    }

    /**
     * Retrieves a dictionary by its from and to languages.
     *
     * @param languageFrom from language
     * @param languageTo   to language
     * @return the dictionary that matches the languages
     */
    public Dictionary getDictionary(Language languageFrom, Language languageTo) {
        if (languageFrom == null || languageTo == null) {
            LOGGER.error("languageFrom == null || languageTo == null");
            throw new IllegalArgumentException("languageFrom == null || languageTo == null");
        }

        List<Dictionary> dictionaries = getDictionaries();

        for (Dictionary dictionary : dictionaries) {
            if (dictionary.getFromLanguage().equals(languageFrom) && dictionary.getToLanguage().equals(languageTo)) {
                return dictionary;
            }
        }

        return null;
    }

    public void addRankEntry(String word, Language language) {
        if (word == null || word.isEmpty()) {
            LOGGER.error("word == null || word.isEmpty()");
            throw new IllegalArgumentException("word == null || word.isEmpty()");
        }

        if (language == null) {
            LOGGER.error("language is null");
            throw new IllegalArgumentException("language is null");
        }

        RankEntry re = new RankEntry();
        re.setLanguage(language);
        re.setWord(word);
        re.setRank(1);

        re.persist();
    }

    public void addRankEntry(String word, Language language, int rank) {
        if (word == null || word.isEmpty()) {
            LOGGER.error("word == null || word.isEmpty()");
            throw new IllegalArgumentException("word == null || word.isEmpty()");
        }

        if (language == null) {
            LOGGER.error("language is null");
            throw new IllegalArgumentException("language is null");
        }

        if (rank < 0) {
            LOGGER.error("rank < 0");
            throw new IllegalArgumentException("rank < 0");
        }
        RankEntry re = null;
        try {
            //re = EM.createQuery("select re from RankEntry re where re.word = :word and re.language = :language", RankEntry.class).setParameter("word", word).setParameter("language", language).getSingleResult();
        } catch (Exception e) {
            re = new RankEntry();
            re.setLanguage(language);
            re.setWord(word);
        }
        re.setRank(rank);

        re.persist();
    }

    /**
     * Checks if a dictionary is complemented(dual).
     *
     * @param dictionary the dictionary to check
     * @return true if the dictionary is complemented, false otherwise
     */
    public boolean isComplemented(Dictionary dictionary) {
        return getComplement(dictionary) != null;
    }

    /**
     * Retrieves a dictionary complement
     *
     * @param dictionary the dictionary for which we need a complement
     * @return the dictionary's complement
     */
    public Dictionary getComplement(Dictionary dictionary) {
        List<Dictionary> candidates = Lists.newArrayList();

        List<Dictionary> dictionaries = getDictionaries();

        for (Dictionary d : dictionaries) {
            if (d.getFromLanguage().equals(dictionary.getToLanguage()) && d.getToLanguage().equals(dictionary.getFromLanguage())) {
                candidates.add(d);
            }
        }

        for (Dictionary tCandidate : candidates) {
            String[] langs = tCandidate.getName().split("-");

            if (langs.length == 2 && (langs[0].equalsIgnoreCase(dictionary.getToLanguage().getName())) &&
                    langs[1].equalsIgnoreCase(dictionary.getFromLanguage().getName())) {
                return tCandidate;
            }
        }

        return null;
    }

    public Dictionary createDictionary(Language from, Language to, String name, boolean special, byte[] smallIcon, byte[] bigIcon) {
        Dictionary dictionary = new Dictionary();
        dictionary.setFromLanguage(from);
        dictionary.setToLanguage(to);
        dictionary.setName(name);
        dictionary.setSpecial(special);
        dictionary.setIconSmall(smallIcon);
        dictionary.setIconBig(bigIcon);

        try {
            PreparedStatement ps = dbConnection.prepareStatement("INSERT INTO ? (name, from_language, to_language, special, icon_small, icon_big) " +
                    "values(?, ?, ?, ?, ?, ?)");

            ps.setString(1, Dictionary.TABLE_NAME);
            ps.setString(2, name);
            ps.setInt(3, from.ordinal());
            ps.setInt(4, to.ordinal());
            ps.setBoolean(5, special);
            ps.setBytes(6, smallIcon);
            ps.setBytes(7, bigIcon);

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        LOGGER.info("Created dictionary " + name + "with id " + dictionary.getId());

        return dictionary;
    }

    public void addWords(List<DictionaryEntry> dictionaryEntries) {
        for (DictionaryEntry tDictionaryEntry : dictionaryEntries) {
            tDictionaryEntry.persist();

            // only normal dictionaries contribute to the rank
            if (!tDictionaryEntry.getDictionary().isSpecial()) {
                RankEntry re = new RankEntry();
                re.setLanguage(tDictionaryEntry.getDictionary().getFromLanguage());
                re.setWord(tDictionaryEntry.getWord());
                re.setRank(1);

                re.persist();
            }
        }
    }
}
