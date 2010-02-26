package com.drowltd.spellbook.core.i18n;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * A simple wrapper around resource bundles, useful for translation purposes.
 *
 * @author Bozhidar Batsov
 * @since 0.1
 */
public class Translator {
    private String resourceBundleName;
    private ResourceBundle resourceBundle;

    private static Map<String, Translator> translators = new HashMap<String, Translator>();

    private Translator(final String resourceBundleName) {
        this.resourceBundleName = resourceBundleName;
        resourceBundle = ResourceBundle.getBundle("i18n/" + resourceBundleName, Locale.getDefault());

        if (resourceBundle == null) {
            throw new IllegalArgumentException("No such resource bundle - " + resourceBundleName);
        }
    }

    public String translate(String resourceKey) {
        return resourceBundle.getString(resourceKey);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Translator other = (Translator) obj;
        if (this.resourceBundle != other.resourceBundle && (this.resourceBundle == null || !this.resourceBundle.equals(other.resourceBundle))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + (this.resourceBundle != null ? this.resourceBundle.hashCode() : 0);
        return hash;
    }

    public static Translator getTranslator(final String resourceBundleName) {
        if (translators.containsKey(resourceBundleName)) {
            return translators.get(resourceBundleName);
        } else {
            Translator t = new Translator(resourceBundleName);
            translators.put(resourceBundleName, t);
            return t;
        }
    }

    public void reset() {
        resourceBundle = ResourceBundle.getBundle("i18n/" + resourceBundleName, Locale.getDefault());
    }
}
