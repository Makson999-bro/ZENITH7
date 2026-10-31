package com.zenith.client.mixin;

import com.zenith.client.ZenithClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    // Hide vanilla crosshair when Zenith custom crosshair is on
    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void zenith$crosshair(DrawContext context, CallbackInfo ci) {
        if (ZenithClient.modules != null && ZenithClient.modules.crosshair.enabled) ci.cancel();
    }
}
