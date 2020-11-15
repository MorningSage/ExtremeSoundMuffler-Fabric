package morningsage.extremesoundmuffler.events;

import morningsage.extremesoundmuffler.utils.MuffledSound;

public class SoundReplacement {
    private MuffledSound soundInstance = null;

    public void setSoundInstance(MuffledSound soundInstance) {
        this.soundInstance = soundInstance;
    }

    public MuffledSound getSoundInstance() {
        return soundInstance;
    }
}
