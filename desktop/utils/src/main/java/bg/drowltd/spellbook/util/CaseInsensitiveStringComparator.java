/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bg.drowltd.spellbook.util;

import java.util.Comparator;

/**
 *
 * @author bozhidar
 */
public class CaseInsensitiveStringComparator implements Comparator<String> {

    @Override
    public int compare(String o1, String o2) {
        return o1.compareToIgnoreCase(o2);
    }
}
