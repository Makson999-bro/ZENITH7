package com.zenith.client.hud;

import com.zenith.client.module.Category;
import com.zenith.client.module.Setting;
import com.zenith.client.util.Theme;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;

/** Minimap-style radar of nearby mobs (players are never shown) */
public class RadarHud extends HudModule {
    public final Setting.Num range = add(new Setting.Num("Range", 32, 8, 64, 4));
    public final Setting.Bool hostile = add(new Setting.Bool("Hostile", true));
    public final Setting.Bool passive = add(new Setting.Bool("Passive", true));

    public RadarHud() { super("Mob Radar", "Radar of nearby mobs (no players)", Category.INFO, 6, -1); }

    @Override protected void draw(DrawContext ctx) {
        int s = 84, c = s / 2;
        box(ctx, s, s);
        ctx.fill(c, 3, c + 1, s - 3, 0x28FFFFFF);
        ctx.fill(3, c, s - 3, c + 1, 0x28FFFFFF);
        double yaw = Math.toRadians(mc.player.getYaw());
        double cos = Math.cos(yaw), sin = Math.sin(yaw), r = range.value;
        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof MobEntity) || !e.isAlive()) continue;
            boolean host = e instanceof Monster;
            if (host ? !hostile.value : !passive.value) continue;
            double dx = e.getX() - mc.player.getX(), dz = e.getZ() - mc.player.getZ();
            if (dx * dx + dz * dz > r * r) continue;
            double sx = -(dx * cos + dz * sin), fwd = dz * cos - dx * sin;
            int px = c + (int) (sx / r * (c - 4)), py = c - (int) (fwd / r * (c - 4));
            ctx.fill(px - 1, py - 1, px + 2, py + 2, host ? 0xFFFF5555 : 0xFF55FF55);
        }
        ctx.fill(c - 1, c - 1, c + 2, c + 2, Theme.accent());
    }
}
