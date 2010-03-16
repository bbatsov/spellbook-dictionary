/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drowltd.spellbook.core.service.study;

import com.drowltd.spellbook.core.exception.DictionaryDbLockedException;
import com.drowltd.spellbook.core.model.WordsForStudy;
import com.drowltd.spellbook.core.service.AbstractPersistenceService;
import java.util.List;
import javax.persistence.EntityTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author Sasho
 */
public class StudyService extends AbstractPersistenceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StudyService.class);

    public StudyService() throws DictionaryDbLockedException {
        super(null);
    }

    public List<String> getWordsForStudy() {
        return EM.createQuery("select wfs.word from WordsForStudy wfs ").getResultList();
    }

    public List<String> getTranslationForStudy() {
        return EM.createQuery("select wfs.translation from WordsForStudy wfs").getResultList();
    }

    public Long getCountOfTheWords() {
        return (Long) EM.createQuery("select count(*) from WordsForStudy").getSingleResult();
    }

    public void addWordForStudy(String word, String translation) {

        if (word == null || word.isEmpty()) {
            LOGGER.error("word == null || word.isEmpty()");
            throw new IllegalArgumentException("word == null || word.isEmpty()");
        }

        if (translation == null || translation.isEmpty()) {
            LOGGER.error("translation == null || translation.isEmpty()");
            throw new IllegalArgumentException("word == null || word.isEmpty()");
        }

        final WordsForStudy wfs = new WordsForStudy();

        wfs.setWord(word);
        wfs.setTranslation(translation);

        EntityTransaction t = EM.getTransaction();
        t.begin();
        EM.persist(wfs);
        t.commit();
    }

    public void deleteWord(String word) {
        word.replaceAll("'", "''");
        EntityTransaction t = EM.getTransaction();
        t.begin();
        EM.createQuery("delete from WordsForStudy  where word= :word").setParameter("word", word).executeUpdate();
        t.commit();
    }

    
}
