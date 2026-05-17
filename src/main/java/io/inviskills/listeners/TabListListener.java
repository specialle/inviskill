package io.inviskills.listeners;

import io.inviskills.InvisKillsPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent.Action;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffectType;

/**
 * Obfuscates the tab-list name of invisible players without touching their entity.
 *
 * <h3>Approach</h3>
 * <p>Uses {@link Player#setPlayerListName(String)} with legacy §k format codes rather
 * than the Adventure {@code playerListName(Component)} setter — the legacy path writes
 * directly to the underlying tab-list packet and is reliably applied on all Paper/Folia
 * builds.  The player's entity is untouched: they remain fully collidable and hittable.</p>
 *
 * <h3>State tracking</h3>
 * <p>Intent is read directly from {@link EntityPotionEffectEvent#getAction()} rather
 * than re-checking {@code hasPotionEffect()} at event time.  The event fires
 * <em>before</em> the effect is actually applied or removed, so a live {@code hasPotionEffect}
 * call at that moment can return a stale value and cause transitions to be skipped.</p>
 */
public final class TabListListener implements Listener {

    /** Legacy format: gray (§7) + obfuscated (§k).  Applied to the real username. */
    private static final String OBFUSCATED_PREFIX = "\u00a77\u00a7k";

    private final InvisKillsPlugin plugin;

    public TabListListener(InvisKillsPlugin plugin) {
        this.plugin = plugin;
    }

    // ─── Potion effect gain / loss ───────────────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPotionEffectChange(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player target)) return;

        PotionEffectType effectType = resolveType(event);
        if (effectType == null || !effectType.equals(PotionEffectType.INVISIBILITY)) return;

        // Read intent from the action — DO NOT use hasPotionEffect() here.
        // The event fires before the effect state changes, so that call is stale.
        boolean willBeInvisible = switch (event.getAction()) {
            case ADDED, CHANGED -> true;
            case REMOVED, CLEARED -> false;
        };

        if (willBeInvisible) {
            target.getScheduler().run(plugin, $ -> applyObfuscatedName(target), null);
        } else {
            // null resets to the real username (server default)
            target.getScheduler().run(plugin, $ -> target.setPlayerListName(null), null);
        }
    }

    // ─── Player join sync ────────────────────────────────────────────────────

    /**
     * If a player connects while already invisible (e.g. a persisted potion),
     * scramble their tab-list name before any other player can see it.
     * Deferred one tick so the player is fully registered first.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player joining = event.getPlayer();
        joining.getScheduler().run(plugin, $ -> {
            if (joining.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                applyObfuscatedName(joining);
            }
        }, null);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    /**
     * Writes a §7§k-prefixed copy of the player's real username into the
     * tab-list name slot.  Same character count as the real name, so the
     * column width stays consistent.
     */
    private static void applyObfuscatedName(Player player) {
        player.setPlayerListName(OBFUSCATED_PREFIX + player.getName());
    }

    /**
     * Returns the {@link PotionEffectType} involved in the transition.
     * <ul>
     *   <li>ADDED / CHANGED → read from {@code getNewEffect()}.</li>
     *   <li>REMOVED / CLEARED → read from {@code getOldEffect()}.</li>
     * </ul>
     */
    private static PotionEffectType resolveType(EntityPotionEffectEvent event) {
        if (event.getNewEffect() != null) return event.getNewEffect().getType();
        if (event.getOldEffect() != null) return event.getOldEffect().getType();
        return null;
    }
}
