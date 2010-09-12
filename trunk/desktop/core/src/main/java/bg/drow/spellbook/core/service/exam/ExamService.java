package bg.drow.spellbook.core.service.exam;

import bg.drow.spellbook.core.model.Dictionary;
import bg.drow.spellbook.core.model.Difficulty;
import bg.drow.spellbook.core.model.ExamScoreEntry;
import bg.drow.spellbook.core.service.AbstractPersistenceService;

import java.util.List;

/**
 * @author Bozhidar Batsov
 * @since 0.4
 */
public class ExamService extends AbstractPersistenceService {
    private static final ExamService INSTANCE = new ExamService();

    public static final ExamService getInstance() {
        return INSTANCE;
    }

    /**
     * A suitable dictionary for an exam is not special. Further down the line
     * additional restrictions might be added.
     *
     * @return
     */
    public List<Dictionary> getSuitableDictionaries() {
        return null;
    }

    /**
     * Provides a list of appropriate words for an exam.
     *
     * @param dictionary the dictionary to be used
     * @param difficulty the exam's difficulty
     * @param size the size of the exam in words
     * @return a list of suitable words for the requested type of exam
     */
    public List<String> getWordsForExam(Dictionary dictionary, Difficulty difficulty, int size) {
        return null;
    }

    public boolean checkAnswer(Dictionary dictionary, String word) {
        return false;
    }

        public void addScoreboardResult(ExamScoreEntry examScoreEntry) {
//        EntityTransaction t = EM.getTransaction();
//        t.begin();
//        EM.persist(examScoreEntry);
//        t.commit();
    }

    public List<ExamScoreEntry> getExamScores() {
        //return EM.createQuery("select se from ExamScoreEntry se order by se.created asc", ExamScoreEntry.class).getResultList();

        return null;
    }
}
