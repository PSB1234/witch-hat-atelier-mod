package net.oshino.witchhatateliermod.client.mixin;

import net.minecraft.client.Mouse;
import net.oshino.witchhatateliermod.client.feature.InkBrushSizeFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {
	@Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
	private void witchHatAtelierMod$changeInkBrushSize(
			long window,
			double horizontal,
			double vertical,
			CallbackInfo info
	) {
		if (InkBrushSizeFeature.handleScroll(vertical)) {
			info.cancel();
		}
	}
}
