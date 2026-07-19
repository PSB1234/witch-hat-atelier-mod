package net.oshino.witchhatateliermod.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.oshino.witchhatateliermod.client.screen.PaperScreen;
import net.oshino.witchhatateliermod.client.screen.SpellbookScreen;
import net.oshino.witchhatateliermod.client.screen.paper.PaperWorkspace;
import net.oshino.witchhatateliermod.client.feature.BlackPixelRenderer;
import net.oshino.witchhatateliermod.client.feature.InkSacCoordinateFeature;
import net.oshino.witchhatateliermod.item.ModItems;

public class WitchHatAtelierModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		InkSacCoordinateFeature.register();
		BlackPixelRenderer.register();
		MinecraftClient client = MinecraftClient.getInstance();
		PaperWorkspace paperWorkspace = new PaperWorkspace(client.runDirectory.toPath());
		ClientTickEvents.END_CLIENT_TICK.register(ignored -> paperWorkspace.tick());
		ClientLifecycleEvents.CLIENT_STOPPING.register(ignored -> paperWorkspace.saveOnShutdown());
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (world.isClient
					&& !player.isSpectator()
					&& player.getStackInHand(hand).isOf(Items.PAPER)
					&& world.getBlockState(hitResult.getBlockPos()).isOf(Blocks.LECTERN)) {
				MinecraftClient.getInstance().setScreen(new PaperScreen(paperWorkspace));
				return ActionResult.SUCCESS;
			}
			return ActionResult.PASS;
		});
		UseItemCallback.EVENT.register((player, world, hand) -> {
			if (world.isClient && player.getStackInHand(hand).isOf(ModItems.SPELLBOOK)) {
				MinecraftClient.getInstance().setScreen(new SpellbookScreen());
				return TypedActionResult.success(player.getStackInHand(hand), true);
			}
			return TypedActionResult.pass(player.getStackInHand(hand));
		});
	}
}
