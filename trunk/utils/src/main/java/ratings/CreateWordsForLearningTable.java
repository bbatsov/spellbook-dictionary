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
public class CreateWordsForLearningTable {
     private static String url = "jdbc:h2:/home/bozhidar/downloads/db/dictionary";

    private static String user = "bozhidar";
    private static String password = "bozhidar";
    private static String tableName = "WORDS_FOR_LEARNING";

    public static void main(String[] args) {
        try {
            Connection connection = DriverManager.getConnection(url, user, password);

            PreparedStatement ps1 = connection.prepareStatement("CREATE TABLE " + tableName + " ("
                + "ID INT PRIMARY KEY AUTO_INCREMENT,"
                + "WORD VARCHAR(255) UNIQUE,"
                + "TRANSLATION VARCHAR(1000) NOT NULL"
                + ")");

            ps1.execute();
        } catch (SQLException ex) {
            Logger.getLogger(AddRatingsToDB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
