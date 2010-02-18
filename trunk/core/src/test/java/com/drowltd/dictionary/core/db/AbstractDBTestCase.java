package com.drowltd.dictionary.core.db;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author iivalchev
 */
public abstract class AbstractDBTestCase {

    static Connection connection;

    public static void setUpDB() throws SQLException {

        String url = "jdbc:h2:mem:db1";
        connection = DriverManager.getConnection(url);

    }

    static void initDB() throws IOException, SQLException {
        final String pathToDB = "resources/db.sql";
        connection.prepareStatement(readDbFromFile(pathToDB)).execute();
    }

    static String readDbFromFile(String file) throws FileNotFoundException, IOException {
        assert file != null : "file is null";
        assert !file.isEmpty() : "file is empty";


        final InputStream stream = AbstractDBTestCase.class.getResourceAsStream(file);
        assert stream != null : "stream is null";

        final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

        String line = null;
        StringBuilder builder = new StringBuilder();
        try {
            for (;;) {
                line = reader.readLine();
                if (line == null) {
                    break;
                }
                builder.append(line);
            }
        } finally {
            reader.close();
        }

        assert builder.length() > 0 : "file not read";

        return builder.toString();
    }
}
