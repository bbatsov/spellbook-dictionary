/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drowltd.spellbook.core.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author bozhidar
 */
public class AbstractPersistenceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPersistenceService.class);
    protected static EntityManager EM;

    protected AbstractPersistenceService(String dbFile) {
        // there can be only one entity manager ;-)
        if (EM == null) {
            initEntityManager(dbFile);
        } else {
            LOGGER.info("Entity manager is already initalized");
        }
    }

    protected AbstractPersistenceService() {
    }

    private static void initEntityManager(String dbFile) {
        LOGGER.info("dictionary database: " + dbFile.replace(".h2.db", ""));

        String url = "jdbc:h2:" + dbFile.replace(".h2.db", "");

        // we need to override the db url from persistence.xml
        if (dbFile != null) {
            Map<String, String> properties = new HashMap<String, String>();
            properties.put("hibernate.connection.url", url);

            EM = Persistence.createEntityManagerFactory("Spellbook", properties).createEntityManager();
        } else {
            // if dbFile is null use the default configuration from persistence.xml
            EM = Persistence.createEntityManagerFactory("Spellbook").createEntityManager();
        }

    }
}
