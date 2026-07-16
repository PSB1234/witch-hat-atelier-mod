package net.oshino.witchhatateliermod.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.util.TypedActionResult;
import net.oshino.witchhatateliermod.client.screen.SpellbookScreen;
import net.oshino.witchhatateliermod.client.feature.BlackPixelRenderer;
import net.oshino.witchhatateliermod.client.feature.InkSacCoordinateFeature;
import net.oshino.witchhatateliermod.item.ModItems;

public class WitchHatAtelierModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		InkSacCoordinateFeature.register();
		BlackPixelRenderer.register();
		UseItemCallback.EVENT.register((player, world, hand) -> {
			if (world.isClient && player.getStackInHand(hand).isOf(ModItems.SPELLBOOK)) {
				net.minecraft.client.MinecraftClient.getInstance().setScreen(new SpellbookScreen());
				return TypedActionResult.success(player.getStackInHand(hand), true);
			}
			return TypedActionResult.pass(player.getStackInHand(hand));
		});
	}
}
