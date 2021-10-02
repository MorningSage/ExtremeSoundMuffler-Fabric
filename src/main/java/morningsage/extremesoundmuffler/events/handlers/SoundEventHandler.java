package morningsage.extremesoundmuffler.events.handlers;

import morningsage.extremesoundmuffler.Config;
import morningsage.extremesoundmuffler.events.SoundPlayingEvents;
import morningsage.extremesoundmuffler.mufflers.SoundMufflers;
import morningsage.extremesoundmuffler.mufflers.instances.AnchorMuffler;
import morningsage.extremesoundmuffler.mufflers.instances.GenericMuffler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

import java.util.*;

public final class SoundEventHandler {

    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final Set<String> forbiddenSounds = new HashSet<>();
    private static final SortedSet<Identifier> recentSoundsList = new TreeSet<>();
    private static Identifier overriddenSoundIdentifier = null;

    public static void init() {
        forbiddenSounds.addAll(Config.forbiddenSounds);
        SoundPlayingEvents.SOUND_VOLUME_EVENT.register(SoundEventHandler::getSoundVolume);
    }

    private static void getSoundVolume(SoundPlayingEvents.SoundInfo soundInfo) {
        if (client.world == null) return;

        if (overriddenSoundIdentifier == soundInfo.getSoundIdentifier()) {
            overriddenSoundIdentifier = null;
            return;
        }

        for (String fs : forbiddenSounds) {
            if (soundInfo.getSoundIdentifier().toString().contains(fs)) {
                soundInfo.setVolume(0.0F);
                return;
            }
        }

        recentSoundsList.add(soundInfo.getSoundIdentifier());

        if (!SoundMufflers.isMuffling()) return;

        if (GenericMuffler.INSTANCE.hasSound(soundInfo.getSoundIdentifier())) {
            soundInfo.setVolume(GenericMuffler.INSTANCE.getMuffledSounds().get(soundInfo.getSoundIdentifier()).floatValue());
            return;
        }

        if (Config.disableAnchors) return;

        for (AnchorMuffler anchor : SoundMufflers.getAnchors()) {
            if (anchor.getAnchorPos() == null) continue;

            boolean sameDimension = client.world.getRegistryKey().getValue().equals(anchor.getDimension());
            if (sameDimension && soundInfo.getSourceLocation().isWithinDistance(anchor.getAnchorPos(), anchor.getRadius())) {
                if (anchor.getMuffledSounds().containsKey(soundInfo.getSoundIdentifier())) {
                    soundInfo.setVolume(anchor.getMuffledSounds().get(soundInfo.getSoundIdentifier()).floatValue());
                    return;
                }
            }
        }
    }

    public static void setOverrideSound(Identifier overriddenSound) {
        overriddenSoundIdentifier = overriddenSound;
    }
    public static Set<String> getForbiddenSounds() {
        return forbiddenSounds;
    }
    public static SortedSet<Identifier> getRecentSounds() {
        return recentSoundsList;
    }
}
