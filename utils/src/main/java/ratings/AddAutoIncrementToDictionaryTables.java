/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ratings;

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
public class AddAutoIncrementToDictionaryTables {
 private static String url = "jdbc:h2:/opt/spellbook/db/dictionary";

    private static String user = "bozhidar";
    private static String password = "bozhidar";

    public static void main(String[] args) {
        try {
            Connection connection = DriverManager.getConnection(url, user, password);

            PreparedStatement ps1 = connection.prepareStatement("ALTER TABLE EN_BG ALTER COLUMN ID INT AUTO_INCREMENT");

            ps1.execute();

            ps1 = connection.prepareStatement("ALTER TABLE BG_EN ALTER COLUMN ID INT AUTO_INCREMENT");

            ps1.execute();
        } catch (SQLException ex) {
            Logger.getLogger(AddRatingsToDB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
