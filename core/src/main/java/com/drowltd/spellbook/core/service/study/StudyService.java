/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drowltd.spellbook.core.service.study;

import com.drowltd.spellbook.core.exception.DictionaryDbLockedException;
import com.drowltd.spellbook.core.model.DictionaryEntry;
import com.drowltd.spellbook.core.model.Dictionary;
import com.drowltd.spellbook.core.model.StudySetEntry;
import com.drowltd.spellbook.core.model.StudySet;
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

    /**
     * Builds a service object.
     *
     * @throws DictionaryDbLockedException
     */
    public StudyService() throws DictionaryDbLockedException {
        super(null);
    }

    /**
     * Retrieves all words for study. The words are cached for subsequent
     * invokations of the method
     *
     * @return a list of the words for study
     */
    public List<String> getWordsForStudy() {
        return EM.createQuery("select de.word from DictionaryEntry de, StudySetEntry se where se.dictionary_entry_id = de.id").getResultList();
    }

    /**
     * Retrieves the translations of the words for study.
     *
     * @return a list of the translations for study
     */
    public List<String> getTranslationsForStudy() {
        return EM.createQuery("select de.translation from DictionaryEntry de, StudySetEntry se where se.dictionary_entry_id = de.id").getResultList();
    }

    /**
     * Retrieves count of the words for study.
     *
     * @return current number of words for study
     */
    public Long getCountOfTheWords() {
        return (Long) EM.createQuery("select count(*) from StudySetEntry").getSingleResult();
    }

    /**
     * Adds a new word for study
     *
     * @param word the word to add
     * @param dictionary dictionary from which will be taken the word
     * @param studySetName determined StudySet's name in which will be added the word
     * @see Dictionary
     */
    public void addWordForStudy(String word, Dictionary dictionary, String studySetName) {

        if (word == null || word.isEmpty()) {
            LOGGER.error("word == null || word.isEmpty()");
            throw new IllegalArgumentException("word == null || word.isEmpty()");
        }

        DictionaryEntry de = (DictionaryEntry) EM.createQuery("select de from DictionaryEntry de where de.word = :word and de.dictionary = :dictionary").setParameter("word", word).setParameter("dictionary", dictionary).getSingleResult();

        StudySet ss = (StudySet) EM.createQuery("select ss from StudySet ss where ss.name = :StudySetName").setParameter("StudySetName", studySetName).getSingleResult();

        StudySetEntry se = new StudySetEntry();
        se.setDictionaryEntry(de);
        se.setStudySet(ss);

        EntityTransaction t = EM.getTransaction();
        t.begin();
        EM.persist(se);
        t.commit();
    }

    /**
     * Deletes a word which no want any more to study.
     *
     * @param word word to delete
     */
    public void deleteWord(String word) {
        word.replaceAll("'", "''");

        long id = (Long) EM.createQuery("select de.id from DictionaryEntry de where de.word = :word").setParameter("word", word).getSingleResult();

        EntityTransaction t = EM.getTransaction();
        t.begin();
        EM.createQuery("delete from StudySetEntry se where se.dictionary_entry_id = :id").setParameter("id", id).executeUpdate();
        t.commit();
    }
}
