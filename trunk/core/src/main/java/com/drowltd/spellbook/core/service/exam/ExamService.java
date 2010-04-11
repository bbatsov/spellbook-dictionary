/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drowltd.spellbook.core.service.exam;

import com.drowltd.spellbook.core.model.Difficulty;
import com.drowltd.spellbook.core.model.Dictionary;
import com.drowltd.spellbook.core.model.Language;
import com.drowltd.spellbook.core.model.ScoreboardEntry;
import com.drowltd.spellbook.core.service.AbstractPersistenceService;
import com.drowltd.spellbook.core.service.DictionaryService;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;
import javax.persistence.EntityTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Franky
 * @since 0.2
 */
public class ExamService extends AbstractPersistenceService {

    private DictionaryService dictionaryService;
    private List<String> words;
    private List<String> answers;
    private int examWordIndex;
    private Random random = new Random();
    private String translation;
    private Logger LOGGER = LoggerFactory.getLogger(ExamService.class);

    public ExamService() {
        super(null);

        dictionaryService = DictionaryService.getInstance();
    }

    /**
     * Gets a random word from the selected dictionary
     *
     * @param dictionary the target dictionary
     *
     */
    public void getExamWord(Dictionary selectedDic) {
        examWordIndex = random.nextInt(words.size());
        String translation0 = null;

        while ((translation0 = dictionaryService.getTranslation(words.get(examWordIndex), selectedDic)).contains("\u0432\u0436.")) {
            examWordIndex = random.nextInt(words.size());
        }

        translation = translation0;
    }

    /**
     * Returns a word from the selected dictionary
     *
     * @return selected word from the getExamWord method
     *
     */
    public String examWord() {
        return words.get(examWordIndex);
    }

    /**
     *
     * @return the translation of the selected word
     *
     */
    public String getTranslation() {
        return translation;
    }

    /**
     *
     * Splits the whole translation of a word into all possible answers
     *
     */
    public void possibleAnswers() {

        answers = new ArrayList<String>();
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

    /**
     *
     * Removes all forward slashes and gives all possible combinations for correct answer
     *
     * @param s one of the possible translations
     */
    private void slash(String s) {
        String first = new String();
        String last = new String();

        if (s.contains("/")) {
            String[] slash = s.split("/");
            //Using this loop we make all possible combinations for correct answer
            for (int j = 0; j < slash.length; j++) {
                answers.add(slash[j]);

                if (j != slash.length - 1) {

                    //Combines the whole last string(the string after the last forward slash) with every other string
                    answers.add(slash[j] + " " + slash[slash.length - 1]);
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
                    answers.add(first + " " + slash[j]);
                }
            }

            if (!(last.isEmpty())) {

                //Combines the new last string with every other string
                for (int j = 0; j < slash.length; j++) {
                    answers.add(slash[j] + " " + last);
                }
            }
        } else {
            answers.add(s);
        }

        Set<String> set = new LinkedHashSet<String>(answers);
        answers = new ArrayList<String>(set);
    }

    /**
     *
     * Checks if the answer given by the user is correct
     *
     * @param guess answer given by the user
     * @return true if the answer is correct;false if it is not
     */
    public boolean isCorrect(String guess) {

        for (String possibleAnswers : answers) {

            possibleAnswers = possibleAnswers.trim();

            if (possibleAnswers.isEmpty()) {
                continue;
            }

            if (possibleAnswers.equalsIgnoreCase(guess)) {
                return true;
            }
        }
        return false;
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

    public void getDifficultyWords(Dictionary dictionary, Language language, Difficulty difficulty) {
        if (language == null) {
            LOGGER.error("language == null");
            throw new IllegalArgumentException("language == null");
        }

        if (difficulty == null) {
            LOGGER.error("difficulty == null");
            throw new IllegalArgumentException("difficulty == null");
        }

        words = EM.createQuery("select re.word from RankEntry re where"
                + " re.rank > :low and re.rank <= :high and LENGTH(re.word) >=3 and "
                + "exists (select de.word from DictionaryEntry de where de.word = re.word and de.dictionary.fromLanguage = re.language and re.language = :language)").setParameter("low", difficulty.getLow()).setParameter("high", difficulty.getHigh()).setParameter("language", language).getResultList();

        if (words.isEmpty()) {
            words = dictionaryService.getWordsFromDictionary(dictionary);
        }
    }

    public void addScoreboardResult(String username, Double examWords, Double wrongWords, String difficulty) {

        ScoreboardEntry se = new ScoreboardEntry();
        se.setUsername(username);
        se.setExamWords(examWords);
        se.setWrongWords(wrongWords);
        se.setDifficulty(difficulty);

        EntityTransaction t = EM.getTransaction();
        t.begin();
        EM.persist(se);
        t.commit();
    }

    public List<String> getScoreboardUsername() {
        return EM.createQuery("select se.username from ScoreboardEntry se order by ((se.examWords - se.wrongWords)*100)/se.examWords desc").getResultList();
    }

    public List<Double> getScoreboardWrongword() {
        return EM.createQuery("select se.wrongWords from ScoreboardEntry se order by ((se.examWords - se.wrongWords)*100)/se.examWords desc").getResultList();
    }

    public List<Double> getScoreboardExamword() {
        return EM.createQuery("select se.examWords from ScoreboardEntry se order by ((se.examWords - se.wrongWords)*100)/se.examWords desc").getResultList();
    }

    public List<String> getScoreboardDifficulty() {
        return EM.createQuery("select se.difficulty from ScoreboardEntry se order by ((se.examWords - se.wrongWords)*100)/se.examWords desc").getResultList();
    }
}
