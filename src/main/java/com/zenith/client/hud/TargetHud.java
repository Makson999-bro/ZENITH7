package com.zenith.client.hud;

import com.zenith.client.ZenithClient;
import com.zenith.client.module.Category;
import com.zenith.client.util.RenderUtil;
import com.zenith.client.util.Theme;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;

import java.util.Locale;

/** Enemy health bar + "winning/losing" indicator */
public class TargetHud extends HudModule {
    private float shown = 1f;

    public TargetHud() { super("Target HUD", "Enemy health bar & fight status", Category.PVP, -2, -2); }

    @Override protected void draw(DrawContext ctx) {
        var m = ZenithClient.modules;
        LivingEntity t = null;
        if (mc.targetedEntity instanceof LivingEntity le) t = le;
        else if (m.lastTarget != null && m.lastTarget.isAlive() && System.currentTimeMillis() - m.lastHit < 4000) t = m.lastTarget;
        if (t == null) { placeholder(ctx, 130, 38); return; }

        box(ctx, 130, 38);
        float hp = t.getHealth() + t.getAbsorptionAmount();
        float frac = Math.max(0f, Math.min(1f, hp / Math.max(1f, t.getMaxHealth())));
        shown += (frac - shown) * 0.15f;

        RenderUtil.text(ctx, t.getName().getString(), 7, 5, 0xFFFFFFFF);
        RenderUtil.rrect(ctx, 7, 18, 116, 5, 2, 0xFF22222F);
        int col = RenderUtil.lerp(0xFFFF4040, 0xFF55FF55, shown);
        RenderUtil.rrect(ctx, 7, 18, Math.max(3, (int) (116 * shown)), 5, 2, col);

        RenderUtil.text(ctx, String.format(Locale.ROOT, "%.1f HP", hp), 7, 27, 0xFFB0B0C8);
        float mine = mc.player.getHealth() + mc.player.getAbsorptionAmount();
        String st = mine > hp ? "§aWinning" : mine < hp ? "§cLosing" : "§eEven";
        RenderUtil.text(ctx, st, 123 - RenderUtil.width(st), 27, 0xFFFFFFFF);
    }
}
