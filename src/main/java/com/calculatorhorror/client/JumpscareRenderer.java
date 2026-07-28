package com.calculatorhorror.client;

import com.calculatorhorror.CalculatorHorror;
import com.calculatorhorror.effect.CalculatorHorrorEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/**
 * Draws a solid white full-screen flash while the local player has
 * {@link CalculatorHorrorEffects#JUMPSCARE_FLASH} - drawn after the HUD ({@link RenderGuiEvent.Post})
 * so it covers everything, hotbar included, same reasoning as {@link ShortSightRenderer} blurring
 * post-HUD rather than pre-HUD.
 */
@EventBusSubscriber(modid = CalculatorHorror.MODID, value = Dist.CLIENT)
public final class JumpscareRenderer {
    private JumpscareRenderer() {
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !minecraft.player.hasEffect(CalculatorHorrorEffects.JUMPSCARE_FLASH)) {
            return;
        }
        GuiGraphics guiGraphics = event.getGuiGraphics();
        guiGraphics.fill(0, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight(), 0xFFFFFFFF);
    }
}
