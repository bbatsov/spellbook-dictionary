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
     * @param studySetName a name which uniquely identifies a study set from which will taken the words
     * @return a list of the words for study
     */
    public List<String> getWordsForStudy(String studySetName) {
        return EM.createQuery("select se.dictionaryEntry.word from StudySetEntry se where se.studySet.name = :name").setParameter("name", studySetName).getResultList();
    }

    /**
     * Retrieves the translations of the words for study.
     *
     * @param studySetName a name which uniquely identifies a study set from which will taken the translations
     * @return a list of the translations for study
     */
    public List<String> getTranslationsForStudy(String studySetName) {
        return EM.createQuery("select se.dictionaryEntry.translation from StudySetEntry se where se.studySet.name = :name").setParameter("name", studySetName).getResultList();
    }

    /**
     * Retrieves count of the words for study.
     *
     * @param studySetName sets of which study set will be taken count of words
     * @return current number of words for study from respective study set
     */
    public Long getCountOfTheWords(String studySetName) {
        return (Long) EM.createQuery("select count(*) from StudySetEntry se where se.studySet.name = :name").setParameter("name", studySetName).getSingleResult();
    }

    /**
     * Retrieves all names of study sets
     *
     * @return a list with the names of all study sets
     * @see StudySet
     */
    public List<String> getNamesOfStudySets(){
       return EM.createQuery("select ss.name from StudySet ss").getResultList();
    }

    /**
     * Retrieves all study sets
     *
     * @return a list of study sets
     * @see StudySet
     */
    public List<StudySet> getStudySets(){
        return EM.createQuery("select ss from StudySet ss").getResultList();
    }

    /**
     * Retrieves a study set
     *
     * @param name determined which study set will be returned
     * @return a StudySet
     */
    public StudySet getStudySet(String name){
        return (StudySet) EM.createQuery("select ss from StudySet ss where ss.name = :name").setParameter("name", name).getSingleResult();
    }

    /**
     * Adds a new word for study
     *
     * @param word the word to add
     * @param dictionary dictionary from which will be taken the word
     * @param studySetName a name which uniquely identifies a study set
     * @see Dictionary
     */
    public void addWord(String word, Dictionary dictionary, String studySetName) {

        if (word == null || word.isEmpty()) {
            LOGGER.error("word == null || word.isEmpty()");
            throw new IllegalArgumentException("word == null || word.isEmpty()");
        }

        DictionaryEntry de = (DictionaryEntry) EM.createQuery("select de from DictionaryEntry de where de.word = :word and de.dictionary = :dictionary").setParameter("word", word).setParameter("dictionary", dictionary).getSingleResult();

        StudySet ss = (StudySet) EM.createQuery("select ss from StudySet ss where ss.name = :StudySetName").setParameter("StudySetName", studySetName).getSingleResult();

        StudySetEntry se = new StudySetEntry();
        se.setDictionaryEntry(de);
        se.setStudySet(ss);
        ss.setStudySetEntry(se);
        EntityTransaction t = EM.getTransaction();
        t.begin();
        EM.persist(se);
        EM.persist(ss);
        t.commit();
    }

    /**
     * Deletes a word which no want any more to study
     *
     * @param word word to delete
     * @param studySetName
     */
    /*public void deleteWord(String word, String studySetName) {
        word.replaceAll("'", "''");

        EntityTransaction t = EM.getTransaction();
        t.begin();
        EM.createQuery("delete from StudySetEntry se where se.dictionaryEntry.word = :word and se.studySet.name = :name").setParameter("word", word).setParameter("name", studySetName).executeUpdate();
        t.commit();
    }*/

    /**
     * Adds a new study set
     * @param name study set's name
     * @see StudySet
     */
    public void addStudySet(String name){

        StudySet ss = new StudySet();
        ss.setName(name);

        EntityTransaction t = EM.getTransaction();
        t.begin();
        EM.persist(ss);
        t.commit();
    }

    /**
     * Deletes a study set
     *
     * @param studySetName determined which study set to be deleted
     * @see StudySet
     */
    public void deleteStudySet(String studySetName){ 
        EntityTransaction t = EM.getTransaction();
        t.begin();
        //button query has thrown grammarExeption
        //EM.createQuery("delete from StudySetEntry se where se.studySet.name = :name").setParameter("name", studySetName).executeUpdate();
        EM.createQuery("delete from StudySet ss where ss.name = :name").setParameter("name", studySetName).executeUpdate();
        t.commit();
    }
}
