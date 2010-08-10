package bg.drow.spellbook.ui.desktop.spellcheck;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author iivalchev
 */
public class MisspelledWord {

    private static final Logger LOGGER = LoggerFactory.getLogger(MisspelledWord.class);
    private final String word;
    private final String wordInLowerCase;
    private final Set<Position> occurances = new HashSet<Position>();

    public MisspelledWord(String word, int startIndex) {

        if (startIndex < 0) {
            LOGGER.error("startIndex < 0; startIndex == " + startIndex);
            throw new IllegalArgumentException("startIndex < 0; startIndex == " + startIndex);
        }

        if (word == null) {
            LOGGER.error("word is null");
            throw new NullPointerException("word is null");
        }

        if (word.isEmpty()) {
            LOGGER.error("word is empty");
            throw new IllegalArgumentException("word is empty");
        }

        occurances.add(new Position(startIndex, startIndex + word.length() - 1));
        this.word = word;
        this.wordInLowerCase = word.toLowerCase();
    }

    public boolean isIndexInWord(int index) {
        if (index < 0) {
            return false;
        }
        for (Position p : occurances) {
            if (index >= p.startIndex && index <= p.endIndex) {
                return true;
            }
        }
        return false;
    }

    public void addOccurance(String word, int startIndex) {
        if (startIndex < 0) {
            LOGGER.error("startIndex < 0; startIndex == " + startIndex);
            throw new IllegalArgumentException("startIndex < 0; startIndex == " + startIndex);
        }

        if (word == null) {
            LOGGER.error("word is null");
            throw new NullPointerException("word is null");
        }

        if (word.isEmpty()) {
            LOGGER.error("word is empty");
            throw new IllegalArgumentException("word is empty");
        }

        Position position = new Position(startIndex, startIndex + word.length() - 1);
        if (!occurances.contains(position)) {
            occurances.add(position);
        }
    }

    public String getWord() {
        return word;
    }

    public String getWordInLowerCase() {
        return wordInLowerCase;
    }

    public Set<Position> getOccurances() {
        return Collections.unmodifiableSet(occurances);
    }


    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (this.getClass() != o.getClass()) {
            return false;
        }

        if (this == o) {
            return true;
        }

        if (this.word.equals(((MisspelledWord) o).word)) {
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.word != null ? this.word.hashCode() : 0);
        return hash;
    }

    public class Position {

        private int startIndex;
        private int endIndex;

        public Position(int startIndex, int endIndex) {

            if (startIndex < 0) {
                LOGGER.error("startIndex < 0");
                throw new IllegalArgumentException("startIndex < 0; startIndex == " + startIndex);
            }

            if (startIndex >= endIndex) {
                LOGGER.error("startIndex >= endIndex; startIndex == " + startIndex + "; endIndex == " + endIndex);
                throw new IllegalArgumentException("startIndex >= endIndex; startIndex == " + startIndex + "; endIndex == " + endIndex);
            }

            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }

        public int getEndIndex() {
            return endIndex;
        }

        public int getStartIndex() {
            return startIndex;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }
            if (this.getClass() != o.getClass()) {
                return false;
            }

            if (startIndex == ((Position) o).startIndex && endIndex == ((Position) o).endIndex) {
                return true;
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 83 * hash + this.startIndex;
            hash = 83 * hash + this.endIndex;
            return hash;
        }
    }
}
