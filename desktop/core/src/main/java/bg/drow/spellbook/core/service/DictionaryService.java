package bg.drow.spellbook.core.service;

import bg.drow.spellbook.core.model.Dictionary;
import bg.drow.spellbook.core.model.DictionaryEntry;
import bg.drow.spellbook.core.model.Language;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:bozhidar@drow.bg">Bozhidar Batsov</a>
 * @since 0.4
 */
public interface DictionaryService {
    List<Dictionary> getDictionaries();

    Dictionary getDictionary(String dictionaryName);

    List<String> getWordsFromDictionary(Dictionary d);

    String getTranslation(String word, Dictionary d);

    @Transactional
    void addWord(String word, String translation, Dictionary d);

    @Transactional
    void updateWord(String word, String translation, Dictionary d);

    @Transactional
    void deleteWord(String word, Dictionary dictionary);

    boolean containsWord(String word, Dictionary d);

    Map<String, Integer> getRatings(Language language);

    Dictionary getDictionary(Language languageFrom, Language languageTo);

    @Transactional
    void addRankEntry(String word, Language language);

    @Transactional
    void addRankEntry(String word, Language language, int rank);

    boolean isComplemented(Dictionary dictionary);

    Dictionary getComplement(Dictionary dictionary);

    @Transactional
    Dictionary createDictionary(Language from, Language to, String name, boolean special, byte[] smallIcon, byte[] bigIcon);

    @Transactional
    void addWords(List<DictionaryEntry> dictionaryEntries);
}
