package morningsage.extremesoundmuffler.events.handlers;

import morningsage.extremesoundmuffler.Config;
import morningsage.extremesoundmuffler.events.InitGuiEvents;
import morningsage.extremesoundmuffler.gui.buttons.InvButton;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;

import java.util.function.Consumer;

public final class GuiEventHandler {
    public static void init() {
        InitGuiEvents.POST_INIT.register(GuiEventHandler::onPostInitGui);
    }

    private static void onPostInitGui(Screen gui, Consumer<AbstractButtonWidget> add) {
        if (Config.disableInventoryButton || !(gui instanceof InventoryScreen)) return;
        add.accept(new InvButton((HandledScreen<?>) gui, 64, 9));
    }
}
