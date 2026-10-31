package com.zenith.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public final class Notifications {
    private record N(String text, long time) {}
    private static final List<N> LIST = new ArrayList<>();
    private static final long LIFE = 2400;

    public static void push(String text) {
        LIST.add(new N(text, System.currentTimeMillis()));
        if (LIST.size() > 5) LIST.remove(0);
    }

    public static void toggled(String name, boolean on) {
        push("§f" + name + " " + (on ? "§aON" : "§cOFF"));
    }

    public static void render(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        long now = System.currentTimeMillis();
        LIST.removeIf(n -> now - n.time > LIFE);
        int sw = mc.getWindow().getScaledWidth(), sh = mc.getWindow().getScaledHeight();
        int i = 0;
        for (int k = LIST.size() - 1; k >= 0; k--) {
            N n = LIST.get(k);
            long age = now - n.time;
            float in = Math.min(1f, age / 250f);
            float out = Math.min(1f, (LIFE - age) / 300f);
            float p = 1f - (1f - Math.min(in, out)) * (1f - Math.min(in, out));
            int w = Math.max(100, RenderUtil.width(n.text) + 20), h = 20;
            int x = sw - (int) ((w + 8) * p);
            int y = sh - 52 - i * 24;
            RenderUtil.rrect(ctx, x, y, w, h, 4, 0xDC101018);
            ctx.fill(x, y + 3, x + 2, y + h - 3, Theme.color(i));
            RenderUtil.text(ctx, n.text, x + 9, y + 6, 0xFFFFFFFF);
            i++;
        }
    }
}
