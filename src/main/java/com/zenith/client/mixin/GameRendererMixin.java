package com.zenith.client.mixin;

import com.zenith.client.ZenithClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    // Smooth zoom
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void zenith$fov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> cir) {
        if (ZenithClient.modules == null) return;
        var zoom = ZenithClient.modules.zoom;
        if (zoom.enabled && zoom.active()) cir.setReturnValue(cir.getReturnValue() * zoom.fovFactor());
    }

    // No hurt camera shake
    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
    private void zenith$hurt(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (ZenithClient.modules != null && ZenithClient.modules.noHurtCam.enabled) ci.cancel();
    }
}
