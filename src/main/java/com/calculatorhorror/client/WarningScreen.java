package com.calculatorhorror.client;

import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * A blocking horror-content warning: the player must tick the checkbox before Continue becomes
 * clickable. Can't be dismissed with Escape ({@link #shouldCloseOnEsc} is false) - agreeing is
 * the only way through, matching the "just stays blocked" behavior the user asked for over an
 * explicit decline button.
 */
@OnlyIn(Dist.CLIENT)
public final class WarningScreen extends Screen {
    private static final int MARGIN = 20;

    private final Screen parent;
    private final List<Component> body;
    private final Runnable onAgree;
    private MultiLineLabel message = MultiLineLabel.EMPTY;
    private Button continueButton;

    public WarningScreen(Screen parent, Component title, List<Component> body, Runnable onAgree) {
        super(title);
        this.parent = parent;
        this.body = body;
        this.onAgree = onAgree;
    }

    @Override
    protected void init() {
        this.message = MultiLineLabel.create(this.font, this.width - MARGIN * 2, this.body.toArray(Component[]::new));
        int messageTop = 40;
        int checkboxY = messageTop + this.message.getLineCount() * 9 + 20;

        this.addRenderableWidget(
            Checkbox.builder(Component.translatable("calculatorhorror.warning.checkbox"), this.font)
                .pos(this.width / 2 - 155, checkboxY)
                .onValueChange((checkbox, checked) -> this.continueButton.active = checked)
                .build());

        this.continueButton = this.addRenderableWidget(
            Button.builder(Component.translatable("calculatorhorror.warning.continue"), button -> {
                this.onAgree.run();
                this.minecraft.setScreen(this.parent);
            }).bounds(this.width / 2 - 100, checkboxY + 30, 200, 20).build());
        this.continueButton.active = false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
        this.message.renderLeftAligned(guiGraphics, MARGIN, 40, 9, 0xCCCCCC);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
