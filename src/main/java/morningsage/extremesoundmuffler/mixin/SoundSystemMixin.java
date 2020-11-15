package morningsage.extremesoundmuffler.mixin;

import morningsage.extremesoundmuffler.events.PlaySoundEvent;
import morningsage.extremesoundmuffler.events.SoundReplacement;
import morningsage.extremesoundmuffler.utils.MuffledSound;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundSystem.class)
@Environment(EnvType.CLIENT)
public abstract class SoundSystemMixin {

    @Shadow public abstract void play(SoundInstance soundInstance);

    @Inject(
        at = @At("HEAD"),
        method = "play(Lnet/minecraft/client/sound/SoundInstance;)V",
        cancellable = true
    )
    public void play(SoundInstance soundInstance, CallbackInfo callbackInfo) {
        if (soundInstance instanceof MuffledSound) return;

        SoundReplacement soundReplacement = new SoundReplacement();

        ActionResult result = PlaySoundEvent.EVENT.invoker().onPlaySound((SoundSystem) (Object) this, soundInstance, soundReplacement);

        if (result != ActionResult.PASS) callbackInfo.cancel();
        if (soundReplacement.getSoundInstance() != null) play(soundReplacement.getSoundInstance());
    }
}
