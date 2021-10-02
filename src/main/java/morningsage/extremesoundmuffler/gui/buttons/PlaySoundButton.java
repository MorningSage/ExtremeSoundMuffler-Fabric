package morningsage.extremesoundmuffler.gui.buttons;

import morningsage.extremesoundmuffler.utils.eventHndlers.SoundEventHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AbstractPressableButtonWidget;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;

import java.util.Objects;

@Environment(EnvType.CLIENT)
public class PlaySoundButton extends AbstractPressableButtonWidget {

    private final MinecraftClient minecraft = MinecraftClient.getInstance();
    private final SoundEvent sound;

    public PlaySoundButton(int x, int y, SoundEvent sound) {
        super(x, y, 10, 10, Text.of("Play Sound"));

        this.setAlpha(0);
        this.sound = sound;
    }

    @Override
    public void onPress() {
        if (minecraft.player != null) {
            SoundEventHandler.setOverrideSound(sound.getId());
            minecraft.player.playSound(sound, 80, 1.0F);
        }
    }
}
