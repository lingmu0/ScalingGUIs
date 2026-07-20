Changelog

1.21.1-2.0.0
* Ported the mod to Minecraft 1.21.1 and NeoForge.
* Replaced the removed Forge config GUI API with native 1.21.1 screens and widgets.
* Reworked screen/HUD scaling around the modern window GUI scale.
* Added a Mixin-backed tooltip scale that preserves independent tooltip sizing.
* Preserved the legacy JSON schema and added one-time import of general `.cfg` settings.
* Added English JSON translations and Simplified Chinese translations.
* Added the GUI Scales button as the final Options entry and added a rebindable F9 shortcut.

1.12.2-1.0.3.1
* Bug Fix - Fixes ConcurrentModificationException (#1). Thanks to Srdjan-V.

1.12.2-1.0.3.0
* Added dynamic scaling for whitelisted GuiContainers
* Bug Fix - Button click no longer playing twice on Gui Scales when Optifine is installed

1.12.2-1.0.2.1
* Bug Fix - Video Settings now scales correctly after changing the main GUI scale settings

1.12.2-1.0.2.0
* Added compatibility with Optifine

1.12.2-1.0.1.0
* Added delete function to custom entry screens

1.12.2-1.0.0.1
* Select-value screens now return to parent screen on ESC press

1.12.2-1.0.0.0
* Initial release

1.12.2-0.0.3.4
* Prep for initial release.

1.12.2-0.0.3.3
* Bug Fix - Incorrect import statement. (scala Array instead of java.util)

1.12.2-0.0.3.2
* Changed RenderTooltipEvent.PostText method priority to catch Apple Skin tooltip info

1.12.2-0.0.3.1
* Bug Fix - New GUI entry screen now goes back when "Done" is pressed and the gui name field is blank

1.12.2-0.0.3.0
* Changed select-value screens to list GUI names in order from most recent to least
  * Added config to display in alphabetical order

1.12.2-0.0.2.0

* Added blacklist to handle screens consisting of multiple GUIs (specifically Tinkers Construct)
  * Added default blacklist values to handle known cases (based on mods present)
  * Added config to update blacklist with default values (default true)
* Added config to log GUI class names to chat (default false)
* Rearranged settings to be a little cleaner due to additon of blacklist
  * General settings now have their own category screen
* Changed tooltip scaling to lowest event priority to prevent firing before More Overlays draws its JEI item search overlay
