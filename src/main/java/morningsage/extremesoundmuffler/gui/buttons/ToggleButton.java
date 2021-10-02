package morningsage.extremesoundmuffler.gui.buttons;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.AbstractPressableButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class ToggleButton<T extends Enum<T> & ToggleButton.TextIdentifiable> extends AbstractPressableButtonWidget {
    // Because "Java", enum constants, are technically not constant.
    // Using the class allows this button to continue to function
    // if/when values are added to the enum.
    protected final Class<T> type;
    protected int index = 0;
    protected final PressAction<T> onPress;

    public ToggleButton(Class<T> type, int x, int y, int width, int height, PressAction<T> onPress) {
        super(x, y, width, height, LiteralText.EMPTY);
        this.onPress = onPress;

        this.type = type;
    }

    public void onPress() {
        index = (index + 1) % type.getEnumConstants().length;
        this.onPress.onPress(this);
    }

    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.renderButton(matrices, mouseX, mouseY, delta);
        if (this.isHovered()) {
            this.renderToolTip(matrices, mouseX, mouseY);
        }
    }

    public T getValue() {
        return type.getEnumConstants()[index];
    }

    public void setValue(T value) {
        T[] values = type.getEnumConstants();

        for (int i = 0; i < values.length; i++) {
            if (value.equals(values[i])) {
                this.index = i;
                break;
            }
        }
    }

    public Text getMessage() {
        return getValue().asText();
    }

    @Environment(EnvType.CLIENT)
    public interface PressAction<T extends Enum<T> & ToggleButton.TextIdentifiable> {
        void onPress(ToggleButton<T> button);
    }

    public interface TextIdentifiable {
        Text asText();
    }
}
