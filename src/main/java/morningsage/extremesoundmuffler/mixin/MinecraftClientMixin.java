package morningsage.extremesoundmuffler.mixin;

import morningsage.extremesoundmuffler.events.CombinedWorldEvents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
@Environment(EnvType.CLIENT)
public class MinecraftClientMixin {
    @Shadow @Nullable public ClientWorld world;

    @Inject(
        at = @At("HEAD"),
        method = "joinWorld"
    )
    public void joinWorld(ClientWorld world, CallbackInfo callbackInfo) {
        if (this.world != null) CombinedWorldEvents.UNLOAD.invoker().onWorldEvent(this.world);
    }

    @Inject(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/MinecraftClient;reset(Lnet/minecraft/client/gui/screen/Screen;)V",
            shift = At.Shift.AFTER
        ),
        method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V"
    )
    public void disconnect(Screen screen, CallbackInfo callbackInfo) {
        if (world != null) CombinedWorldEvents.UNLOAD.invoker().onWorldEvent(this.world);
    }
}
