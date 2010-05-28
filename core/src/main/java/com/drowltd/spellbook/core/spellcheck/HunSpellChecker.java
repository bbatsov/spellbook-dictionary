package com.drowltd.spellbook.core.spellcheck;

import com.drowltd.spellbook.core.exception.SpellCheckerException;
import com.drowltd.spellbook.core.model.Language;
import com.stibocatalog.hunspell.Hunspell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
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
    private static SpellChecker INSTANCE;
    private static Logger LOGGER = LoggerFactory.getLogger(HunSpellChecker.class);
    private static final String HUNSPELL_DIC_DIR = System.getProperty("user.home") + File.separator + ".spellbook" + File.separator + "hundict" + File.separator;

    public static void init(Language language) throws SpellCheckerException {

        if (INSTANCE == null || language != INSTANCE.getLanguage()) {
            INSTANCE = new HunSpellChecker(language);
        }
    }

    public static SpellChecker getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException("no spellchecker initialized");
        }

        return INSTANCE;
    }


    public HunSpellChecker(Language language) throws SpellCheckerException {
        if (language == null) {
            throw new IllegalArgumentException("language null");
        }

        String dict = language.getPathToHunDictionary();

        LOGGER.info(HUNSPELL_DIC_DIR + dict + File.separator + dict + ".dic");

        String dictDir = HUNSPELL_DIC_DIR + dict;
        File hundir = new File(dictDir);
        if (!hundir.exists()) {
            hundir.mkdirs();
        }

        String baseFileName = dictDir + File.separator + dict;
        File dicFile = new File(baseFileName + ".dic");
        File affFile = new File(baseFileName + ".aff");

        if (!dicFile.exists()) {
            InputStream dicInStream = null;
            OutputStream dicOutStream = null;
            try {
                dicFile.createNewFile();
                dicInStream = HunSpellChecker.class.getClassLoader().getResourceAsStream(dict + "/" + dict + ".dic");
                dicOutStream = new BufferedOutputStream(new FileOutputStream(dicFile));
                byte[] data = new byte[4096];
                int len = 0;
                while ((len = dicInStream.read(data)) > 0) {
                    dicOutStream.write(data, 0, len);
                }
            } catch (IOException e) {
                throw new SpellCheckerException(e);
            }
            finally {
                if (dicInStream != null)
                    try {
                        dicInStream.close();
                    } catch (IOException e) {
                        throw new SpellCheckerException(e);
                    }
                if (dicOutStream != null)
                    try {
                        dicOutStream.close();
                    } catch (IOException e) {
                        throw new SpellCheckerException(e);
                    }
            }

        }

        if (!affFile.exists()) {
            InputStream affInStream = null;
            OutputStream affOutStream = null;
            try {
                affFile.createNewFile();
                affInStream = HunSpellChecker.class.getClassLoader().getResourceAsStream(dict + "/" + dict + ".aff");
                affOutStream = new BufferedOutputStream(new FileOutputStream(affFile));
                byte[] data = new byte[4096];
                int len = 0;
                while ((len = affInStream.read(data)) > 0) {
                    affOutStream.write(data, 0, len);
                }
            } catch (IOException e) {
                throw new SpellCheckerException(e);
            }
            finally {
                if (affInStream != null)
                    try {
                        affInStream.close();
                    } catch (IOException e) {
                        throw new SpellCheckerException(e);
                    }
                if (affOutStream != null)
                    try {
                        affOutStream.close();
                    } catch (IOException e) {
                        throw new SpellCheckerException(e);
                    }
            }
        }

        try {
            dictionary = Hunspell.getInstance().getDictionary(baseFileName);
        } catch (Exception e) {
           throw new SpellCheckerException(e);
        }
        this.language = language;
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
