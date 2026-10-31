package com.zenith.client.util;

import com.zenith.client.ZenithClient;
import net.minecraft.util.math.MathHelper;

public final class Theme {
    public static int accent() { return color(0f); }

    /** Accent color; offset makes a gradient / rainbow wave */
    public static int color(float offset) {
        var t = ZenithClient.modules.theme;
        float hue;
        if (t.rainbow.value) {
            double period = 6000.0 / t.speed.value;
            hue = (float) (((System.currentTimeMillis() % (long) period) / period + offset * 0.12) % 1.0);
        } else {
            hue = (float) ((t.hue.value / 360.0 + offset * 0.04) % 1.0);
        }
        if (hue < 0) hue += 1f;
        return 0xFF000000 | MathHelper.hsvToRgb(hue, 0.62f, 1f);
    }
}
