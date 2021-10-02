package morningsage.extremesoundmuffler.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.WorldAccess;

@FunctionalInterface
public interface CombinedWorldEvents {
    Event<CombinedWorldEvents> LOAD = EventFactory.createArrayBacked(CombinedWorldEvents.class,
        callbacks -> (world) -> {

        for (CombinedWorldEvents callback : callbacks) {
            callback.onWorldEvent(world);
        }
    });

    Event<CombinedWorldEvents> UNLOAD = EventFactory.createArrayBacked(CombinedWorldEvents.class,
        callbacks -> (world) -> {
        for (CombinedWorldEvents callback : callbacks) {
            callback.onWorldEvent(world);
        }
    });

    void onWorldEvent(WorldAccess world);
}
