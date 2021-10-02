package morningsage.extremesoundmuffler.mufflers.instances;

import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class GenericMuffler implements ISoundMuffler {
    public static final GenericMuffler INSTANCE = new GenericMuffler();
    private final Map<Identifier, Double> muffledSounds = new HashMap<>();

    private GenericMuffler() { }

    @Override
    public SortedMap<Identifier, Double> getMuffledSounds() {
        return new TreeMap<>(muffledSounds);
    }

    @Override
    public void addSound(Identifier sound, double volume) {
        muffledSounds.put(sound, volume);
    }

    @Override
    public void removeSound(Identifier sound) {
        muffledSounds.remove(sound);
    }

    @Override
    public boolean hasSounds() {
        return !muffledSounds.isEmpty();
    }

    @Override
    public void clearSounds() {
        muffledSounds.clear();
    }

    @Override
    public boolean hasSound(Identifier sound) {
        return muffledSounds.containsKey(sound);
    }

    @Override
    public boolean isValidMuffler() {
        return true;
    }

    @Override
    public String getName() {
        return "ESM - Main Screen";
    }

    @Override
    public int getIndex() {
        return -1;
    }
}
