package bg.drowltd.spellbook.core.model;

import bg.drowltd.spellbook.core.i18n.Translator;

/**
 * @author Bozhidar Batsov
 * @since 0.4
 */
public enum SupportedFileType {
    BGOFFICE("BgOffice"), BABYLON("Babylon");

    public static final Translator TRANSLATOR = Translator.getTranslator("Model");

    private String name;

    SupportedFileType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return TRANSLATOR.translate(name + "(FileType)");
    }
}
