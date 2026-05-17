package io.inviskills.listeners;

import io.inviskills.InvisKillsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.potion.PotionEffectType;

/**
 * Scrambles advancement (achievement) broadcast messages for invisible players.
 *
 * <p>Vanilla sends messages like
 * <em>"[Steve has made the advancement [Stone Age]]"</em>.
 * When the earner is invisible the entire broadcast is replaced with a
 * {@link TextDecoration#OBFUSCATED} (§k) string of equal visual length,
 * wrapped in the same gold bracket styling Minecraft uses for advancements.
 * Observers see a flurry of enchantment-table glyphs with no useful
 * information about who earned what.</p>
 *
 * <p>Advancements that have {@code announceToChat: false} in their JSON
 * already produce a {@code null} message, so
 * {@link PlayerAdvancementDoneEvent#message()} returns {@code null} and we
 * skip them naturally.</p>
 */
public final class AdvancementListener implements Listener {

    @SuppressWarnings("unused")
    private final InvisKillsPlugin plugin;

    public AdvancementListener(InvisKillsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        Player player = event.getPlayer();

        // Only scramble if the advancing player is invisible
        if (!player.hasPotionEffect(PotionEffectType.INVISIBILITY)) return;

        Component original = event.message();
        if (original == null) return;   // silenced advancement – nothing to do

        /*
         * Serialise the entire component to plain text so we know how many
         * characters to fill with obfuscated glyphs (keeps the message at a
         * plausible length without leaking any real content).
         */
        String plainText = PlainTextComponentSerializer.plainText().serialize(original);

        /*
         * Reconstruct the message using the same gold bracket framing that
         * vanilla uses, but with the inner text fully §k-scrambled.
         *
         *   §6[§k<same-length gibberish>§r§6]
         *
         * The obfuscated run uses DARK_AQUA to mimic the enchantment-table
         * glyph colour; the brackets remain GOLD so it's recognisable as
         * *some* advancement without revealing whose or which.
         */
        Component scrambled = Component.empty()
                // Opening bracket
                .append(Component.text("[").color(NamedTextColor.GOLD))
                // Gibberish body — same character count as the original plain text
                .append(Component.text(plainText)
                        .color(NamedTextColor.DARK_AQUA)
                        .decorate(TextDecoration.OBFUSCATED))
                // Closing bracket
                .append(Component.text("]").color(NamedTextColor.GOLD));

        event.message(scrambled);
    }
}
