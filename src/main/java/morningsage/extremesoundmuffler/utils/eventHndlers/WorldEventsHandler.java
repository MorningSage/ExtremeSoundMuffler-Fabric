package morningsage.extremesoundmuffler.utils.eventHndlers;

import morningsage.extremesoundmuffler.events.CombinedWorldEvents;
import morningsage.extremesoundmuffler.gui.MainScreen;
import morningsage.extremesoundmuffler.utils.Anchor;
import morningsage.extremesoundmuffler.utils.JsonIO;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

import static morningsage.extremesoundmuffler.utils.ISoundLists.muffledSounds;

public class WorldEventsHandler {
    public static void init() {
        CombinedWorldEvents.LOAD.register(world -> {
            JsonIO.loadMuffledMap().forEach((R, V) -> muffledSounds.put(new Identifier(R), V));

            //TODO figure out how to save The anchors on the player
            //^^ look at saving the list of anchors not the anchors themselves
            //Save all the anchors and only the anchors
            //Simple muffled sounds still uses json save

            if (JsonIO.loadAnchors() != null) {
                MainScreen.addAnchors(JsonIO.loadAnchors());
            } else {
                MainScreen.setAnchors();
            }
        });

        CombinedWorldEvents.UNLOAD.register(world -> {
            List<Anchor> temp = new ArrayList<>();
            JsonIO.saveMuffledMap(muffledSounds);
            //For anchors
            for (int i = 0; i <= 9; i++) {
                Anchor anchor = MainScreen.getAnchor(i);
                if (!temp.contains(anchor)) {
                    temp.add(anchor);
                }
            }
            JsonIO.saveAnchors(temp);
        });
    }
}
