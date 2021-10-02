package morningsage.extremesoundmuffler.mixin;

import morningsage.extremesoundmuffler.events.InitGuiEvents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Screen.class)
@Environment(EnvType.CLIENT)
public abstract class ScreenMixin {
    @Shadow protected abstract <T extends AbstractButtonWidget> T addButton(T button);

    @Inject(
        at = @At("TAIL"),
        method = "init(Lnet/minecraft/client/MinecraftClient;II)V"
    )
    public void init(MinecraftClient client, int width, int height, CallbackInfo callbackInfo) {
        InitGuiEvents.POST_INIT.invoker().onGuiPostInit(
            (Screen) (Object) this, this::addButton
        );
    }
}
