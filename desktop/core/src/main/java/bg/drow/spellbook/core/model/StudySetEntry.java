package bg.drow.spellbook.core.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity(name = "StudySetEntry")
@Table(name = "STUDY_ENTRIES")

/**
 *
 * @author Sasho
 */
public class StudySetEntry extends AbstractEntity {

    @ManyToOne
    @JoinColumn(name="STUDY_SET_ID")
    private StudySet studySet;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="DICTIONARY_ENTRY_ID")
    private DictionaryEntry dictionaryEntry;

    public StudySetEntry() {
    }

    public StudySet getStudySet() {
        return studySet;
    }

    public void setStudySet(StudySet studySet) {
        this.studySet = studySet;
    }

    public DictionaryEntry getDictionaryEntry(){
        return dictionaryEntry;
    }

    public void setDictionaryEntry(DictionaryEntry dictionaryEntry){
        this.dictionaryEntry = dictionaryEntry;
    }
}
