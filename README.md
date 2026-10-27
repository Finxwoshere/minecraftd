# NeoClient 1.1.0

Fabric client mod for Minecraft 1.21.11. Requires Java 21, Fabric Loader 0.18+, and Fabric API 0.141.1+.

## Current modules
- ClickGUI: **Right Shift**, with Visuals, Camera, Spotify, HUD, View, Points, and Auction sections. Blue, violet, mint, and amber themes.
- Camera: **F6** Freecam and **Left Alt** Freelook. Camera rotation is handled on the detached camera; Freelook leaves the player rotation intact. Movement keys still move the player in Freelook. Freecam uses WASD, Space/Shift, sprint boost, and optional scroll speed.
- HUD editor: open Right Shift → HUD → **Edit HUD layout**, then drag HUD panels. Positions save locally. Current draggable panels include Info/Coordinates, Donut Tracker, Waypoints, Spotify, Floating Lyrics, Keystrokes, Armor Status, Potion Effects, and Auction.
- HUD modules: FPS/coordinates/direction, Keystrokes, armor durability, potion timers, earnings/balance tracker, custom crosshair, waypoint distance, and Spotify.
- Visual settings: fullbright, clear weather, zoom with eased transitions, no hurt camera, no view bobbing, theme accents, crosshair size/gap, and first-person viewmodel X/Y/Z adjustments.
- Spotify HUD: album art, track/artist/album, progress, synced/floating lyrics with fades, theme accents, and rounded panels.
- Waypoints: enter name and coordinates under Right Shift → Points; the HUD shows a square, name, and distance. Nether distance uses the 8:1 Overworld scale. Waypoint scale is adjustable under HUD.
- Auction tracker: set an item, starting top bid, and countdown under Right Shift → Auction. Matching payment chat lines add to the displayed top bid. This widget only reads chat; it does not issue commands or payments.

## Module keybinds
Open **Options → Controls → Key Binds → NeoClient**. Assign keys to the HUD module toggles, Freecam, Freelook, Zoom, and menu. Newly added HUD-module bindings default to unbound.

## Build from GitHub
1. Upload the contents of this project folder into the root of your GitHub repository. Keep `.github/workflows/build.yml`.
2. Push/commit to the default branch. GitHub Actions runs **Build NeoClient** automatically; it can also be started manually from Actions.
3. Download the `neoclient-jar` artifact from a successful run. Install `neoclient-1.1.0.jar` from it (not the `-sources.jar`) in `.minecraft/mods`.

The workflow uses Java 21 and Gradle 9.2.1. Locally, run `gradle build --no-daemon` with those tools installed; output is in `build/libs/`.

## Spotify setup
1. In the Spotify Developer Dashboard, register the Redirect URI exactly as `http://127.0.0.1:8888/callback`.
2. In the client, open Right Shift → Spotify, enter that app's Client ID, and connect.

Tokens are stored in Minecraft's `config/neoclient.json`. Keep that file private and never commit it.

## Donut balance and auction notes
The balance tracker reads balance snapshots from chat; it cannot query DonutSMP's private balance directly. After enabling the tracker, run the server's balance command (for example `/bal`) so the first matching message sets the baseline; later matching balance messages update the change. `balanceRegex` in `config/neoclient.json` controls the pattern and capture group 1 must contain the amount.

The tracker also records matching earnings lines in `config/neoclient-transactions.csv`. The auction widget uses `auctionRegex` to identify payment lines; adjust it in `config/neoclient.json` if DonutSMP's message wording differs. These regexes only observe incoming chat.

## Fair-play scope
The client focuses on cosmetic and informational overlays. It does not include Anti-invis or other features that reveal hidden players, bypass server rules, or automate gameplay/economy actions.
