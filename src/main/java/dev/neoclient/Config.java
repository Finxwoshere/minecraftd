package dev.neoclient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("neoclient.json");
    public static Config I = new Config();

    // ---- Visuals ----
    public boolean infoHud = true, showFps = true, showCoords = true, showFacing = true, chroma = true;
    public boolean zoomEnabled = true;
    public int zoomFov = 30;            // 30..110 (vanilla FOV slider range)
    public boolean fullbright = false;
    public boolean clearWeather = false;
    public boolean cinematic = false;
    public int tint = 0;                // index into Hud.TINTS

    // ---- Freecam ----
    public boolean freecamEnabled = true;
    public float freecamSpeed = 1.0f;       // blocks per tick (scroll wheel changes this)
    public float freecamSens = 1.0f;        // multiplier on top of vanilla mouse sensitivity
    public int freecamBoost = 3;            // speed multiplier while holding sprint
    public boolean freecamScroll = true;    // scroll wheel adjusts speed
    public boolean freecamInvertY = false;

    // ---- Freelook ----
    public boolean freelookEnabled = true;
    public float freelookSens = 1.0f;
    public boolean freelookToggle = false;  // false = hold key, true = press to toggle
    public boolean freelookFront = false;   // false = behind player, true = in front
    public boolean freelookInvertY = false;

    // ---- Spotify ----
    public boolean spotifyEnabled = true;
    public boolean showCover = true, showTitle = true, showArtist = true, showAlbum = false, showProgress = true;
    public boolean showLyrics = true;
    public int lyricLines = 1;          // 1 or 3
    public int lyricOffsetMs = 0;       // shift lyrics earlier/later to fix latency
    public boolean floatingLyrics = true;
    public int floatingLyricY = 0;
    public int hudX = 4, hudY = 4;
    public float hudScale = 1.0f;
    public boolean donutHud = true;
    // Capture group 1 must be the numeric amount from a matching server chat line.
    public String donutRegex = "(?:\\+|earned|received|sold).*?\\$([0-9][0-9,]*(?:\\.[0-9]{1,2})?)";
    public int coverSize = 48;
    public int corner = 1;              // 0 TL, 1 TR, 2 BL, 3 BR
    public int bgOpacity = 140;         // 0..255
    public String spotifyClientId = "";
    public String refreshToken = "";    // keep this file private!

    public static void load() {
        try {
            if (Files.exists(FILE)) {
                Config c = GSON.fromJson(Files.readString(FILE), Config.class);
                if (c != null) I = c;
            }
        } catch (Exception e) {
            NeoClient.LOG.warn("Could not read config, using defaults", e);
        }
        I.clamp();
        save();
    }

    public static void save() {
        try {
            Files.writeString(FILE, GSON.toJson(I));
        } catch (Exception e) {
            NeoClient.LOG.warn("Could not save config", e);
        }
    }

    private void clamp() {
        zoomFov = Math.max(30, Math.min(110, zoomFov));
        tint = Math.max(0, Math.min(Hud.TINTS.length - 1, tint));
        lyricLines = lyricLines == 3 ? 3 : 1;
        coverSize = Math.max(16, Math.min(96, coverSize));
        corner = Math.max(0, Math.min(3, corner));
        freecamSpeed = Math.max(0.05f, Math.min(12f, freecamSpeed));
        freecamSens = Math.max(0.1f, Math.min(3f, freecamSens));
        freelookSens = Math.max(0.1f, Math.min(3f, freelookSens));
        freecamBoost = Math.max(1, Math.min(8, freecamBoost));
        bgOpacity = Math.max(0, Math.min(255, bgOpacity));
        hudScale = Math.max(0.5f, Math.min(2.0f, hudScale));
        hudX = Math.max(0, Math.min(10000, hudX));
        hudY = Math.max(0, Math.min(10000, hudY));
        if (spotifyClientId == null) spotifyClientId = "";
        if (refreshToken == null) refreshToken = "";
        if (donutRegex == null || donutRegex.length() > 256) donutRegex = "";
    }
}
