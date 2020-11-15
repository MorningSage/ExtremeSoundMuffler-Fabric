package morningsage.extremesoundmuffler.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.world.WorldAccess;

import java.util.List;
import java.util.function.Consumer;

public final class InitGuiEvents {
    public static final Event<PostInit> POST = EventFactory.createArrayBacked(PostInit.class,
        callbacks -> (gui, list, add, remove) -> {
        for (PostInit callback : callbacks) {
            callback.onGuiPostInit(gui, list, add, remove);
        }
    });

    @FunctionalInterface
    public interface PostInit {
        void onGuiPostInit(Screen gui, List<AbstractButtonWidget> list, Consumer<AbstractButtonWidget> add, Consumer<AbstractButtonWidget> remove);
    }
}
