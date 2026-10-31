package com.zenith.client.hud;

import com.zenith.client.module.Category;
import com.zenith.client.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ArmorHud extends HudModule {
    public ArmorHud() { super("Armor Status", "Armor + held item durability", Category.PVP, -1, 70); }

    @Override protected void draw(DrawContext ctx) {
        List<ItemStack> list = new ArrayList<>();
        for (int i = 3; i >= 0; i--) {
            ItemStack s = mc.player.getInventory().armor.get(i);
            if (!s.isEmpty()) list.add(s);
        }
        ItemStack main = mc.player.getMainHandStack();
        if (!main.isEmpty()) list.add(main);
        if (list.isEmpty()) { placeholder(ctx, 64, 18); return; }

        box(ctx, 64, list.size() * 18 + 2);
        int y = 2;
        for (ItemStack s : list) {
            ctx.drawItem(s, 3, y);
            ctx.drawItemInSlot(mc.textRenderer, s, 3, y);
            if (s.isDamageable()) {
                int max = s.getMaxDamage();
                int pct = (max - s.getDamage()) * 100 / Math.max(1, max);
                int col = RenderUtil.lerp(0xFFFF4040, 0xFF55FF55, pct / 100f);
                RenderUtil.text(ctx, pct + "%", 24, y + 5, col);
            }
            y += 18;
        }
    }
}
