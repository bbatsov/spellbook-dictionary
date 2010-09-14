package bg.drow.spellbook.core.model;

import com.google.common.collect.Lists;

import java.util.List;

/**
 *
 * @author Sasho
 */
public class StudySet extends AbstractEntity {

    private Dictionary dictionary;
    private List<StudySetEntry> studySetEntries = Lists.newArrayList();
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
