package dev.neoclient;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/** In-game drag editor for the client's HUD panels. */
public final class HudEditorScreen extends Screen {
    private enum Part { INFO, DONUT, WAYPOINT, SPOTIFY, AUCTION, LYRICS, KEYSTROKES, ARMOR, POTIONS }
    private Part dragging;
    private double grabX, grabY;

    public HudEditorScreen() { super(Text.literal("HUD Editor")); }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);
        ctx.fill(0, 0, width, height, 0x55000000);
        ctx.fill(width / 2 - 155, 8, width / 2 + 155, 42, 0xE8191D24);
        ctx.fill(width / 2 - 155, 8, width / 2 + 155, 10, Config.I.accentColor);
        ctx.drawCenteredTextWithShadow(textRenderer, "HUD Editor", width / 2, 15, 0xFFFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer, "Drag panels to place them. Esc saves and closes.", width / 2, 29, 0xFFB9C5D2);
        for (Part p : Part.values()) drawPart(ctx, p, mouseX, mouseY);
        super.render(ctx, mouseX, mouseY, delta);
    }

    private void drawPart(DrawContext ctx, Part p, int mx, int my) {
        int x = px(p), y = py(p), w = partWidth(p), h = partHeight(p);
        boolean over = mx >= x && mx <= x + w && my >= y && my <= y + h;
        ctx.fill(x, y, x + w, y + h, over || dragging == p ? 0xE82B3949 : 0xC0191D24);
        ctx.fill(x, y, x + w, y + 2, Config.I.accentColor);
        ctx.drawTextWithShadow(textRenderer, label(p), x + 5, y + 5, 0xFFFFFFFF);
        if (p == Part.INFO) ctx.drawTextWithShadow(textRenderer, "FPS 120   XYZ 120 64 -40", x + 5, y + 17, 0xFFD8E1EA);
        else if (p == Part.AUCTION) ctx.drawTextWithShadow(textRenderer, "Auction top bid and countdown", x + 5, y + 17, 0xFFD8E1EA);
        else if (p == Part.LYRICS) ctx.drawTextWithShadow(textRenderer, "Floating Spotify lyrics", x + 5, y + 17, 0xFFD8E1EA);
        else if (p == Part.KEYSTROKES) ctx.drawTextWithShadow(textRenderer, "W  A S D  SPACE", x + 5, y + 17, 0xFFD8E1EA);
        else if (p == Part.ARMOR) ctx.drawTextWithShadow(textRenderer, "Armor 100%  86%  42%", x + 5, y + 17, 0xFFD8E1EA);
        else if (p == Part.POTIONS) ctx.drawTextWithShadow(textRenderer, "Speed II  1:24", x + 5, y + 17, 0xFFD8E1EA);
        else if (p == Part.DONUT) ctx.drawTextWithShadow(textRenderer, "Session $0.00  Today $0.00", x + 5, y + 17, 0xFFD8E1EA);
        else if (p == Part.WAYPOINT) ctx.drawTextWithShadow(textRenderer, "Home  120m", x + 5, y + 17, 0xFFD8E1EA);
        else ctx.drawTextWithShadow(textRenderer, "Spotify track and floating lyrics", x + 5, y + 17, 0xFFD8E1EA);
    }

    private String label(Part p) { return switch (p) {
        case INFO -> "Info / Coordinates"; case DONUT -> "Donut Tracker"; case WAYPOINT -> "Waypoints";
        case SPOTIFY -> "Spotify HUD"; case AUCTION -> "Auction Tracker"; case LYRICS -> "Floating Lyrics"; case KEYSTROKES -> "Keystrokes"; case ARMOR -> "Armor Status"; case POTIONS -> "Potion Effects";
    }; }
    private int partWidth(Part p) { return p == Part.SPOTIFY ? 230 : p == Part.KEYSTROKES ? 100 : p == Part.LYRICS || p == Part.AUCTION ? 230 : 176; }
    private int partHeight(Part p) { return p == Part.KEYSTROKES ? 70 : p == Part.LYRICS ? 24 : 40; }
    private int px(Part p) { Config c = Config.I; return switch (p) {
        case INFO -> c.hudX; case DONUT -> c.donutX; case WAYPOINT -> c.waypointX; case SPOTIFY -> c.spotifyX < 0 ? (c.corner % 2 == 0 ? 6 : width - 236) : c.spotifyX; case AUCTION -> c.auctionX; case LYRICS -> c.floatingLyricX < 0 ? width / 2 - 115 : c.floatingLyricX;
        case KEYSTROKES -> c.keystrokesX; case ARMOR -> c.armorX; case POTIONS -> c.potionX;
    }; }
    private int py(Part p) { Config c = Config.I; return switch (p) {
        case INFO -> c.hudY; case DONUT -> c.donutY; case WAYPOINT -> c.waypointY; case SPOTIFY -> c.spotifyY < 0 ? (c.corner < 2 ? 6 : height - 50) : c.spotifyY; case AUCTION -> c.auctionY; case LYRICS -> c.floatingLyricY <= 0 ? height - 54 : c.floatingLyricY;
        case KEYSTROKES -> c.keystrokesY; case ARMOR -> c.armorY; case POTIONS -> c.potionY;
    }; }
    private void setPos(Part p, int x, int y) { Config c = Config.I; switch (p) {
        case INFO -> { c.hudX = x; c.hudY = y; } case DONUT -> { c.donutX = x; c.donutY = y; }
        case WAYPOINT -> { c.waypointX = x; c.waypointY = y; } case SPOTIFY -> { c.spotifyX = x; c.spotifyY = y; } case AUCTION -> { c.auctionX = x; c.auctionY = y; } case LYRICS -> { c.floatingLyricX = x; c.floatingLyricY = y; }
        case KEYSTROKES -> { c.keystrokesX = x; c.keystrokesY = y; } case ARMOR -> { c.armorX = x; c.armorY = y; }
        case POTIONS -> { c.potionX = x; c.potionY = y; }
    } }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);
        for (Part p : Part.values()) {
            if (mouseX >= px(p) && mouseX <= px(p) + partWidth(p) && mouseY >= py(p) && mouseY <= py(p) + partHeight(p)) {
                dragging = p; grabX = mouseX - px(p); grabY = mouseY - py(p); return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging == null || button != 0) return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        int x = (int) Math.max(0, Math.min(width - partWidth(dragging), mouseX - grabX));
        int y = (int) Math.max(44, Math.min(height - partHeight(dragging), mouseY - grabY));
        setPos(dragging, x, y); return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && dragging != null) { dragging = null; Config.save(); return true; }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override public void close() { Config.save(); super.close(); }
}
