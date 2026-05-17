package io.inviskills.listeners;

import io.inviskills.InvisKillsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffectType;

/**
 * Obfuscates the name of an invisible player in their own death message.
 *
 * <p>When an invisible player dies the broadcast death message has their
 * plain-text name replaced by a {@link TextDecoration#OBFUSCATED} run of the
 * same characters – the classic {@code §k} "enchantment table" effect.
 * Observers see something like <em>"§k§fSteve§r was slain by Zombie"</em>
 * rather than the real username.</p>
 *
 * <p>The replacement uses Adventure's {@link Component#replaceText} which
 * recursively visits all children including translatable component arguments,
 * so it works correctly with Vanilla's translatable death messages.</p>
 */
public final class DeathListener implements Listener {

    /** Colour applied to the obfuscated name so it blends with vanilla messages. */
    private static final TextColor GHOST_COLOUR = NamedTextColor.WHITE;

    @SuppressWarnings("unused")
    private final InvisKillsPlugin plugin;

    public DeathListener(InvisKillsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player deceased = event.getPlayer();

        // Only modify the message if the dead player was invisible
        if (!deceased.hasPotionEffect(PotionEffectType.INVISIBILITY)) return;

        Component original = event.deathMessage();
        if (original == null) return;

        String realName = deceased.getName();

        /*
         * Build the replacement component:
         *   - Same character content as the real name (preserves message length / rhythm)
         *   - OBFUSCATED decoration  →  §k   (enchantment-table scramble)
         *   - White colour so it sits naturally inside vanilla death message grey
         */
        Component obfuscatedName = Component.text(realName)
                .color(GHOST_COLOUR)
                .decorate(TextDecoration.OBFUSCATED);

        /*
         * replaceText walks the entire component tree (text + translatable args)
         * and replaces every literal run whose content equals `realName`.
         */
        Component patched = original.replaceText(cfg -> cfg
                .matchLiteral(realName)
                .replacement(obfuscatedName));

        event.deathMessage(patched);
    }
}
