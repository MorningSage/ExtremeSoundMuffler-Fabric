package morningsage.extremesoundmuffler.utils;

import net.minecraft.util.Identifier;

import java.util.*;

public interface ISoundLists {
    Set<String> forbiddenSounds = new HashSet<>();
    SortedSet<Identifier> soundsList = new TreeSet<>();
    SortedSet<Identifier> recentSoundsList = new TreeSet<>();
}
