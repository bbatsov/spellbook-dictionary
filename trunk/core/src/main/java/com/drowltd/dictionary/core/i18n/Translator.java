package com.drowltd.dictionary.core.i18n;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * A simple wrapper around resource bundles, useful for translation purposes.
 *
 * @author Bozhidar Batsov
 * @since 0.1
 */
public class Translator {
    private ResourceBundle resourceBundle;

    private static List<Translator> translators = new ArrayList<Translator>();

    public Translator(final String resourceBundleName) {
        resourceBundle = ResourceBundle.getBundle("i18n/" + resourceBundleName, Locale.getDefault());

        if (resourceBundle == null) {
            throw new IllegalArgumentException("No such resource bundle - " + resourceBundleName);
        }
    }

    public String translate(String resourceKey) {
        return resourceBundle.getString(resourceKey);
    }

    // TODO implement factory method with caching
    public static Translator getTranslator(final String resourceBundleName) {
        return null;
    }

    // TODO reinit translators
    public static void reset() {

    }
}
