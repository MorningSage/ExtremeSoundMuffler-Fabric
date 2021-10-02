package morningsage.extremesoundmuffler.gui;

import morningsage.extremesoundmuffler.Config;
import morningsage.extremesoundmuffler.SoundMuffler;
import morningsage.extremesoundmuffler.gui.buttons.MuffledSlider;
import morningsage.extremesoundmuffler.gui.buttons.ToggleButton;
import morningsage.extremesoundmuffler.mufflers.SoundMufflers;
import morningsage.extremesoundmuffler.mufflers.instances.AnchorMuffler;
import morningsage.extremesoundmuffler.mufflers.instances.ISoundMuffler;
import morningsage.extremesoundmuffler.utils.AbstractButtonWidgetDuck;
import morningsage.extremesoundmuffler.events.handlers.SoundEventHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.BackgroundHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.lwjgl.glfw.GLFW;

import java.util.*;

@Environment(EnvType.CLIENT)
public class MainScreen extends Screen {
    public static final Identifier GUI = new Identifier(SoundMuffler.MODID, "textures/gui/sm_gui.png");
    private static final MinecraftClient minecraft = MinecraftClient.getInstance();

    private final List<AbstractButtonWidget> filteredButtons = new ArrayList<>();
    private final SortedSet<Identifier> soundsList = new TreeSet<>();
    private final int xSize = 256;
    private final int ySize = 202;
    private final int whiteText = 0xffffff;
    private final boolean isAnchorsDisabled = Config.disableAnchors;

    private String screenTitle;
    private final int mufflerIndex;
    private final SoundType soundType;
    private final String searchBarMessage;
    private final Text emptyText = LiteralText.EMPTY;

    private int minYButton, maxYButton, index;
    private ToggleButton<SoundType> btnToggleSoundsList;
    private ButtonWidget btnToggleMuffled, btnDelete, btnSetAnchor, btnEditAnchor, btnNextSounds, btnPrevSounds, btnAccept, btnCancel;
    private TextFieldWidget searchBar, editAnchorTitleBar, editAnchorRadiusBar;

    private MainScreen(int mufflerIndex, SoundType soundType, String searchBarMessage) {
        super(Text.of(""));

        this.mufflerIndex = mufflerIndex;
        this.screenTitle = SoundMufflers.getMufflerByIndex(mufflerIndex).getName();
        this.soundType = soundType;
        this.searchBarMessage = searchBarMessage;
    }

    private static void open(int mufflerIndex, SoundType soundType, String searchBarMessage) {
        minecraft.openScreen(new MainScreen(mufflerIndex, soundType, searchBarMessage));
    }

    public static void open() {
        open(-1, SoundType.RECENT, "");
    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrix);
        minecraft.getTextureManager().bindTexture(GUI);
        this.drawTexture(matrix, getX(), getY(), 0, 0, xSize, ySize); //Main screen bounds
        drawCenteredString(matrix, textRenderer, screenTitle, getX() + 128, getY() + 8, whiteText); //Screen title
        renderButtonsTextures(matrix, mouseX, mouseY);
        super.render(matrix, mouseX, mouseY, partialTicks);
    }

    @Override public boolean isPauseScreen() { return false; }

    @Override
    protected void init() {
        super.init();

        updateText();
        minecraft.keyboard.setRepeatEvents(true);
        minYButton = getY() + 46;
        maxYButton = getY() + 164;

        addChild(btnToggleSoundsList = new ToggleButton<>(SoundType.class, getX() + 13, getY() + 181, 52, 13, b ->
            open(mufflerIndex, b.getValue(), searchBar.getText())
        )).setValue(soundType);

        addSoundButtons();
        addAnchorButtons();

        addButton(btnToggleMuffled = new ButtonWidget(getX() + 229, getY() + 179, 17, 17, emptyText, b -> SoundMufflers.toggleMuffling())).setAlpha(0);

        addButton(btnDelete = new ButtonWidget(getX() + 205, getY() + 179, 17, 17, emptyText, b -> {
            SoundMufflers.getMufflerByIndex(mufflerIndex).clearSounds();
            open(mufflerIndex, btnToggleSoundsList.getValue(), searchBar.getText());
        })).setAlpha(0);

        addButton(btnSetAnchor = new ButtonWidget(getX() + 260, getY() + 62, 11, 11, emptyText, b ->
            ((AnchorMuffler) SoundMufflers.getMufflerByIndex(mufflerIndex)).setAnchor()
        )).setAlpha(0);

        addButton(btnEditAnchor = new ButtonWidget(
            getX() + 274, getY() + 62, 11, 11, emptyText, b -> editTitle()
        )).setAlpha(0);

        addEditAnchorButtons();

        if (mufflerIndex < 0) {
            btnSetAnchor.visible = false;
            btnEditAnchor.visible = false;
        }

        addButton(searchBar = new TextFieldWidget(textRenderer, getX() + 74, getY() + 183, 119, 13, emptyText));
        searchBar.setHasBorder(false);
        searchBar.setText(searchBarMessage);

        addChild(btnPrevSounds = new ButtonWidget(
            getX() + 10, getY() + 22, 13, 20, emptyText, b -> listScroll(searchBar.getText().length() > 0 ? filteredButtons : buttons, -1)
        ));

        addChild(btnNextSounds = new ButtonWidget(
            getX() + 233, getY() + 22, 13, 20, emptyText, b -> listScroll(searchBar.getText().length() > 0 ? filteredButtons : buttons, 1)
        ));

        updateText();
    }

    private void addSoundButtons() {
        int buttonH = minYButton;
        ISoundMuffler muffler = SoundMufflers.getMufflerByIndex(mufflerIndex);
        soundsList.clear();

        switch (btnToggleSoundsList.getValue()) {
            case RECENT:
                if (muffler.hasSounds()) soundsList.addAll(muffler.getMuffledSounds().keySet());
                soundsList.addAll(SoundEventHandler.getRecentSounds());
                break;
            case ALL:
                soundsList.addAll(Registry.SOUND_EVENT.getIds());
                SoundEventHandler.getForbiddenSounds().forEach(fs -> soundsList.removeIf(sl -> sl.toString().contains(fs)));
                break;
            case MUFFLED:
                if (muffler.hasSounds()) soundsList.addAll(muffler.getMuffledSounds().keySet());
                break;
            default:
                soundsList.add(new Identifier("unknown:toggle/bug"));
        }

        if (soundsList.isEmpty()) return;

        int index = 0;
        for (Identifier sound : soundsList) {
            double volume = muffler.getMuffledSounds().get(sound) == null ? 1D : muffler.getMuffledSounds().get(sound);

            MuffledSlider volumeSlider = new MuffledSlider(index++, getX() + 11, buttonH, 205, 11, volume, sound, muffler);

            boolean muffledAnchor = mufflerIndex >= 0 && mufflerIndex == muffler.getIndex() && muffler.getMuffledSounds().containsKey(sound);
            boolean muffledScreen = mufflerIndex <  0 && muffler.getMuffledSounds().containsKey(sound);

            if (muffledAnchor || muffledScreen) {
                volumeSlider.sliderType = MuffledSlider.SliderType.MUTED;
            }

            buttonH += volumeSlider.getHeight() + 2;
            addButton(volumeSlider);
            volumeSlider.visible = buttons.indexOf(volumeSlider) < index + 10;
            addChild(volumeSlider.getBtnToggleSound());
            addChild(volumeSlider.getBtnPlaySound());
        }
    }
    private void addAnchorButtons() {
        int buttonW = getX() + 30;
        String[] disabledMsg = {"-", "D", "i", "s", "a", "b", "l", "e", "d", "-"};

        for (int i = 0; i <= 9; i++) {
            ButtonWidget btnAnchor;
            if (isAnchorsDisabled) {
                btnAnchor = new ButtonWidget(buttonW, getY() + 24, 16, 16, Text.of(disabledMsg[i]), b -> { });
                btnAnchor.active = false;
            } else {
                int finalI = i;
                btnAnchor = new ButtonWidget(buttonW, getY() + 24, 16, 16, Text.of(String.valueOf(i)), b -> {
                    ISoundMuffler muffler = SoundMufflers.getMufflerByIndex(finalI);
                    open(muffler.getIndex() == mufflerIndex ? -1 : muffler.getIndex(), btnToggleSoundsList.getValue(), searchBar.getText());
                });
            }

            ((AbstractButtonWidgetDuck) btnAnchor).setFGColor(whiteText);
            addButton(btnAnchor).setAlpha(0);
            buttonW += 20;
        }
    }
    private void addEditAnchorButtons() {
        addButton(editAnchorTitleBar = new TextFieldWidget(textRenderer, getX() + 302, btnEditAnchor.y + 20, 84, 11, emptyText)).visible = false;
        addButton(editAnchorRadiusBar = new TextFieldWidget(textRenderer, getX() + 302, editAnchorTitleBar.y + 15, 30, 11, emptyText)).visible = false;
        addButton(btnAccept = new ButtonWidget(getX() + 259, editAnchorRadiusBar.y + 15, 40, 20, Text.of("Accept"), b -> {
            ISoundMuffler muffler = SoundMufflers.getMufflerByIndex(mufflerIndex);
            if (!editAnchorTitleBar.getText().isEmpty() && !editAnchorRadiusBar.getText().isEmpty() && muffler instanceof AnchorMuffler) {
                AnchorMuffler anchor = (AnchorMuffler) muffler;
                anchor.setName(editAnchorTitleBar.getText());
                anchor.setRadius(Math.max(Math.min(Integer.parseInt(editAnchorRadiusBar.getText()), 32), 1));
                screenTitle = editAnchorTitleBar.getText();
                editTitle();
            }
        })).visible = false;
        addButton(btnCancel = new ButtonWidget(
            getX() + 300, editAnchorRadiusBar.y + 15, 40, 20, Text.of("Cancel"), b -> editTitle()
        )).visible = false;
    }

    private void renderButtonsTextures(MatrixStack matrix, double mouseX, double mouseY) {
        int x;          //start x point of the button
        int y;          //start y point of the button
        String message; //Button message
        int stringW;    //text width
        int darkBG = BackgroundHelper.ColorMixer.getArgb(223, 0, 0, 0); //background color for Screen::fill()

        //Mute sound buttons and play sound buttons; Sound names
        if (buttons.size() < soundsList.size()) return;

        //Delete button
        if (btnDelete.isHovered()) {
            x = btnDelete.x + 8;
            y = btnDelete.y;
            message = mufflerIndex < 0 ? "Delete Muffled List" : "Delete Anchor";
            stringW = textRenderer.getWidth(message) / 2;
            fill(matrix, x - stringW - 2, y + 20, x + stringW + 2, y + 31, darkBG);
            drawCenteredString(matrix, textRenderer, message, x, y + 22, whiteText);
        }

        //toggle muffled button
        x = btnToggleMuffled.x + 8;
        y = btnToggleMuffled.y;
        minecraft.getTextureManager().bindTexture(GUI);

        if (SoundMufflers.isMuffling()) {
            drawTexture(matrix, x - 8, y, 54F, 202F, 17, 17, xSize, xSize); //muffle button
        }

        if (btnToggleMuffled.isHovered()) {
            message = SoundMufflers.isMuffling() ? "Stop Muffling" : "Start Muffling";
            stringW = textRenderer.getWidth(message) / 2;
            fill(matrix, x - stringW - 2, y + 20, x + stringW + 2, y + 31, darkBG);
            drawCenteredString(matrix, textRenderer, message, x, y + 22, whiteText);
        }

        //Anchor coordinates and set coord button
        ISoundMuffler muffler = SoundMufflers.getMufflerByIndex(mufflerIndex);
        String dimensionName = "";
        x = btnSetAnchor.x;
        y = btnSetAnchor.y;

        if (muffler instanceof AnchorMuffler) {
            AnchorMuffler anchor = (AnchorMuffler) muffler;
            stringW = textRenderer.getWidth("Dimension: ");
            String radius = anchor.getRadius() == 0 ? "" : String.valueOf(anchor.getRadius());
            if (anchor.getDimension() != null) {
                stringW += textRenderer.getWidth(anchor.getDimension().getPath());
                dimensionName = anchor.getDimension().getPath();
            }
            fill(matrix, x - 5, y - 56, x + stringW + 6, y + 16, darkBG);
            drawStringWithShadow(matrix, textRenderer, "X: " + anchor.getX(), x + 1, y - 50, whiteText);
            drawStringWithShadow(matrix, textRenderer, "Y: " + anchor.getY(), x + 1, y - 40, whiteText);
            drawStringWithShadow(matrix, textRenderer, "Z: " + anchor.getZ(), x + 1, y - 30, whiteText);
            drawStringWithShadow(matrix, textRenderer, "Radius: " + radius, x + 1, y - 20, whiteText);
            drawStringWithShadow(matrix, textRenderer, "Dimension: " + dimensionName, x + 1, y - 10, whiteText);
            minecraft.getTextureManager().bindTexture(GUI);
            drawTexture(matrix, x, y, 0, 69.45F, 11, 11, 88, 88); //set coordinates button

            if (anchor.getAnchorPos() != null) {
                btnEditAnchor.active = true;
                drawTexture(matrix, btnEditAnchor.x, btnEditAnchor.y, 32F, 213F, 11, 11, xSize, xSize); //change title button
            } else {
                btnEditAnchor.active = false;
            }

            for (AbstractButtonWidget button : buttons) {
                if (!(button instanceof MuffledSlider)) {
                    if (button.getMessage().getString().equals(String.valueOf(anchor.getIndex()))) {
                        drawTexture(matrix, button.x - 5, button.y - 2, 71F, 202F, 27, 22, xSize, xSize); //fancy selected AnchorMuffler indicator
                        break;
                    }
                }
            }
        }

        //Set AnchorMuffler tooltip
        if (btnSetAnchor.isHovered() && !editAnchorTitleBar.visible) {
            message = "Set Anchor";
            stringW = textRenderer.getWidth(message) + 2;
            fill(matrix, x - 5, y + 16, x + stringW, y + 29, darkBG);
            textRenderer.draw(matrix, message, x, y + 18, whiteText);
        }

        if (btnEditAnchor.visible && !editAnchorTitleBar.visible && btnEditAnchor.isHovered()) {
            message = "Edit Anchor";
            stringW = textRenderer.getWidth(message) + 2;
            fill(matrix, x - 5, y + 16, x + stringW + 2, y + 29, darkBG);
            textRenderer.draw(matrix, message, x, y + 18, whiteText);
        }

        //draw anchor buttons tooltip
        for (int i = 0; i <= 9; i++) {
            AbstractButtonWidget btn = buttons.get(soundsList.size() + i);
            x = btn.x + 8;
            y = btn.y;
            message = isAnchorsDisabled ? "Anchors are disabled" : SoundMufflers.getAnchor(i).getName();
            stringW = textRenderer.getWidth(message) / 2;

            if (btn.isHovered()) {
                fill(matrix, x - stringW - 2, y - 2, x + stringW + 2, y - 13, darkBG);
                drawCenteredString(matrix, textRenderer, message, x, y - 11, whiteText);
            }
        }

        x = btnToggleSoundsList.x;
        y = btnToggleSoundsList.y;
        message = btnToggleSoundsList.getMessage().getString();
        int centerText = x + (btnToggleSoundsList.getWidth() / 2) - (textRenderer.getWidth(message) / 2);
        textRenderer.draw(matrix, message, centerText, y + 3, 0);

        //Toggle List button draw message
        if (mouseX > x && mouseX < x + 43 && mouseY > y && mouseY < y + 13) {
            String text = "Showing " + message + " sounds";
            int textW = textRenderer.getWidth(text);
            int textX = x + (btnToggleSoundsList.getWidth() / 2) - (textW / 2) + 6;
            fill(matrix, textX - 2, y + 20, textX + textW + 2, y + 22 + textRenderer.fontHeight, darkBG);
            textRenderer.draw(matrix, text, textX, y + 22, whiteText);
        }

        //Show Radius and Title text when editing AnchorMuffler and bg
        if (editAnchorRadiusBar.visible) {
            x = btnSetAnchor.x;
            y = editAnchorTitleBar.y;
            fill(matrix, x - 4, y - 4, editAnchorTitleBar.x + editAnchorTitleBar.getWidth() + 3, btnAccept.y + 23, darkBG);
            textRenderer.draw(matrix, "Title: ", x - 2, y + 1, whiteText);
            textRenderer.draw(matrix, "Radius: ", x - 2, editAnchorRadiusBar.y + 1, whiteText);

            x = editAnchorRadiusBar.x + editAnchorRadiusBar.getWidth();
            y = editAnchorRadiusBar.y;
            message = "Range: 1 - 32";
            stringW = textRenderer.getWidth(message);
            if (editAnchorRadiusBar.isHovered()) {
                fill(matrix, x + 3, y, x + stringW + 6, y + 12, darkBG);
                textRenderer.draw(matrix, message, x + 5, y + 2, whiteText);
            }
        }

        //Draw Searchbar prompt text
        if (!this.searchBar.isFocused() && this.searchBar.getText().isEmpty()) {
            x = searchBar.x;
            y = searchBar.y;
            Text searchHint = (new TranslatableText("gui.recipebook.search_hint")).formatted(Formatting.ITALIC).formatted(Formatting.GRAY); //Stolen from Vanilla ;)
            drawTextWithShadow(matrix, textRenderer, searchHint, x + 1, y, -1);
        }

        x = btnNextSounds.x;
        y = btnNextSounds.y;
        message = "Next Sounds";
        stringW = textRenderer.getWidth(message) / 2;
        //next sounds button tooltip
        if (mouseX > x && mouseX < x + btnNextSounds.getWidth() && mouseY > y && mouseY < y + btnNextSounds.getHeight()) {
            fill(matrix, x - stringW - 2, y - 2, x + stringW + 2, y - 13, darkBG);
            drawCenteredString(matrix, textRenderer, message, x, y - 11, whiteText);
        }

        x = btnPrevSounds.x;
        y = btnPrevSounds.y;
        message = "Previous Sounds";
        stringW = textRenderer.getWidth(message) / 2;
        // previous sounds button tooltip
        if (mouseX > x && mouseX < x + btnPrevSounds.getWidth() && mouseY > y && mouseY < y + btnPrevSounds.getHeight()) {
            fill(matrix, x - stringW - 2, y - 2, x + stringW + 2, y - 13, darkBG);
            drawCenteredString(matrix, textRenderer, message, x, y - 11, whiteText);
        }
    }

    private void editTitle() {
        ISoundMuffler muffler = SoundMufflers.getMufflerByIndex(mufflerIndex);

        if (muffler instanceof AnchorMuffler) {
            AnchorMuffler anchor = (AnchorMuffler) muffler;

            editAnchorTitleBar.setText(anchor.getName());
            editAnchorTitleBar.visible = !editAnchorTitleBar.visible;

            editAnchorRadiusBar.setText(String.valueOf(anchor.getRadius()));
            editAnchorRadiusBar.visible = !editAnchorRadiusBar.visible;

            btnAccept.visible = !btnAccept.visible;
            btnCancel.visible = !btnCancel.visible;

            editAnchorRadiusBar.setEditableColor(0xffffff);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double direction) {
        return listScroll(searchBar.getText().length() > 0 ? filteredButtons : buttons, direction * -1);
    }

    private boolean listScroll(List<AbstractButtonWidget> buttonList, double direction) {
        int buttonH = minYButton;

        if (index <= 0 && direction < 0) return false;
        if ((index >= buttonList.size() - 10 || index >= soundsList.size() - 10) && direction > 0) return false;

        index += direction > 0 ? 10 : -10;

        for (AbstractButtonWidget button : buttonList) {
            if (button instanceof MuffledSlider) {
                MuffledSlider slider = (MuffledSlider) button;
                int buttonIndex = buttonList.indexOf(slider);
                slider.visible = buttonIndex < index + 10 && buttonIndex >= index;

                if (slider.visible) {
                    slider.y = buttonH;
                    buttonH += slider.getHeight() + 2;
                }

                slider.getBtnToggleSound().y      = slider.y;
                slider.getBtnToggleSound().active = slider.visible;
                slider.getBtnPlaySound().y        = slider.y;
                slider.getBtnPlaySound().active   = slider.visible;
            }
        }

        return true;
    }

    private void updateText() {
        int buttonH = minYButton;
        filteredButtons.clear();

        for (AbstractButtonWidget button : buttons) {
            if (button instanceof MuffledSlider) {
                MuffledSlider slider = (MuffledSlider) button;

                if (slider.getMessage().toString().contains(searchBar.getText().toLowerCase())) {
                    if (!filteredButtons.contains(slider)) filteredButtons.add(slider);

                    slider.y = buttonH;
                    buttonH += slider.getHeight() + 2;

                    slider.visible = slider.y < maxYButton;
                } else {
                    slider.visible = false;
                }

                slider.getBtnToggleSound().y = button.y;
                slider.getBtnToggleSound().active = button.visible;
                slider.getBtnPlaySound().y = button.y;
                slider.getBtnPlaySound().active = button.visible;
            }
        }
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (!editAnchorRadiusBar.getText().isEmpty()) {
            int radius = Integer.parseInt(editAnchorRadiusBar.getText());
            if (radius > 32 || radius < 1) {
                editAnchorRadiusBar.setEditableColor(0xff0000);
            } else {
                editAnchorRadiusBar.setEditableColor(0xffffff);
            }
        } else {
            editAnchorRadiusBar.setEditableColor(0xffffff);
        }

        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (this.searchBar.charTyped(codePoint, modifiers)) {
            updateText();
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        //Radius only accepts numbers
        editAnchorRadiusBar.setTextPredicate(this::isStringValid);

        //Type inside the search bar
        if (searchBar.keyPressed(keyCode, scanCode, modifiers)) {
            updateText();
            return true;
        }

        //Search bar, Edit title bar & Edit AnchorMuffler radius bar looses focus when pressed "Enter" or "Intro"
        if (keyCode == 257 || keyCode == 335) {
            searchBar.setSelected(false);
            editAnchorTitleBar.setSelected(false);
            editAnchorRadiusBar.setSelected(false);
            return true;
        }

        //Close screen when press "E" or the mod hotkey outside the search bar or edit title bar
        if (!searchBar.isFocused() && !editAnchorTitleBar.isFocused() && !editAnchorRadiusBar.isFocused() && (keyCode == 69 || keyCode == SoundMuffler.getHotkey())) {
            onClose();
            filteredButtons.clear();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    //What you mean I stole this from Quark?
    private boolean isStringValid(String s) {
        return s.matches("[0-9]*(?:[0-9]*)?");
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            if (searchBar.isFocused()) {
                searchBar.setText("");
                updateText();
                return true;
            }
            if (editAnchorTitleBar.isFocused()) {
                editAnchorTitleBar.setText("");
                return true;
            }
            if (editAnchorRadiusBar.isHovered()) {
                editAnchorRadiusBar.setText("");
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        MuffledSlider.showSlider = false;
        MuffledSlider.tickSound = null;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        super.onClose();
        SoundMufflers.saveMufflers();
    }

    private int getX() {
        return (this.width - xSize) / 2;
    }

    private int getY() {
        return (this.height - ySize) / 2;
    }

    public enum SoundType implements ToggleButton.TextIdentifiable {
        RECENT("Recent"), ALL("All"), MUFFLED("Muffled");

        private final String string;

        SoundType(String string) { this.string = string; }

        @Override public Text asText() { return Text.of(string); }
    }
}
