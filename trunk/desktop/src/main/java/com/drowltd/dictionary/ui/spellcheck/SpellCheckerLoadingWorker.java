package com.drowltd.dictionary.ui.spellcheck;

import com.drowltd.dictionary.core.db.DatabaseService;
import com.drowltd.dictionary.core.db.Dictionary;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A SwingWorker responsible for creating and setting a Map<String, Integer> 
 * needed by the SpellChecker.
 *
 *
 * @author iivalchev
 */
public class SpellCheckerLoadingWorker extends SwingWorker<Void, Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpellCheckerLoadingWorker.class);
    
    private String fileName;

    private SpellCheckFrame frame;

    private final Map<String, Integer> resultMap = new HashMap<String, Integer>();

    //Number of lines in big.txt
    private final double LINES = 128444;

    public SpellCheckerLoadingWorker(String fileName) {
        if (fileName == null) {
            LOGGER.error("fileName is null");
            throw new NullPointerException("fileName is null");
        }
        
        if (fileName.length() == 0) {
            LOGGER.error("fileName is empty");
            throw new IllegalArgumentException("fileName is empty");
        }

        this.fileName = fileName;
    }

    public SpellCheckerLoadingWorker(SpellCheckFrame frame, String fileName) {
        this(fileName);

        if (frame == null) {
            LOGGER.error("frame is null");
            throw new NullPointerException("frame is null");
        }
        this.frame = frame;
    }

    /**
     * This method is only for testing purpose.
     *
     * @return the Map<String, Integer> needed by the SpellChecker
     */
    public Map<String, Integer> getResultMap() {
        return resultMap;
    }

    /**
     *
     * All exceptions throwed by methods invoked in this method
     * are implicitly catched !
     * @return
     * @throws Exception
     */
    @Override
    protected Void doInBackground() throws Exception {
        setProgress(0);

        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(fileName));
            Pattern p = Pattern.compile("\\w+");
            int lines = 0;
            for (String temp = ""; temp != null; temp = in.readLine()) {
                Matcher m = p.matcher(temp.toLowerCase());
                while (m.find()) {
                    String word = m.group();
                        resultMap.put(
                                word, resultMap.containsKey(word) ? resultMap.get(word) + 1 : 1);                    
                }

                setProgress((int) ((++lines / LINES) * 100));
            }

            addMissingWords();
        } catch (FileNotFoundException ex) {
            LOGGER.error("Can not open big.txt");
        } catch (IOException ex) {
            LOGGER.error("Can not read from big.txt");
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                LOGGER.error("Can not close big.txt");
            }
        }

        return null;
    }

    private void addMissingWords() {
        List<String> wordsInDb = DatabaseService.getInstance().getWordsFromDictionary(Dictionary.EN_BG);

        for (String word : wordsInDb) {
            resultMap.put(
                    word, resultMap.containsKey(word) ? resultMap.get(word) + 1 : 1);
        }


    }

    @Override
    protected void done() {
        if (resultMap.isEmpty()) {
            LOGGER.error("The result of the execution of the Worker is empty map");
            throw new IllegalStateException("The result of the execution of the Worker is empty map");
        }
        if (frame != null) {
            frame.initSpellChecker(resultMap);
        }
    }
}
