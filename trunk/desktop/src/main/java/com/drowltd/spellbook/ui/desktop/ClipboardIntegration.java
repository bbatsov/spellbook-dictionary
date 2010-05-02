package com.drowltd.spellbook.ui.desktop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public final class ClipboardIntegration implements ClipboardOwner {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClipboardIntegration.class);

    private SpellbookFrame spellbookFrame;

    public ClipboardIntegration(SpellbookFrame spellbookFrame) {
        this.spellbookFrame = spellbookFrame;
    }

    /**
     * A nasty hack occurs here - Spellbook uses this method to get notified of sys clipboard
     * changes by grabbing its ownership and waiting to other apps to request it.
     */
    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        try {
            // this delay in necessary - otherwise all sort of nasty things happen
            Thread.sleep(200);
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }

        LOGGER.info("Clipboard ownership lost");

        // call the frame callback
        spellbookFrame.clipboardCallback();

        // replace the contents in the clipboard with the same contents
        // just to restore the ownership to Spellbook
        setClipboardContents(getClipboardContents());
    }

    /**
     * Place a String on the clipboard, and make this class the
     * owner of the Clipboard's contents.
     *
     * @param text the string to place in the clipboard
     */
    public void setClipboardContents(String text) {
        StringSelection stringSelection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, this);
    }

    /**
     * Get the String residing on the clipboard.
     *
     * @return any text found on the Clipboard; if none found, return an
     *         empty String.
     */
    public String getClipboardContents() {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

            final DataFlavor stringFlavor = DataFlavor.stringFlavor;

            if (clipboard.isDataFlavorAvailable(stringFlavor)) {
                try {
                    String text = (String) clipboard.getData(stringFlavor);

                    if (text != null) {
                        return text;
                    }
                } catch (UnsupportedFlavorException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }


        return "";
    }
}

