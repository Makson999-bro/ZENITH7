package com.zenith.client;

import com.zenith.client.gui.ClickGuiScreen;
import com.zenith.client.module.ModuleManager;
import com.zenith.client.util.Config;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.ActionResult;
import org.lwjgl.glfw.GLFW;

public class ZenithClient implements ClientModInitializer {
    public static ModuleManager modules;
    public static KeyBinding guiKey, zoomKey;

    @Override
    public void onInitializeClient() {
        modules = new ModuleManager();
        Config.load();

        guiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.zenith.gui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.zenith"));
        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.zenith.zoom", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, "category.zenith"));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (guiKey.wasPressed()) {
                if (mc.currentScreen == null && mc.player != null) mc.setScreen(new ClickGuiScreen());
            }
            modules.tick(mc);
        });

        HudRenderCallback.EVENT.register((ctx, delta) -> modules.renderHud(ctx));

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClient) modules.onAttack(entity);
            return ActionResult.PASS;
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(c -> Config.save());
    }
}
