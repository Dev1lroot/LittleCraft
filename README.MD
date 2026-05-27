# LittleCraft

[![Minecraft Version](https://img.shields.io/badge/Minecraft-26.1.2-darkgreen.svg?logo=minecraft)](https://minecraft.net)
[![NeoForge Version](https://img.shields.io/badge/NeoForge-26.1.2.64--beta-orange.svg)](https://neoforged.net)
[![License](https://img.shields.io/badge/License-GNU%20GPL%203.0--Only-blue.svg)](https://www.gnu.org/licenses/gpl-3.0.html)
[![Mod Version](https://img.shields.io/badge/Version-2.0.4-purple.svg)](https://semver.org/)

**LittleCraft** is a lightweight NeoForge mod for **Minecraft 26.1.2**, created to bring **Agere** and **ABDL** elements into roleplay gameplay.

The mod currently adds the following features:

- **Age Scaling** - the player model, hitbox, and camera position scale smoothly to the player's age (range 6..18)
- **Diaper** - can be worn for immersive roleplay. While equipped, it gradually **loses durability**, simulating wetness or fullness. Both the **player model** and the **inventory item** visually update to reflect the current diaper state.
- **Diaper Change** - another player can **change your diaper** by holding a fresh Diaper in hand and **right-clicking** on you.
- **Potion of Regression** - brewed from an Awkward Potion and a **Golden Dandelion**. Drinking it reduces the player's little age by 1. Throwing the splash variant turns adult animals and monsters into their baby form (where one exists).
- **Potion of Growth** - brewed from an Awkward Potion and **Bone Meal**. Drinking it increases the player's little age by 1. Throwing the splash variant turns baby animals and monsters into adults. The **lingering** variant also acts as a continuous Bone Meal source, growing nearby plants once per second while the cloud is active.

These features are especially useful on **ABDL** and **Agere** servers where size differences and roleplay props matter.

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
| Potion of Regression | Awkward Potion + Golden Dandelion |
| Potion of Growth | Awkward Potion + Bone Meal |

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

- **Current Maintained Minecraft Version:** 26.1.2
- **No backports** to older versions are planned.

---

## Planned Features

- Items (pacifiers, bottles, plushies) to toggle or influence Little Mode naturally.
- Caregiver-specific interaction mechanics.
- Additional diaper designs and customization options.

---

## License

**GPL-3.0-only** - see [LICENSE](LICENSE) for full terms.

## Author

**David Eichendorf** (`dev1lroot` / `DavyBoy`)  
Contact: admin@dev1lroot.com
