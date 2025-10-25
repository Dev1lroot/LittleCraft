# LittleCraft

**LittleCraft** is a soft and cozy mod for **Minecraft 1.21.1** built on **NeoForge**.  
It introduces a **Little Mode** that lets players shrink down to child-like proportions — their size and hitbox match that of a **baby zombie**.  
While small, players can walk through 1-block gaps and sneak under trapdoors, experiencing the world from a whole new perspective.

---

## 🧸 Gameplay Overview

When a player enters **Little Mode**, their visual and physical scale are reduced to baby-zombie size (0.35× width, 0.9× height).  
While sneaking, they become even smaller (0.7× height).  
All states are fully synchronized between server and client and persist across sessions.

---

## 💬 Commands

| Command | Description |
|----------|-------------|
| `/islittle` | Displays whether you are in Little Mode or Adult Mode. |
| `/little true` | Switch to Little Mode. |
| `/little false` | Return to Adult Mode. |

> Currently, mode switching is only available through commands.  
> Future versions will include themed Agere/ABDL items (pacifiers, plushies, bottles, etc.) for immersive roleplay transitions.

---

## 🌍 Supported Languages

- English (`en_us`)
- Russian (`ru_ru`)
- French (`fr_fr`)
- German (`de_de`)
- Ukrainian (`uk_ua`)
- Spanish (`es_es`)

---

## ⚙️ Technical Information

LittleCraft works in both **Singleplayer** and **Multiplayer (Server)** environments.  
It introduces two NBT tags that store player-specific data (applied **only to players**):

| Tag | Type | Description |
|------|------|-------------|
| `IsLittle` | `boolean` | Indicates whether the player is in Little Mode. |
| `LittleAge` | `int` | Represents the player’s “Little Age” (currently unused, reserved for future updates). |

These tags are saved in the world’s player data and automatically synchronized between client and server.

---

## 🧩 Known Issues

- Player **nameplates** (nicknames) become distorted or misaligned when scaled via PoseStack.  
  *(Pull requests to improve this are warmly welcome!)*

---

## 🧱 Version Information

- **Current Minecraft Version:** 1.21.1  
- **Next Planned Update:** 1.21.10  
- **No backports** to older Minecraft versions are planned.

---

## 🚧 Planned Features

- **LittleAge-based scaling:**  
  Player size will gradually scale depending on their LittleAge value, ranging from **baby zombie size** to **full adult Steve height**.
- **Agere/ABDL items:**  
  Pacifiers, bottles, plushies, and comfort items that toggle Little Mode or apply emotional effects.
- **Caregiver mechanics:**  
  Interaction features between adult and little players.
- **Improved rendering:**  
  Proper nameplate alignment and visual polish for scaled models.

---

## 👤 Author

**David von Eichendorf** (`dev1lroot` / `Davy`)  
License: **MIT**  
Made with love and care for the Agere & ABDL Minecraft community.
