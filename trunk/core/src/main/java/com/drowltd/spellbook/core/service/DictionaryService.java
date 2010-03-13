package com.drowltd.spellbook.core.service;

import com.drowltd.spellbook.core.exam.Difficulty;
import com.drowltd.spellbook.core.exception.DictionaryDbLockedException;
import com.drowltd.spellbook.core.model.Dictionary;
import com.drowltd.spellbook.core.model.DictionaryEntry;
import com.drowltd.spellbook.core.model.Language;
import com.drowltd.spellbook.core.model.RankEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author bozhidar
 */
public class DictionaryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DictionaryService.class);
    private static EntityManager EM;
    private static DictionaryService instance;

    private DictionaryService(String dictDbFile) throws DictionaryDbLockedException {
        LOGGER.info("dictionary database: " + dictDbFile.replace(".data.db", ""));

        String url = "jdbc:h2:" + dictDbFile.replace(".data.db", "");

        try {
            // we need to override the db url from persistence.xml
            Map<String, String> properties = new HashMap<String, String>();
            properties.put("hibernate.connection.url", url);

            EM = Persistence.createEntityManagerFactory("Spellbook", properties).createEntityManager();
        } catch (javax.persistence.PersistenceException e) {
            if (e.getMessage() != null) {
                if (e.getMessage().contains("Cannot open connection")) {
                    throw new DictionaryDbLockedException();
                }
            }

            e.printStackTrace();
        }
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

    public static DictionaryService getInstance() {
        return instance;
    }

    public List<Dictionary> getDictionaries() {
        return EM.createNamedQuery("Dictionary.getAllDictionaries").getResultList();
    }

    public List<String> getWordsFromDictionary(Dictionary d) {
        return EM.createQuery("select de.word from DictionaryEntry de "
                + "where de.dictionary = :dictionary order by LOWER(de.word) asc").setParameter("dictionary", d).getResultList();
    }

    public String getTranslation(String word, Dictionary d) {
        return (String) EM.createQuery("select de.translation from DictionaryEntry de"
                + " where de.word = :word and de.dictionary = :dictionary").setParameter("word", word).setParameter("dictionary", d).getSingleResult();
    }

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

    public void upateWord(String word, String translation, Dictionary d) {
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

        de.setTranslation(translation);
        final EntityTransaction t = EM.getTransaction();
        t.begin();
        EM.persist(de);
        t.commit();
    }

    public boolean containsWord(String word, Dictionary d) {
        if (d == null) {
            LOGGER.error("d == null");
            throw new IllegalArgumentException("d == null");
        }


        long count = (Long) EM.createQuery("SELECT COUNT(de.word) FROM DictionaryEntry de "
                + "WHERE de.word = :word and de.dictionary = :dictionary").setParameter("word", word).setParameter("dictionary", d).getSingleResult();
        return count > 0;
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

    public Dictionary getDictionary(Language languageFrom, Language languageTo) {
        if (languageFrom == null || languageTo == null) {
            LOGGER.error("languageFrom == null || languageTo == null");
            throw new IllegalArgumentException("languageFrom == null || languageTo == null");
        }
        return (Dictionary) EM.createQuery("select d from Dictionary d "
                + " where d.fromLanguage = :fromLanguage and d.toLanguage = :toLanguage").setParameter("fromLanguage", languageFrom).setParameter("toLanguage", languageTo).getSingleResult();
    }

    public List<Language> getToLanguages(Language fromLanguage) {
        if (fromLanguage == null) {
            LOGGER.error("fromLanguage == null");
            throw new IllegalArgumentException("fromLanguage");
        }

        List<Dictionary> dictionaries = EM.createQuery("select d from Dictionary d where d.fromLanguage = :fromLanguage").setParameter("fromLanguage", fromLanguage).getResultList();

        List<Language> languagesTo = new ArrayList<Language>(dictionaries.size());
        for (Dictionary dictionary : dictionaries) {
            languagesTo.add(dictionary.getToLanguage());
        }

        return languagesTo;
    }

    public List<String> getDifficultyWords(Dictionary dictionary, Difficulty difficulty) {
        if (dictionary == null) {
            LOGGER.error("dictionary == null");
            throw new IllegalArgumentException("dictionary == null");
        }

        if (difficulty == null) {
            LOGGER.error("difficulty == null");
            throw new IllegalArgumentException("difficulty == null");
        }

        List<String> words = EM.createQuery("select re.word from RankEntry re where"
                + " re.rank > :low and re.rank <= :high and LENGTH(re.word) >=3 and " +
                "exists (select de.word from DictionaryEntry de where de.word = re.word and de.dictionary.fromLanguage = re.language)").setParameter("low", difficulty.getLow()).setParameter("high", difficulty.getHigh()).getResultList();


        return words;
    }

    public String getApproximation(Dictionary dictionary, String searchKey) {
        if (searchKey != null && !searchKey.isEmpty()) {

            LOGGER.info("Getting approximation for " + searchKey);

            StringBuilder builder = new StringBuilder(searchKey);

            // we start looking for approximate matches of the full search key, but if we fail - we start looking
            // for shorter matches
            do {
                List<String> matches = EM.createQuery("select de.word from DictionaryEntry de where de.dictionary = :dictionary and de.word like :searchKey order by de.word asc").setParameter("dictionary", dictionary).setParameter("searchKey", builder.toString() + "%").getResultList();

                if (matches.size() > 0) {
                    return matches.get(0);
                }

                builder.deleteCharAt(builder.length() - 1);
            } while (builder.length() > 0);
        }

        return null;
    }

    public boolean isComplemented(Dictionary dictionary) {
        return !EM.createNamedQuery("Dictionary.getDictionaryByLanguages")
                .setParameter("fromLanguage", dictionary.getToLanguage())
                .setParameter("toLanguage", dictionary.getFromLanguage())
                .getResultList().isEmpty();
    }

    public Dictionary getComplement(Dictionary dictionary) {
        return (Dictionary)EM.createNamedQuery("Dictionary.getDictionaryByLanguages")
                .setParameter("fromLanguage", dictionary.getToLanguage())
                .setParameter("toLanguage", dictionary.getFromLanguage())
                .getSingleResult();
    }
}
