package morningsage.extremesoundmuffler.mufflers.instances;

import net.minecraft.util.Identifier;
import java.util.SortedMap;

public interface ISoundMuffler {
    SortedMap<Identifier, Double> getMuffledSounds();
    void addSound(Identifier sound, double volume);
    void removeSound(Identifier sound);
    boolean hasSounds();
    boolean hasSound(Identifier sound);
    void clearSounds();
    boolean isValidMuffler();
    String getName();
    void setName(String name);
    int getIndex();
    void setAnchor();
}
