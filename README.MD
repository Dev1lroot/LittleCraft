# LittleCraft

**LittleCraft** is a lightweight NeoForge mod for **Minecraft 1.21.1**, designed for roleplay-focused gameplay.  
It introduces a **Little Mode** that lets players take the role of a smaller “Little” character with a reduced model size, hitbox, and camera height.  
The mod is especially useful for **ABDL** and **Agere** themed servers, where size-based interactions and character dynamics are part of the roleplay experience.  
It also offers a unique gameplay perspective by changing how players move, explore, and interact with the environment.  
Further updates are planned to expand customization, add interactive items, and enhance compatibility with community-driven features.


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

---

## 🗓 Version Info

- **Minecraft:** 1.21.1  
- **Next planned update:** 1.21.10  
- **No backports** to older versions are planned.

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
