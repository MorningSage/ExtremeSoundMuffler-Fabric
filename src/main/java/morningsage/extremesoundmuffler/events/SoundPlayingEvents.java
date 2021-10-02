package morningsage.extremesoundmuffler.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

@FunctionalInterface
public interface SoundPlayingEvents {
    Event<SoundPlayingEvents> SOUND_VOLUME_EVENT = EventFactory.createArrayBacked(SoundPlayingEvents.class,
        callbacks -> (soundInfo) -> {
        for (SoundPlayingEvents callback : callbacks) {
            callback.onSoundVolume(soundInfo);
        }
    });

    void onSoundVolume(SoundInfo soundInfo);

    class SoundInfo {
        private final Identifier soundIdentifier;
        private float volume;
        private final BlockPos sourceLocation;

        public SoundInfo(Identifier soundIdentifier, float volume, BlockPos sourceLocation) {
            this.soundIdentifier = soundIdentifier;
            this.volume = volume;
            this.sourceLocation = sourceLocation;
        }

        public Identifier getSoundIdentifier() {
            return soundIdentifier;
        }
        public BlockPos getSourceLocation() {
            return sourceLocation.mutableCopy();
        }
        public float getVolume() {
            return volume;
        }
        public void setVolume(float volume) {
            this.volume = volume;
        }
    }
}
