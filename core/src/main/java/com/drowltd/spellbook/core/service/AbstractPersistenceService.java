/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drowltd.spellbook.core.service;

import com.drowltd.spellbook.core.exception.DictionaryDbLockedException;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author bozhidar
 */
public class AbstractPersistenceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPersistenceService.class);
    protected static EntityManager EM;

    protected AbstractPersistenceService(String dbFile) throws DictionaryDbLockedException {
        // there can be only one entity manager ;-)
        if (EM == null) {
            initEntityManager(dbFile);
        } else {
            LOGGER.info("Entity manager is already initalized");
        }
    }

    protected AbstractPersistenceService(){
    }

    private static void initEntityManager(String dbFile) throws DictionaryDbLockedException {
        LOGGER.info("dictionary database: " + dbFile.replace(".data.db", ""));

        String url = "jdbc:h2:" + dbFile.replace(".data.db", "");

        try {
            // we need to override the db url from persistence.xml
            if (dbFile != null) {
                Map<String, String> properties = new HashMap<String, String>();
                properties.put("hibernate.connection.url", url);

                EM = Persistence.createEntityManagerFactory("Spellbook", properties).createEntityManager();
            } else {
                // if dbFile is null use the default configuration from persistence.xml
                EM = Persistence.createEntityManagerFactory("Spellbook").createEntityManager();
            }
        } catch (javax.persistence.PersistenceException e) {
            if (e.getMessage() != null) {
                if (e.getMessage().contains("Cannot open connection")) {
                    throw new DictionaryDbLockedException();
                }
            }

            e.printStackTrace();
        }
    }
}
