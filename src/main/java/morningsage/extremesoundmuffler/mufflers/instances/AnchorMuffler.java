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
    private final Map<Identifier, Double> muffledSounds = new HashMap<>();

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

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public SortedMap<Identifier, Double> getMuffledSounds() {
        return new TreeMap<>(this.muffledSounds);
    }

    @Override
    public void addSound(Identifier sound, double volume) {
        if (muffledSounds.containsKey(sound)) {
            muffledSounds.replace(sound, volume);
        } else {
            muffledSounds.put(sound, volume);
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

    @Override
    public void removeSound(Identifier sound) {
        muffledSounds.remove(sound);
    }

    @Override
    public void setAnchor() {
        ClientPlayerEntity player = Objects.requireNonNull(MinecraftClient.getInstance().player);
        setAnchorPos(player.getBlockPos());
        setDimension(player.clientWorld.getRegistryKey().getValue());
        setRadius(this.getRadius() == 0 ? 32 : this.getRadius());
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
