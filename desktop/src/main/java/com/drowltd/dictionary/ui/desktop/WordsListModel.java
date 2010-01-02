package com.drowltd.dictionary.ui.desktop;

import java.util.List;
import javax.swing.AbstractListModel;

/**
 *
 * @author bozhidar
 */
public class WordsListModel extends AbstractListModel {
    private List<String> wordsList;

    public WordsListModel(List<String> wordsList) {
        this.wordsList = wordsList;
    }

    @Override
    public int getSize() {
        return wordsList.size();
    }

    @Override
    public Object getElementAt(int index) {
        return wordsList.get(index);
    }
}
