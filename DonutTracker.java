package dev.neoclient;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.text.Text;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Tracks explicit economy values reported by the server in chat. */
public final class DonutTracker {
    private record Entry(long epochMs, long cents) {}
    private static final Path FILE = net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir().resolve("neoclient-transactions.csv");
    private static final List<Entry> ENTRIES = new ArrayList<>();
    private static final long SESSION_START = System.currentTimeMillis();
    private static long currentBalanceCents = -1, baselineCents = -1;
    private DonutTracker() {}

    public static void init() {
        load();
        ClientReceiveMessageEvents.GAME.register((Text message, boolean overlay) -> capture(message.getString()));
    }

    public static synchronized void setEnabled(boolean enabled) {
        Config.I.donutHud = enabled;
        if (enabled) baselineCents = currentBalanceCents;
        Config.save();
    }

    private static synchronized void load() {
        try {
            if (!Files.exists(FILE)) return;
            for (String line : Files.readAllLines(FILE)) {
                String[] p = line.split(",", 2);
                if (p.length == 2) ENTRIES.add(new Entry(Long.parseLong(p[0]), Long.parseLong(p[1])));
            }
        } catch (Exception e) { NeoClient.LOG.warn("Could not load donut transactions", e); }
    }

    private static synchronized void capture(String line) {
        Config c = Config.I;
        if (!c.donutHud) return;
        try {
            if (c.balanceRegex != null && !c.balanceRegex.isBlank()) {
                Matcher balance = Pattern.compile(c.balanceRegex).matcher(line);
                if (balance.find() && balance.groupCount() >= 1) {
                    long value = parseCents(balance.group(1));
                    if (value >= 0) {
                        currentBalanceCents = value;
                        if (baselineCents < 0) baselineCents = value;
                        return;
                    }
                }
            }
            if (c.donutRegex == null || c.donutRegex.isBlank()) return;
            Matcher payment = Pattern.compile(c.donutRegex, Pattern.CASE_INSENSITIVE).matcher(line);
            if (!payment.find() || payment.groupCount() < 1) return;
            long cents = parseCents(payment.group(1));
            if (cents <= 0 || cents > 100_000_000_000L) return;
            long now = System.currentTimeMillis();
            ENTRIES.add(new Entry(now, cents));
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, now + "," + cents + System.lineSeparator(), java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } catch (Exception e) { NeoClient.LOG.debug("Ignoring unmatched balance/transaction chat line", e); }
    }

    private static long parseCents(String raw) {
        String amount = raw.replace(",", "").replace("$", "").trim();
        return Math.round(Double.parseDouble(amount) * 100.0);
    }

    public static synchronized String balanceChangeLabel() {
        if (baselineCents < 0 || currentBalanceCents < 0) return "Balance Δ  waiting for balance chat";
        long delta = currentBalanceCents - baselineCents;
        return "Balance Δ  " + (delta >= 0 ? "+" : "-") + money(Math.abs(delta));
    }
    private static String money(long cents) { return String.format(Locale.ROOT, "$%,d.%02d", cents / 100, Math.abs(cents % 100)); }
    public static synchronized long sessionCents() { return sum(e -> e.epochMs >= SESSION_START); }
    public static synchronized long todayCents() {
        long midnight = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return sum(e -> e.epochMs >= midnight);
    }
    public static synchronized long weekCents() {
        long since = Instant.now().minusSeconds(7 * 24 * 60 * 60L).toEpochMilli();
        return sum(e -> e.epochMs >= since);
    }
    private interface Filter { boolean accept(Entry e); }
    private static long sum(Filter f) { return ENTRIES.stream().filter(f::accept).mapToLong(e -> e.cents).sum(); }
}
