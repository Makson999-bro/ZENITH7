package com.zenith.client.module;

import com.zenith.client.ZenithClient;
import com.zenith.client.util.RenderUtil;
import com.zenith.client.util.Theme;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public final class FeatureModules {

    // ---------------------------------------------------------------- Theme
    public static class ThemeModule extends Module {
        public final Setting.Num hue = add(new Setting.Num("Accent Hue", 270, 0, 360, 1));
        public final Setting.Bool rainbow = add(new Setting.Bool("Rainbow", false));
        public final Setting.Num speed = add(new Setting.Num("Rainbow Speed", 1.0, 0.2, 3.0, 0.1));
        public ThemeModule() {
            super("Theme", "Colors of the whole client", Category.SETTINGS);
            enabled = true; locked = true; expanded = true;
        }
    }

    // ----------------------------------------------------------- Fullbright
    public static class Fullbright extends Module {
        private boolean added;
        public Fullbright() { super("Fullbright", "See clearly in the dark", Category.VISUAL); }

        @Override public void onTick() {
            var p = mc.player;
            if (p == null) return;
            if (!p.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, -1, 0, false, false, false));
                added = true;
            }
        }
        @Override public void onDisable() {
            if (added && mc.player != null) mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
            added = false;
        }
    }

    // ----------------------------------------------------------------- Zoom
    public static class Zoom extends Module {
        public final Setting.Num level = add(new Setting.Num("Zoom Level", 4.0, 1.5, 12.0, 0.5));
        public final Setting.Bool cinematic = add(new Setting.Bool("Cinematic Camera", true));
        private boolean zooming;
        private double cur = 1.0;
        private long last = System.nanoTime();

        public Zoom() { super("Zoom", "Hold C to zoom (OptiFine style)", Category.VISUAL); }

        public boolean active() { return zooming || cur < 0.995; }

        /** Called every frame from GameRenderer mixin -> smooth animation */
        public double fovFactor() {
            long now = System.nanoTime();
            float dt = Math.min(0.1f, (now - last) / 1e9f);
            last = now;
            double target = zooming ? 1.0 / level.value : 1.0;
            cur += (target - cur) * Math.min(1.0, dt * 14.0);
            return cur;
        }

        @Override public void onTick() {
            boolean down = mc.currentScreen == null && ZenithClient.zoomKey.isPressed();
            if (down && !zooming) { zooming = true; last = System.nanoTime(); }
            if (!down && zooming) { zooming = false; mc.options.smoothCameraEnabled = false; }
            if (zooming && cinematic.value) mc.options.smoothCameraEnabled = true;
        }
        @Override public void onDisable() { zooming = false; cur = 1.0; mc.options.smoothCameraEnabled = false; }
    }

    // ---------------------------------------------------------- Toggle sprint
    public static class ToggleSprint extends Module {
        public ToggleSprint() { super("Toggle Sprint", "Always sprint while moving forward", Category.UTILITY); }
        @Override public void onTick() {
            if (mc.currentScreen == null) mc.options.sprintKey.setPressed(true);
        }
        @Override public void onDisable() { mc.options.sprintKey.setPressed(false); }
    }

    // ------------------------------------------------------------ No hurt cam
    public static class NoHurtCam extends Module {
        public NoHurtCam() { super("No Hurt Cam", "Removes camera shake when hit", Category.VISUAL); }
    }

    // ------------------------------------------------------------- Hit sound
    public static class HitSound extends Module {
        public final Setting.Num sound = add(new Setting.Num("Sound (1-3)", 1, 1, 3, 1));
        public final Setting.Num pitch = add(new Setting.Num("Pitch", 1.4, 0.5, 2.0, 0.1));
        public final Setting.Num volume = add(new Setting.Num("Volume", 0.6, 0.1, 1.0, 0.1));
        public HitSound() { super("Hit Sound", "Plays a ding when you hit someone", Category.UTILITY); }

        public void play() {
            SoundEvent s = switch ((int) sound.value) {
                case 2 -> SoundEvents.ENTITY_ARROW_HIT_PLAYER;
                case 3 -> SoundEvents.ENTITY_PLAYER_ATTACK_CRIT;
                default -> SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP;
            };
            mc.getSoundManager().play(PositionedSoundInstance.master(s, (float) pitch.value, (float) volume.value));
        }
    }

    // --------------------------------------------------------- Hit particles
    public static class HitParticles extends Module {
        public final Setting.Num count = add(new Setting.Num("Amount", 8, 1, 30, 1));
        public final Setting.Bool enchant = add(new Setting.Bool("Magic Style", false));
        public HitParticles() { super("Hit Particles", "Extra crit particles on hit (visual)", Category.VISUAL); }

        public void spawn(Entity e) {
            ThreadLocalRandom r = ThreadLocalRandom.current();
            ParticleEffect type = enchant.value ? ParticleTypes.ENCHANTED_HIT : ParticleTypes.CRIT;
            for (int i = 0; i < (int) count.value; i++) {
                mc.particleManager.addParticle(type,
                        e.getX() + r.nextDouble(-0.4, 0.4), e.getBodyY(0.2 + r.nextDouble() * 0.7), e.getZ() + r.nextDouble(-0.4, 0.4),
                        r.nextDouble(-0.4, 0.4), r.nextDouble(0.1, 0.5), r.nextDouble(-0.4, 0.4));
            }
        }
    }

    // -------------------------------------------------------------- Crosshair
    public static class Crosshair extends Module {
        public final Setting.Num style = add(new Setting.Num("Style", 1, 1, 3, 1));
        public final Setting.Num size = add(new Setting.Num("Size", 5, 1, 15, 1));
        public final Setting.Num gap = add(new Setting.Num("Gap", 3, 0, 10, 1));
        public final Setting.Num thick = add(new Setting.Num("Thickness", 1, 1, 4, 1));
        public final Setting.Bool dynamic = add(new Setting.Bool("Dynamic (moves with you)", true));
        public final Setting.Bool outline = add(new Setting.Bool("Outline", true));
        public final Setting.Bool redOnTarget = add(new Setting.Bool("Red on target", true));
        private float dyn;

        public Crosshair() { super("Crosshair", "Custom crosshair: 1 plus, 2 dot, 3 ring", Category.VISUAL); }

        @Override public void renderOverlay(DrawContext c) {
            if (mc.currentScreen != null || mc.player == null || !mc.options.getPerspective().isFirstPerson()) return;
            int cx = mc.getWindow().getScaledWidth() / 2, cy = mc.getWindow().getScaledHeight() / 2;
            dyn += ((dynamic.value && ZenithClient.modules.speed > 0.6 ? 3f : 0f) - dyn) * 0.2f;
            int t = (int) thick.value, s = (int) size.value, g = Math.round((float) gap.value + dyn);
            int col = (redOnTarget.value && mc.targetedEntity != null) ? 0xFFFF4A4A : Theme.accent();
            if (outline.value) shape(c, cx, cy, t, s, g, 1, 0xFF000000);
            shape(c, cx, cy, t, s, g, 0, col);
        }

        private void shape(DrawContext c, int cx, int cy, int t, int s, int g, int i, int col) {
            int o = t / 2;
            switch ((int) style.value) {
                case 2 -> { int d = Math.max(1, s / 3); c.fill(cx - d - i, cy - d - i, cx + d + i, cy + d + i, col); }
                case 3 -> {
                    double r = s + g;
                    for (int a = 0; a < 360; a += 6) {
                        int x = cx + (int) Math.round(Math.cos(Math.toRadians(a)) * r) - o;
                        int y = cy + (int) Math.round(Math.sin(Math.toRadians(a)) * r) - o;
                        c.fill(x - i, y - i, x + t + i, y + t + i, col);
                    }
                }
                default -> {
                    c.fill(cx - o - i, cy - g - s - i, cx - o + t + i, cy - g + i, col);
                    c.fill(cx - o - i, cy + g - i, cx - o + t + i, cy + g + s + i, col);
                    c.fill(cx - g - s - i, cy - o - i, cx - g + i, cy - o + t + i, col);
                    c.fill(cx + g - i, cy - o - i, cx + g + s + i, cy - o + t + i, col);
                }
            }
        }
    }

    // ------------------------------------------------- glow-outline ESP base
    /** Uses the vanilla glowing outline (visible through walls). Client-side only. */
    public abstract static class GlowModule extends Module {
        private final Set<Integer> marked = new HashSet<>();
        protected GlowModule(String n, String d, Category c) { super(n, d, c); }
        protected abstract boolean want(Entity e);

        @Override public void onTick() {
            if (mc.world == null || mc.player == null) return;
            Set<Integer> now = new HashSet<>();
            for (Entity e : mc.world.getEntities()) {
                if (e instanceof PlayerEntity || !e.isAlive() || !want(e)) continue;
                now.add(e.getId());
                if (!e.isGlowing()) e.setGlowing(true);
            }
            for (int id : marked) {
                if (now.contains(id)) continue;
                Entity e = mc.world.getEntityById(id);
                if (e != null) e.setGlowing(false);
            }
            marked.clear(); marked.addAll(now);
        }

        @Override public void onDisable() {
            if (mc.world != null)
                for (int id : marked) { Entity e = mc.world.getEntityById(id); if (e != null) e.setGlowing(false); }
            marked.clear();
        }
    }

    public static class MobEsp extends GlowModule {
        public final Setting.Bool hostile = add(new Setting.Bool("Hostile mobs", true));
        public final Setting.Bool passive = add(new Setting.Bool("Passive mobs", false));
        public final Setting.Num range = add(new Setting.Num("Range", 48, 8, 128, 4));
        public MobEsp() { super("Mob ESP", "Outline on mobs through walls (never players)", Category.VISUAL); }
        @Override protected boolean want(Entity e) {
            if (!(e instanceof MobEntity)) return false;
            if (mc.player.squaredDistanceTo(e) > range.value * range.value) return false;
            return e instanceof Monster ? hostile.value : passive.value;
        }
    }

    public static class ItemEsp extends GlowModule {
        public final Setting.Num range = add(new Setting.Num("Range", 32, 8, 96, 4));
        public ItemEsp() { super("Item ESP", "Outline on dropped items", Category.VISUAL); }
        @Override protected boolean want(Entity e) {
            return e instanceof ItemEntity && mc.player.squaredDistanceTo(e) <= range.value * range.value;
        }
    }

    // ----------------------------------------------------------- Time / weather
    public static class TimeChanger extends Module {
        public final Setting.Num time = add(new Setting.Num("Time (0=dawn 6000=noon 18000=midnight)", 6000, 0, 24000, 500));
        public TimeChanger() { super("Time Changer", "Client-side time of day", Category.VISUAL); }
        @Override public void onTick() {
            if (mc.world != null) mc.world.getLevelProperties().setTimeOfDay((long) time.value);
        }
    }

    public static class ClearWeather extends Module {
        public ClearWeather() { super("Clear Weather", "Hides rain and thunder (visual)", Category.VISUAL); }
        @Override public void onTick() {
            if (mc.world != null) { mc.world.setRainGradient(0f); mc.world.setThunderGradient(0f); }
        }
    }
}
