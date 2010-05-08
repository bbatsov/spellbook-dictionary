/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drowltd.spellbook.core.service;

import com.drowltd.spellbook.core.exception.AuthenticationException;
import com.drowltd.spellbook.core.exception.UpdateServiceException;
import com.drowltd.spellbook.core.model.Language;
import com.drowltd.spellbook.core.model.RemoteDictionary;
import com.drowltd.spellbook.core.model.RemoteDictionaryEntry;
import com.drowltd.spellbook.core.model.RevisionEntry;
import com.drowltd.spellbook.core.model.UpdateEntry;

import java.sql.SQLException;
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
    private static RemoteDictionary dictionary;
    static UpdateEntry updateEntry;
    static UpdateEntry updateEntry0;
    static RemoteDictionaryEntry entry;
    static RemoteDictionaryEntry entry0;
    static RevisionEntry revisionEntry0;
    static RevisionEntry revisionEntry1;
    static UpdateService updateService;
    static String word = "upadded";
    static String word0 = "remoteEntry00";
    static String translation = "translation0";
    static String dictionaryName = "English-Bulgarian";
    static Date testDate;

    public UpdateServiceTest() {
    }

    //@BeforeClass
    public static void initEM() throws AuthenticationException, UpdateServiceException, SQLException {
        DictionaryService.init("/opt/spellbook/db/spellbook.data.db");
        updateService = UpdateService.getInstance("iivalchev", "pass");

        dictionary = new RemoteDictionary();
        dictionary.setName("English-Bulgarian");
        dictionary.setFromLanguage(Language.ENGLISH);
        dictionary.setToLanguage(Language.BULGARIAN);

        UpdateService.EM_REMOTE.getTransaction().begin();
        UpdateService.EM_REMOTE.persist(dictionary);
        UpdateService.EM_REMOTE.getTransaction().commit();
    }

    @BeforeClass
    public static void init() throws UpdateServiceException, InterruptedException, AuthenticationException, SQLException {
        testDate = new Date();
        Random random = new Random();
        translation = translation + (random.nextLong());
        word = word + random.nextLong();
        word0 = word0 + random.nextLong();

        initEM();

        em = UpdateService.EM_REMOTE;

        EntityTransaction t = em.getTransaction();
        t.begin();

        entry = new RemoteDictionaryEntry();
        entry.setRemoteDictionary(dictionary);
        entry.setWord(word);

        revisionEntry0 = new RevisionEntry();
        revisionEntry0.setTranslation(translation);
        revisionEntry0.setRemoteDictionaryEntry(entry);

        updateEntry = new UpdateEntry();
        updateEntry.addRemoteDictionaryEntry(entry);

        entry0 = new RemoteDictionaryEntry();
        entry0.setWord(word0);
        entry0.setRemoteDictionary(dictionary);

        revisionEntry1 = new RevisionEntry();
        revisionEntry1.setRemoteDictionaryEntry(entry0);
        revisionEntry1.setTranslation(translation);

        updateEntry0 = new UpdateEntry();
        updateEntry0.addRemoteDictionaryEntry(entry0);


        em.persist(updateEntry);
        em.persist(entry);
        Thread.sleep(2000);
        em.persist(updateEntry0);
        em.persist(entry0);
        em.persist(revisionEntry0);
        em.persist(revisionEntry1);
        t.commit();

    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    // @Test
    public void testRemoteEntry() {
        //assertTrue(!updateEntry.getRemoteDictionaryEntries().isEmpty());
    }

    //@Test
    public void testUpdate() throws InterruptedException {
        DictionaryService.init("/opt/spellbook/db/spellbook.data.db");
        try {
            updateService = UpdateService.getInstance();
        } catch (UpdateServiceException ex) {
            ex.getCause().printStackTrace();
        }
        assertTrue("no updates available", updateService.checkForUpdates());
        //assertFalse("no updates available",updateService.checkForUpdates());
        updateService.update();
        DictionaryService service = DictionaryService.getInstance();
        service.getTranslation(word, service.getDictionary(dictionaryName));
        assertTrue(service.getRatings(Language.ENGLISH).containsKey(word));
    }

    @Test
    public void testCommit() {
        updateService.commit();
        assertTrue("no updates available", updateService.checkForUpdates());
    }
}
