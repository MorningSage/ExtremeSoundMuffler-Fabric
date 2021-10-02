package morningsage.extremesoundmuffler.mufflers;

import morningsage.extremesoundmuffler.mufflers.instances.AnchorMuffler;
import morningsage.extremesoundmuffler.mufflers.instances.GenericMuffler;
import morningsage.extremesoundmuffler.mufflers.instances.ISoundMuffler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class SoundMufflers {
    private static final List<AnchorMuffler> anchors = new ArrayList<>();

    private static boolean isMuffling = true;

    public static ISoundMuffler getMufflerByIndex(int index) {
        if (index < 0) return GenericMuffler.INSTANCE;
        return anchors.get(index);
    }

    public static AnchorMuffler getAnchor(int id) {
        return anchors.get(id);
    }

    public static List<AnchorMuffler> getAnchors() {
        return anchors;
    }

    public static void setAnchors() {
        for (int i = 0; i <= 9; i++) {
            anchors.add(new AnchorMuffler(i, "Anchor: " + i));
        }
    }

    public static void addAnchors(List<AnchorMuffler> anchorList) {
        anchors.clear();
        anchors.addAll(anchorList);
    }

    @Nullable
    public static AnchorMuffler getAnchorByName(String name) {
        return anchors.stream().filter(a -> a.getName().equals(name)).findFirst().orElse(null);
    }

    public static boolean isMuffling() {
        return isMuffling;
    }

    public static void toggleMuffling() {
        isMuffling = !isMuffling;
    }
}
