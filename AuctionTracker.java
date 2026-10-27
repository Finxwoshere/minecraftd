package dev.neoclient;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.text.Text;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Local countdown and payment tally for an auction the player starts manually. */
public final class AuctionTracker {
    private AuctionTracker() {}
    public static void init() { ClientReceiveMessageEvents.GAME.register((Text message, boolean overlay) -> capture(message.getString())); }
    public static synchronized void start(String item, long startBidCents, int durationMinutes) {
        Config c = Config.I;
        c.auctionItem = item == null ? "" : item.trim();
        c.auctionTopBidCents = Math.max(0, startBidCents);
        c.auctionEndEpochMs = System.currentTimeMillis() + Math.max(1, Math.min(240, durationMinutes)) * 60_000L;
        c.auctionEnabled = true;
        Config.save();
    }
    public static synchronized void stop() { Config.I.auctionEnabled = false; Config.save(); }
    private static synchronized void capture(String line) {
        Config c = Config.I;
        if (!c.auctionEnabled || c.auctionEndEpochMs < System.currentTimeMillis() || c.auctionRegex == null || c.auctionRegex.isBlank()) return;
        try {
            Matcher m = Pattern.compile(c.auctionRegex).matcher(line);
            if (!m.find() || m.groupCount() < 1) return;
            long cents = Math.round(Double.parseDouble(m.group(1).replace(",", "").replace("$", "")) * 100.0);
            if (cents > 0 && cents < 100_000_000_000L) { c.auctionTopBidCents += cents; Config.save(); }
        } catch (Exception e) { NeoClient.LOG.debug("Ignoring unmatched auction payment chat line", e); }
    }
    public static String status() {
        Config c = Config.I;
        long remaining = Math.max(0, c.auctionEndEpochMs - System.currentTimeMillis()) / 1000;
        String clock = String.format(java.util.Locale.ROOT, "%02d:%02d", remaining / 60, remaining % 60);
        return c.auctionEnabled ? clock : "Stopped";
    }
    public static String money() { long cents = Config.I.auctionTopBidCents; return String.format(java.util.Locale.ROOT, "$%,d.%02d", cents / 100, Math.abs(cents % 100)); }
}
