package com.zenith.client.module;

import com.zenith.client.hud.*;
import com.zenith.client.util.ClickTracker;
import com.zenith.client.util.Notifications;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ModuleManager {
    public final List<Module> all = new ArrayList<>();

    public final FeatureModules.ThemeModule theme = new FeatureModules.ThemeModule();
    public final FeatureModules.Crosshair crosshair = new FeatureModules.Crosshair();
    public final FeatureModules.Zoom zoom = new FeatureModules.Zoom();
    public final FeatureModules.NoHurtCam noHurtCam = new FeatureModules.NoHurtCam();
    public final FeatureModules.Fullbright fullbright = new FeatureModules.Fullbright();
    public final FeatureModules.HitSound hitSound = new FeatureModules.HitSound();
    public final FeatureModules.HitParticles hitParticles = new FeatureModules.HitParticles();
    public final FeatureModules.ToggleSprint toggleSprint = new FeatureModules.ToggleSprint();
    public final FeatureModules.MobEsp mobEsp = new FeatureModules.MobEsp();
    public final FeatureModules.ItemEsp itemEsp = new FeatureModules.ItemEsp();
    public final FeatureModules.TimeChanger timeChanger = new FeatureModules.TimeChanger();
    public final FeatureModules.ClearWeather clearWeather = new FeatureModules.ClearWeather();

    // live stats
    public int fps, combo;
    public long lastHit;
    public double lastReach, speed;
    public LivingEntity lastTarget;
    private int frames;
    private long fpsStamp = System.currentTimeMillis();
    private boolean welcomed;

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private void reg(Module m, boolean on) { m.enabled = on; m.anim = on ? 1f : 0f; all.add(m); }

    public ModuleManager() {
        Category I = Category.INFO, P = Category.PVP;

        // ---- HUD info
        reg(new WatermarkHud(), true);
        reg(new TextHud("FPS", "Frames per second", I, 6, 26, () -> List.of("§7FPS §f" + fps)), true);
        reg(new TextHud("Ping", "Latency to the server", I, 6, 44, () -> {
            int p = ping();
            String c = p < 80 ? "§a" : p < 150 ? "§e" : "§c";
            return List.of("§7Ping " + c + p + "ms");
        }), true);
        reg(new TextHud("Coordinates", "Your XYZ position", I, 6, 62, () -> List.of(String.format(Locale.ROOT,
                "§7XYZ §f%.1f %.1f %.1f", mc.player.getX(), mc.player.getY(), mc.player.getZ()))), true);
        reg(new TextHud("Direction", "Facing + yaw", I, 6, 80, () -> {
            String d = mc.player.getHorizontalFacing().asString();
            d = Character.toUpperCase(d.charAt(0)) + d.substring(1);
            return List.of(String.format(Locale.ROOT, "§7Facing §f%s §8(%.0f)", d, MathHelper.wrapDegrees(mc.player.getYaw())));
        }), false);
        reg(new TextHud("Biome", "Current biome", I, 6, 98, () -> List.of("§7Biome §f" +
                mc.world.getBiome(mc.player.getBlockPos()).getKey().map(k -> k.getValue().getPath()).orElse("?"))), false);
        reg(new TextHud("Speed", "Blocks per second", I, 6, 116, () -> List.of(String.format(Locale.ROOT, "§7Speed §f%.2f §7b/s", speed))), false);
        reg(new TextHud("Memory", "RAM usage", I, 6, 134, () -> {
            Runtime r = Runtime.getRuntime();
            long used = (r.totalMemory() - r.freeMemory()) / 1048576L, max = r.maxMemory() / 1048576L;
            return List.of("§7RAM §f" + used + "§7/§f" + max + " MB");
        }), false);
        reg(new TextHud("Clock", "Real time", I, 6, 152, () -> List.of("§7Time §f" +
                LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")))), false);
        reg(new TextHud("Server IP", "Server address", I, 6, 170, () -> List.of("§7IP §f" +
                (mc.getCurrentServerEntry() == null ? "Singleplayer" : mc.getCurrentServerEntry().address))), false);
        reg(new RadarHud(), true);
        reg(new TextHud("Mob Counter", "Hostile mobs within 24 blocks", I, 6, 188, () -> {
            int n = 0;
            for (Entity e : mc.world.getEntities())
                if (e instanceof net.minecraft.entity.mob.Monster && e.isAlive() && mc.player.squaredDistanceTo(e) < 576) n++;
            return List.of("§7Hostiles §f" + n);
        }), false);
        reg(new ModuleListHud(), true);

        // ---- PvP HUD
        reg(new TextHud("CPS", "Clicks per second (LMB | RMB)", P, 6, 188, () -> List.of(
                "§7CPS §f" + ClickTracker.left() + " §8| §f" + ClickTracker.right())), true);
        reg(new KeystrokesHud(), true);
        reg(new ArmorHud(), true);
        reg(new PotionHud(), true);
        reg(new ItemsHud(), true);
        reg(new CooldownHud(), true);
        reg(new TargetHud(), true);
        reg(new TextHud("Combo", "Your hit streak", P, 6, 206, () -> List.of("§7Combo §f" + combo)), true);
        reg(new TextHud("Reach", "Distance of your last hit", P, 6, 224, () -> List.of(
                String.format(Locale.ROOT, "§7Reach §f%.2f", lastReach))), false);

        // ---- Visual / Utility / Settings
        reg(crosshair, true);
        reg(zoom, true);
        reg(mobEsp, false);
        reg(itemEsp, false);
        reg(fullbright, false);
        reg(timeChanger, false);
        reg(clearWeather, false);
        reg(noHurtCam, true);
        reg(hitParticles, false);
        reg(toggleSprint, false);
        reg(hitSound, false);
        reg(theme, true);
    }

    public List<Module> byCategory(Category c) {
        List<Module> l = new ArrayList<>();
        for (Module m : all) if (m.category == c) l.add(m);
        return l;
    }

    private int ping() {
        if (mc.getNetworkHandler() == null || mc.player == null) return 0;
        var e = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        return e == null ? 0 : e.getLatency();
    }

    public void tick(MinecraftClient mc) {
        if (mc.player == null) { welcomed = false; return; }
        if (!welcomed) { welcomed = true; Notifications.push("§dZenith §floaded §8- §7RShift = menu"); }
        speed = Math.hypot(mc.player.getX() - mc.player.prevX, mc.player.getZ() - mc.player.prevZ) * 20.0;
        if (mc.player.maxHurtTime > 0 && mc.player.hurtTime == mc.player.maxHurtTime) combo = 0;
        if (combo > 0 && System.currentTimeMillis() - lastHit > 2500) combo = 0;
        for (Module m : all) if (m.enabled) m.onTick();
    }

    public void renderHud(DrawContext ctx) {
        frames++;
        long now = System.currentTimeMillis();
        if (now - fpsStamp >= 1000) { fps = frames; frames = 0; fpsStamp = now; }
        ClickTracker.poll();
        if (mc.player == null || mc.options.hudHidden) return;
        for (Module m : all) {
            if (!m.enabled) continue;
            if (m instanceof HudModule h) h.renderHud(ctx); else m.renderOverlay(ctx);
        }
        Notifications.render(ctx);
    }

    /** called when the local player attacks an entity */
    public void onAttack(Entity e) {
        if (mc.player == null || !(e instanceof LivingEntity le)) return;
        lastTarget = le;
        lastHit = System.currentTimeMillis();
        combo++;
        Vec3d eye = mc.player.getEyePos();
        Box b = e.getBoundingBox();
        double dx = MathHelper.clamp(eye.x, b.minX, b.maxX) - eye.x;
        double dy = MathHelper.clamp(eye.y, b.minY, b.maxY) - eye.y;
        double dz = MathHelper.clamp(eye.z, b.minZ, b.maxZ) - eye.z;
        lastReach = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (hitSound.enabled) hitSound.play();
        if (hitParticles.enabled) hitParticles.spawn(e);
    }
}
