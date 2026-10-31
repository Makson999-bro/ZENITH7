package com.zenith.client.hud;

import com.zenith.client.module.Category;
import com.zenith.client.util.ClickTracker;
import com.zenith.client.util.RenderUtil;
import com.zenith.client.util.Theme;
import net.minecraft.client.gui.DrawContext;

public class KeystrokesHud extends HudModule {
    private final float[] a = new float[7];

    public KeystrokesHud() { super("Keystrokes", "WASD + mouse + space with CPS", Category.PVP, 6, 100); }

    @Override protected void draw(DrawContext ctx) {
        w = 70; h = 88;
        var o = mc.options;
        key(ctx, 0, 24, 0, 22, 22, "W", null, o.forwardKey.isPressed());
        key(ctx, 1, 0, 24, 22, 22, "A", null, o.leftKey.isPressed());
        key(ctx, 2, 24, 24, 22, 22, "S", null, o.backKey.isPressed());
        key(ctx, 3, 48, 24, 22, 22, "D", null, o.rightKey.isPressed());
        key(ctx, 4, 0, 48, 34, 24, "LMB", String.valueOf(ClickTracker.left()), ClickTracker.leftDown());
        key(ctx, 5, 36, 48, 34, 24, "RMB", String.valueOf(ClickTracker.right()), ClickTracker.rightDown());
        key(ctx, 6, 0, 74, 70, 14, "SPACE", null, o.jumpKey.isPressed());
    }

    private void key(DrawContext ctx, int i, int x, int y, int kw, int kh, String label, String sub, boolean down) {
        a[i] += ((down ? 1f : 0f) - a[i]) * 0.3f;
        int on = RenderUtil.alpha(Theme.accent(), 200);
        RenderUtil.rrect(ctx, x, y, kw, kh, 3, RenderUtil.lerp(0x96101018, on, a[i]));
        int tw = RenderUtil.width(label);
        if (sub == null) {
            RenderUtil.text(ctx, label, x + (kw - tw) / 2, y + (kh - 8) / 2, 0xFFFFFFFF);
        } else {
            RenderUtil.text(ctx, label, x + (kw - tw) / 2, y + 3, 0xFFFFFFFF);
            RenderUtil.text(ctx, sub, x + (kw - RenderUtil.width(sub)) / 2, y + 13, 0xFFB0B0C8);
        }
    }
}
