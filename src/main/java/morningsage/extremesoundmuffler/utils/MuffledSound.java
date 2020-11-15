package morningsage.extremesoundmuffler.utils;

import net.minecraft.client.sound.Sound;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.sound.WeightedSoundSet;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class MuffledSound implements SoundInstance {

    private final SoundInstance sound;
    private final float volume;

    public MuffledSound(SoundInstance sound, float volume) {
        this.sound = sound;
        this.volume = volume;
    }

    @Override
    public Identifier getId() {
        return sound.getId();
    }

    @Override
    public @Nullable WeightedSoundSet getSoundSet(SoundManager soundManager) {
        return sound.getSoundSet(soundManager);
    }

    @Override
    public Sound getSound() {
        return sound.getSound();
    }

    @Override
    public SoundCategory getCategory() {
        return sound.getCategory();
    }

    @Override
    public boolean isRepeatable() {
        return sound.isRepeatable();
    }

    @Override
    public boolean isLooping() {
        return sound.isLooping();
    }

    @Override
    public int getRepeatDelay() {
        return sound.getRepeatDelay();
    }

    @Override
    public float getVolume() {
        return sound.getVolume() * volume;
    }

    @Override
    public float getPitch() {
        return sound.getPitch();
    }

    @Override
    public double getX() {
        return sound.getX();
    }

    @Override
    public double getY() {
        return sound.getY();
    }

    @Override
    public double getZ() {
        return sound.getZ();
    }

    @Override
    public AttenuationType getAttenuationType() {
        return sound.getAttenuationType();
    }
}
