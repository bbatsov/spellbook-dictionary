/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bg.drow.spellbook.util;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author bozhidar
 */
public class SearchUtilsTest {
    @Test
    public void findInsertionIndex() {
        List<String> words = new ArrayList<String>();

        int index = SearchUtils.findInsertionIndex(words, "test");

        assertEquals(0, index);

        words.add("abal");
        words.add("bala");

        index = SearchUtils.findInsertionIndex(words, "test");

        assertEquals(2, index);

        words.add("titan");

        index = SearchUtils.findInsertionIndex(words, "test");

        assertEquals(2, index);
    }
}
