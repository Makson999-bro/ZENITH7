package com.zenith.client.hud;

import com.zenith.client.module.Category;
import com.zenith.client.util.RenderUtil;
import com.zenith.client.util.Theme;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.List;

/** Ender pearl / gapple / chorus / shield cooldown bars */
public class CooldownHud extends HudModule {
    private static final Item[] ITEMS = {Items.ENDER_PEARL, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE,
            Items.CHORUS_FRUIT, Items.SHIELD, Items.TOTEM_OF_UNDYING};

    public CooldownHud() { super("Cooldowns", "Pearl / gapple / shield cooldown bars", Category.PVP, -1, 220); }

    @Override protected void draw(DrawContext ctx) {
        List<Item> active = new ArrayList<>();
        List<Float> prog = new ArrayList<>();
        for (Item it : ITEMS) {
            float p = mc.player.getItemCooldownManager().getCooldownProgress(it, 0f);
            if (p > 0f) { active.add(it); prog.add(p); }
        }
        if (active.isEmpty()) { placeholder(ctx, 100, 22); return; }
        box(ctx, 100, active.size() * 20 + 2);
        for (int i = 0; i < active.size(); i++) {
            int y = 2 + i * 20;
            ctx.drawItem(new ItemStack(active.get(i)), 3, y);
            int bx = 24, bw = 70;
            RenderUtil.rrect(ctx, bx, y + 7, bw, 4, 2, 0xFF22222F);
            RenderUtil.rrect(ctx, bx, y + 7, Math.max(3, (int) (bw * prog.get(i))), 4, 2, Theme.accent());
        }
    }
}
