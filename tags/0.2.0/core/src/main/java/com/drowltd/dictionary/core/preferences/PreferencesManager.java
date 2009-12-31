/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drowltd.dictionary.core.preferences;

import java.util.prefs.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author bozhidar
 */
public class PreferencesManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(PreferencesManager.class);

    private static PreferencesManager instance;

    private Preferences preferences;

    private PreferencesManager(Class<?> mainClass) {
        preferences = Preferences.userNodeForPackage(mainClass);
    }

    public static void init(Class<?> mainClass) {
        if (instance == null) {
            instance = new PreferencesManager(mainClass);
        } else {
            LOGGER.info("Preferences manager is already initialized");
        }
    }

    public static PreferencesManager getInstance() {
        return instance;
    }

    public String get(String key, String def) {
        return preferences.get(key, def);
    }

    public void put(String key, String value) {
        preferences.put(key, value);
    }

    public int getInt(String key, int def) {
        return preferences.getInt(key, def);
    }

    public void putInt(String key, int value) {
        preferences.putInt(key, value);
    }

    public boolean getBoolean(String key, boolean def) {
        return preferences.getBoolean(key, def);
    }

    public void putBoolean(String key, boolean value) {
        preferences.putBoolean(key, value);
    }
}
