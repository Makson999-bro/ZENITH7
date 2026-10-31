package com.zenith.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public final class RenderUtil {
    private static MinecraftClient mc() { return MinecraftClient.getInstance(); }

    public static int alpha(int c, int a) {
        return (Math.max(0, Math.min(255, a)) << 24) | (c & 0xFFFFFF);
    }

    public static int lerp(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int r = 0;
        for (int s = 24; s >= 0; s -= 8) {
            int ca = (a >>> s) & 255, cb = (b >>> s) & 255;
            r |= ((int) (ca + (cb - ca) * t) & 255) << s;
        }
        return r;
    }

    /** Rounded rectangle (pixel-art corners, looks smooth at GUI scale) */
    public static void rrect(DrawContext c, int x, int y, int w, int h, int r, int col) {
        r = Math.min(r, Math.min(w, h) / 2);
        if (r <= 0) { c.fill(x, y, x + w, y + h, col); return; }
        for (int i = 0; i < r; i++) {
            double dy = r - i - 0.5;
            int inset = (int) Math.round(r - Math.sqrt(r * r - dy * dy));
            c.fill(x + inset, y + i, x + w - inset, y + i + 1, col);
            c.fill(x + inset, y + h - i - 1, x + w - inset, y + h - i, col);
        }
        c.fill(x, y + r, x + w, y + h - r, col);
    }

    public static void outline(DrawContext c, int x, int y, int w, int h, int col) {
        c.fill(x, y, x + w, y + 1, col);
        c.fill(x, y + h - 1, x + w, y + h, col);
        c.fill(x, y + 1, x + 1, y + h - 1, col);
        c.fill(x + w - 1, y + 1, x + w, y + h - 1, col);
    }

    public static void hgradient(DrawContext c, int x, int y, int w, int h, int c1, int c2) {
        for (int i = 0; i < w; i += 2)
            c.fill(x + i, y, x + Math.min(i + 2, w), y + h, lerp(c1, c2, i / (float) w));
    }

    public static void text(DrawContext c, String s, int x, int y, int col) {
        c.drawText(mc().textRenderer, s, x, y, col, true);
    }

    public static int width(String s) { return mc().textRenderer.getWidth(s); }

    /** switch toggle (iOS style) */
    public static void toggle(DrawContext c, int x, int y, int w, int h, float anim) {
        rrect(c, x, y, w, h, h / 2, lerp(0xFF2A2A3A, Theme.accent(), anim));
        int k = h - 4;
        rrect(c, x + 2 + (int) (anim * (w - h)), y + 2, k, k, k / 2, 0xFFFFFFFF);
    }
}
