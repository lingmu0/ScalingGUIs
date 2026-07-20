# ScalingGUIs (NeoForge 1.21.1)

ScalingGUIs decouples the scale used by normal screens, the HUD and tooltips. It also supports exact screen-class rules, superclass/interface group rules, blacklisted helper screens and dynamically fitted container screens.

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.235 or newer in the 21.1 line
- Java 21

## Configuration

Open **Mods → ScalingGUIs → Config**, or use the **GUI Scales** button added to Video Settings.

The 1.12.2 configuration at `config/ScalingGUIs/ScalingGUIsCustomScales.json` is loaded in place. General flags from the legacy `ScalingGUIs.cfg` are imported on first launch. Invalid JSON is backed up before a new configuration is created.

Scale values retain the original meaning: `0` is automatic/max, `1`–`8` are explicit scales, and `9` means “use the main GUI scale” where that choice applies.

## Building

Run `gradlew build`. The output JAR is written to `build/libs`.

## License

MIT. See `LICENSE.txt`.
