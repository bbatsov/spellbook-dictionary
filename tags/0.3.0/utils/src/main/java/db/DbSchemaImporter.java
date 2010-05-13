package db;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 *
 * @author iivalchev
 */
public class DbSchemaImporter {

    //Set it to your local path
    private static String url = "jdbc:h2:/home/bozhidar/downloads/db/dictionary";
    private static String user = "bozhidar";
    private static String password = "bozhidar";
    private static String imagePath16 = "/home/bozhidar/NetBeansProjects/trunk/desktop/src/main/resources/icons/16x16/";
    private static String imagePath24 = "/home/bozhidar/NetBeansProjects/trunk/desktop/src/main/resources/icons/24x24/";
    private static String bg_en16 = imagePath16 + "bg-en.png";
    private static String en_bg16 = imagePath16 + "en-bg.png";
    private static String bg_flag16 = imagePath16 + "flag_bulgaria.png";
    private static String en_flag16 = imagePath16 + "flag_great_britain.png";
    private static String bg_en24 = imagePath24 + "bg-en.png";
    private static String en_bg24 = imagePath24 + "en-bg.png";

    public static void main(String[] args) throws SQLException, FileNotFoundException, IOException {

        Connection connection = DriverManager.getConnection(url, user, password);

        createAndPopulateLanguages(connection);
        createAndPopulateDictSchema(connection);

    }

    private static void createAndPopulateLanguages(Connection connection) throws SQLException, FileNotFoundException {
        final String createQuery = "CREATE  TABLE IF NOT EXISTS `LANGUAGES` ("
                + "`ID` INT NOT NULL AUTO_INCREMENT PRIMARY KEY ,"
                + "`NAME` VARCHAR(45) NOT NULL ,"
                + "`ALPHABET` VARCHAR(255) NOT NULL ,"
                + "`FLAG_16` BLOB NULL,"
                + "`FLAG_24` BLOB NULL )";

        final String populateQuery0 = "INSERT INTO LANGUAGES ( ID, NAME , ALPHABET, FLAG_16 ) VALUES(1,'English','abcdefghijklmnopqrstuvwxyz', ?)";
        final String populateQuery1 = "INSERT INTO LANGUAGES ( ID, NAME , ALPHABET, FLAG_16 ) VALUES(2,'Bulgarian','абвгдежзийклмнопрстуфхцчшщъьюя', ?)";

        connection.prepareStatement(createQuery).execute();

        final PreparedStatement ps0 = connection.prepareStatement(populateQuery0);
        ps0.setBinaryStream(1, new BufferedInputStream(new FileInputStream(en_flag16)));
        ps0.execute();

        final PreparedStatement ps1 = connection.prepareStatement(populateQuery1);
        ps1.setBinaryStream(1, new BufferedInputStream(new FileInputStream(bg_flag16)));
        ps1.execute();
    }

    private static void createAndPopulateDictSchema(Connection connection) throws SQLException, FileNotFoundException {
        final String createQuery = "CREATE  TABLE IF NOT EXISTS `DICTIONARY_SCHEMA` ("
                + "`ID` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,"
                + "`LANGUAGE_FROM` INT NOT NULL ,"
                + "`lANGUAGE_TO` INT NOT NULL ,"
                + "`TRANSLATIONS_TABLE` VARCHAR(5) NOT NULL ,"
                + "`RATINGS_TABLE` VARCHAR(45) NOT NULL ,"
                + "`FLAG_16` BLOB NULL ,"
                + "`FLAG_24` BLOB NULL ,"
                + "FOREIGN KEY (`LANGUAGE_FROM` , `lANGUAGE_TO` )"
                + "REFERENCES `LANGUAGES` (`ID` , `ID` ))";

        final String populateQuery0 = "INSERT INTO DICTIONARY_SCHEMA ( ID, LANGUAGE_FROM , LANGUAGE_TO , TRANSLATIONS_TABLE , RATINGS_TABLE, FLAG_16, FLAG_24) "
                + "VALUES(1,1,2,'EN_BG','SPELLCHECK_EN', ?, ?)";
        final String populateQuery1 = "INSERT INTO DICTIONARY_SCHEMA ( ID, LANGUAGE_FROM , LANGUAGE_TO , TRANSLATIONS_TABLE , RATINGS_TABLE, FLAG_16, FLAG_24) "
                + "VALUES(2,2,1,'BG_EN','SPELLCHECK_BG', ?, ?)";


        connection.prepareStatement(createQuery).execute();

        final PreparedStatement ps0 = connection.prepareStatement(populateQuery0);
        ps0.setBinaryStream(1, new BufferedInputStream(new FileInputStream(en_bg16)));
        ps0.setBinaryStream(2, new BufferedInputStream(new FileInputStream(en_bg24)));
        ps0.execute();

        final PreparedStatement ps1 = connection.prepareStatement(populateQuery1);
        ps1.setBinaryStream(1, new BufferedInputStream(new FileInputStream(bg_en16)));
        ps1.setBinaryStream(2, new BufferedInputStream(new FileInputStream(bg_en24)));
        ps1.execute();

    }
}
