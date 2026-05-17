# InvisKills-Folia — Invisible Player Management

A Folia-safe Paper/Folia plugin for **Minecraft 1.21.1** that enforces complete
anonymity for players under the Invisibility potion effect.

---

## Features

| Feature | Detail |
|---|---|
| **Tab-list hiding** | Invisible players are removed from the tab list for every other online player via `hidePlayer()` / `showPlayer()`. |
| **Death-message obfuscation** | The invisible player's username is replaced with `§k` (enchantment-table gibberish) in the server-wide death broadcast. |
| **Advancement scrambling** | Advancement broadcasts are replaced with a fully `§k`-scrambled string of equal length; the gold `[` / `]` brackets remain so it looks like *a* notification, but reveals nothing. |
| **`§k` magic text** | All obfuscated text uses Adventure's `TextDecoration.OBFUSCATED` — the canonical enchantment-table effect. |

---

## Build

### Requirements

| Tool | Version |
|---|---|
| Java | 21 |
| Gradle | 8.x (wrapper recommended) |
| Network | access to `repo.papermc.io` |

### Steps

```bash
# 1. Generate the Gradle wrapper (first time only)
gradle wrapper --gradle-version 8.10

# 2. Compile & package
./gradlew jar

# 3. Copy the output JAR to your server
cp build/libs/InvisKills-Folia-1.0.0.jar  /path/to/server/plugins/
```

---

## Installation

1. Drop `InvisKills-Folia-1.0.0.jar` into your server's `plugins/` folder.
2. Restart or `/reload confirm` the server.
3. No configuration file is needed — the plugin is fully automatic.

### Folia support

`plugin.yml` declares `folia-supported: true`.  All deferred work is routed
through `Server#getGlobalRegionScheduler()` — no deprecated
`BukkitScheduler` calls are made.

---

## How it works

### Tab list (`TabListListener`)

```
EntityPotionEffectEvent (ADDED/CHANGED)
  → GlobalRegionScheduler.run()
      → for every online player: viewer.hidePlayer(plugin, target)

EntityPotionEffectEvent (REMOVED/CLEARED)
  → GlobalRegionScheduler.run()
      → for every online player: viewer.showPlayer(plugin, target)

PlayerJoinEvent
  → GlobalRegionScheduler.run()
      → hide already-invisible players from newcomer
      → if newcomer is invisible: hide them from everyone
```

### Death messages (`DeathListener`)

```
PlayerDeathEvent (invisible player only)
  → Component.replaceText(matchLiteral(realName))
      → replacement: §k§fRealName§r  (same chars, obfuscated + white)
  → event.deathMessage(patched)
```

`replaceText` recurses into translatable component arguments, so it works
with Vanilla's translatable death messages out of the box.

### Advancements (`AdvancementListener`)

```
PlayerAdvancementDoneEvent (invisible player only, non-null message only)
  → PlainTextComponentSerializer.serialize(original)  →  plainText
  → scrambled = §6[§3<plainText obfuscated>§6]
  → event.message(scrambled)
```

---

## Project layout

```
InvisKills-Folia/
├── build.gradle
├── settings.gradle
└── src/main/
    ├── resources/
    │   └── plugin.yml
    └── java/io/inviskills/
        ├── InvisKillsPlugin.java          ← entry point
        └── listeners/
            ├── TabListListener.java            ← tab-list hide/show
            ├── DeathListener.java              ← §k death message names
            └── AdvancementListener.java        ← §k advancement scramble
```

---

## Compatibility

| Server software | Status |
|---|---|
| **Folia 1.21.1** | ✅ Primary target |
| Paper 1.21.1 | ✅ Full compatibility |
| Spigot 1.21.1 | ⚠️ Should work but `GlobalRegionScheduler` falls back to the global thread — not tested |
| Versions < 1.21 | ❌ `api-version: 1.21` declared in `plugin.yml` |
