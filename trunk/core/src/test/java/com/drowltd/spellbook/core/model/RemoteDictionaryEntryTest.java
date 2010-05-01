/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drowltd.spellbook.core.model;

import com.drowltd.spellbook.core.service.DictionaryService;
import com.drowltd.spellbook.core.service.UpdateService;
import javax.persistence.EntityManager;
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
public class RemoteDictionaryEntryTest {

    static UpdateService updateService;
    static EntityManager em;
    static RemoteDictionary dictionary;
    static UpdateEntry updateEntry;
    static RemoteDictionaryEntry dictionaryEntry;
    static RevisionEntry revisionEntry0;
    static RevisionEntry revisionEntry1;
    static String word0 = "word0";
    static String word1 = "word1";
    static String translation = "translation";


    public RemoteDictionaryEntryTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        DictionaryService.init("/opt/spellbook/db/spellbook.data.db");
        updateService = UpdateService.getInstance("iivalchev", "pass");
        em = UpdateService.getEM_REMOTE();

        init();
    }

    private static void init() throws InterruptedException{
        dictionary = new RemoteDictionary();
        dictionary.setName("English-Bulgarian");
        dictionary.setFromLanguage(Language.ENGLISH);
        dictionary.setToLanguage(Language.BULGARIAN);

        

        dictionaryEntry = new RemoteDictionaryEntry();
        dictionaryEntry.setWord(word0);

        revisionEntry0 = new RevisionEntry();
        revisionEntry0.setTranslation(translation);
        revisionEntry0.setRemoteDictionaryEntry(dictionaryEntry);

        revisionEntry1 = new RevisionEntry();
        revisionEntry1.setTranslation(translation);
        revisionEntry1.setRemoteDictionaryEntry(dictionaryEntry);

        dictionaryEntry.setRemoteDictionary(dictionary);
        dictionaryEntry.addRevision(revisionEntry0);
        dictionaryEntry.addRevision(revisionEntry1);

        updateEntry = new UpdateEntry();
        updateEntry.addRemoteDictionaryEntry(dictionaryEntry);

        em.getTransaction().begin();
        em.persist(dictionary);
        em.persist(revisionEntry0);
        Thread.sleep(2000);
        em.persist(revisionEntry1);
        em.persist(dictionaryEntry);
        em.persist(updateEntry);
        em.getTransaction().commit();

    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }


    @Test
    public void testGetRevisions() {
        assertEquals(revisionEntry1, em.createNamedQuery("RemoteDictionaryEntry.getLastRevision", RevisionEntry.class).setParameter("remoteDictionaryEntry", dictionaryEntry).getSingleResult());
    }

}
