package bg.drowltd.spellbook.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

/**
 *
 * @author iivalchev
 */
@Entity(name = "RankEntry")
@Table(name = "RANK_ENTRIES")
public class RankEntry extends AbstractEntity {
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "word_language", nullable=false)
    private Language language;

    @Column(nullable = false)
    private String word;

    @Column(nullable = false)
    private int rank;

    public RankEntry() {
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language lang) {
        this.language = lang;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (o == this) {
            return true;
        }

        if (o.getClass() != getClass()) {
            return false;
        }

        RankEntry other = (RankEntry) o;

        if (!word.equals(other.word)) {
            return false;
        }

        if (language != other.language) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (this.language != null ? this.language.hashCode() : 0);
        hash = 67 * hash + (this.word != null ? this.word.hashCode() : 0);
        return hash;
    }
}
