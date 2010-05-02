package com.drowltd.spellbook.ui.desktop;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ClipboardIntegration implements ClipboardOwner {
    private SpellbookFrame spellbookFrame;

    public ClipboardIntegration(SpellbookFrame spellbookFrame) {
        this.spellbookFrame = spellbookFrame;
    }

    /**
     * Empty implementation of the ClipboardOwner interface.
     */
    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        System.out.println("Clipboard contents replaced");

        spellbookFrame.clipboardCallback();

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
                    Logger.getLogger(ClipboardIntegration.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(ClipboardIntegration.class.getName()).log(Level.SEVERE, null, ex);
                }
            }


        return "";
    }
}

