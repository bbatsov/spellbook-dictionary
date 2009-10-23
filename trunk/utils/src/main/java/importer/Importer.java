package importer;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * User: bozhidar
 * Date: Sep 5, 2009
 * Time: 8:44:52 AM
 */
public class Importer {
    private static final String DELIMITER = "<========>";

    public static void main(String[] args) {
        String url = "jdbc:h2:/home/bozhidar/projects/DrowDictionary/ZDict/src/main/resources/db/dictionary";
        String user = "bozhidar";
        String password = "bozhidar";

        try {
            Connection connection = DriverManager.getConnection(url, user, password);

            PreparedStatement ps1 = connection.prepareStatement("drop table bg_en");

            ps1.executeUpdate();

            PreparedStatement ps = connection.prepareStatement("CREATE TABLE BG_EN(" +
                    "ID INT PRIMARY KEY," +
                    "WORD VARCHAR(255) UNIQUE," +
                    "TRANSLATION VARCHAR(20000)" +
                    ")");

            ps.executeUpdate();
            File file = new File("output_bg_en.txt");

            Scanner scanner = new Scanner(file);

            int id = 1;

            while (scanner.hasNext()) {
                String word = scanner.nextLine();
                System.out.println(word);

                StringBuilder translation = new StringBuilder();

                while (true) {
                    String line = scanner.nextLine();
                    if (!line.startsWith(DELIMITER)) {
                        translation.append(line).append("\n");
                    } else {
                        break;
                    }
                }

                System.out.println(translation);

                String tr = translation.toString();

                word = word.replaceAll("'", "''");
                tr = tr.replaceAll("'", "''");

                PreparedStatement ps2 = connection.prepareStatement("insert into BG_EN values(" + id++ + ", '" +
                        word + "', '" + tr + "')");

                ps2.executeUpdate();
            }
        } catch (final SQLException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
