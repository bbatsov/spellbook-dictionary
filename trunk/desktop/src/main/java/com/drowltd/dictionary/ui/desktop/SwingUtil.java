package com.drowltd.dictionary.ui.desktop;

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
}
