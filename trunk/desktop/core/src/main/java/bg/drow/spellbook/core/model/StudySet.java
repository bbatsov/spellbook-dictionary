package bg.drow.spellbook.core.model;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 * @author Sasho
 */
@Entity(name = "StudySet")
@Table(name = "STUDY_SETS")
public class StudySet extends AbstractEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Dictionary dictionary;
    @OneToMany(mappedBy = "studySet")
    private List<StudySetEntry> studySetEntries = new ArrayList<StudySetEntry>();
    @Column(nullable = false)
    private String name;

    public void setStudySetEntry(StudySetEntry studySetEntries) {
        this.studySetEntries.add(studySetEntries);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Dictionary getDictionary() {
        return dictionary;
    }

    public void setDictionary(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    public List<StudySetEntry> getStudySetEntries() {
        return studySetEntries;
    }
}
