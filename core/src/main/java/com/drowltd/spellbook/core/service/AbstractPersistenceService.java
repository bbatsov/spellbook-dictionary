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


    protected AbstractPersistenceService(String dictDbFile) throws DictionaryDbLockedException {
        LOGGER.info("dictionary database: " + dictDbFile.replace(".data.db", ""));

        String url = "jdbc:h2:" + dictDbFile.replace(".data.db", "");

        try {
            // we need to override the db url from persistence.xml
            Map<String, String> properties = new HashMap<String, String>();
            properties.put("hibernate.connection.url", url);

            EM = Persistence.createEntityManagerFactory("Spellbook", properties).createEntityManager();
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
