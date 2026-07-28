package com.calculatorhorror.client;

import com.calculatorhorror.CalculatorHorror;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

/**
 * Shows the horror-content {@link WarningScreen} at two points, each remembered separately (via
 * {@link WarningState}) so a returning player isn't nagged twice: once ever in front of the main
 * menu (covers the "I own this mod at all" disclosure), and once ever per world/server (covers
 * "I'm about to actually play this specific save/server").
 *
 * {@code value = Dist.CLIENT} on the class keeps FML from even classloading this on a dedicated
 * server, where {@code Screen}/{@code Minecraft} don't exist.
 *
 * The per-world warning can't be shown directly from {@code LoggingIn} - at that point vanilla
 * has just put up its own {@code ReceivingLevelScreen} ("Loading terrain"), and once chunks
 * finish loading it unconditionally calls {@code Minecraft#setScreen(null)} to dismiss it,
 * clobbering whatever screen we'd set in the meantime. Confirmed by testing against a real
 * client: the warning never actually appeared, clobbered before the screenshot even landed.
 * Fixed by only recording which world needs the warning in {@code LoggingIn}, then actually
 * showing it from a tick handler once {@code minecraft.screen} has gone back to {@code null} on
 * its own - i.e. after vanilla's own loading screen has already cleared itself.
 */
@EventBusSubscriber(modid = CalculatorHorror.MODID, value = Dist.CLIENT)
public final class ClientWarningEvents {
    private static final Component TITLE = Component.translatable("calculatorhorror.warning.title");
    private static final List<Component> BODY = List.of(
        Component.translatable("calculatorhorror.warning.body.intro"),
        Component.translatable("calculatorhorror.warning.body.line1"),
        Component.translatable("calculatorhorror.warning.body.line2"),
        Component.translatable("calculatorhorror.warning.body.line3"),
        Component.translatable("calculatorhorror.warning.body.line4"),
        Component.translatable("calculatorhorror.warning.body.line5"),
        Component.translatable("calculatorhorror.warning.body.line6"),
        Component.translatable("calculatorhorror.warning.body.line7"),
        Component.translatable("calculatorhorror.warning.body.line8"),
        Component.translatable("calculatorhorror.warning.body.line9"),
        Component.translatable("calculatorhorror.warning.body.outro"));

    private static volatile String pendingWorldKey;

    private ClientWarningEvents() {
    }

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (event.getNewScreen() instanceof TitleScreen titleScreen && !WarningState.get().hasAgreedGlobally()) {
            event.setNewScreen(new WarningScreen(titleScreen, TITLE, BODY, () -> WarningState.get().agreeGlobally()));
        }
    }

    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        String worldKey = currentWorldKey(Minecraft.getInstance());
        if (!WarningState.get().hasAgreedToWorld(worldKey)) {
            pendingWorldKey = worldKey;
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (pendingWorldKey == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen == null && minecraft.player != null) {
            String worldKey = pendingWorldKey;
            pendingWorldKey = null;
            minecraft.setScreen(new WarningScreen(null, TITLE, BODY, () -> WarningState.get().agreeToWorld(worldKey)));
        }
    }

    private static String currentWorldKey(Minecraft minecraft) {
        if (minecraft.isLocalServer() && minecraft.getSingleplayerServer() != null) {
            return "singleplayer:" + minecraft.getSingleplayerServer().getWorldData().getLevelName();
        }
        ServerData server = minecraft.getCurrentServer();
        return "server:" + (server != null ? server.ip : "unknown");
    }
}
