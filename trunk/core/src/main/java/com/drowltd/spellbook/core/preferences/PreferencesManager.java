/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drowltd.spellbook.core.preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

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

    public String get(Preference key, String def) {
        return preferences.get(key.toString(), def);
    }

    public void put(Preference key, String value) {
        preferences.put(key.toString(), value);
    }

    public int getInt(Preference key, int def) {
        return preferences.getInt(key.toString(), def);
    }

    public void putInt(Preference key, int value) {
        preferences.putInt(key.toString(), value);
    }

    public boolean getBoolean(Preference key, boolean def) {
        return preferences.getBoolean(key.toString(), def);
    }

    public void putBoolean(Preference key, boolean value) {
        preferences.putBoolean(key.toString(), value);
    }

    public void putDouble(Preference key, double value) {
        preferences.putDouble(key.toString(), value);
    }

    public double getDouble(Preference key, double def) {
       return preferences.getDouble(key.toString(), def);
    }

    public void clear() throws BackingStoreException {
        preferences.clear();
    }

    public enum Preference {
        MIN_TO_TRAY,
        CLOSE_TO_TRAY,
        TRAY_POPUP,
        CLIPBOARD_INTEGRATION,
        SHOW_MEMORY_USAGE,
        UI_LANG,
        DEFAULT_DICTIONARY,
        ALWAYS_ON_TOP,
        LOOK_AND_FEEL,
        START_IN_TRAY,
        FONT_NAME,
        FONT_SIZE,
        FONT_STYLE,
        EMPTY_LINE,
        EXAM_WORDS,
        EXAM_DIFFICULTY,
        EXAM_FROM_LANG,
        EXAM_TO_LANG,
        EXAM_TIMER,
        FRAME_X,
        FRAME_Y,
        FRAME_HEIGHT,
        FRAME_WIDTH,
        DIVIDER_LOCATION,
        PATH_TO_DB,
        DICTIONARIES,
        STUDY_SETS,
        LEARNING_IN_ORDER,
        LEARNING_IN_REVERSE_ORDER,
        LEARNING_RANDOM,
        REPEAT_MISSPELLED_WORDS,
        REPEAT_WORDS,
        REMOTE_DB_USERNAME,
        REMOTE_DB_PASSWORD
    }
}
