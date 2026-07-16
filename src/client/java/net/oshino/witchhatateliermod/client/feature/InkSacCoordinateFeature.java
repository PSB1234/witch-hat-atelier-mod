package net.oshino.witchhatateliermod.client.feature;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;

public final class InkSacCoordinateFeature {
	private InkSacCoordinateFeature() {
	}

	public static void register() {
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (!world.isClient || !player.getStackInHand(hand).isOf(Items.INK_SAC)) {
				return ActionResult.PASS;
			}

			BakedModelPixelFeature.findPixels(
					MinecraftClient.getInstance(), hitResult, InkBrushSizeFeature.getSize()
			).forEach(BlackPixelRenderer::addPixel);

			return ActionResult.PASS;
		});
	}
}
