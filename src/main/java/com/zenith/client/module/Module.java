package com.zenith.client.module;

import com.zenith.client.util.Notifications;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    protected static final MinecraftClient mc = MinecraftClient.getInstance();

    public final String name, description;
    public final Category category;
    public boolean enabled, expanded, locked;
    public float anim;
    public final List<Setting> settings = new ArrayList<>();

    protected Module(String name, String description, Category category) {
        this.name = name; this.description = description; this.category = category;
    }

    protected <T extends Setting> T add(T s) { settings.add(s); return s; }

    public void toggle() {
        if (locked) return;
        setEnabled(!enabled);
        Notifications.toggled(name, enabled);
    }

    public void setEnabled(boolean e) {
        if (enabled == e) return;
        enabled = e;
        if (e) onEnable(); else onDisable();
    }

    public void onEnable() {}
    public void onDisable() {}
    public void onTick() {}
    /** For non-HUD overlays (crosshair etc.) */
    public void renderOverlay(DrawContext ctx) {}
}
