package com.zenith.client.gui;

import com.zenith.client.ZenithClient;
import com.zenith.client.hud.HudModule;
import com.zenith.client.module.Category;
import com.zenith.client.module.Module;
import com.zenith.client.module.Setting;
import com.zenith.client.util.Config;
import com.zenith.client.util.RenderUtil;
import com.zenith.client.util.Theme;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ClickGuiScreen extends Screen {
    private static final int SIDE = 108, ROW = 30;
    private static int offX, offY;

    private Category cat = Category.INFO;
    private final float[] catAnim = new float[Category.values().length];
    private float scroll, targetScroll, maxScroll;
    private final long opened = System.currentTimeMillis();

    private HudModule dragging;
    private int dragX, dragY;
    private boolean draggingPanel;
    private Setting.Num sliding;
    private int slideX, slideW;

    private final List<Hit> hits = new ArrayList<>();
    private int px, py, pw, ph, cx, cw, listTop, listBottom;

    private record Hit(int x, int y, int w, int h, Module m, Setting s, int type) {
        boolean in(double mx, double my) { return mx >= x && mx < x + w && my >= y && my < y + h; }
    }

    public ClickGuiScreen() { super(Text.literal("Zenith")); }

    @Override public boolean shouldPause() { return false; }

    // ------------------------------------------------------------------ render
    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        float t = Math.min(1f, (System.currentTimeMillis() - opened) / 240f);
        float e = 1f - (1f - t) * (1f - t) * (1f - t);
        var mm = ZenithClient.modules;
        int acc = Theme.accent();

        ctx.fillGradient(0, 0, width, height, RenderUtil.alpha(0x05050F, (int) (110 * e)), RenderUtil.alpha(0x2A0A4A, (int) (150 * e)));

        // outlines around draggable HUD widgets
        for (Module m : mm.all) {
            if (m instanceof HudModule h && h.enabled && h.draggable()) {
                int ws = (int) (h.w * h.scale.value), hs = (int) (h.h * h.scale.value);
                RenderUtil.outline(ctx, h.x - 1, h.y - 1, ws + 2, hs + 2, h == dragging ? acc : 0x70FFFFFF);
            }
        }

        pw = Math.min(440, width - 30);
        ph = Math.min(280, height - 30);
        px = (width - pw) / 2 + offX;
        py = (height - ph) / 2 + offY + (int) ((1f - e) * 20);

        // window
        RenderUtil.rrect(ctx, px - 2, py - 2, pw + 4, ph + 4, 8, RenderUtil.alpha(acc, (int) (70 * e)));
        RenderUtil.rrect(ctx, px, py, pw, ph, 7, RenderUtil.alpha(0x0B0B12, (int) (245 * e)));

        // sidebar
        RenderUtil.rrect(ctx, px + 5, py + 5, SIDE, ph - 10, 6, 0xFF11111B);
        MatrixStack ms = ctx.getMatrices();
        ms.push();
        ms.translate(px + 17, py + 15, 0);
        ms.scale(1.6f, 1.6f, 1f);
        String logo = "ZENITH";
        int lx = 0;
        for (int i = 0; i < logo.length(); i++) {
            String c = String.valueOf(logo.charAt(i));
            RenderUtil.text(ctx, c, lx, 0, Theme.color(i * 0.8f));
            lx += RenderUtil.width(c);
        }
        ms.pop();
        RenderUtil.text(ctx, "PvP CLIENT  v1.0", px + 17, py + 34, 0xFF6A6A88);

        Category[] cats = Category.values();
        for (int i = 0; i < cats.length; i++) {
            int y = py + 52 + i * 26;
            boolean sel = cats[i] == cat;
            boolean hov = mx >= px + 9 && mx < px + SIDE + 1 && my >= y && my < y + 22;
            catAnim[i] += ((sel ? 1f : 0f) - catAnim[i]) * 0.25f;
            RenderUtil.rrect(ctx, px + 9, y, SIDE - 8, 22, 5, hov ? 0xFF1A1A28 : 0xFF11111B);
            if (catAnim[i] > 0.02f) {
                RenderUtil.rrect(ctx, px + 9, y, SIDE - 8, 22, 5, RenderUtil.alpha(acc, (int) (70 * catAnim[i])));
                ctx.fill(px + 9, y + 5, px + 11, y + 17, RenderUtil.alpha(acc, (int) (255 * catAnim[i])));
            }
            RenderUtil.text(ctx, cats[i].label, px + 20, y + 7, RenderUtil.lerp(0xFF9090A8, 0xFFFFFFFF, catAnim[i]));
        }
        RenderUtil.text(ctx, "§7LMB §8toggle", px + 17, py + ph - 40, 0xFFFFFFFF);
        RenderUtil.text(ctx, "§7RMB §8settings", px + 17, py + ph - 29, 0xFFFFFFFF);
        RenderUtil.text(ctx, "§7Drag §8HUD to move", px + 17, py + ph - 18, 0xFFFFFFFF);

        // content header
        cx = px + SIDE + 16;
        cw = pw - SIDE - 26;
        ms.push();
        ms.translate(cx, py + 12, 0);
        ms.scale(1.3f, 1.3f, 1f);
        RenderUtil.text(ctx, cat.label, 0, 0, 0xFFFFFFFF);
        ms.pop();
        ctx.fill(cx, py + 27, cx + 26, py + 28, acc);
        List<Module> list = mm.byCategory(cat);
        String cnt = list.size() + " modules";
        RenderUtil.text(ctx, cnt, cx + cw - RenderUtil.width(cnt), py + 14, 0xFF6A6A88);

        // modules
        listTop = py + 36;
        listBottom = py + ph - 10;
        hits.clear();
        ctx.enableScissor(cx - 4, listTop, cx + cw + 4, listBottom);
        int start = listTop - (int) scroll + 2;
        int y = start;
        for (Module m : list) y = drawModule(ctx, m, y, mx, my);
        ctx.disableScissor();

        maxScroll = Math.max(0, (y - start) - (listBottom - listTop) + 6);
        targetScroll = Math.max(0, Math.min(maxScroll, targetScroll));
        scroll += (targetScroll - scroll) * 0.3f;
        if (maxScroll > 0) {
            int trackH = listBottom - listTop;
            int barH = Math.max(14, (int) (trackH * (trackH / (trackH + maxScroll))));
            int barY = listTop + (int) ((trackH - barH) * (scroll / maxScroll));
            RenderUtil.rrect(ctx, cx + cw + 5, barY, 2, barH, 1, RenderUtil.alpha(acc, 160));
        }
    }

    private int drawModule(DrawContext ctx, Module m, int y, int mx, int my) {
        int acc = Theme.accent();
        m.anim += ((m.enabled ? 1f : 0f) - m.anim) * 0.25f;
        boolean hov = mx >= cx && mx < cx + cw && my >= y && my < y + ROW && my >= listTop && my < listBottom;

        RenderUtil.rrect(ctx, cx, y, cw, ROW, 5, hov ? 0xFF1D1D2B : 0xFF161620);
        if (m.anim > 0.02f) {
            RenderUtil.rrect(ctx, cx, y, cw, ROW, 5, RenderUtil.alpha(acc, (int) (34 * m.anim)));
            ctx.fill(cx, y + 7, cx + 2, y + ROW - 7, RenderUtil.alpha(acc, (int) (255 * m.anim)));
        }
        RenderUtil.text(ctx, m.name, cx + 10, y + 6, 0xFFFFFFFF);
        RenderUtil.text(ctx, m.description, cx + 10, y + 17, 0xFF80809A);
        if (!m.locked) RenderUtil.toggle(ctx, cx + cw - 34, y + 9, 24, 12, m.anim);
        if (!m.settings.isEmpty())
            RenderUtil.text(ctx, m.expanded ? "-" : "+", cx + cw - (m.locked ? 14 : 48), y + 11, 0xFF9090A8);
        hits.add(new Hit(cx, y, cw, ROW, m, null, 0));
        y += ROW + 2;

        if (m.expanded && !m.settings.isEmpty()) {
            int total = 0;
            for (Setting s : m.settings) total += s instanceof Setting.Num ? 26 : 20;
            RenderUtil.rrect(ctx, cx + 4, y, cw - 8, total + 6, 4, 0xFF10101A);
            int sy = y + 3;
            for (Setting s : m.settings) {
                if (s instanceof Setting.Bool b) {
                    b.anim += ((b.value ? 1f : 0f) - b.anim) * 0.25f;
                    RenderUtil.text(ctx, b.name, cx + 12, sy + 6, 0xFFC8C8D8);
                    RenderUtil.toggle(ctx, cx + cw - 32, sy + 5, 20, 10, b.anim);
                    hits.add(new Hit(cx + 4, sy, cw - 8, 20, m, b, 1));
                    sy += 20;
                } else if (s instanceof Setting.Num n) {
                    String val = n.step >= 1 ? String.valueOf((int) n.value) : String.format(Locale.ROOT, "%.1f", n.value);
                    RenderUtil.text(ctx, n.name, cx + 12, sy + 3, 0xFFC8C8D8);
                    RenderUtil.text(ctx, val, cx + cw - 12 - RenderUtil.width(val), sy + 3, acc);
                    int tx = cx + 12, tw = cw - 24, ty = sy + 16;
                    int fw = (int) (tw * (n.value - n.min) / (n.max - n.min));
                    RenderUtil.rrect(ctx, tx, ty, tw, 4, 2, 0xFF22222F);
                    RenderUtil.rrect(ctx, tx, ty, Math.max(4, fw), 4, 2, acc);
                    RenderUtil.rrect(ctx, tx + Math.max(0, fw - 3), ty - 2, 6, 8, 3, 0xFFFFFFFF);
                    hits.add(new Hit(tx, ty - 5, tw, 14, m, n, 2));
                    sy += 26;
                }
            }
            y += total + 6 + 2;
        }
        return y;
    }

    // ------------------------------------------------------------------- input
    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        boolean inside = mx >= px && mx < px + pw && my >= py && my < py + ph;
        if (!inside) {
            if (btn == 0) {
                List<Module> l = ZenithClient.modules.all;
                for (int i = l.size() - 1; i >= 0; i--) {
                    if (l.get(i) instanceof HudModule h && h.enabled && h.hit(mx, my)) {
                        dragging = h; dragX = (int) mx - h.x; dragY = (int) my - h.y;
                        return true;
                    }
                }
            }
            return false;
        }
        Category[] cats = Category.values();
        for (int i = 0; i < cats.length; i++) {
            int y = py + 52 + i * 26;
            if (mx >= px + 9 && mx < px + SIDE + 1 && my >= y && my < y + 22) {
                cat = cats[i]; scroll = 0; targetScroll = 0;
                return true;
            }
        }
        if (btn == 0 && my < py + 34) { draggingPanel = true; return true; }

        if (my >= listTop && my < listBottom) {
            for (int i = hits.size() - 1; i >= 0; i--) {
                Hit h = hits.get(i);
                if (!h.in(mx, my)) continue;
                switch (h.type) {
                    case 0 -> {
                        Module m = h.m;
                        boolean zone = !m.settings.isEmpty() && mx >= cx + cw - 56 && mx < cx + cw - 38;
                        if (btn == 1 || zone || m.locked) { if (!m.settings.isEmpty()) m.expanded = !m.expanded; }
                        else if (btn == 0) m.toggle();
                    }
                    case 1 -> { if (btn == 0 && h.s instanceof Setting.Bool b) b.value = !b.value; }
                    case 2 -> {
                        if (btn == 0 && h.s instanceof Setting.Num n) { sliding = n; slideX = h.x; slideW = h.w; slide(mx); }
                    }
                    default -> {}
                }
                return true;
            }
        }
        return true;
    }

    private void slide(double mx) {
        if (sliding == null) return;
        sliding.set(sliding.min + (mx - slideX) / slideW * (sliding.max - sliding.min));
    }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        if (sliding != null) { slide(mx); return true; }
        if (draggingPanel) { offX += (int) dx; offY += (int) dy; return true; }
        if (dragging != null) {
            int ws = (int) (dragging.w * dragging.scale.value), hs = (int) (dragging.h * dragging.scale.value);
            int nx = Math.max(0, Math.min(width - ws, (int) mx - dragX));
            int ny = Math.max(0, Math.min(height - hs, (int) my - dragY));
            // snapping to edges / center
            if (Math.abs(nx) < 5) nx = 0;
            if (Math.abs(nx + ws - width) < 5) nx = width - ws;
            if (Math.abs(nx + ws / 2 - width / 2) < 4) nx = width / 2 - ws / 2;
            if (Math.abs(ny) < 5) ny = 0;
            if (Math.abs(ny + hs - height) < 5) ny = height - hs;
            if (Math.abs(ny + hs / 2 - height / 2) < 4) ny = height / 2 - hs / 2;
            dragging.x = nx; dragging.y = ny;
            return true;
        }
        return super.mouseDragged(mx, my, btn, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int btn) {
        dragging = null; sliding = null; draggingPanel = false;
        return super.mouseReleased(mx, my, btn);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double amount) {
        targetScroll = Math.max(0, Math.min(maxScroll, targetScroll - (float) amount * 24f));
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) { close(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override public void removed() { Config.save(); }
}
