package com.drowltd.spellbook.core.service;

import com.drowltd.spellbook.core.exception.DictionaryDbLockedException;
import com.drowltd.spellbook.core.model.Dictionary;
import com.drowltd.spellbook.core.model.DictionaryEntry;
import com.drowltd.spellbook.core.model.Language;
import com.drowltd.spellbook.core.model.RankEntry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides Spellbook's basic dictionary related functionality like looking for dictionaries, words, adding/updating/
 * deleting dictionary entries.
 *
 * @author bozhidar
 */
public class DictionaryService extends AbstractPersistenceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DictionaryService.class);
    private static DictionaryService instance;

    private static Map<String, List<String>> dictionaryWordsCache = new HashMap<String, List<String>>();

    /**
     * Builds a service object.
     *
     * @param dictDbFile the path to the H2 database file
     * @throws DictionaryDbLockedException
     */
    private DictionaryService(String dictDbFile) throws DictionaryDbLockedException {
        super(dictDbFile);
    }

    /**
     * Bootstraps the dictionary service. The method can be executed only once.
     *
     * @param dictDbFile the dictionary database file
     *
     * @throws DictionaryDbLockedException if another process is already using the db file
     */
    public static void init(String dictDbFile) throws DictionaryDbLockedException {
        if (instance == null) {
            instance = new DictionaryService(dictDbFile);
        } else {
            LOGGER.info("Dictionary service is already initialized");
        }
    }

    /**
     * Obtains the service single instance.
     *
     * @return service instance
     */
    public static DictionaryService getInstance() {
        return instance;
    }

    /**
     * Retrieve a list of all available dictionaries.
     *
     * @return a list of available dictionaries, emtpy list if none are available
     */
    public List<Dictionary> getDictionaries() {
        return EM.createNamedQuery("Dictionary.getAllDictionaries").getResultList();
    }

    /**
     * Retrieves a dictionary by its name.
     *
     * @param dictionaryName the name of the dictionary we wish to obtain
     * @return the dictionary corresponding to the name
     */
    public Dictionary getDictionary(String dictionaryName) {
        return (Dictionary)EM.createQuery("select d from Dictionary d where d.name = :name").setParameter("name", dictionaryName).getSingleResult();
    }

    /**
     * Retrieves all words from the target dictionary. The words are cached for subsequent
     * invokations of the method
     *
     * @param d the target dictionary
     * @return a list of the words in the dictionary
     */
    public List<String> getWordsFromDictionary(Dictionary d) {
        if (!dictionaryWordsCache.containsKey(d.getName())) {
            LOGGER.info("Caching dictionary " + d.getName());
            dictionaryWordsCache.put(d.getName(),
                    EM.createQuery("select de.word from DictionaryEntry de "
                + "where de.dictionary = :dictionary order by LOWER(de.word) asc").setParameter("dictionary", d).getResultList());
        } else {
            LOGGER.info("Loading from cache dictionary " + d.getName());
        }

        return dictionaryWordsCache.get(d.getName());
    }

    /**
     * Retrieves the translation of a word from the specified dictionary.
     *
     * @param word the target word
     * @param d the target dictionary
     * @return the word's translation
     */
    public String getTranslation(String word, Dictionary d) {
        return (String) EM.createQuery("select de.translation from DictionaryEntry de"
                + " where de.word = :word and de.dictionary = :dictionary").setParameter("word", word).setParameter("dictionary", d).getSingleResult();
    }

    /**
     * Adds a new word to a dictionary
     *
     * @param word the word to add
     * @param translation the word's translation
     * @param d the dictionary in which the word will be added
     */
    public void addWord(String word, String translation, Dictionary d) {
        if (word == null || word.isEmpty()) {
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
            return;
        }

        final DictionaryEntry de = new DictionaryEntry();
        de.setDictionary(d);
        de.setWord(word);
        de.setTranslation(translation);
        de.setAddedByUser(true);

        RankEntry re = new RankEntry();
        re.setWord(word);
        re.setRank(1);
        re.setLanguage(d.getFromLanguage());

        EntityTransaction t = EM.getTransaction();
        t.begin();
        EM.persist(de);
        EM.persist(re);
        t.commit();

    }

    /**
     * Updates a dictionary entry. Both the word itself and its translation can be updated.
     *
     * @param word the word before the update(needed to find the entry to update)
     * @param newWord the possibly new word
     * @param translation the new translation
     * @param d the dictionary containing the word
     */
    public void updateWord(String word, String newWord, String translation, Dictionary d) {
        if (word == null || word.isEmpty()) {
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

        if (!containsWord(word, d)) {
            return;
        }

        DictionaryEntry de = (DictionaryEntry) EM.createQuery("select de from DictionaryEntry de "
                + "where de.dictionary = :dictionary and de.word = :word").setParameter("dictionary", d).setParameter("word", word).getSingleResult();

        de.setWord(newWord);
        de.setTranslation(translation);
        final EntityTransaction t = EM.getTransaction();
        t.begin();
        EM.persist(de);
        t.commit();
    }

    /**
     * Deletes a word from the specified dictionary.
     *
     * @param word word to delete
     * @param dictionary the dictionary to remove the word from
     */
    public void deleteWord(String word, Dictionary dictionary) {
        EntityTransaction entityTransaction = EM.getTransaction();
        entityTransaction.begin();
        EM.createQuery("delete from DictionaryEntry de where de.word = :word and de.dictionary = :dictionary")
                .setParameter("word", word)
                .setParameter("dictionary", dictionary)
                .executeUpdate();
        entityTransaction.commit();
    }

    /**
     * Checks whether a dictionary contains a word.
     *
     * @param word the word for which to check
     * @param d the dictionary in which to check
     * @return true if the word is present, false otherwise
     */
    public boolean containsWord(String word, Dictionary d) {
        if (d == null) {
            LOGGER.error("d == null");
            throw new IllegalArgumentException("d == null");
        }

        try {
            EM.createQuery("SELECT de FROM DictionaryEntry de "
                    + "WHERE de.word = :word and de.dictionary = :dictionary").setParameter("word", word).setParameter("dictionary", d).getSingleResult();

            // we don't care about the result of the query - there will be an exception if something is wrong
            return true;
        } catch (NoResultException ex) {
            return false;
        }
    }

    public Map<String, Integer> getRatings(Language language) {
        if (language == null) {
            LOGGER.error("language == null");
            throw new IllegalArgumentException("language == null");
        }

        final List<RankEntry> ratingslist = EM.createQuery("select re from RankEntry re "
                + " where re.language = :language").setParameter("language", language).getResultList();

        Map<String, Integer> ratingsMap = new HashMap<String, Integer>();
        for (RankEntry re : ratingslist) {
            ratingsMap.put(re.getWord(), re.getRank());
        }

        return ratingsMap;
    }

    /**
     * Retrieves a dictionary by its from and to languages.
     *
     * @param languageFrom from language
     * @param languageTo to language
     * @return the dictionary that matches the languages
     */
    public Dictionary getDictionary(Language languageFrom, Language languageTo) {
        if (languageFrom == null || languageTo == null) {
            LOGGER.error("languageFrom == null || languageTo == null");
            throw new IllegalArgumentException("languageFrom == null || languageTo == null");
        }
        return (Dictionary) EM.createQuery("select d from Dictionary d "
                + " where d.fromLanguage = :fromLanguage and d.toLanguage = :toLanguage").setParameter("fromLanguage", languageFrom).setParameter("toLanguage", languageTo).getSingleResult();
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
        
        EntityTransaction t = EM.getTransaction();
        t.begin();
        EM.persist(re);
        t.commit();
    }

    /**
     * Checks if a dictionary is complemented(dual).
     *
     * @param dictionary the dictionary to check
     * @return true if the dictionary is complemented, false otherwise
     */
    public boolean isComplemented(Dictionary dictionary) {
        return !EM.createNamedQuery("Dictionary.getDictionaryByLanguages").setParameter("fromLanguage", dictionary.getToLanguage()).setParameter("toLanguage", dictionary.getFromLanguage()).getResultList().isEmpty();
    }

    /**
     * Retrieves a dictionary complement
     *
     * @param dictionary the dictionary for which we need a complement
     * @return the dictionary's complement
     */
    public Dictionary getComplement(Dictionary dictionary) {
        return (Dictionary) EM.createNamedQuery("Dictionary.getDictionaryByLanguages").setParameter("fromLanguage", dictionary.getToLanguage()).setParameter("toLanguage", dictionary.getFromLanguage()).getSingleResult();
    }

    
}
