/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drowltd.spellbook.core.service;

import com.drowltd.spellbook.core.exception.DictionaryDbLockedException;
import com.drowltd.spellbook.core.model.Dictionary;
import com.drowltd.spellbook.core.model.Language;
import com.drowltd.spellbook.core.model.RemoteDictionaryEntry;
import com.drowltd.spellbook.core.model.UpdateEntry;
import java.util.Date;
import java.util.Random;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ikkari
 */
@Ignore
public class UpdateServiceTest {

    static EntityManager em;
    private static Dictionary dictionary;
    static UpdateEntry updateEntry;
    static RemoteDictionaryEntry entry;
    static UpdateService updateService;
    static String word = "remoteEntry0";
    static String translation = "translation0";
    static String dictionaryName = "English-Bulgarian";
    static Date testDate;

    public UpdateServiceTest() {
    }

    @BeforeClass
    public static void init() throws Exception, DictionaryDbLockedException {
        testDate = new Date();
        translation = translation+(new Random().nextLong());
        
        DictionaryService.init("/opt/spellbook/db/spellbook.data.db");
        updateService = UpdateService.getInstance();
        
        em = UpdateService.EM_REMOTE;

        EntityTransaction t = em.getTransaction();
        t.begin();
        
        updateEntry = new UpdateEntry();

        dictionary = new Dictionary();
        dictionary.setName("English-Bulgarian");
        dictionary.setIconName("en-bg.png");
        dictionary.setFromLanguage(Language.ENGLISH);
        dictionary.setToLanguage(Language.BULGARIAN);

        entry = new RemoteDictionaryEntry();
        entry.setUpdateEntry(updateEntry);
        
        entry.setDictionary(dictionary);
        entry.setWord(word);
        entry.setTranslation(translation);
     
        em.persist(dictionary);
        em.persist(updateEntry);
        em.persist(entry);
        
        t.commit();

    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    //@Test
    public void testRemoteEntry(){
        assertTrue(!updateEntry.getRemoteDictionaryEntries().isEmpty());
    }


    @Test
    public void testUpdate(){
        assertTrue("no updates available",updateService.checkForUpdates(testDate));
        updateService.update();
        DictionaryService service = DictionaryService.getInstance();
        service.getTranslation(word, service.getDictionary(dictionaryName));
    }
}
