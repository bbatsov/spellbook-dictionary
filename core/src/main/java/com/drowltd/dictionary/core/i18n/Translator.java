package com.drowltd.dictionary.core.i18n;

import java.util.ResourceBundle;
import java.util.Locale;

/**
 * User: bozhidar
 * Date: Sep 18, 2009
 * Time: 10:55:37 AM
 */
public class Translator {
    private ResourceBundle resourceBundle;

    public Translator(final String resourceBundleName) {
        resourceBundle = ResourceBundle.getBundle("i18n/" + resourceBundleName, new Locale("bg", "BG"));

        if (resourceBundle == null) {
            throw new IllegalArgumentException("No such resource bundle - " + resourceBundleName);
        }
    }

    public String translate(String resourceKey) {
        System.out.println(resourceBundle.getLocale().getLanguage());
        System.out.println(resourceBundle.getString(resourceKey));
        return resourceBundle.getString(resourceKey);
    }
}
