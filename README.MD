# LittleCraft

![Minecraft Version](https://img.shields.io/badge/Minecraft-26.1.2-green.svg)
![Loader](https://img.shields.io/badge/Loader-NeoForge-orange.svg)
[![License: Custom](https://img.shields.io/badge/License-Custom-blue.svg)](LICENSE)

**LittleCraft** is a lightweight NeoForge mod for **Minecraft 26.1.2**, created to bring **Agere** and **ABDL** elements into roleplay gameplay.

The mod currently adds these main features:
- **Little Mode** - players can shrink into a smaller "Little" form with reduced model size, hitbox, and camera height. Switching is done via commands.
- **Diaper** - can be worn for immersive roleplay. While equipped, it gradually **loses durability**, simulating **wetness or fullness** equal to the durability lost. Both the **player model** and the **item in inventory** visually update to reflect the diaper’s current state.
- **Diaper Change** - another player can **change your diaper** by holding a fresh Diaper item in hand and **right-clicking** on you.  
This feature enhances interactivity and roleplay immersion, especially on ABDL and Agere servers.

These features are especially useful for **ABDL** and **Agere** servers, where size differences and roleplay props matter.  

Future updates will expand customization, add more interactive items, and improve compatibility with community-driven features.

---

## 💬 Commands

| Command | Description |
|----------|-------------|
| `/islittle` | Shows whether you are currently in Little Mode. |
| `/little true` | Enables Little Mode. |
| `/little false` | Disables Little Mode. |

> Currently, switching modes is only available via commands.  
> Future versions will introduce optional items to toggle size and enhance immersion.

---

## 🌍 Localization

Supported languages:
- English (`en_us`)
- French (`fr_fr`)
- German (`de_de`)
- Japanese (`ja_jp`)
- Russian (`ru_ru`)
- Spanish (`es_es`)
- Ukrainian (`uk_ua`)

---

## ⚙️ Technical Details

LittleCraft supports both **Singleplayer** and **Multiplayer** environments.  
It uses two player-specific NBT tags:

| Tag | Type | Description |
|------|------|-------------|
| `IsLittle` | `boolean` | Indicates whether the player is in Little Mode. |
| `LittleAge` | `int` | Reserved for future features involving gradual scaling. |

These tags are saved in the player’s data and synchronized between client and server.

---

## 🧱 Known Issues

- Player nameplates may appear slightly misaligned when scaled.  
  *Contributions and pull requests are welcome.*

---

## 🗓 Version Info

- **Minecraft:** 1.21.1 / 26.1.2
- **Next planned update:** 26.2
- **No backports** to older versions are planned.
- **Since 2.0:** Added a **Diaper** item for immersive ABDL roleplay.
- **Since 2.0.2:** Added **Diaper Change** mechanic - another player can change your diaper by right-clicking with a fresh one.

---

## 🔮 Planned Features

- Gradual scaling based on `LittleAge`.
- Optional items to toggle or influence Little Mode.
- Caregiver-style interaction mechanics.
- Improved rendering and nameplate alignment.

---

## 👤 Author

**David von Eichendorf** (`dev1lroot` / `Davy`)  
License: **MIT-SCAG or GPL-3.0**
