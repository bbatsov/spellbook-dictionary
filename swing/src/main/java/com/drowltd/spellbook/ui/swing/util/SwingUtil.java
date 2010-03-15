package com.drowltd.spellbook.ui.swing.util;

import javax.swing.JComponent;
import net.java.balloontip.BalloonTip;
import net.java.balloontip.utils.TimingUtils;

/**
 * Utility class for recurring Swing related tasks.
 *
 * @author Bozhidar Batsov
 * @since 0.3
 */
public class SwingUtil {
    private static final int DEFAULT_BALLOONTIP_DISPLAY_TIME = 5000;

    public static void showBalloonTip(JComponent component, String text) {
        showBalloonTip(component, text, DEFAULT_BALLOONTIP_DISPLAY_TIME);
    }

    public static void showBalloonTip(JComponent component, String text, int displayTime) {
        BalloonTip balloonTip = new BalloonTip(component, text);
        balloonTip.setIcon(IconManager.getImageIcon("about.png", IconManager.IconSize.SIZE16));
        TimingUtils.showTimedBalloon(balloonTip, displayTime);

    }

    public static String formatTranslation(String word, String translation) {
        StringBuilder result = new StringBuilder();

        result.append("<html><head></head><body>");

        result.append("<b>" + word + "</b>");

        result.append("<p>" + formatTranslation(translation) + "</p>");

        result.append("</body></html>");

        return result.toString();
    }

    private static String formatTranslation(String translation) {
        StringBuffer result = new StringBuffer();

        String[] lines = translation.split("\n");

        for (String line : lines) {
            // some special handling for transcripts
            if (line.startsWith("[") && line.endsWith("]")) {
                result.append("<span style=\"color:blue\">" + line + "</span>");
            } else {
                result.append(line);
            }

            if (line.startsWith("[") && line.endsWith("]")) {
                result.append("<br/><br/>");
            } else {
                result.append("<br/>");
            }
        }

        return result.toString();
    }
}
