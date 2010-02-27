/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drowltd.spellbook.util;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author bozhidar
 */
public class SearchUtils {
    public static int findInsertionIndex(List<String> list, String elem) {
        return -(Collections.binarySearch(list, elem, new CaseInsensitiveStringComparator())) - 1;
    }
}
