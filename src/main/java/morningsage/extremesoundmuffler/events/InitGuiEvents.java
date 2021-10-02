package morningsage.extremesoundmuffler.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import java.util.function.Consumer;

@FunctionalInterface
public interface InitGuiEvents {
    Event<InitGuiEvents> POST_INIT = EventFactory.createArrayBacked(InitGuiEvents.class,
        callbacks -> (gui, add) -> {
        for (InitGuiEvents callback : callbacks) {
            callback.onGuiPostInit(gui, add);
        }
    });

    void onGuiPostInit(Screen gui, Consumer<AbstractButtonWidget> add);
}
