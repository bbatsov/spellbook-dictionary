package com.drowltd.spellbook.core.exception;

/**
 * This exception represents the situation in which the dictionary database is
 * locked by another process(embedded databases like h2 cannot be used by multiple
 * processes at once). This other process is most like another instance of Spellbook
 * already running.
 *
 * @author Bozhidar Batsov
 * @since 0.1
 */
public class DictionaryDbLockedException extends Throwable {
}
