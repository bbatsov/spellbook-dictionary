package com.drowltd.dictionary.core.i18n;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * User: bozhidar
 * Date: Sep 18, 2009
 * Time: 10:55:37 AM
 */
public class Translator {
    private ResourceBundle resourceBundle;

    public Translator(final String resourceBundleName) {
        resourceBundle = ResourceBundle.getBundle("i18n/" + resourceBundleName, Locale.getDefault());

        if (resourceBundle == null) {
            throw new IllegalArgumentException("No such resource bundle - " + resourceBundleName);
        }
    }

    public String translate(String resourceKey) {
        return resourceBundle.getString(resourceKey);
    }
}
