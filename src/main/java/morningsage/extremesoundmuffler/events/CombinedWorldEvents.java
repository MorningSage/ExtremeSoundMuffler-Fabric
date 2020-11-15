package morningsage.extremesoundmuffler.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.WorldAccess;

public final class CombinedWorldEvents {
    public static final Event<Load> LOAD = EventFactory.createArrayBacked(Load.class,
        callbacks -> (world) -> {
        for (Load callback : callbacks) {
            callback.onWorldLoad(world);
        }
    });

    public static final Event<Unload> UNLOAD = EventFactory.createArrayBacked(Unload.class,
        callbacks -> (world) -> {
        for (Unload callback : callbacks) {
            callback.onWorldUnload(world);
        }
    });

    @FunctionalInterface
    public interface Load {
        void onWorldLoad(WorldAccess world);
    }

    @FunctionalInterface
    public interface Unload {
        void onWorldUnload(WorldAccess world);
    }
}
