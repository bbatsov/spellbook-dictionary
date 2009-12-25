package ratings;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Alters exsiting tables by adding column for the rating of the word,
 * and creates rating tables for the words not in the existing tables.
 *
 * @author iivalchev
 */
public class AddRatingsToDB {

    //Set it to your local path
    private static String url = "jdbc:h2:/home/ikkari/NetBeansProjects/SpellBook/dbRatings/dictionary";
    
    private static String user = "bozhidar";
    private static String password = "bozhidar";
    private static String en_bgTable = "EN_BG";
    private static String bg_enTable = "BG_EN";
    private static String enRatingsTable = "SPELLCHECK_EN";
    private static String bgRatingsTable = "SPELLCHECK_BG";

    //Set it to your local path
    private static String pathToEnBigTxt = "/home/ikkari/NetBeansProjects/SpellBook/db/db/big.txt";

    private static void alterTables(Connection connection) throws SQLException {

        PreparedStatement ps1 = connection.prepareStatement("ALTER TABLE " + en_bgTable + " ADD RATING INT UNSIGNED NOT NULL DEFAULT 1");
        PreparedStatement ps2 = connection.prepareStatement("ALTER TABLE " + bg_enTable + " ADD RATING INT UNSIGNED NOT NULL DEFAULT 1");

        ps1.execute();
        ps2.execute();
    }

    private static void createRatingsTables(Connection connection) throws SQLException {
        PreparedStatement ps1 = connection.prepareStatement("CREATE TABLE " + enRatingsTable + " ("
                + "ID INT PRIMARY KEY AUTO_INCREMENT,"
                + "WORD VARCHAR(255) UNIQUE,"
                + "RATING INT NOT NULL DEFAULT 1"
                + ")");

        PreparedStatement ps2 = connection.prepareStatement("CREATE TABLE " + bgRatingsTable + " ("
                + "ID INT PRIMARY KEY AUTO_INCREMENT,"
                + "WORD VARCHAR(255) UNIQUE,"
                + "RATING INT NOT NULL DEFAULT 1"
                + ")");

        ps1.execute();
        ps2.execute();
    }

    private static void addRatings(Connection connection, String transTable, String ratingsTable, String pahToBigTxt) throws IOException, SQLException {

        Map<String, Integer> wordMap = new HashMap<String, Integer>();
        BufferedReader in = null;

        in = new BufferedReader(new FileReader(pahToBigTxt));
        Pattern p = Pattern.compile("\\w+");

        for (String temp = ""; temp != null; temp = in.readLine()) {
            Matcher m = p.matcher(temp.toLowerCase());
            while (m.find()) {
                String word = m.group();
                wordMap.put(word, wordMap.containsKey(word) ? wordMap.get(word) + 1 : 1);
            }
        }
        Iterator<String> iterator = wordMap.keySet().iterator();
        while(iterator.hasNext()){
            String word = iterator.next();
            PreparedStatement statement = connection.prepareStatement("UPDATE "+transTable+" SET RATING = "+wordMap.get(word)+
                                                                        " WHERE WORD = '"+word+"'");
            if(statement.executeUpdate() >= 1){
                iterator.remove();
            }
        }

        iterator = wordMap.keySet().iterator();
        while(iterator.hasNext()){
            String word = iterator.next();
            PreparedStatement statement = connection.prepareStatement("INSERT INTO "+ratingsTable+
                                                                    " (WORD, RATING) VALUES('"+word+"',"+wordMap.get(word)+")");
            statement.execute();
        }

    }

    public static void main(String[] args) {


        try {
            Connection connection = DriverManager.getConnection(url, user, password);

            alterTables(connection);
            createRatingsTables(connection);
            addRatings(connection, en_bgTable, enRatingsTable,pathToEnBigTxt);

        } catch (IOException ex) {
            Logger.getLogger(AddRatingsToDB.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(AddRatingsToDB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
