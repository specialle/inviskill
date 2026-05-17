package io.inviskills.listeners;

import io.inviskills.InvisKillsPlugin;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;

/**
 * Scrambles the sender's display name in chat when they are invisible.
 *
 * <h3>How it works</h3>
 * <p>Paper's {@link AsyncChatEvent} exposes a {@code renderer} — a function
 * {@code (source, sourceDisplayName, message, viewer) -> Component} that
 * builds the full chat line sent to each viewer.  This listener wraps the
 * existing renderer and swaps {@code sourceDisplayName} with a
 * §k-obfuscated copy of the real username before passing it through.</p>
 *
 * <p>Because the renderer is called once per viewer, every player in chat
 * sees the same scrambled name.  The underlying message content is untouched.</p>
 *
 * <h3>Folia / async note</h3>
 * <p>{@link AsyncChatEvent} fires off the main thread.  The renderer
 * replacement is a pure data swap (no world mutation) and is safe to perform
 * here.  {@link Player#hasPotionEffect} is thread-safe for reads.</p>
 */
public final class ChatListener implements Listener {

    @SuppressWarnings("unused")
    private final InvisKillsPlugin plugin;

    public ChatListener(InvisKillsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Player sender = event.getPlayer();
        if (!sender.hasPotionEffect(PotionEffectType.INVISIBILITY)) return;

        // Build the obfuscated name once; reused for every viewer via the renderer
        Component obfuscatedName = Component.text(sender.getName())
                .color(NamedTextColor.GRAY)
                .decorate(TextDecoration.OBFUSCATED);

        // Wrap the existing renderer — preserve its formatting but replace the name
        var originalRenderer = event.renderer();
        event.renderer((source, sourceDisplayName, message, viewer) ->
                originalRenderer.render(source, obfuscatedName, message, viewer));
    }
}
