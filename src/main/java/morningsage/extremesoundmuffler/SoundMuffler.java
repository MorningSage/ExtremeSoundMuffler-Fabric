package morningsage.extremesoundmuffler;

import morningsage.extremesoundmuffler.config.ConfigFileHandler;
import morningsage.extremesoundmuffler.events.InitGuiEvents;
import morningsage.extremesoundmuffler.events.handlers.GuiEventHandler;
import morningsage.extremesoundmuffler.gui.MainScreen;
import morningsage.extremesoundmuffler.gui.buttons.InvButton;
import morningsage.extremesoundmuffler.mixin.accessors.KeyBindingAccessor;
import morningsage.extremesoundmuffler.events.handlers.SoundEventHandler;
import morningsage.extremesoundmuffler.events.handlers.WorldEventsHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;

public class SoundMuffler implements ClientModInitializer {
	public static final String MODID = "extremesoundmuffler";
	private static KeyBinding openMuffleScreen;
	public static final ConfigFileHandler configFileHandler = new ConfigFileHandler(Config.class, MODID);

	@Override
	public void onInitializeClient() {
		SoundEventHandler.init();
		WorldEventsHandler.init();
		GuiEventHandler.init();

		openMuffleScreen = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"Open sound muffle screen",
			InputUtil.Type.KEYSYM,
			InputUtil.UNKNOWN_KEY.getCode(),
			"key.categories.misc"
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openMuffleScreen.wasPressed()) {
				MainScreen.open();
			}
		});
	}

	public static int getHotkey() {
		return ((KeyBindingAccessor) openMuffleScreen).getBoundKey().getCode();
	}
}
