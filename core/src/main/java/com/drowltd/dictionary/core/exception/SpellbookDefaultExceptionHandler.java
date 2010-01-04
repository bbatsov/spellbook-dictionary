package com.drowltd.dictionary.core.exception;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author bozhidar
 */
public class SpellbookDefaultExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpellbookDefaultExceptionHandler.class);

    public SpellbookDefaultExceptionHandler() {
    }

    @Override
    public void uncaughtException(final Thread t, final Throwable e) {
        if (SwingUtilities.isEventDispatchThread()) {
            showException(t, e);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    showException(t, e);
                }
            });
        }
    }

    private void showException(Thread t, Throwable e) {
        String msg = String.format("Unexpected problem on thread %s: %s",
                t.getName(), e.getMessage());

        LOGGER.info(msg);

        logException(t, e);

        // note: in a real app, you should locate the currently focused frame
        // or dialog and use it as the parent. In this example, I'm just passing
        // a null owner, which means this dialog may get buried behind
        // some other screen.
        JOptionPane.showMessageDialog(null, msg);
    }

    private void logException(Thread t, Throwable e) {

    }
}
