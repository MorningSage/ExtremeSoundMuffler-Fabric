package morningsage.extremesoundmuffler.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.util.ActionResult;

@FunctionalInterface
public interface PlaySoundEvent {
    Event<PlaySoundEvent> EVENT = EventFactory.createArrayBacked(PlaySoundEvent.class,
        (listeners) -> (soundSystem, soundInstance, replacement) -> {
            for (PlaySoundEvent event : listeners) {
                ActionResult result = event.onPlaySound(soundSystem, soundInstance, replacement);

                if (result != ActionResult.PASS) {
                    return result;
                }
            }

            return ActionResult.PASS;
        }
    );

    ActionResult onPlaySound(SoundSystem soundSystem, SoundInstance soundInstance, SoundReplacement replacement);
}
