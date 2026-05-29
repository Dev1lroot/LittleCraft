# LittleCraft

[![Minecraft Version](https://img.shields.io/badge/Minecraft-26.1.2-darkgreen.svg?logo=minecraft)](https://minecraft.net)
[![NeoForge Version](https://img.shields.io/badge/NeoForge-26.1.2.64--beta-orange.svg)](https://neoforged.net)
[![License](https://img.shields.io/badge/License-GNU%20GPL%203.0--Only-blue.svg)](https://www.gnu.org/licenses/gpl-3.0.html)
[![Mod Version](https://img.shields.io/badge/Version-3.0.3-purple.svg)](https://semver.org/)

**LittleCraft** is a NeoForge mod for **Minecraft 26.1.2**, designed to bring immersive **Age Regression** (Agere) and **ABDL** elements into roleplay gameplay. It introduces dynamic character scaling, functional roleplay items, and unique player-to-player interactions tailored for community servers where size mechanics and thematic props matter.

![Overview](./documentation/images/preview.png)

**Key Features:**
- **Dynamic Age Scaling:** Your player model, hitbox, and camera height scale smoothly based on your selected age (ranging from 6 to 18).
- **Functional Diapers:** Adds wearable diapers for immersive roleplay.
  - **The System:** The diaper has capacity and used attributes that calculate its visual wetness; these attributes can be extended by adding additional padding. While equipped, the diaper transfers fluid from the player's bladder to the diaper's NBT data to simulate wetness or fullness. The player model visually updates to reflect its current state.
  - **Custom Designs (NBT):** Supports custom textures via the `DESIGN` NBT tag. Expand available styles by adding textures to `littlecraft/textures/item/diaper/{design}/` (`dry.png`, `wet.png`, `full.png`) and declaring the design in the item model JSON. Texture state thresholds: **dry** `< 10 ml`, **full** `≥ capacity − 100 ml` or `≥ 5000 ml`, **wet** otherwise.
  - **Interactive Diaper Changing:** Another player can change your diaper by right-clicking you while holding a fresh diaper in their main hand.
  - **Bi-color Tinting:** The item icon supports two independently dyeable color layers (base + overlay) blended via the MC dye recipe.
- **Body Mechanics — Bladder & Stomach:**
  - Players accumulate bladder and stomach fill from eating and drinking, scaled by their little age.
  - Pressing the **Piss** keybind transfers fluid from the bladder. Pressing the **Poop** keybind expels stomach contents.
  - **Output priority:** equipped diaper (if not full) → any other equippable pants item (marks as pooped) → seated potty.
  - The **Incontinence** effect causes passive bladder leakage into a worn diaper.
  - The **Stink** effect is applied whenever an item tagged `is_pooped` is equipped in the legs slot, regardless of item type.
- **Potty (16 colors):** Functional sit-on block available in all 16 dye colors.
  - Right-click to sit; press **Shift** to stand up.
  - Stores up to **1 000 ml** of piss and **1 000 g** of poop independently.
  - Broken with contents → drops the potty item carrying its NBT data, exactly like a shulker box. Re-placing restores the stored amounts.
  - Placing the potty in a crafting grid (no other items) resets both values to zero.
- **Carry Mechanic:** Adult players (age 18+) can pick up little players (ages 6–12) by right-clicking them with an empty main hand.
  - The carried player is visually held at arm's height and loses movement control, though they retain full camera rotation.
  - Either player can end the carry at any time by pressing Shift.
  - Both players receive clear chat notifications when the interaction starts and ends.
- **Customizable Thigh-Highs:** A versatile character personalization item for the feet slot.
  - **Dual-Color Blending:** Two independently dyeable color layers — base fabric and stripes — blended using the standard MC dye recipe (1 dye = both layers, 2 dyes = first → base, second → stripes).
  - **Enchantment Support:** Zero armor points but fully enchantable for utility.
- **Pacifier:** A wearable head-slot item rendered as a 3D model on the player's face using the vanilla `CustomHeadLayer` pipeline (same mechanism as carved pumpkin).
  - **Dual-Color Blending:** Two independently dyeable parts — nipple/body and guard ring — using the same blending logic as Thigh-Highs.
  - In inventory and in-hand, displays as a flat two-layer icon tinted by the active colors.
  - On the head, switches to a physical box-geometry block model with `tintindex`-based coloring.
  - Crafted from a diamond ring of **Plastic** pieces.
- **Cozy Cribs:** Decorative and functional colored cribs tailored specifically for little players.
- **New Potions:**
  - **Potion of Regression:** Brewed from an **Awkward Potion** and a **Golden Dandelion**.
    - **Drinking:** Reduces the player's age by 1.
    - **Splash Variant:** Reverts adult animals and monsters into their baby forms (where applicable).
  - **Potion of Growth:** Brewed from an **Awkward Potion** and **Bone Meal**.
    - **Drinking:** Increases the player's age by 1.
    - **Splash Variant:** Forces baby animals and monsters to grow into adults instantly.
    - **Lingering Variant:** Acts as a continuous AoE fertilizer, applying a bone meal effect to nearby crops and plants once per second while the cloud is active.

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
| `littlecraft:bladder` | `int` | Current bladder fill in ml. Max scales with age (200–2000 ml). |
| `littlecraft:stomach` | `int` | Current stomach fill in g. Max scales with age (100–1000 g). |

Custom **MobEffects** (`littlecraft:regression`, `littlecraft:growth`) are instantaneous and apply their logic immediately when the potion is consumed or the splash hits an entity.

The **Potty** block entity stores `piss` (0–1000 ml) and `poop` (0–1000 g). On break, the values are serialised into `DataComponents.BLOCK_ENTITY_DATA` on the dropped item and restored automatically when the item is re-placed, following the same pattern as vanilla shulker boxes.

The **Diaper**, **Thigh-Highs**, and **Pacifier** store their color and state data in `DataComponents.CUSTOM_DATA`. Item model tinting uses the MC 26.1.2 `items/*.json` pipeline with registered `ItemTintSource` types (`littlecraft:thigh_highs_base_color`, `littlecraft:thigh_highs_stripe_color`, `littlecraft:pacifier_body_color`, `littlecraft:pacifier_ring_color`). The `is_pooped` boolean in `CUSTOM_DATA` on any equippable legs item triggers the **Stink** effect each server tick.

---

## Version History

| Game Version | Mod Version | Changes                                                                                                                                                                                                                                |
|--------------|-------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1.20.1 | 1.0         | Base feature of toggling between little and adult modes                                                                                                                                                                                |
| 1.20.1 | 2.0         | Added Diaper item for immersive ABDL roleplay                                                                                                                                                                                          |
| 1.20.1 | 2.0.2       | Added Diaper Change mechanic - another player can change your diaper                                                                                                                                                                   |
| 26.1.2 | 2.0.3       | Player model gradual scaling based on little age                                                                                                                                                                                       |
| 26.1.2 | 2.0.4       | Added Potion of Regression and Potion of Growth with splash and lingering variants, license changed to GPL 3.0-ONLY                                                                                                                    |
| 26.1.2 | 2.0.5       | Colored Cribs                                                                                                                                                                                                                          |
| 26.1.2 | 2.0.6       | Carry mechanic - adult players can carry little players (age 1-12)                                                                                                                                                                     |
| 26.1.2 | 2.0.7       | Customizable bi-color Thigh-Highs added to the mod                                                                                                                                                                                     |
| 26.1.2 | 3.0.0       | Bladder and Stomach Systems introduced                                                                                                                                                                                                 |
| 26.1.2 | 3.0.1       | The Potty introduced - 16 colors, sit mechanic, NBT persistence, crafting clear; diaper per-design textures (dry/wet/full); `is_pooped` tag generalised to all equippable pants items; output priority system (diaper → pants → potty) |
| 26.1.2 | 3.0.2       | Pacifier (head-slot, bi-color, 3D head model)                                                                                                                                                                                          |
| 26.1.2 | 3.0.3       | Annoying HUD is hidden, now it only appears when you wear a pacifier with name "Debug"                                                                                                                                                  |

- **Current Maintained Minecraft Version:** 26.1.2
- **No backports** to older versions are planned.

---

## Planned Features

- Additional caregiver interaction mechanics (feeding, rocking, etc.).
- Additional baby equipment (plushies, toys, high chairs, bottles).
- Additional diaper designs and customization options.

---

## License

**GPL-3.0-only** - see [LICENSE](LICENSE) for full terms.

## Author

**David Eichendorf** (`dev1lroot` / `DavyBoy`)  
Contact: admin@dev1lroot.com
