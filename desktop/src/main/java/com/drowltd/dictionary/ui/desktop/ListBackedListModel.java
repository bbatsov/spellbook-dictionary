package com.drowltd.dictionary.ui.desktop;

import java.util.List;
import javax.swing.AbstractListModel;

/**
 * A simple JList model backed by a list collection.
 *
 * @author Bozhidar Batsov
 * @since 0.1
 */
public class ListBackedListModel extends AbstractListModel {
    private List<String> backingList;

    public ListBackedListModel(List<String> wordsList) {
        this.backingList = wordsList;
    }

    @Override
    public int getSize() {
        return backingList.size();
    }

    @Override
    public Object getElementAt(int index) {
        return backingList.get(index);
    }
}
