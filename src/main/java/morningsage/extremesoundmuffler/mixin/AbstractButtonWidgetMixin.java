package morningsage.extremesoundmuffler.mixin;

import morningsage.extremesoundmuffler.utils.AbstractButtonWidgetDuck;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AbstractButtonWidget.class)
@Environment(EnvType.CLIENT)
public class AbstractButtonWidgetMixin implements AbstractButtonWidgetDuck {

    @Shadow public boolean active;
    private static final int UNSET_FG_COLOR = -1;
    private int packedFGColor = UNSET_FG_COLOR;

    @ModifyConstant(
        method = "renderButton",
        constant = {@Constant(intValue = 0xFFFFFF), @Constant(intValue = 0xA0A0A0)}
    )
    public int correctedForecolor(int j, MatrixStack matrices, int mouseX, int mouseY, float delta) {
        return getFGColor();
    }

    @Override
    public int getFGColor() {
        if (packedFGColor != UNSET_FG_COLOR) return packedFGColor;

        return this.active ? 0xFFFFFF : 0xA0A0A0;
    }

    @Override
    public void setFGColor(int color) {
        this.packedFGColor = color;
    }
}
