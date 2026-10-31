package com.zenith.client.hud;

import com.zenith.client.module.Category;
import com.zenith.client.util.RenderUtil;
import com.zenith.client.util.Theme;
import net.minecraft.client.gui.DrawContext;

public class WatermarkHud extends HudModule {
    public WatermarkHud() { super("Watermark", "Zenith logo with animated colors", Category.INFO, 6, 6); }

    @Override protected void draw(DrawContext ctx) {
        String logo = "ZENITH";
        String rest = " §8| §7RShift";
        int lw = 0;
        for (char c : logo.toCharArray()) lw += RenderUtil.width(String.valueOf(c));
        box(ctx, lw + RenderUtil.width(rest) + 14, 16);
        int x = 7;
        for (int i = 0; i < logo.length(); i++) {
            String c = String.valueOf(logo.charAt(i));
            RenderUtil.text(ctx, c, x, 4, Theme.color(i * 0.8f));
            x += RenderUtil.width(c);
        }
        RenderUtil.text(ctx, rest, x, 4, 0xFFFFFFFF);
    }
}
