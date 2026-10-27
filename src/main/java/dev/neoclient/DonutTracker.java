package dev.neoclient;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.text.Text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Tracks money amounts explicitly reported in chat; it does not infer balances. */
public final class DonutTracker {
    private record Entry(long epochMs, long cents) {}
    private static final Path FILE = net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir().resolve("neoclient-transactions.csv");
    private static final List<Entry> ENTRIES = new ArrayList<>();
    private static final long SESSION_START = System.currentTimeMillis();

    private DonutTracker() {}

    public static void init() {
        load();
        ClientReceiveMessageEvents.GAME.register((Text message, boolean overlay) -> capture(message.getString()));
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
        if (c.donutRegex == null || c.donutRegex.isBlank()) return;
        try {
            Matcher m = Pattern.compile(c.donutRegex, Pattern.CASE_INSENSITIVE).matcher(line);
            if (!m.find() || m.groupCount() < 1) return;
            String amount = m.group(1).replace(",", "").replace("$", "").trim();
            long cents = Math.round(Double.parseDouble(amount) * 100.0);
            if (cents <= 0 || cents > 100_000_000_000L) return;
            long now = System.currentTimeMillis();
            ENTRIES.add(new Entry(now, cents));
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, now + "," + cents + System.lineSeparator(),
                    java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } catch (Exception e) {
            // Invalid user regex or amount is ignored; never disrupt chat rendering.
            NeoClient.LOG.debug("Ignoring unmatched donut transaction", e);
        }
    }

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
