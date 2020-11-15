package morningsage.extremesoundmuffler;

import morningsage.extremesoundmuffler.config.ConfigField;

import java.util.Arrays;
import java.util.List;

public class Config {
    @ConfigField(
        category = "general",
        comment = "Disable the anchors?"
    )
    public static boolean disableAnchors = false;

    @ConfigField(
        category = "general",
        comment = "Volume set when pressed the mute button (Range: 0.0 ~ 0.9)"
    )
    public static double defaultMuteVolume = 0.0D;

    @ConfigField(
        category = "general",
        comment = "Disable the Muffle button in the player inventory?"
    )
    public static boolean disableInventoryButton = false;

    @ConfigField(
        category = "general",
        comment = "Blacklisted Sounds - add the name of the sounds to blacklist, separated with comma"
    )
    public static List<String> forbiddenSounds = Arrays.asList("ui.", "music.", "ambient.");
}
