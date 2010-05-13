/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drowltd.spellbook.core.service.study;

import com.drowltd.spellbook.core.model.DictionaryEntry;
import com.drowltd.spellbook.core.model.Dictionary;
import com.drowltd.spellbook.core.model.StudySetEntry;
import com.drowltd.spellbook.core.model.StudySet;
import com.drowltd.spellbook.core.service.AbstractPersistenceService;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import javax.persistence.EntityTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Sasho
 */
public class StudyService extends AbstractPersistenceService {

    private List<String> translations = new ArrayList<String>();
    private static final Logger LOGGER = LoggerFactory.getLogger(StudyService.class);

    /**
     * Builds a service object.
     *
     * @throws DictionaryDbLockedException
     */
    public StudyService() {
        super(null);
    }

    public List<String> getAnothersPossiblesAnswers() {
        return translations;
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
    public List<String> getNamesOfStudySets() {
        return EM.createQuery("select ss.name from StudySet ss").getResultList();
    }

    /**
     * Retrieves all study sets
     *
     * @return a list of study sets
     * @see StudySet
     */
    public List<StudySet> getStudySets() {
        return EM.createQuery("select ss from StudySet ss").getResultList();
    }

    /**
     * Retrieves a study set
     *
     * @param name determined which study set will be returned
     * @return a StudySet
     */
    public StudySet getStudySet(String name) {
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
    public void deleteWord(String word, String studySetName) {
        word.replaceAll("'", "''");

        long wordID = (Long) EM.createQuery("select id from DictionaryEntry where word = :word").setParameter("word", word).getSingleResult();
        long studySetID = (Long) EM.createQuery("select id from StudySet where name = :name").setParameter("name", studySetName).getSingleResult();

        EntityTransaction t = EM.getTransaction();
        t.begin();
        EM.createNativeQuery("delete from Study_Entries  where study_set_id = :studySetID and dictionary_entry_id = :wordID").setParameter("studySetID", studySetID).setParameter("wordID", wordID).executeUpdate();
        t.commit();
    }

    /**
     * Adds a new study set
     * @param name study set's name
     * @see StudySet
     */
    public void addStudySet(String name) {

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
    public void deleteStudySet(String studySetName) {
        EntityTransaction t = EM.getTransaction();
        t.begin();
        EM.createNativeQuery("delete from Study_Entries where study_set_id = (select ss.id from Study_Sets ss where ss.name = :name)").setParameter("name", studySetName).executeUpdate();
        EM.createQuery("delete from StudySet ss where ss.name = :name").setParameter("name", studySetName).executeUpdate();
        t.commit();
    }

    public List<String> getPossiblesTranslations(String translation) {
        translation = translation.toLowerCase();

        List<String> rows = new ArrayList<String>();
        rows = splitTranslationOfRows(translation);

        List<String> rowsWithPossiblesTranslations = new ArrayList<String>();
        rowsWithPossiblesTranslations = getRowsWithPossiblesTranslations(rows);

        List<String> rowsWithPossiblesTranslationsWithoutUnneededThings = new ArrayList<String>();
        rowsWithPossiblesTranslationsWithoutUnneededThings = removingAllUnneededThings(rowsWithPossiblesTranslations);

        List<String> possibleTranslations = new ArrayList<String>();
        possibleTranslations = splitAllTranslations(rowsWithPossiblesTranslationsWithoutUnneededThings);

        return possibleTranslations;
    }

    private List<String> splitTranslationOfRows(String translation) {
        List<String> rows = new ArrayList<String>();
        int endIndex = 0;
        while (translation.contains("\n")) {
            endIndex = translation.indexOf("\n");
            rows.add(translation.substring(0, endIndex));
            translation = translation.substring(endIndex + 1);
        }

        return rows;
    }

    private List<String> getRowsWithPossiblesTranslations(List<String> rows) {
        List<String> rowsWithPossiblesTranslations = new ArrayList<String>();
        String id1 = " ";
        String id2 = " ";
        int beginIndex = 0;
        int endIndex = 0;

        String[] identificatorsForRowsWithPossibleTranslations = {
            "1.", "2.", "3.", "4.", "5.", "6.", "7.", "8.", "9.", "10.", "11.", "12.", "13.", "14.", "15.", "16.", "17.", "18.", "19.", "20.",
            "21.","22.", "23.", "24.", "25.", "26.", "27.", "28.", "29.", "30.", "31.", "32.", "33.", "34.","35.",
            "n", "pl", "a", "adv", "v", "int", "sl.","prep","\u043E\u0431\u0438\u043A\u002E","\u0438",
            "i.", "ii.", "iii.", "iv.", "v.", "vi.", "vii.", "viii.", "ix.", "x.", "xi.", "xii.", "xiii.", "xiv.", "xv."
        };

        for (String row : rows) {
            endIndex = row.indexOf(" ");
            if (endIndex != -1) {
                id1 = row.substring(0, endIndex);
                beginIndex = endIndex + 1;
                endIndex = row.indexOf(" ", beginIndex);
                if (endIndex != -1) {
                    id2 = row.substring(beginIndex, endIndex);
                }
                for (String identificator : identificatorsForRowsWithPossibleTranslations) {
                    if (id1.equals(identificator) || id2.equals(identificator)) {
                        rowsWithPossiblesTranslations.add(row);
                        break;
                    }
                }
            }
        }
        return rowsWithPossiblesTranslations;
    }

    private List<String> removingAllUnneededThings(List<String> rowsWithPossiblesTranslations) {
        List<String> rowsWithPossiblesTranslationsWithoutAbbreviations = new ArrayList<String>();
        String rowWithoutAbbreviations = null;
        int beginIndex = 0;
        int endIndex = 0;
        int countOfBrackets = 0;
        String inTheBrackets = null;
        for (String row : rowsWithPossiblesTranslations) {
            rowWithoutAbbreviations = removeAllAbbreviations(row);
            rowWithoutAbbreviations = rowWithoutAbbreviations.replaceAll("[a-z]", "");
            while (rowWithoutAbbreviations.indexOf("(", beginIndex) != -1) {
                beginIndex = rowWithoutAbbreviations.indexOf("(", beginIndex) + 1;
                countOfBrackets++;
            }
            for (int i = 0; i < countOfBrackets; i++) {
                beginIndex = rowWithoutAbbreviations.indexOf("(");
                if (beginIndex != -1) {
                    endIndex = rowWithoutAbbreviations.indexOf(")") + 1;
                    inTheBrackets = rowWithoutAbbreviations.substring(beginIndex, endIndex);
                    if (!inTheBrackets.equals("( )")) {
                        rowWithoutAbbreviations = rowWithoutAbbreviations.replaceAll(inTheBrackets, "");
                    }
                    rowWithoutAbbreviations = rowWithoutAbbreviations.replaceFirst("\\(", "");
                    rowWithoutAbbreviations = rowWithoutAbbreviations.replaceFirst("\\)", "");
                }
            }
            rowsWithPossiblesTranslationsWithoutAbbreviations.add(rowWithoutAbbreviations);
        }
        return rowsWithPossiblesTranslationsWithoutAbbreviations;
    }

    private String removeAllAbbreviations(String translation) {
        int beginIndex = 0, endIndex = 0;

        String str = null;
        StringBuilder strForRegularExpression = new StringBuilder();
        while (translation.contains(".")) {
            endIndex = translation.indexOf(".");
            beginIndex = endIndex;
            while (translation.charAt(beginIndex) != ' ' && translation.charAt(beginIndex) != '\n' && translation.charAt(beginIndex) != '(' && translation.charAt(beginIndex) != ')' && beginIndex != 0) {
                beginIndex--;
            }
            if (beginIndex == 0) {
                translation = translation.substring(endIndex + 1);
            } else {
                str = translation.substring(beginIndex + 1, endIndex);
                if (str.isEmpty()) {
                    translation = translation.replaceFirst("\\.", "");
                } else {
                    strForRegularExpression = new StringBuilder();
                    strForRegularExpression.append(str);
                    strForRegularExpression.append("\\.");
                    translation = translation.replaceAll(strForRegularExpression.toString(), "");
                }
            }
        }
        //translation = removeSpacesInTheBeginningAndEnd(translation);
        return translation;
    }

    private List<String> splitAllTranslations(List<String> rowsWithPossiblesTranslationsWithoutAbbreviations) {
        String[] translationsFromRow = null;
        List<String> possibleTranslations = new ArrayList<String>();
        for (String row : rowsWithPossiblesTranslationsWithoutAbbreviations) {
            translationsFromRow = row.split("[,|!|?]+");
            for (String possibleTranslation : translationsFromRow) {
                //if (!possibleTranslation.isEmpty()) {

                possibleTranslation = removeSpacesInTheBeginningAndEnd(possibleTranslation);
                if (!possibleTranslation.isEmpty()) {
                    possibleTranslations.add(possibleTranslation);
                }
                //}
            }
        }
        return possibleTranslations;
    }

    public String removeSpacesInTheBeginningAndEnd(String word) {
        while (word.length() != 0 && word.charAt(0) == ' ') {
            word = word.substring(1);
        }
        int endIndex = word.length() - 1;
        while (word.length() != 0 && word.charAt(endIndex) == ' ') {
            endIndex--;
        }
        if (endIndex != word.length() - 1) {
            word = word.substring(0, endIndex + 1);
        }
        return word;
    }

    public void possibleAnswers(String translation) {

        translations = new ArrayList<String>();
        translation = translation.toLowerCase();

        //Removes the uneeded characters from the translation
        String t = translation.replaceAll("\\b(n|a|v|(attr)|(adv)|[0-9]+)\\b\\s?", "");

        //Splits the translation around matches of the pattern
        String[] s = Pattern.compile("\\s*[,|;|.|\\n]\\s*").split(t, 0);

        for (int i = 0; i < s.length; i++) {

            if (s[i].isEmpty()) {
                continue;
            }

            if (s[i].contains("(")) {
                //removes the parenthesis and everything inside them
                slash(s[i].replaceAll("\\(([^()]*)\\)?", ""));
                //removes the parenthesis only
                slash(s[i].replaceAll("\\(([^()]*)\\)", "$1"));
            } else {
                slash(s[i]);
            }
        }
    }

    private void slash(String s) {
        String first = new String();
        String last = new String();

        if (s.contains("/")) {
            String[] slash = s.split("/");
            //Using this loop we make all possible combinations for correct answer
            for (int j = 0; j < slash.length; j++) {
                translations.add(slash[j]);

                if (j != slash.length - 1) {

                    //Combines the whole last string(the string after the last forward slash) with every other string
                    translations.add(slash[j] + " " + slash[slash.length - 1]);
                }
            }

            //removes the last word from the first string
            if ((slash[0].contains(" "))) {
                first = slash[0].substring(0, slash[0].lastIndexOf(" "));
            }

            //removes the first word from the last string
            if ((slash[slash.length - 1].contains(" "))) {
                last = slash[slash.length - 1].substring(slash[slash.length - 1].indexOf(" ") + 1);
            }

            if (!(first.isEmpty())) {

                //Combines the new fist string with every other string
                for (int j = 0; j < slash.length; j++) {
                    translations.add(first + " " + slash[j]);
                }
            }

            if (!(last.isEmpty())) {

                //Combines the new last string with every other string
                for (int j = 0; j < slash.length; j++) {
                    translations.add(slash[j] + " " + last);
                }
            }
        } else {
            translations.add(s);
        }

        Set<String> set = new LinkedHashSet<String>(translations);
        translations = new ArrayList<String>(set);
    }

    public String combinePossiblesTranslationsForTheTable(List<String> translations) {
        StringBuilder translation = new StringBuilder();

        for (int i = 0; i < translations.size(); i++) {
            translation.append(translations.get(i));
            if (i != translations.size() - 1) {
                translation.append(", ");
            }
        }
        return translation.toString();
    }
}
