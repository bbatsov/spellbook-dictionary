package com.drowltd.dictionary.core.db;

import com.drowltd.dictionary.core.db.DictionaryService.DictionaryConfig;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 *
 * @author iivalchev
 */
public abstract class AbstractDictionaryServiceTest {

    static Connection connection;
    static Map<SDictionary, DictionaryConfig> dictConfigMap = new HashMap<SDictionary, DictionaryConfig>();
    static Language english;
    static Language bulgarian;
    static SDictionary dictionaryEN_BG;
    static SDictionary dictionaryBG_EN;


    @BeforeClass
    public static void setUpClass() throws Exception {

        final String pathToDB = "resources/db.sql";
        String url = "jdbc:h2:mem:db1";

        connection = DriverManager.getConnection(url);
        connection.prepareStatement(readDbFromFile(pathToDB)).execute();

        final ImageIcon imageIcon = new ImageIcon("");

        english = new Language("English", "abcdefghijklmnopqrstuvwxyz",imageIcon);
        bulgarian = new Language("Bulgarian", "абвгдежзийклмнопрстуфхцчшщъьюя",imageIcon);

        
        dictionaryEN_BG = new SDictionary("English-Bulgarian", english, bulgarian, imageIcon, imageIcon);
        dictionaryBG_EN = new SDictionary("Bulgarian-English", bulgarian, english, imageIcon, imageIcon);

        DictionaryConfig configEN_BG = new DictionaryConfig(dictionaryEN_BG, "EN_BG", "SPELLCHECK_EN");
        DictionaryConfig configBG_EN = new DictionaryConfig(dictionaryBG_EN, "BG_EN", "SPELLCHECK_BG");


        dictConfigMap.put(dictionaryEN_BG, configEN_BG);
        dictConfigMap.put(dictionaryBG_EN, configBG_EN);
    }

    private static String readDbFromFile(String file) throws FileNotFoundException, IOException {
        assert file != null : "file is null";
        assert !file.isEmpty() : "file is empty";


        final InputStream stream = AbstractDictionaryServiceTest.class.getResourceAsStream(file);
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

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
}
