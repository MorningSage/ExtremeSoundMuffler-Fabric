package morningsage.extremesoundmuffler.utils.eventHndlers;

import morningsage.extremesoundmuffler.Config;
import morningsage.extremesoundmuffler.gui.MainScreen;
import morningsage.extremesoundmuffler.utils.Anchor;
import morningsage.extremesoundmuffler.utils.ISoundLists;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class SoundEventHandler implements ISoundLists {

    private static Identifier overriddenSoundIdentifier = null;

    public static float getSoundVolume(Identifier soundIdentifier, float defaultVolume, BlockPos sourceLocation) {
        if (MinecraftClient.getInstance().world == null) return defaultVolume;

        if (overriddenSoundIdentifier == soundIdentifier) {
            overriddenSoundIdentifier = null;
            return defaultVolume;
        }

        for (String fs : forbiddenSounds) {
            if (soundIdentifier.toString().contains(fs)) return 0.0F;
        }

        recentSoundsList.add(soundIdentifier);

        if (!MainScreen.isMuffled()) return defaultVolume;

        if (muffledSounds.containsKey(soundIdentifier)) {
            return muffledSounds.get(soundIdentifier).floatValue();
        }

        if (Config.disableAnchors) return defaultVolume;

        for (Anchor anchor : MainScreen.getAnchors()) {
            if (anchor.getAnchorPos() == null) continue;

            boolean sameDimension = MinecraftClient.getInstance().world.getRegistryKey().getValue().equals(anchor.getDimension());
            if (sameDimension && sourceLocation.isWithinDistance(anchor.getAnchorPos(), anchor.getRadius())) {
                if (anchor.getMuffledSounds().containsKey(soundIdentifier)) {
                    return anchor.getMuffledSounds().get(soundIdentifier).floatValue();
                }
            }
        }

        return defaultVolume;
    }

    public static void setOverrideSound(Identifier overriddenSoundIdentifier) {
        SoundEventHandler.overriddenSoundIdentifier = overriddenSoundIdentifier;
    }
}
