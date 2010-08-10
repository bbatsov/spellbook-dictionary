package bg.drowltd.spellbook.core;

import java.io.File;

public final class SpellbookConstants {
    public static final String SPELLBOOK_HOME = System.getProperty("user.home") + File.separator + ".spellbook";
    public static final String SPELLBOOK_DB_PATH = SPELLBOOK_HOME + File.separator + "db" + File.separator + "spellbook.h2.db";
}
