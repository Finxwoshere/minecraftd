package dev.neoclient;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class SettingsScreen extends Screen {
    private static int page = 0; // 0 = Visuals, 1 = Camera, 2 = Spotify, 3 = Waypoints
    private static final String[] CORNERS = {"Top-left", "Top-right", "Bottom-left", "Bottom-right"};
    private static final float[] SENS = {0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f};
    private static final float[] SPEEDS = {0.1f, 0.25f, 0.5f, 1.0f, 1.5f, 2.0f, 3.0f, 5.0f};

    public SettingsScreen() {
        super(Text.literal("NeoClient"));
    }

    @Override
    protected void init() {
        Config c = Config.I;

        addDrawableChild(ButtonWidget.builder(Text.literal("Visuals"), b -> { page = 0; clearAndInit(); })
                .dimensions(width / 2 - 164, 22, 80, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Camera"), b -> { page = 1; clearAndInit(); })
                .dimensions(width / 2 - 82, 22, 80, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Spotify"), b -> { page = 2; clearAndInit(); })
                .dimensions(width / 2, 22, 80, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Waypoints"), b -> { page = 3; clearAndInit(); })
                .dimensions(width / 2 + 82, 22, 80, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
                .dimensions(width - 62, 4, 58, 20).build());

        if (page == 0) {
            toggle(0, "Info HUD", () -> c.infoHud, v -> c.infoHud = v);
            toggle(1, "Show FPS", () -> c.showFps, v -> c.showFps = v);
            toggle(2, "Show coords", () -> c.showCoords, v -> c.showCoords = v);
            toggle(3, "Show facing", () -> c.showFacing, v -> c.showFacing = v);
            toggle(4, "Chroma text", () -> c.chroma, v -> c.chroma = v);
            toggle(5, "Zoom (hold C)", () -> c.zoomEnabled, v -> c.zoomEnabled = v);
            cycleInt(6, "Zoom FOV", new int[]{30, 40, 50, 60}, () -> c.zoomFov, v -> c.zoomFov = v);
            toggle(7, "Fullbright", () -> c.fullbright, v -> c.fullbright = v);
            toggle(8, "Clear weather", () -> c.clearWeather, v -> c.clearWeather = v);
            toggle(9, "Cinematic bars", () -> c.cinematic, v -> c.cinematic = v);
            cycleNamed(10, "Screen tint", Hud.TINT_NAMES, () -> c.tint, v -> c.tint = v);
            toggle(11, "Donut earnings HUD", () -> c.donutHud, v -> c.donutHud = v);
        } else if (page == 1) {
            toggle(0, "Freecam", () -> c.freecamEnabled, v -> c.freecamEnabled = v);
            toggle(1, "Freelook", () -> c.freelookEnabled, v -> c.freelookEnabled = v);
            cycleFloat(2, "Freecam sens", SENS, () -> c.freecamSens, v -> c.freecamSens = v);
            cycleFloat(3, "Freelook sens", SENS, () -> c.freelookSens, v -> c.freelookSens = v);
            cycleFloat(4, "Freecam speed", SPEEDS, () -> c.freecamSpeed, v -> c.freecamSpeed = v);
            toggle(5, "Scroll = speed", () -> c.freecamScroll, v -> c.freecamScroll = v);
            cycleInt(6, "Sprint boost x", new int[]{1, 2, 3, 4, 6, 8}, () -> c.freecamBoost, v -> c.freecamBoost = v);
            cycleNamed(7, "Freelook key", new String[]{"Hold", "Toggle"}, () -> c.freelookToggle ? 1 : 0, v -> c.freelookToggle = v == 1);
            cycleNamed(8, "Freelook view", new String[]{"Behind", "In front"}, () -> c.freelookFront ? 1 : 0, v -> c.freelookFront = v == 1);
            toggle(9, "Freecam invert Y", () -> c.freecamInvertY, v -> c.freecamInvertY = v);
            toggle(10, "Freelook invert Y", () -> c.freelookInvertY, v -> c.freelookInvertY = v);
        } else if (page == 2) {
            toggle(0, "Spotify HUD", () -> c.spotifyEnabled, v -> c.spotifyEnabled = v);
            toggle(1, "Album cover", () -> c.showCover, v -> c.showCover = v);
            toggle(2, "Song title", () -> c.showTitle, v -> c.showTitle = v);
            toggle(3, "Artist name", () -> c.showArtist, v -> c.showArtist = v);
            toggle(4, "Album name", () -> c.showAlbum, v -> c.showAlbum = v);
            toggle(5, "Progress bar", () -> c.showProgress, v -> c.showProgress = v);
            toggle(6, "Lyrics", () -> c.showLyrics, v -> c.showLyrics = v);
            cycleInt(7, "Lyric lines", new int[]{1, 3}, () -> c.lyricLines, v -> c.lyricLines = v);
            cycleInt(8, "Cover size", new int[]{32, 48, 64}, () -> c.coverSize, v -> c.coverSize = v);
            cycleNamed(9, "Position", CORNERS, () -> c.corner, v -> c.corner = v);
            cycleInt(10, "Background", new int[]{0, 80, 140, 200}, () -> c.bgOpacity, v -> c.bgOpacity = v);
            cycleInt(11, "Lyric offset ms", new int[]{-1000, -750, -500, -250, 0, 250, 500, 750, 1000},
                    () -> c.lyricOffsetMs, v -> c.lyricOffsetMs = v);

            toggle(12, "Floating lyrics", () -> c.floatingLyrics, v -> c.floatingLyrics = v);
            int fy = 50 + 7 * 22;
            TextFieldWidget f = new TextFieldWidget(textRenderer, width / 2 - 152, fy, 304, 20, Text.literal("Client ID"));
            f.setMaxLength(64);
            f.setPlaceholder(Text.literal("Paste your Spotify Client ID"));
            f.setText(c.spotifyClientId);
            f.setChangedListener(s -> c.spotifyClientId = s.trim());
            addDrawableChild(f);

            boolean connected = !c.refreshToken.isEmpty();
            addDrawableChild(ButtonWidget.builder(Text.literal(connected ? "Disconnect Spotify" : "Connect Spotify"), b -> {
                if (connected) SpotifyClient.disconnect(); else SpotifyClient.connect();
                Config.save();
                clearAndInit();
            }).dimensions(width / 2 - 152, fy + 24, 304, 20).build());
        } else if (page == 3) {
            TextFieldWidget name = new TextFieldWidget(textRenderer, width / 2 - 152, 58, 304, 20, Text.literal("Name"));
            name.setPlaceholder(Text.literal("Waypoint name")); name.setMaxLength(48); addDrawableChild(name);
            TextFieldWidget x = new TextFieldWidget(textRenderer, width / 2 - 152, 84, 96, 20, Text.literal("X")); x.setPlaceholder(Text.literal("X")); addDrawableChild(x);
            TextFieldWidget y = new TextFieldWidget(textRenderer, width / 2 - 50, 84, 96, 20, Text.literal("Y")); y.setPlaceholder(Text.literal("Y")); addDrawableChild(y);
            TextFieldWidget z = new TextFieldWidget(textRenderer, width / 2 + 52, 84, 100, 20, Text.literal("Z")); z.setPlaceholder(Text.literal("Z")); addDrawableChild(z);
            addDrawableChild(ButtonWidget.builder(Text.literal("Add waypoint"), b -> {
                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc.player == null || mc.world == null) return;
                try { WaypointManager.add(name.getText(), Double.parseDouble(x.getText()), Double.parseDouble(y.getText()), Double.parseDouble(z.getText()), mc.world.getRegistryKey().getValue().toString()); }
                catch (NumberFormatException ex) { return; }
                clearAndInit();
            }).dimensions(width / 2 - 152, 110, 304, 20).build());
            java.util.List<WaypointManager.Waypoint> points = WaypointManager.all();
            for (int i = 0; i < Math.min(4, points.size()); i++) {
                final int index = i; WaypointManager.Waypoint w = points.get(i);
                addDrawableChild(ButtonWidget.builder(Text.literal("Remove  " + w.name()), b -> { WaypointManager.remove(index); clearAndInit(); })
                        .dimensions(width / 2 - 152, 138 + i * 23, 304, 20).build());
            }
        }
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        int panelW = Math.min(340, width - 24), panelH = Math.min(286, height - 20);
        int px = (width - panelW) / 2, py = (height - panelH) / 2;
        ctx.fill(px, py, px + panelW, py + panelH, 0xE8191D24);
        ctx.fill(px, py, px + panelW, py + 1, 0xFF438BCA);
        ctx.fill(px, py + panelH - 1, px + panelW, py + panelH, 0xFF343A44);
        super.render(ctx, mx, my, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, 8, 0xFFFFFFFF);
        if (page == 1) {
            int y = 50 + 6 * 22;
            ctx.drawCenteredTextWithShadow(textRenderer, "Keybinds (Freecam F6, Freelook Left Alt, Menu Right Shift):",
                    width / 2, y + 4, 0xFFB3B3B3);
            ctx.drawCenteredTextWithShadow(textRenderer, "Options > Controls > Key Binds > NeoClient",
                    width / 2, y + 16, 0xFFB3B3B3);
            ctx.drawCenteredTextWithShadow(textRenderer, "Freecam: WASD + Space/Shift to fly, Ctrl (sprint) = boost, scroll = speed",
                    width / 2, y + 32, 0xFF808080);
        } else if (page == 2) {
            int y = 50 + 7 * 22 + 48;
            ctx.drawCenteredTextWithShadow(textRenderer, "Status: " + SpotifyClient.status, width / 2, y, 0xFF1DB954);
            ctx.drawCenteredTextWithShadow(textRenderer, "Redirect URI for your Spotify app: " + SpotifyClient.REDIRECT,
                    width / 2, y + 12, 0xFF909090);
        }
    }

    @Override
    public void removed() {
        Config.save();
    }

    // ---- widget helpers ----
    private int gx(int i) { return width / 2 - 152 + (i % 2) * 154; }
    private int gy(int i) { return 50 + (i / 2) * 22; }

    private void toggle(int i, String name, BooleanSupplier get, Consumer<Boolean> set) {
        addDrawableChild(ButtonWidget.builder(onOff(name, get.getAsBoolean()), b -> {
            set.accept(!get.getAsBoolean());
            b.setMessage(onOff(name, get.getAsBoolean()));
        }).dimensions(gx(i), gy(i), 150, 20).build());
    }

    private void cycleInt(int i, String name, int[] vals, IntSupplier get, IntConsumer set) {
        addDrawableChild(ButtonWidget.builder(Text.literal(name + ": " + get.getAsInt()), b -> {
            int cur = 0;
            for (int k = 0; k < vals.length; k++) if (vals[k] == get.getAsInt()) cur = k;
            set.accept(vals[(cur + 1) % vals.length]);
            b.setMessage(Text.literal(name + ": " + get.getAsInt()));
        }).dimensions(gx(i), gy(i), 150, 20).build());
    }

    private interface FloatGet { float get(); }
    private interface FloatSet { void set(float v); }

    private static String f2(float v) { return String.format(Locale.ROOT, "%.2f", v); }

    private void cycleFloat(int i, String name, float[] vals, FloatGet get, FloatSet set) {
        addDrawableChild(ButtonWidget.builder(Text.literal(name + ": " + f2(get.get())), b -> {
            int cur = 0;
            for (int k = 0; k < vals.length; k++) {
                if (Math.abs(vals[k] - get.get()) < Math.abs(vals[cur] - get.get())) cur = k;
            }
            set.set(vals[(cur + 1) % vals.length]);
            b.setMessage(Text.literal(name + ": " + f2(get.get())));
        }).dimensions(gx(i), gy(i), 150, 20).build());
    }

    private void cycleNamed(int i, String name, String[] labels, IntSupplier get, IntConsumer set) {
        addDrawableChild(ButtonWidget.builder(Text.literal(name + ": " + labels[get.getAsInt()]), b -> {
            set.accept((get.getAsInt() + 1) % labels.length);
            b.setMessage(Text.literal(name + ": " + labels[get.getAsInt()]));
        }).dimensions(gx(i), gy(i), 150, 20).build());
    }

    private static Text onOff(String name, boolean v) {
        return Text.literal(name + ": " + (v ? "ON" : "OFF"));
    }
}
