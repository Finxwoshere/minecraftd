package dev.neoclient;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class Hud {
    public static final int[] TINTS = {0, 0x22FF8800, 0x2200AAFF, 0x22FF2D95, 0x2200FF66, 0x229933FF};
    public static final String[] TINT_NAMES = {"Off", "Warm", "Cool", "Rose", "Mint", "Violet"};
    private static final int GREEN = 0xFF1DB954;
    private static final int W = 230, PAD = 5, LH = 11;

    private Hud() {}

    public static void init() {
        HudRenderCallback.EVENT.register(Hud::render);
    }

    private static void render(DrawContext g, RenderTickCounter tick) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options.hudHidden) return;
        Config c = Config.I;
        int sw = g.getScaledWindowWidth(), sh = g.getScaledWindowHeight();

        if (c.tint > 0) g.fill(0, 0, sw, sh, TINTS[c.tint]);
        if (c.cinematic) {
            int bar = sh / 9;
            g.fill(0, 0, sw, bar, 0xFF000000);
            g.fill(0, sh - bar, sw, sh, 0xFF000000);
        }
        if (CameraControl.mode() != CameraControl.Mode.NONE) {
            String label = CameraControl.mode() == CameraControl.Mode.FREECAM
                    ? String.format(Locale.ROOT, "FREECAM  speed %.2f", c.freecamSpeed) : "FREELOOK";
            int ly = c.cinematic ? sh / 9 + 4 : 4;
            g.drawCenteredTextWithShadow(mc.textRenderer, label, sw / 2, ly, 0xFFFFFFFF);
        }
        if (c.infoHud) info(g, mc);
        if (c.donutHud) donut(g, mc, sw, sh);
        waypoint(g, mc);
        if (c.spotifyEnabled) spotify(g, mc, sw, sh);
        if (c.spotifyEnabled && c.showLyrics && c.floatingLyrics) floatingLyrics(g, mc, sw, sh);
    }


    /** Karaoke-style lyric line floating above the hotbar. */
    private static void floatingLyrics(DrawContext g, MinecraftClient mc, int sw, int sh) {
        if (!SpotifyClient.hasTrack || SpotifyClient.lyrics.isEmpty()) return;
        Config c = Config.I;
        long t = elapsed() + c.lyricOffsetMs;
        List<SpotifyClient.Line> lines = SpotifyClient.lyrics;
        int idx = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).timeMs() <= t) idx = i; else break;
        }
        if (idx < 0) return;
        String current = lines.get(idx).text();
        int alpha = 0xFF;
        if (idx + 1 < lines.size()) {
            long remaining = lines.get(idx + 1).timeMs() - t;
            if (remaining < 350) alpha = (int) (0xFF * Math.max(0.35, remaining / 350.0));
        }
        int y = c.floatingLyricY > 0 ? c.floatingLyricY : sh - 54;
        String shown = mc.textRenderer.trimToWidth(current, Math.max(80, sw - 32));
        int width = mc.textRenderer.getWidth(shown);
        g.fill(sw / 2 - width / 2 - 10, y - 4, sw / 2 + width / 2 + 10, y + 13, 0x78000000);
        g.drawCenteredTextWithShadow(mc.textRenderer, shown, sw / 2, y, (alpha << 24) | 0xFFFFFF);
    }

    private static void donut(DrawContext g, MinecraftClient mc, int sw, int sh) {
        TextRenderer tr = mc.textRenderer;
        Config c = Config.I;
        int x = c.hudX;
        int y = c.hudY + (c.infoHud ? 36 : 0);
        String[] lines = {"Donut earnings", "Session  " + money(DonutTracker.sessionCents()),
                "Today     " + money(DonutTracker.todayCents()), "7 days   " + money(DonutTracker.weekCents())};
        int w = 0;
        for (String line : lines) w = Math.max(w, tr.getWidth(line));
        g.fill(x - 3, y - 3, x + w + 5, y + lines.length * 10 + 2, 0xA8191D24);
        g.fill(x - 3, y - 3, x - 1, y + lines.length * 10 + 2, 0xFF438BCA);
        for (int i = 0; i < lines.length; i++) g.drawTextWithShadow(tr, lines[i], x, y + i * 10, i == 0 ? 0xFF76C7FF : 0xFFE8E8E8);
    }

    private static String money(long cents) {
        return String.format(Locale.ROOT, "$%,d.%02d", cents / 100, Math.abs(cents % 100));
    }

    private static void waypoint(DrawContext g, MinecraftClient mc) {
        String label = WaypointManager.nearestLabel(mc);
        if (label.isEmpty()) return;
        int y = Config.I.hudY + (Config.I.infoHud ? 36 : 0) + (Config.I.donutHud ? 48 : 0);
        g.drawTextWithShadow(mc.textRenderer, "Waypoint  " + label, Config.I.hudX, y, 0xFFFFD36A);
    }
    // ---------------------------------------------------------------- info HUD
    private static void info(DrawContext g, MinecraftClient mc) {
        Config c = Config.I;
        TextRenderer tr = mc.textRenderer;
        List<String> l = new ArrayList<>();
        if (c.showFps) l.add("FPS " + mc.getCurrentFps());
        if (c.showCoords) l.add(String.format(Locale.ROOT, "XYZ %.1f %.1f %.1f",
                mc.player.getX(), mc.player.getY(), mc.player.getZ()));
        if (c.showFacing) l.add("Facing " + mc.player.getHorizontalFacing().asString());
        if (l.isEmpty()) return;

        int w = 0;
        for (String s : l) w = Math.max(w, tr.getWidth(s));
        int x = Config.I.hudX, y = Config.I.hudY;
        g.fill(x - 2, y - 2, x + w + 2, y + l.size() * 10 + 1, 0x90000000);
        for (int i = 0; i < l.size(); i++) g.drawTextWithShadow(tr, l.get(i), x, y + i * 10, color(i));
    }

    private static int color(int i) {
        if (!Config.I.chroma) return 0xFFFFFFFF;
        float h = ((System.currentTimeMillis() % 4000) / 4000f) + i * 0.08f;
        return Color.HSBtoRGB(h, 0.6f, 1f) | 0xFF000000;
    }

    // ---------------------------------------------------------------- Spotify HUD
    private static long elapsed() {
        long e = SpotifyClient.progressMs + (SpotifyClient.playing ? System.currentTimeMillis() - SpotifyClient.fetchedAt : 0);
        long d = SpotifyClient.durationMs;
        return d > 0 ? Math.min(e, d) : e;
    }

    private static String fmt(long ms) {
        long s = Math.max(0, ms / 1000);
        return (s / 60) + ":" + String.format("%02d", s % 60);
    }

    private static void spotify(DrawContext g, MinecraftClient mc, int sw, int sh) {
        Config c = Config.I;
        TextRenderer tr = mc.textRenderer;

        if (!SpotifyClient.hasTrack) {
            if (c.refreshToken.isEmpty()) {
                String s = "Spotify: not connected (Right Shift > Spotify)";
                int hx = (c.corner % 2 == 0) ? 6 : sw - 6 - tr.getWidth(s);
                int hy = c.corner < 2 ? 6 : sh - 16;
                g.drawTextWithShadow(tr, s, hx, hy, 0xFFB3B3B3);
            }
            return;
        }

        int cover = (c.showCover && SpotifyClient.coverReady) ? c.coverSize : 0;
        int textX0 = PAD + (cover > 0 ? cover + PAD : 0);
        int textW = W - textX0 - PAD;
        int rows = (c.showTitle ? 1 : 0) + (c.showArtist ? 1 : 0) + (c.showAlbum ? 1 : 0) + (c.showProgress ? 1 : 0);
        int panelH = PAD * 2 + Math.max(cover, rows * LH);

        int lyRows = c.showLyrics ? c.lyricLines : 0;
        int lyH = lyRows > 0 ? lyRows * 12 + 8 : 0;
        int totalH = panelH + (lyH > 0 ? 3 + lyH : 0);

        int x = (c.corner % 2 == 0) ? 6 : sw - 6 - W;
        int y = c.corner < 2 ? 6 : sh - 6 - totalH;
        int bg = (c.bgOpacity & 0xFF) << 24;

        g.fill(x, y, x + W, y + panelH, bg);
        g.fill(x, y, x + 2, y + panelH, GREEN);

        if (cover > 0) {
            g.drawTexture(RenderPipelines.GUI_TEXTURED, SpotifyClient.COVER_ID,
                    x + PAD, y + PAD, 0f, 0f, cover, cover, 128, 128, 128, 128);
        }

        int tx = x + textX0;
        int ty = y + PAD + Math.max(0, (cover - rows * LH) / 2);
        if (c.showTitle) {
            g.drawTextWithShadow(tr, Text.literal(tr.trimToWidth(SpotifyClient.title, textW - 8)).formatted(Formatting.BOLD),
                    tx, ty, 0xFFFFFFFF);
            ty += LH;
        }
        if (c.showArtist) {
            g.drawTextWithShadow(tr, tr.trimToWidth(SpotifyClient.artist, textW), tx, ty, 0xFFB3B3B3);
            ty += LH;
        }
        if (c.showAlbum) {
            g.drawTextWithShadow(tr, tr.trimToWidth(SpotifyClient.album, textW), tx, ty, 0xFF808080);
            ty += LH;
        }
        if (c.showProgress) {
            long el = elapsed(), d = SpotifyClient.durationMs;
            String a = fmt(el), b = fmt(d);
            int aw = tr.getWidth(a), bw = tr.getWidth(b);
            g.drawTextWithShadow(tr, a, tx, ty, 0xFFB3B3B3);
            g.drawTextWithShadow(tr, b, tx + textW - bw, ty, 0xFFB3B3B3);
            int bx1 = tx + aw + 4, bx2 = tx + textW - bw - 4, by = ty + 3;
            if (bx2 > bx1) {
                g.fill(bx1, by, bx2, by + 3, 0x80FFFFFF);
                if (d > 0) g.fill(bx1, by, bx1 + (int) ((bx2 - bx1) * Math.min(1.0, (double) el / d)), by + 3, GREEN);
            }
        }

        if (lyH > 0) {
            int ly0 = y + panelH + 3;
            int cx = x + W / 2;
            g.fill(x, ly0, x + W, ly0 + lyH, bg);
            List<SpotifyClient.Line> ly = SpotifyClient.lyrics;
            if (ly.isEmpty()) {
                String s = SpotifyClient.lyricsLoading ? "Loading lyrics..." : "No synced lyrics found";
                g.drawCenteredTextWithShadow(tr, s, cx, ly0 + 4, 0xFF808080);
            } else {
                long t = elapsed() + c.lyricOffsetMs;
                int idx = -1;
                for (int i = 0; i < ly.size(); i++) {
                    if (ly.get(i).timeMs() <= t) idx = i; else break;
                }
                int start = lyRows == 3 ? idx - 1 : idx;
                for (int r = 0; r < lyRows; r++) {
                    int i = start + r;
                    boolean cur = lyRows == 1 || r == 1;
                    String s = (i < 0 || i >= ly.size()) ? "" : ly.get(i).text();
                    if (s.isEmpty() && cur) s = "\u266A";
                    g.drawCenteredTextWithShadow(tr, tr.trimToWidth(s, W - 12), cx, ly0 + 4 + r * 12,
                            cur ? 0xFFFFFFFF : 0xFF808080);
                }
            }
        }
    }
}
