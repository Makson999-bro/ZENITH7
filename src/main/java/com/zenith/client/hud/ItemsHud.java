package com.zenith.client.hud;

import com.zenith.client.module.Category;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.List;

/** Counts totems / gapples / pearls / crystals / obsidian in inventory */
public class ItemsHud extends HudModule {
    private static final Item[] ITEMS = {Items.TOTEM_OF_UNDYING, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE,
            Items.ENDER_PEARL, Items.END_CRYSTAL, Items.OBSIDIAN, Items.RESPAWN_ANCHOR, Items.GLOWSTONE};

    public ItemsHud() { super("PvP Items", "Totems, gapples, pearls, crystals...", Category.PVP, -2, -1); }

    @Override protected void draw(DrawContext ctx) {
        List<ItemStack> list = new ArrayList<>();
        for (Item it : ITEMS) {
            int n = mc.player.getInventory().count(it);
            if (n > 0) list.add(new ItemStack(it, n));
        }
        if (list.isEmpty()) { placeholder(ctx, 80, 22); return; }
        box(ctx, list.size() * 18 + 4, 22);
        for (int i = 0; i < list.size(); i++) {
            ctx.drawItem(list.get(i), 3 + i * 18, 3);
            ctx.drawItemInSlot(mc.textRenderer, list.get(i), 3 + i * 18, 3);
        }
    }
}
