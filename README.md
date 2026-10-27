# NeoClient 1.1.0

Fabric client mod for Minecraft 1.21.11 (Java 21+, Fabric Loader 0.18+, Fabric API 0.141.1+).

## Features
- Zoom on **C**, with eased FOV transitions.
- Client-side Freecam on **F6** and Freelook on **Left Alt** (keybinds can be changed in Minecraft controls).
- Info HUD, screen tint, cinematic bars, fullbright, and clear-weather visuals.
- Spotify HUD with album art, track metadata, progress, synced lyrics, and floating lyrics.
- Center-screen settings menu on **Right Shift**.
- Local waypoints with dimension-aware distance and Nether 8:1 coordinate estimates.
- Donut earnings tracker that totals matching chat transactions for this session, today, and the last seven days.

## Build on GitHub

1. Create a new GitHub repository.
2. Upload **the contents of this folder** to the repository root. Keep the `.github/workflows/build.yml` file and its folders.
3. Commit and push to the default branch, or open **Actions → Build NeoClient → Run workflow**.
4. Open the completed workflow run and download the `neoclient-jar` artifact.
5. Use `neoclient-1.1.0.jar` from the downloaded artifact. The `-sources.jar` is not the mod file.
6. Put the JAR in your Minecraft `mods` folder alongside Fabric Loader and Fabric API for Minecraft 1.21.11.

The workflow builds on GitHub-hosted Ubuntu with Java 21 and Gradle 9.2.1. Each push and manual workflow run starts a build.

## Build locally

Install JDK 21 and Gradle 9.2.1, then run this in the repository folder:

```sh
gradle build --no-daemon
```

The remapped mod JAR is written to `build/libs/neoclient-1.1.0.jar`.

## Spotify

1. Create an app in the Spotify developer dashboard and enable the Web API.
2. Add this redirect URI exactly: `http://127.0.0.1:8888/callback`.
3. In game, open **Right Shift → Spotify**, enter the Spotify Client ID, and connect.

Spotify tokens are stored in Minecraft's `config/neoclient.json`. Keep that file private and do not commit it.

## Waypoints and earnings

Waypoints are saved locally in `config/neoclient-waypoints.json`. Add a waypoint from **Right Shift → Waypoints** by entering its name and coordinates. The HUD shows the nearest waypoint's distance when it is in the current dimension or connected through the Overworld/Nether 8:1 scale.

The earnings HUD parses transaction amounts from chat and stores them in `config/neoclient-transactions.csv`. Its default regex looks for a `+`, `earned`, `received`, or `sold` phrase followed by a dollar amount. You can customize `donutRegex` in `config/neoclient.json`; capture group 1 must contain the amount. This tracks matching chat lines while the mod is running; it cannot read the server's actual account balance.

The info HUD's position can be changed with `hudX` and `hudY` in `config/neoclient.json`.
