package morningsage.extremesoundmuffler.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.world.WorldAccess;

import java.util.List;
import java.util.function.Consumer;

@FunctionalInterface
public interface InitGuiEvents {
    Event<InitGuiEvents> POST_INIT = EventFactory.createArrayBacked(InitGuiEvents.class,
        callbacks -> (gui, list, add, remove) -> {
        for (InitGuiEvents callback : callbacks) {
            callback.onGuiPostInit(gui, list, add, remove);
        }
    });

    void onGuiPostInit(Screen gui, List<AbstractButtonWidget> list, Consumer<AbstractButtonWidget> add, Consumer<AbstractButtonWidget> remove);
}
