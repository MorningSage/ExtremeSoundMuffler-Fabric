package morningsage.extremesoundmuffler.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.WorldAccess;

public final class CombinedWorldEvents {
    public static final Event<WorldEvent> LOAD = EventFactory.createArrayBacked(WorldEvent.class,
        callbacks -> (world) -> {

        for (WorldEvent callback : callbacks) {
            callback.onWorldEvent(world);
        }
    });

    public static final Event<WorldEvent> UNLOAD = EventFactory.createArrayBacked(WorldEvent.class,
        callbacks -> (world) -> {
        for (WorldEvent callback : callbacks) {
            callback.onWorldEvent(world);
        }
    });

    @FunctionalInterface
    public interface WorldEvent {
        void onWorldEvent(WorldAccess world);
    }
}
