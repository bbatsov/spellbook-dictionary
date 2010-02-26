
package com.drowltd.spellbook.core.exception;

/**
 *
 * @author iivalchev
 */
public class SpellBookException extends RuntimeException{

    public SpellBookException(){
        super();
    }

    public SpellBookException(String message){
        super(message);
    }

    public SpellBookException(String message, Throwable cause){
        super(message, cause);
    }

    public SpellBookException(Throwable cause){
        super(cause);
    }

}
