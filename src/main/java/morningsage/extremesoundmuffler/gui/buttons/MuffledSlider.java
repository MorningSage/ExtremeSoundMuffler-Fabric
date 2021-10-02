package morningsage.extremesoundmuffler.gui.buttons;

import morningsage.extremesoundmuffler.Config;
import morningsage.extremesoundmuffler.gui.MainScreen;
import morningsage.extremesoundmuffler.mufflers.instances.ISoundMuffler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class MuffledSlider extends AbstractButtonWidget {
    private static final MinecraftClient minecraft = MinecraftClient.getInstance();
    private final ButtonWidget btnToggleSound;
    private final PlaySoundButton btnPlaySound;

    public SliderType sliderType = SliderType.UNMUTED;

    private final int index;
    private double sliderValue;
    private final Identifier sound;
    private final ISoundMuffler muffler;
    public static Identifier tickSound;
    public static boolean showSlider = false;

    public MuffledSlider(int index, int x, int y, int width, int height, double sliderValue, Identifier sound, ISoundMuffler muffler) {
        super(x, y, width, height, Text.of(sound.getPath() + ":" + sound.getNamespace()));

        this.index = index;
        this.sliderValue = sliderValue;
        this.sound = sound;
        this.muffler = muffler;

        btnToggleSound = new ButtonWidget(this.x + width + 5, this.y, 11, 11, LiteralText.EMPTY, b -> {
            switch (this.sliderType) {
                case MUTED:
                    muffler.removeSound(sound);
                    this.sliderType = SliderType.UNMUTED;
                    break;
                case UNMUTED:
                    if (muffler.isValidMuffler()) {
                        setSliderValue(Config.defaultMuteVolume);
                        muffler.addSound(sound, this.sliderValue);
                        this.sliderType = SliderType.MUTED;
                    }
                    break;
            }
        });

        btnPlaySound = new PlaySoundButton(this.x + width + 17, this.y, new SoundEvent(sound));
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        minecraft.getTextureManager().bindTexture(MainScreen.GUI);
        drawBackground(matrices);
        drawTexture(matrices, btnToggleSound.x, btnToggleSound.y, 43.0F, sliderType.V, 11, 11, 256, 256); //muffle button bg
        drawTexture(matrices, btnPlaySound.x, btnPlaySound.y, 32.0F, 202.0F, 11, 11, 256, 256); //play button bg

        if (this.hovered && sliderType.isMuted()) {
            drawCenteredString(matrices, minecraft.textRenderer, "Volume: " + (int) (sliderValue * 100), this.x + (this.width / 2), this.y + 2, sliderType.TextColor); //title
        } else {
            String msgTruncated = getMessage().getString();
            int v = Math.max(this.width, minecraft.textRenderer.getWidth(getMessage().getString()));

            if (this.hovered) {
                fill(matrices, this.x + this.width + 3, this.y, this.x + v + 3, this.y + minecraft.textRenderer.fontHeight + 2, 0xFF000000);
            } else {
                msgTruncated = minecraft.textRenderer.trimToWidth(getMessage(), 205).getString();
            }
            minecraft.textRenderer.drawWithShadow(matrices, msgTruncated, this.x + 2, this.y + 2, sliderType.TextColor); //title
        }
    }

    private void drawBackground(MatrixStack matrices) {
        if (index % 2 == 0) {
            fill(matrices, x, y, x + this.width + 5, this.y + minecraft.textRenderer.fontHeight + 2, 0xC8323232);
            //fill(matrices, this.x, this.y, this.x + v + 3, this.y + minecraft.textRenderer.fontHeight + 2, 0xFF000000);
        }

        drawGradient(matrices);
    }

    private void drawGradient(MatrixStack matrixStack) {
        if (sliderType.isUnMuted()) return;

        drawTexture(matrixStack, this.x, this.y - 1, 0, 234, (int) (sliderValue * (width - 6)) + 5, height + 2, 256, 256); //draw bg

        if (this.hovered) {
            drawTexture(matrixStack, this.x + (int) (sliderValue * (width - 6)) + 1, this.y + 1, 32F, 224F, 5, 9, 256, 256); //Slider
        }
    }
    public ButtonWidget getBtnToggleSound() {
        return btnToggleSound;
    }

    public PlaySoundButton getBtnPlaySound() {
        return btnPlaySound;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean flag = keyCode == GLFW.GLFW_KEY_LEFT;
        if (flag || keyCode == GLFW.GLFW_KEY_RIGHT) {
            float f = flag ? -1.0F : 1.0F;
            this.setSliderValue(this.sliderValue + (double) (f / (float) (this.width - 8)));
        }
        return false;
    }

    private void changeSliderValue(double mouseX) {
        this.setSliderValue((mouseX - (double) (this.x + 4)) / (double) (this.width - 8));
    }

    private void setSliderValue(double value) {
        this.sliderValue = MathHelper.clamp(value, 0.0D, 0.90D);
        muffler.addSound(this.sound, this.sliderValue);
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        this.changeSliderValue(mouseX);
        super.onDrag(mouseX, mouseY, deltaX, deltaY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.hovered && sliderType.isMuted()) {
            this.changeSliderValue(mouseX);
            showSlider = true;
            tickSound = this.sound;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.setFocused(false);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    public enum SliderType {
        MUTED(0x00FFFF, 202.0F),
        UNMUTED(0xFFFFFF, 213.0F);

        public final int TextColor;
        public final float V;

        SliderType(int TextColor, float V) {
            this.TextColor = TextColor;
            this.V = V;
        }

        public boolean isMuted() {
            return this == MUTED;
        }
        public boolean isUnMuted() {
            return this == UNMUTED;
        }
    }
}
