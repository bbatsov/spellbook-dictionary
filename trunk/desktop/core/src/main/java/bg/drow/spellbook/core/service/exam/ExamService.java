/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bg.drow.spellbook.core.service.exam;

import bg.drow.spellbook.core.service.DictionaryService;
import bg.drow.spellbook.core.service.DictionaryServiceImpl;
import bg.drow.spellbook.core.service.Transactional;
import bg.drow.spellbook.core.model.Dictionary;
import bg.drow.spellbook.core.model.Difficulty;
import bg.drow.spellbook.core.model.ExamScoreEntry;
import bg.drow.spellbook.core.model.Language;
import bg.drow.spellbook.core.service.AbstractPersistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityTransaction;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

/**
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

    private static ExamService instance;

    private ExamService() {
        super(null);

        dictionaryService = DictionaryServiceImpl.getInstance();
    }

    public static ExamService getInstance() {
        if (instance == null) {
            instance = new ExamService();
        }

        return instance;
    }

    /**
     * Gets a random word from the selected dictionary
     *
     * @param dictionary the target dictionary
     */
    public void getExamWord(Dictionary dictionary) {
        examWordIndex = random.nextInt(words.size());
        String translation0;

        while ((translation0 = dictionaryService.getTranslation(words.get(examWordIndex), dictionary)).contains("\u0432\u0436.")) {
            examWordIndex = random.nextInt(words.size());
        }

        translation = translation0;
    }

    /**
     * Returns a word from the selected dictionary
     *
     * @return selected word from the getExamWord method
     */
    public String examWord() {
        return words.get(examWordIndex);
    }

    /**
     * @return the translation of the selected word
     */
    public String getTranslation() {
        return translation;
    }

    /**
     * Splits the whole translation of a word into all possible answers
     */
    public void possibleAnswers() {

        answers = new ArrayList<String>();
        translation = translation.toLowerCase();

        //Removes the unneeded characters from the translation
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
     * Removes all forward slashes and gives all possible combinations for correct answer
     *
     * @param s one of the possible translations
     */
    private void slash(String s) {
        String first = "";
        String last = "";

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

        List<Dictionary> dictionaries = EM.createQuery("select d from Dictionary d where d.fromLanguage = :fromLanguage", Dictionary.class).setParameter("fromLanguage", fromLanguage).getResultList();

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

    @Transactional
    public void addScoreboardResult(ExamScoreEntry examScoreEntry) {
        EntityTransaction t = EM.getTransaction();
        t.begin();
        EM.persist(examScoreEntry);
        t.commit();
    }

    public List<ExamScoreEntry> getExamScores() {
        return EM.createQuery("select se from ExamScoreEntry se order by se.created asc", ExamScoreEntry.class).getResultList();
    }
}
