package bg.drow.spellbook.core.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Sasho
 */
public class StudySet extends AbstractEntity {

    private Dictionary dictionary;
    private List<StudySetEntry> studySetEntries = new ArrayList<StudySetEntry>();
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
