package com.calculatorhorror.client;

import com.calculatorhorror.CalculatorHorror;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLPaths;

/**
 * Client-local record of which horror-content warnings this player has already agreed to, so
 * {@link WarningScreen} only nags once per thing rather than on every main menu / server join.
 * Persisted as plain JSON in the config directory rather than through NeoForge's ModConfigSpec,
 * since {@code agreedServers} is an open-ended, runtime-grown set rather than a fixed option.
 *
 * Singleplayer worlds are deliberately <b>not</b> tracked here - see
 * {@link ClientWarningEvents}'s per-save marker file instead. A server address can't be deleted
 * out from under this store the way a world save can, so "agreed once, remembered forever" is
 * the right call for servers but not for worlds (a world can be deleted and a new, unrelated one
 * created with the exact same default name - see the marker-file javadoc for why that matters).
 */
@OnlyIn(Dist.CLIENT)
final class WarningState {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FMLPaths.CONFIGDIR.get().resolve(CalculatorHorror.MODID + "-warnings.json");

    private static WarningState instance;

    private boolean agreedGlobally;
    private final Set<String> agreedServers = new HashSet<>();

    private WarningState() {
    }

    static synchronized WarningState get() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    boolean hasAgreedGlobally() {
        return agreedGlobally;
    }

    boolean hasAgreedToServer(String serverIp) {
        return agreedServers.contains(serverIp);
    }

    void agreeGlobally() {
        agreedGlobally = true;
        save();
    }

    void agreeToServer(String serverIp) {
        agreedServers.add(serverIp);
        save();
    }

    private static WarningState load() {
        WarningState state = new WarningState();
        if (Files.exists(FILE)) {
            try (Reader reader = Files.newBufferedReader(FILE)) {
                Data data = GSON.fromJson(reader, Data.class);
                if (data != null) {
                    state.agreedGlobally = data.agreedGlobally;
                    if (data.agreedServers != null) {
                        state.agreedServers.addAll(data.agreedServers);
                    }
                }
            } catch (IOException | RuntimeException e) {
                CalculatorHorror.LOGGER.warn("Failed to read {}, treating warnings as not yet agreed to", FILE, e);
            }
        }
        return state;
    }

    private void save() {
        Data data = new Data();
        data.agreedGlobally = this.agreedGlobally;
        data.agreedServers = new ArrayList<>(this.agreedServers);
        try {
            Files.createDirectories(FILE.getParent());
            try (Writer writer = Files.newBufferedWriter(FILE)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException e) {
            CalculatorHorror.LOGGER.warn("Failed to save {}", FILE, e);
        }
    }

    private static final class Data {
        boolean agreedGlobally;
        List<String> agreedServers;
    }
}
