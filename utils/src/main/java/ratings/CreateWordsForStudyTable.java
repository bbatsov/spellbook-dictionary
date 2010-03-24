/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ratings;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bozhidar
 */
public class CreateWordsForStudyTable {

    private static String url = "jdbc:h2:/opt/spellbook/db/spellbook";
    private static String user = "spellbook";
    private static String password = "spellbook";
    private static String tableName = "WORDS_FOR_STUDY";

    public static void main(String[] args) {
        try {
            Connection connection = DriverManager.getConnection(url, user, password);

            PreparedStatement ps1 = connection.prepareStatement("CREATE TABLE " + tableName + " ("
                    + "ID BIGINT PRIMARY KEY ,"
                    + "MODIFIED DATETIME,"
                    + "CREATED DATETIME,"
                    + "WORD VARCHAR(255) UNIQUE,"
                    + "WORD_TRANSLATION VARCHAR(1000) NOT NULL"
                    + ")");

            ps1.execute();
        } catch (SQLException ex) {
            Logger.getLogger(AddRatingsToDB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
