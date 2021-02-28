package morningsage.extremesoundmuffler.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

public class Anchor {

    private final int id;
    private BlockPos anchorPos;
    private String name;
    private Identifier dimension;
    private int radius;
    private final SortedMap<String, Double> muffledSounds = new TreeMap<>();

    public Anchor(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public BlockPos getAnchorPos() {
        return anchorPos;
    }

    public void setAnchorPos(BlockPos anchorPos) {
        this.anchorPos = anchorPos;
    }

    public int getId() {
        return id;
    }

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

    public SortedMap<Identifier, Double> getMuffledSounds() {
        SortedMap<Identifier, Double> temp = new TreeMap<>();
        this.muffledSounds.forEach((R,D) -> temp.put(new Identifier(R), D));
        return temp;
    }

    public void addSound(Identifier sound, double volume) {
        muffledSounds.put(sound.toString(), volume);
    }

    public void replaceSound(Identifier sound, double volume) {
        muffledSounds.replace(sound.toString(),volume);
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

    public void removeSound(Identifier sound) {
        muffledSounds.remove(sound.toString());
    }

    public void setAnchor() {
        ClientPlayerEntity player = Objects.requireNonNull(MinecraftClient.getInstance().player);
        setAnchorPos(player.getBlockPos());
        setDimension(player.clientWorld.getRegistryKey().getValue());
        setRadius(this.getRadius() == 0 ? 32 : this.getRadius());
    }

    public void deleteAnchor() {
        setName("Anchor: " + this.getId());
        setAnchorPos(null);
        setDimension(null);
        setRadius(0);
        muffledSounds.clear();
    }

    public void editAnchor(String title, int radius) {
        setName(title);
        setRadius(radius);
    }
}
