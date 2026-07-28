# Calculator (Horror)

A Minecraft horror mod built on [NeoForge](https://neoforged.net/) for Minecraft **1.21.1**.

- Mod ID: `calculatorhorror`
- Loader: NeoForge (ModDevGradle)
- Minecraft: 1.21.1

## Building / running

This project uses the Gradle wrapper, so no local Gradle install is required. It does need a **Java 21** JDK on `JAVA_HOME`.

```bash
./gradlew build
```

Run a client for testing:

```bash
./gradlew runClient
```

Run a dedicated server for testing:

```bash
./gradlew runServer
```

If your IDE is missing dependencies, or something looks stale, refresh the local cache:

```bash
./gradlew --refresh-dependencies
```

Run the automated in-game test suite (spins up a server, runs each `@GameTest`, reports pass/fail):

```bash
./gradlew runGameTestServer
```

Regenerate data-driven files (currently: block loot tables) into `src/generated/resources`:

```bash
./gradlew runData
```

`src/generated/resources` is wired into `sourceSets.main.resources`, so generated output is picked up automatically — review the diff and commit it, no manual copy step needed.

## Project layout

- `src/main/java/com/calculatorhorror/` — mod source code
  - `action/` — the low-level capability toolkit (effects, inventory, containers, blocks, teleport, sound, chunk tricks)
  - `command/` — dev-only test harness (`/calculatorhorror test ...`) for exercising each action manually in-game
  - `gametest/` — `@GameTest` suite (`ActionToolkitGameTests`) that exercises the toolkit against a real connected `ServerPlayer`, run via `./gradlew runGameTestServer`
  - `datagen/` — data providers (loot tables, etc.), run via `./gradlew runData`
- `src/main/resources/` — assets, data, lang files
- `src/generated/resources/` — datagen output, committed to the repo (see above)
- `src/main/templates/META-INF/neoforge.mods.toml` — mod metadata (values are templated from `gradle.properties`)
- `gradle.properties` — mod id/name/version and Minecraft/NeoForge version pins
- `tools/rcon.py` — small RCON client (needs `pip3 install -r tools/requirements.txt`) for sending single commands to a locally running dev server, e.g. `python3 tools/rcon.py "calculatorhorror test selftest"`

## What this mod can do

As part of its horror mechanics, Calculator (Horror) can, on the player:

- apply and clear potion effects
- read and edit the player's inventory
- read and edit the contents of chests and other containers
- place and change blocks in the world
- teleport players

These are intentional gameplay mechanics, disclosed here, in the mod's launcher description (`neoforge.mods.toml`), and in-game. This is not malicious or hidden behavior — it's how the mod's horror effects work.

## Mapping names

By default, this project uses Mojang's official mappings for methods and fields in the Minecraft codebase. These names are covered by a specific license — see https://github.com/NeoForged/NeoForm/blob/main/Mojang.md for the current text.

## Additional resources

- NeoForge docs: https://docs.neoforged.net/
- NeoForged Discord: https://discord.neoforged.net/

## License

Licensed under the [Apache License, Version 2.0](LICENSE).
