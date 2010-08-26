package bg.drow.spellbook.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

/**
 * @author Bozhidar Batsov
 */
@Entity(name = "ExamScoreEntry")
@Table(name = "EXAM_SCORE_ENTRIES")
public class ExamScoreEntry extends AbstractEntity {

    @Column(name = "total_words", nullable = false)
    private int totalWords;

    @Column(name = "correct_words", nullable = false)
    private int correctWords;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private Difficulty difficulty;

    @Column(name = "from_lang", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private Language fromLanguage;

    @Column(name = "to_lang", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private Language toLanguage;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setTotalWords(int totalWords) {
        this.totalWords = totalWords;
    }

    public long getTotalWords() {
        return totalWords;
    }

    public void setCorrectWords(int correctWords) {
        this.correctWords = correctWords;
    }

    public long getCorrectWords() {
        return correctWords;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public Language getFromLanguage() {
        return fromLanguage;
    }

    public void setFromLanguage(Language fromLanguage) {
        this.fromLanguage = fromLanguage;
    }

    public Language getToLanguage() {
        return toLanguage;
    }

    public void setToLanguage(Language toLanguage) {
        this.toLanguage = toLanguage;
    }

    public int getScore() {
        return (int) (correctWords / (double) totalWords * 100);
    }

    public Object[] toArray() {
        return new Object[]{name, fromLanguage.toString(), toLanguage.toString(), getScore()};
    }
}
