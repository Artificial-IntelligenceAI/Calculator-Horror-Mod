package com.calculatorhorror.client;

import com.calculatorhorror.CalculatorHorror;
import com.calculatorhorror.effect.CalculatorHorrorEffects;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/**
 * Renders a real full-screen gaussian-style blur (not just tightened fog) while the local player
 * has {@link CalculatorHorrorEffects#SHORT_SIGHT} - the "-7D vision" screen effect. Reuses
 * vanilla's own {@code minecraft:shaders/post/blur.json} post-chain (the same one that powers the
 * menu background blur added in 1.20.5+) rather than shipping a duplicate shader, driven manually
 * from {@link RenderFrameEvent.Post} instead of {@code Screen#renderBackground} so it blurs the
 * whole composited frame - world *and* HUD - not just whatever's behind a menu screen.
 */
@EventBusSubscriber(modid = CalculatorHorror.MODID, value = Dist.CLIENT)
public final class ShortSightRenderer {
    private static final ResourceLocation BLUR_LOCATION = ResourceLocation.withDefaultNamespace("shaders/post/blur.json");
    private static final float BLUR_RADIUS = 10.0F;

    private static PostChain blurEffect;
    private static int lastWidth = -1;
    private static int lastHeight = -1;

    private ShortSightRenderer() {
    }

    @SubscribeEvent
    public static void onRenderFrame(RenderFrameEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !minecraft.player.hasEffect(CalculatorHorrorEffects.SHORT_SIGHT)) {
            return;
        }
        PostChain effect = ensureLoaded(minecraft);
        if (effect == null) {
            return;
        }
        int width = minecraft.getWindow().getWidth();
        int height = minecraft.getWindow().getHeight();
        if (width != lastWidth || height != lastHeight) {
            effect.resize(width, height);
            lastWidth = width;
            lastHeight = height;
        }
        effect.setUniform("Radius", BLUR_RADIUS);
        effect.process(event.getPartialTick().getGameTimeDeltaPartialTick(true));
    }

    private static PostChain ensureLoaded(Minecraft minecraft) {
        if (blurEffect != null) {
            return blurEffect;
        }
        try {
            blurEffect = new PostChain(minecraft.getTextureManager(), minecraft.getResourceManager(), minecraft.getMainRenderTarget(), BLUR_LOCATION);
            lastWidth = minecraft.getWindow().getWidth();
            lastHeight = minecraft.getWindow().getHeight();
            blurEffect.resize(lastWidth, lastHeight);
        } catch (IOException e) {
            CalculatorHorror.LOGGER.error("Failed to load the blur post-chain for the short sight effect", e);
        }
        return blurEffect;
    }
}
