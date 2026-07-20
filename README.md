# ScalingGUIs (Forge 1.20.1)

ScalingGUIs decouples the scale used by normal screens, the HUD, and tooltips. It also supports exact screen-class rules, superclass/interface group rules, blacklisted helper screens, and dynamically fitted container screens.

## Requirements

- Minecraft 1.20.1
- Forge 47.4.10 or newer in the Forge 47 line
- Java 17

JEI 15.20 and Obscure Tooltips 3.x are optional. Obscure Tooltips also requires its own Fragmentum dependency.

## Configuration

Open **Mods -> ScalingGUIs -> Config**, use the **GUI Scales** entry at the end of the Options screen, or press the rebindable **F9** shortcut.

The legacy configuration at `config/ScalingGUIs/ScalingGUIsCustomScales.json` is loaded in place. General flags from `ScalingGUIs.cfg` are imported on first launch. Invalid JSON is backed up before a new configuration is created.

Scale values retain the original meaning: `0` is automatic/max, `1`-`8` are explicit scales, and `9` means "use the main GUI scale" where that choice applies.

## Development and building

Run `gradlew genIntellijRuns` and refresh the Gradle project to generate IntelliJ IDEA's Client, Server, Game Test Server, and Data run configurations.

Run `gradlew build` to create the release JAR in `build/libs`.

## License

MIT. See `LICENSE.txt`.
