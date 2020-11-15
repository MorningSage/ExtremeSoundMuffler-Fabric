package morningsage.extremesoundmuffler.mixin;

import morningsage.extremesoundmuffler.events.CombinedWorldEvents;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public class ClientWorldMixin {
	@Inject(
		at = @At("TAIL"),
		method = "<init>"
	)
	private void init(CallbackInfo info) {
		CombinedWorldEvents.LOAD.invoker().onWorldLoad((WorldAccess) this);
	}
}
