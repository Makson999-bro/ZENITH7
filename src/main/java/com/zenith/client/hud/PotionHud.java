package com.zenith.client.hud;

import com.zenith.client.ZenithClient;
import com.zenith.client.module.Category;
import com.zenith.client.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

import java.util.ArrayList;
import java.util.List;

public class PotionHud extends HudModule {
    private static final String[] ROMAN = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};

    public PotionHud() { super("Potion Effects", "Active effects with timers", Category.PVP, -1, 160); }

    @Override protected void draw(DrawContext ctx) {
        List<StatusEffectInstance> l = new ArrayList<>(mc.player.getStatusEffects());
        l.removeIf(e -> e.getEffectType() == StatusEffects.NIGHT_VISION && ZenithClient.modules.fullbright.enabled);
        if (l.isEmpty()) { placeholder(ctx, 100, 18); return; }

        int mw = 100;
        for (StatusEffectInstance e : l) mw = Math.max(mw, RenderUtil.width(label(e)) + RenderUtil.width(time(e)) + 26);
        box(ctx, mw, l.size() * 12 + 6);
        int y = 4;
        for (StatusEffectInstance e : l) {
            int col = 0xFF000000 | e.getEffectType().getColor();
            ctx.fill(7, y + 1, 9, y + 8, col);
            RenderUtil.text(ctx, label(e), 13, y, col);
            boolean low = !e.isInfinite() && e.getDuration() < 200 && (System.currentTimeMillis() / 300) % 2 == 0;
            String t = time(e);
            RenderUtil.text(ctx, t, mw - 6 - RenderUtil.width(t), y, low ? 0xFFFF5555 : 0xFFDDDDEE);
            y += 12;
        }
    }

    private String label(StatusEffectInstance e) {
        int amp = e.getAmplifier();
        return e.getEffectType().getName().getString() + " " + (amp < ROMAN.length ? ROMAN[amp] : String.valueOf(amp + 1));
    }

    private String time(StatusEffectInstance e) {
        if (e.isInfinite()) return "**:**";
        int s = e.getDuration() / 20;
        return s / 60 + ":" + String.format("%02d", s % 60);
    }
}
