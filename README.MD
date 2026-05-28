# LittleCraft

[![Minecraft Version](https://img.shields.io/badge/Minecraft-26.1.2-darkgreen.svg?logo=minecraft)](https://minecraft.net)
[![NeoForge Version](https://img.shields.io/badge/NeoForge-26.1.2.64--beta-orange.svg)](https://neoforged.net)
[![License](https://img.shields.io/badge/License-GNU%20GPL%203.0--Only-blue.svg)](https://www.gnu.org/licenses/gpl-3.0.html)
[![Mod Version](https://img.shields.io/badge/Version-2.0.7-purple.svg)](https://semver.org/)

**LittleCraft** is a NeoForge mod for **Minecraft 26.1.2**, designed to bring immersive **Age Regression** (Agere) and **ABDL** elements into roleplay gameplay. It introduces dynamic character scaling, functional roleplay items, and unique player-to-player interactions tailored for community servers where size mechanics and thematic props matter.

**Key Features:**
- **Dynamic Age Scaling:** Your player model, hitbox, and camera height scale smoothly based on your selected age (ranging from 6 to 18).
- **Functional Diapers:** Adds wearable diapers for immersive roleplay.
- - **Durability System:** While equipped, the diaper gradually loses durability to simulate wetness or fullness. Both the item icon and the player model visually update to reflect its current state.
- - **Custom Designs (NBT):** Supports custom textures via the DESIGN NBT tag. You can easily expand the available styles by adding new textures to your asset path: `littlecraft/assets/textures/diaper/{design}/` (requires default.png and wetness.png).
- - **Interactive Diaper Changing:** Promotes cooperative play. Another player can change your diaper by right-clicking you while holding a fresh diaper in their main hand.
- **Carry Mechanic:** Adult players (age 18+) can pick up little players (ages 6 to 12) by right-clicking them with an empty main hand.
- - The carried player is visually held at arm's height and loses movement control, though they retain full camera rotation.
- - Either player can end the carry at any time by pressing Shift.
- - Both players receive clear chat notifications when the interaction starts and ends.
- **Customizable Thigh-Highs:** Added as a versatile character personalization element.
- - **Dual-Color Blending:** Features advanced two-color blending support, allowing for highly customizable and unique color combinations.
- - **Enchantment Support:** While they provide zero armor points, they can be enchanted with useful vanilla and custom enchantments to maintain utility.
- **Cozy Cribs:** Decorative and functional colored cribs tailored specifically for little players.
- **New Potions:**
- - **Potion of Regression:** Brewed from an **Awkward Potion** and a **Golden Dandelion**.
- - - **Drinking:** Reduces the player's age by 1.
- - - **Splash Variant:** Reverts adult animals and monsters into their baby forms (where applicable).
- - **Potion of Growth:** Brewed from an **Awkward Potion** and **Bone Meal**.
- - - **Drinking:** Increases the player's age by 1.
- - - **Splash Variant:** Forces baby animals and monsters to grow into adults instantly.
- - - **Lingering Variant:** Acts as a continuous AoE fertilizer, applying a bone meal effect to nearby crops and plants once per second while the cloud is active.

---

## Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/age get` | Everyone | Shows your current little age. |
| `/age set <number>` | Cheats | Sets your own little age to the given number. |
| `/age <player> get` | Everyone | Shows another player's current little age. |
| `/age <player> set <number>` | Cheats | Sets another player's little age. |

---

## Brewing Recipes

| Result | Ingredients |
|--------|-------------|
| ![](./documentation/images/potion_of_regression.png) Potion of Regression | ![](./documentation/images/awkward_potion.png) Awkward Potion + ![](./documentation/images/golden_dandelion.png) Golden Dandelion |
| ![](./documentation/images/potion_of_growth.png) Potion of Growth | ![](./documentation/images/awkward_potion.png) Awkward Potion + ![](./documentation/images/bone_meal.png) Bone Meal |

Splash and Lingering variants follow standard vanilla brewing (Gunpowder / Dragon's Breath).

---

## Localization

Supported languages:

- English (`en_us`)
- French (`fr_fr`)
- German (`de_de`)
- Japanese (`ja_jp`)
- Russian (`ru_ru`)
- Spanish (`es_es`)
- Ukrainian (`uk_ua`)

---

## Technical Details

LittleCraft supports both **Singleplayer** and **Multiplayer** environments.  
Player data is stored using NeoForge **Attachment Types** and synced between client and server.

| Attachment | Type | Description |
|------------|------|-------------|
| `littlecraft:little_age` | `int` | The player's current little age (6..17 = infant scale, 18+ = adult scale). |

Custom **MobEffects** (`littlecraft:regression`, `littlecraft:growth`) are instantaneous and apply their logic immediately when the potion is consumed or the splash hits an entity.

---

## Version History

| Game Version | Mod Version | Changes |
|--------------|-------------|---------|
| 1.20.1 | 1.0 | Base feature of toggling between little and adult modes |
| 1.20.1 | 2.0 | Added Diaper item for immersive ABDL roleplay |
| 1.20.1 | 2.0.2 | Added Diaper Change mechanic - another player can change your diaper |
| 26.1.2 | 2.0.3 | Player model gradual scaling based on little age |
| 26.1.2 | 2.0.4 | Added Potion of Regression and Potion of Growth with splash and lingering variants, license changed to GPL 3.0-ONLY |
| 26.1.2 | 2.0.5 | Colored Cribs |
| 26.1.2 | 2.0.6 | Carry mechanic - adult players can carry little players (age 1-12) |
| 26.1.2 | 2.0.7 | Customizable bi-color Thigh-Highs added to the mod |

- **Current Maintained Minecraft Version:** 26.1.2
- **No backports** to older versions are planned.

---

## Planned Features

- Additional caregiver interaction mechanics (feeding, rocking, etc.).
- Additional baby equipment (plushies, toys, tall chairs, pacifiers, bottles)
- Additional diaper designs and customization options.

---

## License

**GPL-3.0-only** - see [LICENSE](LICENSE) for full terms.

## Author

**David Eichendorf** (`dev1lroot` / `DavyBoy`)  
Contact: admin@dev1lroot.com
