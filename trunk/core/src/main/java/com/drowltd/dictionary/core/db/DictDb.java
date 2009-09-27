package com.drowltd.dictionary.core.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * User: bozhidar
 * Date: Sep 6, 2009
 * Time: 4:43:46 PM
 */
public class DictDb {
    private static DictDb instance;

    private Connection connection;

    private String selectedDictionary;

    private DictDb(String dictDbFile) {
        System.out.println("db_url " + dictDbFile.replace(".data.db", ""));

        String url = "jdbc:h2:" + dictDbFile.replace(".data.db", "");
        String user = "bozhidar";
        String password = "bozhidar";

        selectedDictionary = "EN_BG";

        try {
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public static void init(String dictDbFile) {
        instance = new DictDb(dictDbFile);
    }

    public static DictDb getInstance() {
        return instance;
    }

    public List<String> getWordsFromSelectedDictionary() {
        final List<String> words = new ArrayList<String>();

        try {
            PreparedStatement ps = connection.prepareStatement("SELECT word FROM " + selectedDictionary);

            final ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                final String word = rs.getString("WORD");
                words.add(word);
            }
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return words;
    }

    public String getTranslation(String word) {
        try {
            PreparedStatement ps = connection.prepareStatement("select translation from " + selectedDictionary + " where word='" + word.replaceAll("'", "''") + "'");

            final ResultSet resultSet = ps.executeQuery();

            resultSet.next();
            return resultSet.getString("TRANSLATION");
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return null;
    }

    public String getSelectedDictionary() {
        return selectedDictionary;
    }

    public void setSelectedDictionary(String selectedDictionary) {
        this.selectedDictionary = selectedDictionary;
    }
}
