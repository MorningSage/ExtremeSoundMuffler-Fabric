package morningsage.extremesoundmuffler.mixin;

import morningsage.extremesoundmuffler.events.CombinedWorldEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.Map;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Shadow @Final private Map<RegistryKey<World>, ServerWorld> worlds;

    @Inject(
        at = @At("TAIL"),
        method = "createWorlds"
    )
    protected void createWorlds(WorldGenerationProgressListener worldGenerationProgressListener, CallbackInfo callbackInfo) {
        for (ServerWorld value : this.worlds.values()) {
            CombinedWorldEvents.LOAD.invoker().onWorldEvent(value);
        }
    }

    @Inject(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/world/ServerWorld;close()V"
        ),
        method = "shutdown",
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    protected void shutdown(CallbackInfo callbackInfo, Iterator<ServerWorld> var1, ServerWorld serverWorld2) {
        CombinedWorldEvents.UNLOAD.invoker().onWorldEvent(serverWorld2);
    }
}
