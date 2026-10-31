package com.zenith.client.util;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zenith.client.ZenithClient;
import com.zenith.client.hud.HudModule;
import com.zenith.client.module.Module;
import com.zenith.client.module.Setting;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public final class Config {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("zenith.json");

    public static void save() {
        try {
            JsonObject root = new JsonObject();
            for (Module m : ZenithClient.modules.all) {
                JsonObject o = new JsonObject();
                o.addProperty("enabled", m.enabled);
                if (m instanceof HudModule h) { o.addProperty("x", h.x); o.addProperty("y", h.y); }
                for (Setting s : m.settings) {
                    if (s instanceof Setting.Bool b) o.addProperty("s:" + b.name, b.value);
                    else if (s instanceof Setting.Num n) o.addProperty("s:" + n.name, n.value);
                }
                root.add(m.name, o);
            }
            Files.writeString(FILE, new GsonBuilder().setPrettyPrinting().create().toJson(root));
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void load() {
        if (!Files.exists(FILE)) return;
        try {
            JsonObject root = JsonParser.parseString(Files.readString(FILE)).getAsJsonObject();
            for (Module m : ZenithClient.modules.all) {
                if (!root.has(m.name)) continue;
                JsonObject o = root.getAsJsonObject(m.name);
                if (!m.locked && o.has("enabled")) m.enabled = o.get("enabled").getAsBoolean();
                m.anim = m.enabled ? 1f : 0f;
                if (m instanceof HudModule h) {
                    if (o.has("x")) h.x = o.get("x").getAsInt();
                    if (o.has("y")) h.y = o.get("y").getAsInt();
                }
                for (Setting s : m.settings) {
                    String k = "s:" + s.name;
                    if (!o.has(k)) continue;
                    if (s instanceof Setting.Bool b) { b.value = o.get(k).getAsBoolean(); b.anim = b.value ? 1f : 0f; }
                    else if (s instanceof Setting.Num n) n.set(o.get(k).getAsDouble());
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}
