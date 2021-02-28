package morningsage.extremesoundmuffler.gui.buttons;

import morningsage.extremesoundmuffler.gui.MainScreen;
import morningsage.extremesoundmuffler.mixin.accessors.HandledScreenAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.AbstractPressableButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

public class InvButton extends AbstractPressableButtonWidget {
    private final MinecraftClient minecraft = MinecraftClient.getInstance();
    private final HandledScreen<?> parent;
    private final int buttonX;

    private static int getGuiLeft(HandledScreen<?> parentGui) {
        return ((HandledScreenAccessor) parentGui).getX();
    }
    private static int getGuiTop(HandledScreen<?> parentGui) {
        return ((HandledScreenAccessor) parentGui).getY();
    }

    public InvButton(HandledScreen<?> parentGui, int x, int y) {
        super(x + getGuiLeft(parentGui) + 11, getGuiTop(parentGui) + y - 2, 11, 11, LiteralText.EMPTY);
        parent = parentGui;
        buttonX = x;
    }

    @Override
    public void onPress() {
        MainScreen.open();
    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            x = buttonX + getGuiLeft(parent) + 11;
            minecraft.getTextureManager().bindTexture(MainScreen.GUI);
            drawTexture(matrix, x, y, 43f, 202f, 11, 11, 256, 256);
            if (this.isHovered(mouseX, mouseY)) {
                drawCenteredString(matrix, minecraft.textRenderer, "Muffler", x + 5, this.y + this.height + 1, 0xffffff);
            }
        }
    }

    private boolean isHovered(int mouseX, int mouseY) {
        return mouseX >= x && mouseY >= this.y && mouseX < x + this.width && mouseY < this.y + this.height;
    }
}
