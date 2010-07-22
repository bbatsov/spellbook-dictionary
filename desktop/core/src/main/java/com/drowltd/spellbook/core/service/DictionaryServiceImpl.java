package com.drowltd.spellbook.core.service;

import com.drowltd.spellbook.core.model.Dictionary;
import com.drowltd.spellbook.core.model.DictionaryEntry;
import com.drowltd.spellbook.core.model.Language;
import com.drowltd.spellbook.core.model.RankEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.NoResultException;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides Spellbook's basic dictionary related functionality like looking for dictionaries, words, adding/updating/
 * deleting dictionary entries.
 *
 * @author bozhidar
 */
public class DictionaryServiceImpl extends AbstractPersistenceService implements DictionaryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DictionaryServiceImpl.class);
    private static DictionaryService instance;
    private static Map<String, List<String>> dictionaryWordsCache = new HashMap<String, List<String>>();

    /**
     * Builds a service object.
     *
     * @param dictDbFile the path to the H2 database file
     */
    private DictionaryServiceImpl(String dictDbFile) {
        super(dictDbFile);
    }

    /**
     * Bootstraps the dictionary service. The method can be executed only once.
     *
     * @param dictDbFile the dictionary database file
     */
    public static void init(String dictDbFile) {
        if (instance == null) {
            instance = new DictionaryServiceImpl(dictDbFile);

            TransactionInvocationHandler<DictionaryService> handler = new TransactionInvocationHandler<DictionaryService>(instance);
            instance = (DictionaryService) Proxy.newProxyInstance(instance.getClass().getClassLoader(), new Class[]{DictionaryService.class}, handler);
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
    @Override
    public List<Dictionary> getDictionaries() {
        return EM.createNamedQuery("Dictionary.getAllDictionaries", Dictionary.class).getResultList();
    }

    /**
     * Retrieves a dictionary by its name.
     *
     * @param dictionaryName the name of the dictionary we wish to obtain
     * @return the dictionary corresponding to the name
     */
    @Override
    public Dictionary getDictionary(String dictionaryName) {
        return EM.createQuery("select d from Dictionary d where d.name = :name", Dictionary.class).setParameter("name", dictionaryName).getSingleResult();
    }

    /**
     * Retrieves all words from the target dictionary. The words are cached for subsequent
     * invokations of the method
     *
     * @param d the target dictionary
     * @return a list of the words in the dictionary
     */
    @Override
    public List<String> getWordsFromDictionary(Dictionary d) {
        if (!dictionaryWordsCache.containsKey(d.getName())) {
            LOGGER.info("Caching dictionary " + d.getName());
            dictionaryWordsCache.put(d.getName(),
                    EM.createQuery("select de.word from DictionaryEntry de "
                            + "where de.dictionary = :dictionary order by LOWER(de.word) asc", String.class).setParameter("dictionary", d).getResultList());
        } else {
            LOGGER.info("Loading from cache dictionary " + d.getName());
        }

        return dictionaryWordsCache.get(d.getName());
    }

    /**
     * Retrieves the translation of a word from the specified dictionary.
     *
     * @param word the target word
     * @param d    the target dictionary
     * @return the word's translation
     */
    @Override
    public String getTranslation(String word, Dictionary d) {
        return EM.createQuery("select de.translation from DictionaryEntry de"
                + " where de.word = :word and de.dictionary = :dictionary", String.class).setParameter("word", word).setParameter("dictionary", d).getSingleResult();
    }

    /**
     * Adds a new word to a dictionary
     *
     * @param word        the word to add
     * @param translation the word's translation
     * @param d           the dictionary in which the word will be added
     */
    @Override
    @Transactional
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

        EM.persist(de);
        EM.persist(re);
    }

    /**
     * Updates a dictionary entry. Only its translation can be updated.
     *
     * @param word        the word definition to update(needed to find the entry to update)
     * @param translation the new translation
     * @param d           the dictionary containing the word
     */
    @Override
    @Transactional
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

        DictionaryEntry de = EM.createQuery("select de from DictionaryEntry de "
                + "where de.dictionary = :dictionary and de.word = :word", DictionaryEntry.class).setParameter("dictionary", d).setParameter("word", word).getSingleResult();

        de.setTranslation(translation);

        EM.merge(de);
    }

    /**
     * Deletes a word from the specified dictionary.
     *
     * @param word       word to delete
     * @param dictionary the dictionary to remove the word from
     */
    @Override
    @Transactional
    public void deleteWord(String word, Dictionary dictionary) {
        DictionaryEntry dictionaryEntry = EM.createQuery("select de from DictionaryEntry de where de.word = :word and de.dictionary = :dictionary", DictionaryEntry.class).setParameter("word", word).setParameter("dictionary", dictionary).getSingleResult();
        EM.remove(dictionaryEntry);
    }

    /**
     * Checks whether a dictionary contains a word.
     *
     * @param word the word for which to check
     * @param d    the dictionary in which to check
     * @return true if the word is present, false otherwise
     */
    @Override
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

    @Override
    public Map<String, Integer> getRatings(Language language) {
        if (language == null) {
            LOGGER.error("language == null");
            throw new IllegalArgumentException("language == null");
        }

        final List<RankEntry> ratingslist = EM.createQuery("select re from RankEntry re "
                + " where re.language = :language", RankEntry.class).setParameter("language", language).getResultList();

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
     * @param languageTo   to language
     * @return the dictionary that matches the languages
     */
    @Override
    public Dictionary getDictionary(Language languageFrom, Language languageTo) {
        if (languageFrom == null || languageTo == null) {
            LOGGER.error("languageFrom == null || languageTo == null");
            throw new IllegalArgumentException("languageFrom == null || languageTo == null");
        }
        return EM.createQuery("select d from Dictionary d "
                + " where d.fromLanguage = :fromLanguage and d.toLanguage = :toLanguage", Dictionary.class).setParameter("fromLanguage", languageFrom).setParameter("toLanguage", languageTo).getSingleResult();
    }

    @Override
    @Transactional
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

        EM.persist(re);
    }

    @Override
    @Transactional
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
            re = EM.createQuery("select re from RankEntry re where re.word = :word and re.language = :language", RankEntry.class).setParameter("word", word).setParameter("language", language).getSingleResult();
        } catch (NoResultException e) {
            re = new RankEntry();
            re.setLanguage(language);
            re.setWord(word);
        }
        re.setRank(rank);

        EM.persist(re);
    }

    /**
     * Checks if a dictionary is complemented(dual).
     *
     * @param dictionary the dictionary to check
     * @return true if the dictionary is complemented, false otherwise
     */
    @Override
    public boolean isComplemented(Dictionary dictionary) {
        return !EM.createNamedQuery("Dictionary.getDictionaryByLanguages").setParameter("fromLanguage", dictionary.getToLanguage()).setParameter("toLanguage", dictionary.getFromLanguage()).getResultList().isEmpty();
    }

    /**
     * Retrieves a dictionary complement
     *
     * @param dictionary the dictionary for which we need a complement
     * @return the dictionary's complement
     */
    @Override
    public Dictionary getComplement(Dictionary dictionary) {
        List<Dictionary> candidates = EM.createNamedQuery("Dictionary.getDictionaryByLanguages", Dictionary.class).setParameter("fromLanguage", dictionary.getToLanguage()).setParameter("toLanguage", dictionary.getFromLanguage()).getResultList();

        for (Dictionary tCandidate : candidates) {
            String[] langs = tCandidate.getName().split("-");

            if (langs.length == 2 && (langs[0].equalsIgnoreCase(dictionary.getToLanguage().getName())) &&
                    langs[1].equalsIgnoreCase(dictionary.getFromLanguage().getName())) {
                return tCandidate;
            }
        }

        return null;
    }

    @Transactional
    @Override
    public Dictionary createDictionary(Language from, Language to, String name, boolean special, byte[] smallIcon, byte[] bigIcon) {
        Dictionary dictionary = new Dictionary();
        dictionary.setFromLanguage(from);
        dictionary.setToLanguage(to);
        dictionary.setName(name);
        dictionary.setSpecial(special);
        dictionary.setIconSmall(smallIcon);
        dictionary.setIconBig(bigIcon);

        EM.persist(dictionary);

        LOGGER.info("Created dictionary " + name + "with id " + dictionary.getId());

        return dictionary;
    }

    @Transactional
    @Override
    public void addWords(List<DictionaryEntry> dictionaryEntries) {
        for (DictionaryEntry tDictionaryEntry : dictionaryEntries) {
            EM.persist(tDictionaryEntry);

            // only normal dictionaries contribute to the rank
            if (!tDictionaryEntry.getDictionary().isSpecial()) {
                RankEntry re = new RankEntry();
                re.setLanguage(tDictionaryEntry.getDictionary().getFromLanguage());
                re.setWord(tDictionaryEntry.getWord());
                re.setRank(1);

                EM.persist(re);
            }
        }
    }
}
