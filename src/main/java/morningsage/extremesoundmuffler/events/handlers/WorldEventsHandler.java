package morningsage.extremesoundmuffler.events.handlers;

import morningsage.extremesoundmuffler.events.CombinedWorldEvents;
import morningsage.extremesoundmuffler.mufflers.SoundMufflers;
import morningsage.extremesoundmuffler.mufflers.instances.GenericMuffler;
import morningsage.extremesoundmuffler.utils.JsonIO;
import net.minecraft.util.Identifier;
import net.minecraft.world.WorldAccess;

public final class WorldEventsHandler {
    public static void init() {
        CombinedWorldEvents.LOAD.register(WorldEventsHandler::onWorldLoad);
        CombinedWorldEvents.UNLOAD.register(WorldEventsHandler::onWorldUnload);
    }

    private static void onWorldLoad(WorldAccess world) {
        JsonIO.loadMuffledMap().forEach((R, V) -> GenericMuffler.INSTANCE.addSound(new Identifier(R), V));

        // TODO figure out how to save The anchors on the player
        // ^^ look at saving the list of anchors not the anchors themselves
        // Save all the anchors and only the anchors
        // Simple muffled sounds still uses json save

        if (JsonIO.loadAnchors() != null) {
            SoundMufflers.addAnchors(JsonIO.loadAnchors());
        } else {
            SoundMufflers.setAnchors();
        }
    }
    private static void onWorldUnload(WorldAccess world) {
        SoundMufflers.saveMufflers();
    }

}
