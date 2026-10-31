package com.zenith.client.hud;

import com.zenith.client.gui.ClickGuiScreen;
import com.zenith.client.module.Category;
import com.zenith.client.module.Module;
import com.zenith.client.module.Setting;
import com.zenith.client.util.RenderUtil;
import com.zenith.client.util.Theme;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

/** Base class for draggable HUD widgets. x/y = -1 -> anchor right/bottom, -2 -> centered (first launch only) */
public abstract class HudModule extends Module {
    public int x, y, w = 60, h = 16;
    public final Setting.Num scale = add(new Setting.Num("Scale", 1.0, 0.5, 2.0, 0.1));
    public final Setting.Bool bg = add(new Setting.Bool("Background", true));

    protected HudModule(String name, String desc, Category cat, int x, int y) {
        super(name, desc, cat);
        this.x = x; this.y = y;
    }

    public boolean draggable() { return true; }

    public boolean hit(double mx, double my) {
        return draggable() && mx >= x && my >= y && mx <= x + w * scale.value && my <= y + h * scale.value;
    }

    public void renderHud(DrawContext ctx) {
        float s = (float) scale.value;
        MatrixStack ms = ctx.getMatrices();
        ms.push();
        ms.translate(Math.max(x, 0), Math.max(y, 0), 0);
        ms.scale(s, s, 1f);
        draw(ctx);
        ms.pop();
        int sw = mc.getWindow().getScaledWidth(), sh = mc.getWindow().getScaledHeight();
        int ws = (int) (w * s), hs = (int) (h * s);
        if (x == -1) x = sw - ws - 6; else if (x == -2) x = (sw - ws) / 2 + 40;
        if (y == -1) y = sh - hs - 6; else if (y == -2) y = sh / 2 + 30;
    }

    protected abstract void draw(DrawContext ctx);

    protected boolean editing() { return mc.currentScreen instanceof ClickGuiScreen; }

    protected void box(DrawContext ctx, int w, int h) {
        this.w = w; this.h = h;
        if (bg.value) {
            RenderUtil.rrect(ctx, 0, 0, w, h, 3, 0x96101018);
            ctx.fill(0, 2, 2, h - 2, Theme.accent());
        }
    }

    /** shown in edit mode when the widget has nothing to display */
    protected void placeholder(DrawContext ctx, int pw, int ph) {
        this.w = pw; this.h = ph;
        if (editing()) {
            box(ctx, pw, ph);
            RenderUtil.text(ctx, "§7" + name, 7, (ph - 8) / 2, 0xFFFFFFFF);
        }
    }
}
