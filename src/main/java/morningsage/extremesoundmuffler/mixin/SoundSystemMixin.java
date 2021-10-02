package morningsage.extremesoundmuffler.mixin;

import morningsage.extremesoundmuffler.events.SoundPlayingEvents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SoundSystem.class)
@Environment(EnvType.CLIENT)
public abstract class SoundSystemMixin {
    @Redirect(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/sound/SoundInstance;getVolume()F"
        ),
        method = "getAdjustedVolume"
    )
    private float getVolume(SoundInstance soundInstance) {
        SoundPlayingEvents.SoundInfo soundInfo = new SoundPlayingEvents.SoundInfo(
            soundInstance.getId(), soundInstance.getVolume(), new BlockPos(soundInstance.getX(), soundInstance.getY(), soundInstance.getZ())
        );

        SoundPlayingEvents.SOUND_VOLUME_EVENT.invoker().onSoundVolume(soundInfo);

        return soundInfo.getVolume();
    }
}
