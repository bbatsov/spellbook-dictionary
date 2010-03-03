package com.drowltd.spellbook.core.service;

import com.drowltd.spellbook.core.db.DatabaseService;
import com.drowltd.spellbook.core.exception.DictionaryDbLockedException;
import com.drowltd.spellbook.core.model.Dictionary;
import com.drowltd.spellbook.core.model.DictionaryEntry;
import com.drowltd.spellbook.core.model.Language;
import com.drowltd.spellbook.core.model.RatingsEntry;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

/**
 *
 * @author bozhidar
 */
public class InitDb {

    private static EntityManager em;
    private static DatabaseService databaseService;

    public static void main(String[] args) throws DictionaryDbLockedException {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("Spellbook");

        em = emf.createEntityManager();

        DatabaseService.init("/opt/spellbook/db/dictionary.data.db");
        databaseService = DatabaseService.getInstance();

         copyLegacyDict(com.drowltd.spellbook.core.db.Dictionary.EN_BG, "en-bg.png", "English-Bulgarian");
         copyLegacyDict(com.drowltd.spellbook.core.db.Dictionary.BG_EN, "bg-en.png", "Bulgarian-English");


        copyLegacyRatings(com.drowltd.spellbook.core.db.Dictionary.EN_BG, "English-Bulgarian", Language.English);
        copyLegacyRatings(com.drowltd.spellbook.core.db.Dictionary.BG_EN, "Bulgarian-English", Language.Bulgarian);
    }

    private static void copyLegacyDict(com.drowltd.spellbook.core.db.Dictionary d, String icon, String name) {
        EntityTransaction t = em.getTransaction();
        t.begin();

        Dictionary newDict = new Dictionary();
        newDict.setName(name);
        newDict.setIconName(icon);

        em.persist(newDict);

        List<String> allWords = databaseService.getWordsFromDictionary(d);

        for (String string : allWords) {
            String translation = databaseService.getTranslation(d, string);

            DictionaryEntry de = new DictionaryEntry();
            de.setDictionary(newDict);
            de.setWord(string);
            de.setWordTranslation(translation);
            de.setSpellcheckRank(0);
            de.setAddedByUser(false);

            em.persist(de);
        }

        t.commit();
    }

    private static void copyLegacyRatings(com.drowltd.spellbook.core.db.Dictionary d, String dic, Language l) {
        EntityTransaction t = em.getTransaction();
        t.begin();

        final Dictionary dictionary = (Dictionary) em.createQuery("select d from Dictionary d where name = :dic").setParameter("dic", dic).getSingleResult();

        final Map<String, Integer> ratings = databaseService.getRatings(d);


        for (String word : ratings.keySet()) {
            final int rating = ratings.get(word);

            RatingsEntry re = new RatingsEntry();
            re.setDictionary(dictionary);
            re.setLang(l);
            re.setWord(word);
            re.setSpellcheckRank(rating == 0 ? 1 : rating);

            em.persist(re);
            System.out.println("Adding ratings entry: "+word+" "+rating);
        }

        t.commit();
    }
}
