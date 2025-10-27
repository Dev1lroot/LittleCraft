# LittleCraft

**LittleCraft** is a lightweight NeoForge mod for **Minecraft 1.21.1**, created to bring **Agere** and **ABDL** elements into roleplay gameplay.

The mod currently adds two main features:
- **Little Mode** - players can shrink into a smaller "Little" form with reduced model size, hitbox, and camera height. Switching is done via commands.
- **Diaper Item (since v2.0)** - can be worn for immersive roleplay. At this stage, it works passively without extra mechanics.

These features are especially useful for **ABDL** and **Agere** servers, where size differences and roleplay props matter.  
They also provide a new perspective on how players move, explore, and interact with the game world.

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
- Russian (`ru_ru`)
- French (`fr_fr`)
- German (`de_de`)
- Ukrainian (`uk_ua`)
- Spanish (`es_es`)

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

- Diapers are currently rendered only on players, since they use a dedicated player renderer.  
  *This issue is planned to be fixed by the author in a future update.*

---

## 🗓 Version Info

- **Minecraft:** 1.21.1
- **Next planned update:** 1.21.10
- **No backports** to older versions are planned.
- **Since 2.0:** Added a **Diaper** item for immersive ABDL roleplay.

---

## 🔮 Planned Features

- Gradual scaling based on `LittleAge`.
- Optional items to toggle or influence Little Mode.
- Caregiver-style interaction mechanics.
- Improved rendering and nameplate alignment.

---

## 👤 Author

**David von Eichendorf** (`dev1lroot` / `Davy`)  
License: **MIT**
