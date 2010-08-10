package bg.drow.spellbook.ui.swing.util;

import javax.swing.UIManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A helper class to deal with look & feel management.
 *
 * @author Bozhidar Batsov
 * @since 0.4
 */
public class LafUtil {
    private final transient static String[] names = {
//            "Substance Autumn",
//            "Substance BusinessBlackSteel",
//            "Substance BusinessBlueSteel",
//            "Substance Business",
//            "Substance ChallengerDeep",
//            "Substance CremeCoffee",
//            "Substance Creme",
//            "Substance DustCoffee",
//            "Substance Dust",
//            "Substance EmeraldDusk",
//            "Substance Gemini",
//            "Substance GraphiteAqua",
//            "Substance GraphiteGlass",
//            "Substance Graphite",
//            "Substance Magellan",
//            "Substance MistAqua",
//            "Substance MistSilver",
//            "Substance Moderate",
//            "Substance NebulaBrickWall",
//            "Substance Nebula",
//            "Substance OfficeBlue2007",
//            "Substance OfficeSilver2007",
//            "Substance Raven",
//            "Substance Sahara",
//            "Substance Twilight",
            "JGoodies Plastic",
            "JGoodies PlasticXP",
            "JGoodies Plastic3D",
            "JGoodies Windows"};

    private final transient static String[] classes = {
//            "org.pushingpixels.substance.api.skin.SubstanceAutumnLookAndFeel",
//            "org.pushingpixels.substance.api.skin.SubstanceBusinessBlackSteelLookAndFeel",
//            "org.pushingpixels.substance.api.skin.SubstanceBusinessBlueSteelLookAndFeel",
//            "org.pushingpixels.substance.api.skin.SubstanceBusinessLookAndFeel",
//            "org.pushingpixels.substance.api.skin.SubstanceChallengerDeepLookAndFeel",
//            "org.pushingpixels.substance.api.skin.SubstanceCremeCoffeeLookAndFeel",
//            "org.pushingpixels.substance.api.skin.SubstanceCremeLookAndFeel",
//            "org.pushingpixels.substance.api.skin.SubstanceDustCoffeeLookAndFeel",
//            "org.pushingpixels.substance.api.skin.SubstanceDustLookAndFeel",
//            "org.pushingpixels.substance.api.skin.SubstanceEmeraldDuskLookAndFeel",
//            "org.pushingpixels.substance.api.skin.SubstanceGeminiLookAndFeel",
//            "org.pushingpixels.substance.api.skin.SubstanceGraphiteAquaLookAndFeel",
//            "org.pushingpixels.substance.api.skin.SubstanceGraphiteGlassLookAndFeel",
//            "org.pushingpixels.substance.api.skin.SubstanceGraphiteLookAndFeel",
//            "org.pushingpixels.substance.api.skin.SubstanceMagellanLookAndFeel",
//            "org.pushingpixels.substance.api.skin.SubstanceMistAquaLookAndFeel",
//            "org.pushingpixels.substance.api.skin.SubstanceMistSilverLookAndFeel",
//            "org.pushingpixels.substance.api.skin.SubstanceModerateLookAndFeel",
//            "org.pushingpixels.substance.api.skin.SubstanceNebulaBrickWallLookAndFeel",
//            "org.pushingpixels.substance.api.skin.SubstanceNebulaLookAndFeel",
//            "org.pushingpixels.substance.api.skin.SubstanceOfficeBlue2007LookAndFeel",
//            "org.pushingpixels.substance.api.skin.SubstanceOfficeSilver2007LookAndFeel",
//            "org.pushingpixels.substance.api.skin.SubstanceRavenLookAndFeel",
//            "org.pushingpixels.substance.api.skin.SubstanceSaharaLookAndFeel",
//            "org.pushingpixels.substance.api.skin.SubstanceTwilightLookAndFeel",
            "com.jgoodies.looks.plastic.PlasticLookAndFeel",
            "com.jgoodies.looks.plastic.PlasticXPLookAndFeel",
            "com.jgoodies.looks.plastic.Plastic3DLookAndFeel",
            "com.jgoodies.looks.windows.WindowsLookAndFeel"};

    public static List<UIManager.LookAndFeelInfo> getAvailableLookAndFeels() {
        final List<UIManager.LookAndFeelInfo> lookAndFeelInfos = new ArrayList<UIManager.LookAndFeelInfo>();

        // build the look and feel section
        lookAndFeelInfos.addAll(Arrays.asList(UIManager.getInstalledLookAndFeels()));

        for (int i = 0; i < names.length; i++) {
            lookAndFeelInfos.add(new UIManager.LookAndFeelInfo(names[i], classes[i]));
        }

        return lookAndFeelInfos;
    }
}
