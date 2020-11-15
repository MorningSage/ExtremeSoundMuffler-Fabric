package morningsage.extremesoundmuffler.gui.buttons;

import morningsage.extremesoundmuffler.Config;
import morningsage.extremesoundmuffler.gui.MainScreen;
import morningsage.extremesoundmuffler.utils.AbstractButtonWidgetAccessor;
import morningsage.extremesoundmuffler.utils.Anchor;
import morningsage.extremesoundmuffler.utils.ISoundLists;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.Objects;

@Environment(EnvType.CLIENT)
public class MuffledSlider extends AbstractButtonWidget implements ISoundLists {

    private final int colorWhite = 0xffffff;
    private final int colorYellow = 0xffff00;
    private final String mainTitle = "ESM - Main Screen";
    private double sliderValue;
    private ButtonWidget btnToggleSound;
    private PlaySoundButton btnPlaySound;
    private final Identifier sound;
    public static boolean showSlider = false;

    public MuffledSlider(int x, int y, int width, int height, double sliderValue, Identifier sound, String screenTitle, Anchor anchor) {
        super(x, y, width, height, Text.of(sound.getPath() + ":" + sound.getNamespace()));
        this.sliderValue = sliderValue;
        this.sound = sound;
        setBtnToggleSound(screenTitle, sound, anchor);
        setBtnPlaySound(sound);
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        MinecraftClient minecraft = MinecraftClient.getInstance();
        minecraft.getTextureManager().bindTexture(MainScreen.GUI);
        drawGradient(matrices);
        float v = ((AbstractButtonWidgetAccessor) this).getFGColor() == 0xffff00 ? 213F : 202F;
        drawTexture(matrices, btnToggleSound.x, btnToggleSound.y, 43F, v, 11, 11, 256, 256); //muffle button bg
        drawTexture(matrices, btnPlaySound.x, btnPlaySound.y, 32F, 202F, 11, 11, 256, 256); //play button bg
        this.drawMessage(matrices, minecraft);
    }


    private void drawMessage(MatrixStack matrixStack, MinecraftClient minecraft) {
        TextRenderer font = minecraft.textRenderer;
        if (showSlider && this.hovered) {
            drawCenteredString(matrixStack, font, "Volume: " + (int) (sliderValue * 100), this.x + (this.width / 2), this.y + 2, 0xffffff); //title
        } else {
            String msgTruncated;
            if (this.hovered) {
                msgTruncated = getMessage().getString();
                fill(matrixStack, this.x + this.width, this.y, this.x + font.getWidth(getMessage().getString()) + 2, this.y + font.fontHeight + 2, -1325400064);
            } else {
                msgTruncated = font.trimToWidth(getMessage(), 205).getString();
            }
            font.drawWithShadow(matrixStack, msgTruncated, this.x + 2, this.y + 2, ((AbstractButtonWidgetAccessor) this).getFGColor()); //title
        }
    }

    private void drawGradient(MatrixStack matrixStack) {
        if (((AbstractButtonWidgetAccessor) this).getFGColor() == colorYellow) {
            drawTexture(matrixStack, this.x, this.y - 1, 0, 234, (int) (sliderValue * (width - 6)) + 5, height + 1, 256, 256); //draw bg
            if (this.hovered) {
                drawTexture(matrixStack, this.x + (int) (sliderValue * (width - 6)) + 1, this.y + 1, 32F, 224F, 5, 9, 256, 256); //Slider
            }
        }
    }

    private void setBtnToggleSound(String screenTitle, Identifier sound, Anchor anchor) {
        btnToggleSound = new ButtonWidget(this.x + width + 5, this.y, 11, 11, LiteralText.EMPTY, b -> {
            if (((AbstractButtonWidgetAccessor) this).getFGColor() == colorYellow) {
                if (screenTitle.equals(mainTitle)) {
                    muffledSounds.remove(sound);
                } else {
                    anchor.removeSound(sound);
                }
                ((AbstractButtonWidgetAccessor) this).setFGColor(colorWhite);
            } else {
                if (screenTitle.equals(mainTitle)) {
                    setSliderValue(Config.defaultMuteVolume);
                    muffledSounds.put(sound, sliderValue);
                } else if (anchor.getAnchorPos() != null) {
                    setSliderValue(Config.defaultMuteVolume);
                    anchor.addSound(sound, sliderValue);
                } else {
                    return;
                }
                ((AbstractButtonWidgetAccessor) this).setFGColor(colorYellow);
            }
        });
    }

    public ButtonWidget getBtnToggleSound() {
        return btnToggleSound;
    }

    private void setBtnPlaySound(Identifier sound) {
        btnPlaySound = new PlaySoundButton(this.x + width + 17, this.y, new SoundEvent(sound));
    }

    public PlaySoundButton getBtnPlaySound() {
        return btnPlaySound;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean flag = keyCode == 263;
        if (flag || keyCode == 262) {
            float f = flag ? -1.0F : 1.0F;
            this.setSliderValue(this.sliderValue + (double) (f / (float) (this.width - 8)));
        }
        return false;
    }

    private void changeSliderValue(double mouseX) {
        this.setSliderValue((mouseX - (double) (this.x + 4)) / (double) (this.width - 8));
    }

    private void setSliderValue(double value) {
        double d0 = this.sliderValue;
        this.sliderValue = MathHelper.clamp(value, 0.0D, 0.9D);
        if (d0 != this.sliderValue) {
            this.func_230972_a_();
        }
        this.func_230979_b_();
        updateVolume();
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        this.changeSliderValue(mouseX);
        super.onDrag(mouseX, mouseY, deltaX, deltaY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.hovered && ((AbstractButtonWidgetAccessor) this).getFGColor() == colorYellow) {
            this.changeSliderValue(mouseX);
            showSlider = true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void updateVolume() {
        String screenTitle = MainScreen.getScreenTitle();

        if (screenTitle.equals(mainTitle)) {
            muffledSounds.replace(this.sound, this.sliderValue);
        } else {
            Objects.requireNonNull(MainScreen.getAnchorByName(screenTitle)).replaceSound(this.sound, this.sliderValue);
        }
    }

    private void func_230979_b_() {
    }

    private void func_230972_a_() {
    }
}
