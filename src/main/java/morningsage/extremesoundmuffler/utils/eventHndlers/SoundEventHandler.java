package morningsage.extremesoundmuffler.utils.eventHndlers;

import morningsage.extremesoundmuffler.Config;
import morningsage.extremesoundmuffler.events.PlaySoundEvent;
import morningsage.extremesoundmuffler.gui.MainScreen;
import morningsage.extremesoundmuffler.utils.Anchor;
import morningsage.extremesoundmuffler.utils.ISoundLists;
import morningsage.extremesoundmuffler.utils.MuffledSound;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

public class SoundEventHandler implements ISoundLists {

    private static boolean isFromPSB = false;

    public static void init() {
        PlaySoundEvent.EVENT.register((soundSystem, soundInstance, soundReplacement) -> {
            if (MinecraftClient.getInstance().world == null) {
                return ActionResult.PASS;
            }

            if (isFromPSB) {
                isFromPSB = false;
                return ActionResult.PASS;
            }

            BlockPos soundPos = new BlockPos(soundInstance.getX(), soundInstance.getY(), soundInstance.getZ());

            for (String fs : forbiddenSounds) {
                if (soundInstance.getId().toString().contains(fs)) {
                    return ActionResult.FAIL;
                }
            }

            recentSoundsList.add(soundInstance.getId());

            if (MainScreen.isMuffled()) {
                if (muffledSounds.containsKey(soundInstance.getId())) {
                    soundReplacement.setSoundInstance(new MuffledSound(soundInstance, muffledSounds.get(soundInstance.getId()).floatValue()));
                    return ActionResult.FAIL;
                }

                //If Anchors are disabled in Config
                if (Config.disableAnchors) {
                    return ActionResult.PASS;
                }

                for (Anchor anchor : MainScreen.getAnchors()) {
                    if (anchor.getAnchorPos() != null) {
                        boolean sameDimension = MinecraftClient.getInstance().world.getRegistryKey().getValue().equals(anchor.getDimension());
                        if (sameDimension && soundPos.isWithinDistance(anchor.getAnchorPos(), anchor.getRadius())) {
                            if (anchor.getMuffledSounds().containsKey(soundInstance.getId())) {
                                soundReplacement.setSoundInstance(new MuffledSound(soundInstance, anchor.getMuffledSounds().get(soundInstance.getId()).floatValue()));
                                return ActionResult.FAIL;
                            }
                        }
                    }
                }
            }

            return ActionResult.PASS;
        });
    }

    public static void isFromPlaySoundButton(boolean b) {
        isFromPSB = b;
    }
}
