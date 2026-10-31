package com.zenith.client.util;

import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.Deque;

/** Frame-accurate CPS counter (polls mouse every frame) */
public final class ClickTracker {
    private static final Deque<Long> L = new ArrayDeque<>(), R = new ArrayDeque<>();
    private static boolean ld, rd;

    public static void poll() {
        MinecraftClient mc = MinecraftClient.getInstance();
        long h = mc.getWindow().getHandle();
        long now = System.currentTimeMillis();
        boolean l = mc.currentScreen == null && GLFW.glfwGetMouseButton(h, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean r = mc.currentScreen == null && GLFW.glfwGetMouseButton(h, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        if (l && !ld) L.add(now);
        if (r && !rd) R.add(now);
        ld = l; rd = r;
        while (!L.isEmpty() && now - L.peekFirst() > 1000) L.pollFirst();
        while (!R.isEmpty() && now - R.peekFirst() > 1000) R.pollFirst();
    }

    public static int left() { return L.size(); }
    public static int right() { return R.size(); }
    public static boolean leftDown() { return ld; }
    public static boolean rightDown() { return rd; }
}
