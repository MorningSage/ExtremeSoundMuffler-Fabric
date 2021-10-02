package morningsage.extremesoundmuffler.mufflers.instances;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public class AnchorMuffler implements ISoundMuffler {
    private final int id;
    private BlockPos anchorPos;
    private String name;
    private Identifier dimension;
    private int radius;
    private final Map<String, Double> muffledSounds = new HashMap<>();

    public AnchorMuffler(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public BlockPos getAnchorPos() {
        return anchorPos;
    }

    public void setAnchorPos(BlockPos anchorPos) {
        this.anchorPos = anchorPos;
    }

    @Override
    public int getIndex() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public SortedMap<Identifier, Double> getMuffledSounds() {
        SortedMap<Identifier, Double> temp = new TreeMap<>();
        this.muffledSounds.forEach((R,D) -> temp.put(new Identifier(R), D));
        return temp;
    }

    @Override
    public void addSound(Identifier sound, double volume) {
        String soundString = sound.toString();

        if (muffledSounds.containsKey(soundString)) {
            muffledSounds.replace(soundString, volume);
        } else {
            muffledSounds.put(soundString, volume);
        }
    }

    public String getX() {
        return anchorPos != null ? String.valueOf(anchorPos.getX()) : "";
    }

    public String getY() {
        return anchorPos != null ? String.valueOf(anchorPos.getY()) : "";
    }

    public String getZ() {
        return anchorPos != null ? String.valueOf(anchorPos.getZ()) : "";
    }

    public Identifier getDimension() {
        return dimension;
    }

    public void setDimension(Identifier dimension) {
        this.dimension = dimension;
    }

    public void setAnchor() {
        ClientPlayerEntity player = Objects.requireNonNull(MinecraftClient.getInstance().player);
        setAnchorPos(player.getBlockPos());
        setDimension(player.clientWorld.getRegistryKey().getValue());
        setRadius(this.getRadius() == 0 ? 32 : this.getRadius());
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
        setName("Anchor: " + this.getIndex());
        setAnchorPos(null);
        setDimension(null);
        setRadius(0);
        muffledSounds.clear();
    }

    @Override
    public boolean hasSound(Identifier sound) {
        return muffledSounds.containsKey(sound);
    }

    @Override
    public boolean isValidMuffler() {
        return getAnchorPos() != null;
    }
}
