package io.inviskills;

import io.inviskills.listeners.AdvancementListener;
import io.inviskills.listeners.ChatListener;
import io.inviskills.listeners.DeathListener;
import io.inviskills.listeners.TabListListener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * InvisKills-Folia — Invisible Player Management
 *
 * <ul>
 *   <li>Invisible players are hidden from the tab list for all other players.</li>
 *   <li>Death messages replace the invisible player's name with §k gibberish.</li>
 *   <li>Advancement broadcasts for invisible players are fully scrambled.</li>
 * </ul>
 *
 * Folia-safe: all deferred work uses the Global Region Scheduler so no
 * deprecated BukkitScheduler calls are made.
 */
public final class InvisKillsPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        var pm = getServer().getPluginManager();
        pm.registerEvents(new TabListListener(this), this);
        pm.registerEvents(new ChatListener(this),     this);
        pm.registerEvents(new DeathListener(this),    this);
        pm.registerEvents(new AdvancementListener(this), this);

        getLogger().info("InvisKills-Folia enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("InvisKills-Folia disabled.");
    }
}
