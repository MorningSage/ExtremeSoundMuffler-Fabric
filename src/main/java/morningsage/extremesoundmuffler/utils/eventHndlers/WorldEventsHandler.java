package morningsage.extremesoundmuffler.utils.eventHndlers;

import morningsage.extremesoundmuffler.events.CombinedWorldEvents;
import morningsage.extremesoundmuffler.gui.MainScreen;
import morningsage.extremesoundmuffler.mufflers.SoundMufflers;
import morningsage.extremesoundmuffler.mufflers.instances.AnchorMuffler;
import morningsage.extremesoundmuffler.mufflers.instances.GenericMuffler;
import morningsage.extremesoundmuffler.utils.JsonIO;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class WorldEventsHandler {
    public static void init() {
        CombinedWorldEvents.LOAD.register(world -> {
            JsonIO.loadMuffledMap().forEach((R, V) -> GenericMuffler.INSTANCE.addSound(new Identifier(R), V));

            //TODO figure out how to save The anchors on the player
            //^^ look at saving the list of anchors not the anchors themselves
            //Save all the anchors and only the anchors
            //Simple muffled sounds still uses json save

            if (JsonIO.loadAnchors() != null) {
                SoundMufflers.addAnchors(JsonIO.loadAnchors());
            } else {
                SoundMufflers.setAnchors();
            }
        });

        CombinedWorldEvents.UNLOAD.register(world -> {
            List<AnchorMuffler> temp = new ArrayList<>();
            JsonIO.saveMuffledMap(GenericMuffler.INSTANCE.getMuffledSounds());
            //For anchors
            for (int i = 0; i <= 9; i++) {
                AnchorMuffler anchor = SoundMufflers.getAnchor(i);
                if (!temp.contains(anchor)) {
                    temp.add(anchor);
                }
            }
            JsonIO.saveAnchors(temp);
        });
    }
}
