package morningsage.extremesoundmuffler.mixin;

import morningsage.extremesoundmuffler.utils.AbstractButtonWidgetAccessor;
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
public class AbstractButtonWidgetMixin implements AbstractButtonWidgetAccessor {

    @Shadow public boolean active;
    private static final int UNSET_FG_COLOR = -1;
    private int packedFGColor = UNSET_FG_COLOR;

    @ModifyConstant(
        method = "renderButton",
        constant = {@Constant(intValue = 16777215), @Constant(intValue = 10526880)}
    )
    public int correctedForecolor(int j, MatrixStack matrices, int mouseX, int mouseY, float delta) {
        return getFGColor();
    }

    @Override
    public int getFGColor() {
        if (packedFGColor != UNSET_FG_COLOR) return packedFGColor;

        return this.active ? 16777215 : 10526880;
    }

    @Override
    public void setFGColor(int color) {
        this.packedFGColor = color;
    }

    @Override
    public void clearFGColor() {
        this.packedFGColor = UNSET_FG_COLOR;
    }
}
