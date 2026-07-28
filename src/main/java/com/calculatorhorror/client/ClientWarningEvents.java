package com.calculatorhorror.client;

import com.calculatorhorror.CalculatorHorror;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

/**
 * Shows the horror-content {@link WarningScreen} at two points, each remembered separately so a
 * returning player isn't nagged twice: once ever in front of the main menu (covers the "I own
 * this mod at all" disclosure), and once ever per world/server (covers "I'm about to actually
 * play this specific save/server").
 *
 * {@code value = Dist.CLIENT} on the class keeps FML from even classloading this on a dedicated
 * server, where {@code Screen}/{@code Minecraft} don't exist.
 *
 * The per-world warning can't be shown directly from {@code LoggingIn} - at that point vanilla
 * has just put up its own {@code ReceivingLevelScreen} ("Loading terrain"), and once chunks
 * finish loading it unconditionally calls {@code Minecraft#setScreen(null)} to dismiss it,
 * clobbering whatever screen we'd set in the meantime. Confirmed by testing against a real
 * client: the warning never actually appeared, clobbered before the screenshot even landed.
 * Fixed by only recording what needs the warning in {@code LoggingIn}, then actually showing it
 * from a tick handler once {@code minecraft.screen} has gone back to {@code null} on its own -
 * i.e. after vanilla's own loading screen has already cleared itself.
 */
@EventBusSubscriber(modid = CalculatorHorror.MODID, value = Dist.CLIENT)
public final class ClientWarningEvents {
    private static final String MARKER_FILE_NAME = "calculatorhorror_warning_agreed.txt";
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
        Component.translatable("calculatorhorror.warning.body.line10"),
        Component.translatable("calculatorhorror.warning.body.outro"));

    private static volatile Runnable pendingOnAgree;

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
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.isLocalServer() && minecraft.getSingleplayerServer() != null) {
            // A marker file living inside the world's own save folder, not a name-keyed entry in
            // our own client config - a world can be deleted and a new, unrelated one created
            // with the exact same default name ("New World"), and a name-based key would
            // wrongly treat that new world as already agreed to. This marker travels with the
            // actual save data instead: gone if the world's deleted, present if it's just being
            // reloaded, and copied along if the save itself is ever copied/shared - all correct.
            Path marker = minecraft.getSingleplayerServer().getWorldPath(LevelResource.ROOT).resolve(MARKER_FILE_NAME);
            if (!Files.exists(marker)) {
                pendingOnAgree = () -> writeMarker(marker);
            }
        } else {
            ServerData serverData = minecraft.getCurrentServer();
            String serverIp = serverData != null ? serverData.ip : "unknown";
            if (!WarningState.get().hasAgreedToServer(serverIp)) {
                pendingOnAgree = () -> WarningState.get().agreeToServer(serverIp);
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (pendingOnAgree == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen == null && minecraft.player != null) {
            Runnable onAgree = pendingOnAgree;
            pendingOnAgree = null;
            minecraft.setScreen(new WarningScreen(null, TITLE, BODY, onAgree));
        }
    }

    private static void writeMarker(Path marker) {
        try {
            Files.createDirectories(marker.getParent());
            Files.writeString(marker,
                "This file records that a player agreed to this world's Calculator (Horror) content "
                    + "warning. Delete it to see the warning again next time this world is loaded.",
                StandardCharsets.UTF_8);
        } catch (IOException e) {
            CalculatorHorror.LOGGER.warn("Failed to write {}", marker, e);
        }
    }
}
