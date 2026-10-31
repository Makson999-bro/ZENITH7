package com.zenith.client.hud;

import com.zenith.client.module.Category;
import com.zenith.client.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;

import java.util.List;
import java.util.function.Supplier;

public class TextHud extends HudModule {
    private final Supplier<List<String>> src;

    public TextHud(String name, String desc, Category cat, int x, int y, Supplier<List<String>> src) {
        super(name, desc, cat, x, y);
        this.src = src;
    }

    @Override protected void draw(DrawContext ctx) {
        List<String> ls = src.get();
        int tw = 0;
        for (String s : ls) tw = Math.max(tw, RenderUtil.width(s));
        box(ctx, tw + 12, ls.size() * 11 + 5);
        for (int i = 0; i < ls.size(); i++) RenderUtil.text(ctx, ls.get(i), 7, 4 + i * 11, 0xFFFFFFFF);
    }
}
