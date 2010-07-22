package com.drowltd.spellbook.core.spellcheck;

import com.drowltd.spellbook.core.exception.SpellCheckerException;
import com.drowltd.spellbook.core.model.Language;
import com.stibocatalog.hunspell.Hunspell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

/**
 * @author ikkari
 *         Date: May 27, 2010
 *         Time: 4:31:47 PM
 */
public class HunSpellChecker implements SpellChecker {

    private final Hunspell.Dictionary dictionary;
    private final Language language;
    private static Logger LOGGER = LoggerFactory.getLogger(HunSpellChecker.class);
    private static final String HUNSPELL_DIR = System.getProperty("user.home") + File.separator + ".spellbook" + File.separator + "hundict" + File.separator;
    private static final String HUNSPELL_SRC_DIR = "hundict";
    private static final int BUFFER_SIZE = 4096;


    public HunSpellChecker(Language language) throws SpellCheckerException {
        if (language == null) {
            throw new IllegalArgumentException("language null");
        }

        String dict = language.getPathToHunDictionary();

        LOGGER.info(HUNSPELL_DIR + dict + File.separator + dict + ".dic");

        String dictDir = HUNSPELL_DIR + dict;
        File hundir = new File(dictDir);
        if (!hundir.exists() && !hundir.mkdirs()) {
            throw new SpellCheckerException("can't create hundir in user home");
        }

        String baseFileName = dictDir + File.separator + dict;
        File dicFile = new File(baseFileName + ".dic");
        File affFile = new File(baseFileName + ".aff");

        if (!dicFile.exists())
            copyHDictFile(dict, ".dic", dicFile);

        if (!affFile.exists())
            copyHDictFile(dict, ".aff", affFile);

        try {
            dictionary = Hunspell.getInstance().getDictionary(baseFileName);
        } catch (Exception e) {
            throw new SpellCheckerException(e);
        }
        this.language = language;
    }

    private void copyHDictFile(String dict, String extension, File dicFile) throws SpellCheckerException {
        InputStream in = null;
        OutputStream out = null;
        try {
            if (!dicFile.createNewFile()) throw new SpellCheckerException("can't create hundic file");
            String resourcePath = HUNSPELL_SRC_DIR + "/" + dict + "/" + dict + extension;
            in = HunSpellChecker.class.getClassLoader().getResourceAsStream(resourcePath);
            out = new BufferedOutputStream(new FileOutputStream(dicFile));
            byte[] buffer = new byte[BUFFER_SIZE];
            int n;
            while ((n = in.read(buffer)) > 0) {
                out.write(buffer, 0, n);
            }
        } catch (IOException e) {
            throw new SpellCheckerException(e);
        }
        finally {
            if (in != null)
                try {
                    in.close();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage());
                }
            if (out != null)
                try {
                    out.close();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage());
                }
        }
    }

    @Override
    public boolean misspelled(String word) {
        return (dictionary.misspelled(word.toLowerCase()) && dictionary.misspelled(word));
    }

    @Override
    public List<String> correct(String word) {
        if (!dictionary.misspelled(word)) return Collections.emptyList();

        return dictionary.suggest(word);
    }

    @Override
    public Language getLanguage() {
        return language;
    }
}
