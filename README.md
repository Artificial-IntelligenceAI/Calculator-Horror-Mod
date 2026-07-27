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

## Project layout

- `src/main/java/com/calculatorhorror/` — mod source code
- `src/main/resources/` — assets, data, lang files
- `src/main/templates/META-INF/neoforge.mods.toml` — mod metadata (values are templated from `gradle.properties`)
- `gradle.properties` — mod id/name/version and Minecraft/NeoForge version pins

## Mapping names

By default, this project uses Mojang's official mappings for methods and fields in the Minecraft codebase. These names are covered by a specific license — see https://github.com/NeoForged/NeoForm/blob/main/Mojang.md for the current text.

## Additional resources

- NeoForge docs: https://docs.neoforged.net/
- NeoForged Discord: https://discord.neoforged.net/

## License

Licensed under the [Apache License, Version 2.0](LICENSE).
