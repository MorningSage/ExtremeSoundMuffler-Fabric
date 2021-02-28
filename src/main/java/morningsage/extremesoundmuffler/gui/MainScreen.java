package morningsage.extremesoundmuffler.gui;

import morningsage.extremesoundmuffler.Config;
import morningsage.extremesoundmuffler.SoundMuffler;
import morningsage.extremesoundmuffler.gui.buttons.MuffledSlider;
import morningsage.extremesoundmuffler.utils.AbstractButtonWidgetAccessor;
import morningsage.extremesoundmuffler.utils.Anchor;
import morningsage.extremesoundmuffler.utils.ISoundLists;
import morningsage.extremesoundmuffler.utils.JsonIO;
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
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Environment(EnvType.CLIENT)
public class MainScreen extends Screen implements ISoundLists {

    public static final Identifier GUI = new Identifier(SoundMuffler.MODID, "textures/gui/sm_gui.png");

    private static final MinecraftClient minecraft = MinecraftClient.getInstance();
    private static final List<Anchor> anchors = new ArrayList<>();

    private final List<AbstractButtonWidget> filteredButtons = new ArrayList<>();
    private final int xSize = 256;
    private final int ySize = 202;
    private final int whiteText = 0xffffff;
    private final boolean isAnchorsDisabled = Config.disableAnchors;
    private final Text emptyText = LiteralText.EMPTY;
    private final String mainTitle = "ESM - Main Screen";

    private static boolean isMuffling = true;
    private static String searchBarText = "";
    private static String screenTitle = "";
    private static Text toggleSoundsListMessage;

    private int minYButton, maxYButton, index;
    private ButtonWidget btnToggleMuffled, btnDelete, btnToggleSoundsList, btnSetAnchor, btnEditAnchor, btnNextSounds, btnPrevSounds, btnAccept, btnCancel;
    private TextFieldWidget searchBar, editAnchorTitleBar, editAnchorRadiusBar;
    private Anchor anchor;

    private MainScreen() {
        super(Text.of(""));
    }

    private static void open(String title, Text message, String searchMessage) {
        toggleSoundsListMessage = message;
        screenTitle = title;
        searchBarText = searchMessage;
        minecraft.openScreen(new MainScreen());
    }

    public static void open() {
        open("ESM - Main Screen", Text.of("Recent"), "");
    }

    public static boolean isMuffled() {
        return isMuffling;
    }

    public static Anchor getAnchor(int id) {
        return anchors.get(id);
    }

    public static List<Anchor> getAnchors() {
        return anchors;
    }

    public static void setAnchors() {
        for (int i = 0; i <= 9; i++) {
            anchors.add(new Anchor(i, "Anchor: " + i));
        }
    }

    public static void addAnchors(List<Anchor> anchorList) {
        anchors.clear();
        anchors.addAll(anchorList);
    }

    @Nullable
    public static Anchor getAnchorByName(String name) {
        return anchors.stream().filter(a -> a.getName().equals(name)).findFirst().orElse(null);
    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrix);
        minecraft.getTextureManager().bindTexture(GUI);
        this.drawTexture(matrix, getX(), getY(), 0, 0, xSize, ySize); //Main screen bounds
        drawCenteredString(matrix, textRenderer, screenTitle, getX() + 128, getY() + 8, whiteText); //Screen title
        renderButtonsTextures(matrix, mouseX, mouseY, partialTicks);
        super.render(matrix, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        super.init();
        minecraft.keyboard.setRepeatEvents(true);
        minYButton = getY() + 46;
        maxYButton = getY() + 164;


        addChild(btnToggleSoundsList = new ButtonWidget(getX() + 13, getY() + 181, 52, 13, toggleSoundsListMessage, b -> {
            boolean isAnchorMuffling = false;

            if (!screenTitle.equals(mainTitle)) {
                isAnchorMuffling = !Objects.requireNonNull(getAnchorByName(screenTitle)).getMuffledSounds().isEmpty();
            }

            if (btnToggleSoundsList.getMessage().equals(Text.of("Recent"))) {
                toggleSoundsListMessage = Text.of("All");
            } else if (btnToggleSoundsList.getMessage().equals(Text.of("All"))) {
                if (!muffledSounds.isEmpty() || isAnchorMuffling) {
                    toggleSoundsListMessage = Text.of("Muffled");
                } else {
                    toggleSoundsListMessage = Text.of("Recent");
                }
            } else {
                toggleSoundsListMessage = Text.of("Recent");
            }

            btnToggleSoundsList.setMessage(toggleSoundsListMessage);
            buttons.clear();
            open(screenTitle, toggleSoundsListMessage, searchBar.getText());
        }));

        addSoundButtons();

        addAnchorButtons();

        addButton(btnToggleMuffled = new ButtonWidget(getX() + 229, getY() + 179, 17, 17, emptyText, b -> isMuffling = !isMuffling)).setAlpha(0);

        addButton(btnDelete = new ButtonWidget(getX() + 205, getY() + 179, 17, 17, emptyText, b -> {
                anchor = getAnchorByName(screenTitle);
                if (screenTitle.equals(mainTitle)) {
                    muffledSounds.clear();
                    open(mainTitle, btnToggleSoundsList.getMessage(), searchBar.getText());
                } else {
                    if (anchor == null) {
                        return;
                    }
                    anchor.deleteAnchor();
                    buttons.clear();
                    open(anchor.getName(), btnToggleSoundsList.getMessage(), searchBar.getText());
                }
            })
        ).setAlpha(0);

        addButton(btnSetAnchor = new ButtonWidget(getX() + 260, getY() + 62, 11, 11, emptyText, b ->
            Objects.requireNonNull(getAnchorByName(screenTitle)).setAnchor())).setAlpha(0);

        addButton(btnEditAnchor = new ButtonWidget(getX() + 274, getY() + 62, 11, 11, emptyText, b ->
            editTitle(Objects.requireNonNull(getAnchorByName(screenTitle))))).setAlpha(0);

        addEditAnchorButtons();

        if (screenTitle.equals(mainTitle)) {
            btnSetAnchor.visible = false;
            btnEditAnchor.visible = false;
        }

        addButton(searchBar = new TextFieldWidget(textRenderer, getX() + 74, getY() + 183, 119, 13, emptyText));
        searchBar.setHasBorder(false);
        searchBar.setText(searchBarText);

        addChild(btnPrevSounds = new ButtonWidget(getX() + 10, getY() + 22, 13, 20, emptyText, b ->
            listScroll(searchBar.getText().length() > 0 ? filteredButtons : buttons, -1)));

        addChild(btnNextSounds = new ButtonWidget(getX() + 233, getY() + 22, 13, 20, emptyText, b ->
            listScroll(searchBar.getText().length() > 0 ? filteredButtons : buttons, 1)));

        updateText();
    }


    private void addSoundButtons() {
        int buttonH = minYButton;
        anchor = getAnchorByName(screenTitle);

        if (!screenTitle.equals(mainTitle) && anchor == null) {
            return;
        }

        if (btnToggleSoundsList.getMessage().equals(Text.of("Recent"))) {
            soundsList.clear();
            if (screenTitle.equals(mainTitle) && !muffledSounds.isEmpty()) {
                soundsList.addAll(muffledSounds.keySet());
            } else if (anchor != null && !anchor.getMuffledSounds().isEmpty()) {
                soundsList.addAll(anchor.getMuffledSounds().keySet());
            }
            soundsList.addAll(recentSoundsList);
        } else if (btnToggleSoundsList.getMessage().equals(Text.of("All"))) {
            soundsList.clear();
            soundsList.addAll(Registry.SOUND_EVENT.getIds());
            forbiddenSounds.forEach(fs -> soundsList.removeIf(sl -> sl.toString().contains(fs)));
        } else {
            soundsList.clear();
            if (screenTitle.equals(mainTitle) && !muffledSounds.isEmpty()) {
                soundsList.addAll(muffledSounds.keySet());
            } else if (anchor != null && !anchor.getMuffledSounds().isEmpty()) {
                soundsList.addAll(anchor.getMuffledSounds().keySet());
            }
        }

        if (soundsList.isEmpty()) {
            return;
        }

        for (Identifier sound : soundsList) {

            double volume;

            if (screenTitle.equals(mainTitle)) {
                volume = muffledSounds.get(sound) == null ? 1D : muffledSounds.get(sound);
            } else if (anchor != null) {
                volume = anchor.getMuffledSounds().get(sound) == null ? 1D : anchor.getMuffledSounds().get(sound);
            } else {
                volume = 1D;
            }

            MuffledSlider volumeSlider = new MuffledSlider(getX() + 11, buttonH, 205, 11, volume, sound, screenTitle, anchor);

            boolean muffledAnchor = anchor != null && screenTitle.equals(anchor.getName()) && !anchor.getMuffledSounds().isEmpty() && anchor.getMuffledSounds().containsKey(sound);
            boolean muffledScreen = screenTitle.equals(mainTitle) && !muffledSounds.isEmpty() && muffledSounds.containsKey(sound);

            if (muffledAnchor || muffledScreen) {
                ((AbstractButtonWidgetAccessor) volumeSlider).setFGColor(0xffff00);
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
                btnAnchor = new ButtonWidget(buttonW, getY() + 24, 16, 16, Text.of(disabledMsg[i]), b -> {

                });
                btnAnchor.active = false;
            } else {
                int finalI = i;
                btnAnchor = new ButtonWidget(buttonW, getY() + 24, 16, 16, Text.of(String.valueOf(i)), b -> {
                    anchor = anchors.get(finalI);
                    if (anchor == null) return;
                    if (screenTitle.equals(anchor.getName())) {
                        screenTitle = mainTitle;
                    } else {
                        screenTitle = anchor.getName();
                    }
                    buttons.clear();
                    open(screenTitle, btnToggleSoundsList.getMessage(), searchBar.getText());
                });
            }

            ((AbstractButtonWidgetAccessor) btnAnchor).setFGColor(whiteText);
            addButton(btnAnchor).setAlpha(0);
            buttonW += 20;
        }
    }

    private void addEditAnchorButtons() {
        addButton(editAnchorTitleBar = new TextFieldWidget(textRenderer, getX() + 302, btnEditAnchor.y + 20, 84, 11, emptyText)).visible = false;
        addButton(editAnchorRadiusBar = new TextFieldWidget(textRenderer, getX() + 302, editAnchorTitleBar.y + 15, 30, 11, emptyText)).visible = false;
        addButton(btnAccept = new ButtonWidget(getX() + 259, editAnchorRadiusBar.y + 15, 40, 20, Text.of("Accept"), b -> {
            anchor = getAnchorByName(screenTitle);
            if (!editAnchorTitleBar.getText().isEmpty() && !editAnchorRadiusBar.getText().isEmpty() && anchor != null) {
                int radius = Integer.parseInt(editAnchorRadiusBar.getText());

                if (radius > 32) {
                    radius = 32;
                } else if (radius < 1) {
                    radius = 1;
                }

                anchor.editAnchor(editAnchorTitleBar.getText(), radius);
                screenTitle = editAnchorTitleBar.getText();
                editTitle(anchor);
            }
        })).visible = false;

        addButton(btnCancel = new ButtonWidget(getX() + 300, editAnchorRadiusBar.y + 15, 40, 20, Text.of("Cancel"), b ->
            editTitle(Objects.requireNonNull(getAnchorByName(screenTitle))))).visible = false;

    }

    private void renderButtonsTextures(MatrixStack matrix, double mouseX, double mouseY, float partialTicks) {
        int x; //start x point of the button
        int y; //start y point of the button
        float v; //start x point of the texture
        String message; //Button message
        int stringW; //text width
        int darkBG = BackgroundHelper.ColorMixer.getArgb(223, 0, 0, 0); //background color for Screen::fill()

        //Mute sound buttons and play sound buttons; Sound names
        if (buttons.size() < soundsList.size()) return;

        //Delete button
        x = btnDelete.x + 8;
        y = btnDelete.y;
        message = screenTitle.equals(mainTitle) ? "Delete Muffled List" : "Delete Anchor";
        stringW = textRenderer.getWidth(message) / 2;
        if (btnDelete.isHovered()) {
            fill(matrix, x - stringW - 2, y + 20, x + stringW + 2, y + 31, darkBG);
            drawCenteredString(matrix, textRenderer, message, x, y + 22, whiteText);
        }

        //toggle muffled button
        x = btnToggleMuffled.x + 8;
        y = btnToggleMuffled.y;
        minecraft.getTextureManager().bindTexture(GUI);

        if (isMuffling) {
            drawTexture(matrix, x - 8, y, 54F, 202F, 17, 17, xSize, xSize); //muffle button
        }

        message = isMuffling ? "Stop Muffling" : "Start Muffling";
        stringW = textRenderer.getWidth(message) / 2;
        if (btnToggleMuffled.isHovered()) {
            fill(matrix, x - stringW - 2, y + 20, x + stringW + 2, y + 31, darkBG);
            drawCenteredString(matrix, textRenderer, message, x, y + 22, whiteText);
        }

        //Anchor coordinates and set coord button
        Anchor anchor = getAnchorByName(screenTitle);
        String dimensionName = "";
        String radius;
        x = btnSetAnchor.x;
        y = btnSetAnchor.y;

        if (anchor != null) {
            stringW = textRenderer.getWidth("Dimension: ");
            radius = anchor.getRadius() == 0 ? "" : String.valueOf(anchor.getRadius());
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
                    if (button.getMessage().getString().equals(String.valueOf(anchor.getId()))) {
                        drawTexture(matrix, button.x - 5, button.y - 2, 71F, 202F, 27, 22, xSize, xSize); //fancy selected Anchor indicator
                        break;
                    }
                }
            }
        }

        message = "Set Anchor";
        stringW = textRenderer.getWidth(message) + 2;

        //Set Anchor tooltip
        if (btnSetAnchor.isHovered() && !editAnchorTitleBar.visible) {
            fill(matrix, x - 5, y + 16, x + stringW, y + 29, darkBG);
            textRenderer.draw(matrix, message, x, y + 18, whiteText);
        }

        message = "Edit Anchor";
        stringW = textRenderer.getWidth(message) + 2;

        if (btnEditAnchor.visible && !editAnchorTitleBar.visible && btnEditAnchor.isHovered()) {
            fill(matrix, x - 5, y + 16, x + stringW + 2, y + 29, darkBG);
            textRenderer.draw(matrix, message, x, y + 18, whiteText);
        }

        //draw anchor buttons tooltip
        for (int i = 0; i <= 9; i++) {
            AbstractButtonWidget btn = buttons.get(soundsList.size() + i);
            x = btn.x + 8;
            y = btn.y;
            message = isAnchorsDisabled ? "Anchors are disabled" : anchors.get(i).getName();
            stringW = textRenderer.getWidth(message) / 2;

            if (btn.isHovered()) {
                fill(matrix, x - stringW - 2, y - 2, x + stringW + 2, y - 13, darkBG);
                drawCenteredString(matrix, textRenderer, message, x, y - 11, whiteText);
            }
        }

        //Toggle List button draw message
        x = btnToggleSoundsList.x;
        y = btnToggleSoundsList.y;
        message = btnToggleSoundsList.getMessage().getString();
        int centerText = x + (btnToggleSoundsList.getWidth() / 2) - (textRenderer.getWidth(message) / 2);
        textRenderer.draw(matrix, message, centerText, y + 3, 0);
        String text = "Showing " + message + " sounds";
        int textW = textRenderer.getWidth(text);
        int textX = x + (btnToggleSoundsList.getWidth() / 2) - (textW / 2) + 6;

        if (mouseX > x && mouseX < x + 43 && mouseY > y && mouseY < y + 13) {
            fill(matrix, textX - 2, y + 20, textX + textW + 2, y + 22 + textRenderer.fontHeight, darkBG);
            textRenderer.draw(matrix, text, textX, y + 22, whiteText);
        }

        //Show Radius and Title text when editing Anchor and bg
        x = btnSetAnchor.x;
        y = editAnchorTitleBar.y;
        if (editAnchorRadiusBar.visible) {
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
        x = searchBar.x;
        y = searchBar.y;
        Text searchHint = (new TranslatableText("gui.recipebook.search_hint")).formatted(Formatting.ITALIC).formatted(Formatting.GRAY); //Stolen from Vanilla ;)
        if (!this.searchBar.isFocused() && this.searchBar.getText().isEmpty()) {
            drawTextWithShadow(matrix, textRenderer, searchHint, x + 1, y, -1);
        }

        //next sounds button tooltip
        x = btnNextSounds.x;
        y = btnNextSounds.y;
        message = "Next Sounds";
        stringW = textRenderer.getWidth(message) / 2;

        if (mouseX > x && mouseX < x + btnNextSounds.getWidth() && mouseY > y && mouseY < y + btnNextSounds.getHeight()) {
            fill(matrix, x - stringW - 2, y - 2, x + stringW + 2, y - 13, darkBG);
            drawCenteredString(matrix, textRenderer, message, x, y - 11, whiteText);
        }

        //previuos sounds button tooltip
        x = btnPrevSounds.x;
        y = btnPrevSounds.y;
        message = "Previuos Sounds";
        stringW = textRenderer.getWidth(message) / 2;

        if (mouseX > x && mouseX < x + btnPrevSounds.getWidth() && mouseY > y && mouseY < y + btnPrevSounds.getHeight()) {
            fill(matrix, x - stringW - 2, y - 2, x + stringW + 2, y - 13, darkBG);
            drawCenteredString(matrix, textRenderer, message, x, y - 11, whiteText);
        }
    }

    private void editTitle(Anchor anchor) {
        editAnchorTitleBar.setText(anchor.getName());
        editAnchorTitleBar.visible = !editAnchorTitleBar.visible;

        editAnchorRadiusBar.setText(String.valueOf(anchor.getRadius()));
        editAnchorRadiusBar.visible = !editAnchorRadiusBar.visible;

        btnAccept.visible = !btnAccept.visible;
        btnCancel.visible = !btnCancel.visible;

        editAnchorRadiusBar.setEditableColor(0xffffff);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double direction) {
        return searchBar.getText().length() > 0 ? listScroll(filteredButtons, direction * -1) : listScroll(buttons, direction * -1);
    }

    private boolean listScroll(List<AbstractButtonWidget> buttonList, double direction) {
        int buttonH = minYButton;

        if (index <= 0 && direction < 0) {
            return false;
        }

        if ((index >= buttonList.size() - 10 || index >= soundsList.size() - 10) && direction > 0) {
            return false;
        }

        index += direction > 0 ? 10 : -10;

        for (AbstractButtonWidget button : buttonList) {
            if (button instanceof MuffledSlider) {
                int buttonIndex = buttonList.indexOf(button);
                button.visible = buttonIndex < index + 10 && buttonIndex >= index;

                if (button.visible) {
                    button.y = buttonH;
                    buttonH += button.getHeight() + 2;
                }

                ((MuffledSlider) button).getBtnToggleSound().y = button.y;
                ((MuffledSlider) button).getBtnToggleSound().active = button.visible;
                ((MuffledSlider) button).getBtnPlaySound().y = button.y;
                ((MuffledSlider) button).getBtnPlaySound().active = button.visible;
            }
        }

        return true;
    }

    private void updateText() {
        int buttonH = minYButton;
        filteredButtons.clear();

        for (AbstractButtonWidget button : buttons) {
            if (button instanceof MuffledSlider) {
                if (button.getMessage().toString().contains(searchBar.getText().toLowerCase())) {
                    if (!filteredButtons.contains(button))
                        filteredButtons.add(button);

                    button.y = buttonH;
                    buttonH += button.getHeight() + 2;

                    button.visible = button.y < maxYButton;
                } else {
                    button.visible = false;
                }

                ((MuffledSlider) button).getBtnToggleSound().y = button.y;
                ((MuffledSlider) button).getBtnToggleSound().active = button.visible;
                ((MuffledSlider) button).getBtnPlaySound().y = button.y;
                ((MuffledSlider) button).getBtnPlaySound().active = button.visible;

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

        //Search bar, Edit title bar & Edit Anchor radius bar looses focus when pressed "Enter" or "Intro"
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
        if (button == 1) {
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
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        updateText();
        super.resize(minecraft, width, height);
    }

    @Override
    public void onClose() {
        super.onClose();
        JsonIO.saveAnchors(anchors);
        JsonIO.saveMuffledMap(muffledSounds);
    }

    public static String getScreenTitle() {
        return screenTitle;
    }

    private int getX() {
        return (this.width - xSize) / 2;
    }

    private int getY() {
        return (this.height - ySize) / 2;
    }

}
