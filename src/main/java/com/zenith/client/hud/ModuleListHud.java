package com.zenith.client.hud;

import com.zenith.client.ZenithClient;
import com.zenith.client.module.Category;
import com.zenith.client.module.Module;
import com.zenith.client.util.RenderUtil;
import com.zenith.client.util.Theme;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Right-aligned list of enabled modules with rainbow wave */
public class ModuleListHud extends HudModule {
    public ModuleListHud() { super("Module List", "List of active modules (top right)", Category.INFO, -1, 4); }

    @Override public boolean draggable() { return false; }

    @Override protected void draw(DrawContext ctx) {
        List<Module> l = new ArrayList<>();
        for (Module m : ZenithClient.modules.all)
            if (m.enabled && m != this && m.category != Category.SETTINGS) l.add(m);
        l.sort(Comparator.comparingInt((Module m) -> -RenderUtil.width(m.name)));
        int mw = 20;
        for (Module m : l) mw = Math.max(mw, RenderUtil.width(m.name) + 10);
        w = mw; h = Math.max(12, l.size() * 11 + 2);
        int i = 0;
        for (Module m : l) {
            int tw = RenderUtil.width(m.name);
            int y = 1 + i * 11;
            int col = Theme.color(i * 0.7f);
            if (bg.value) ctx.fill(w - tw - 8, y - 1, w, y + 10, 0x80101018);
            ctx.fill(w - 2, y - 1, w, y + 10, col);
            RenderUtil.text(ctx, m.name, w - tw - 5, y, col);
            i++;
        }
        x = mc.getWindow().getScaledWidth() - (int) (w * scale.value) - 3;
    }
}
