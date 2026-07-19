package net.oshino.witchhatateliermod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.oshino.witchhatateliermod.item.ModItems;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WitchHatAtelierMod implements ModInitializer {
	public static final String MOD_ID = "witch-hat-atelier-mod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static final String RECEIVED_SPELLBOOK_TAG = MOD_ID + ".received_spellbook";

	@Override
	public void onInitialize() {
		ModItems.register();
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (!world.isClient
					&& !player.isSpectator()
					&& player.getStackInHand(hand).isOf(Items.PAPER)
					&& world.getBlockState(hitResult.getBlockPos()).isOf(Blocks.LECTERN)) {
				return ActionResult.SUCCESS;
			}
			return ActionResult.PASS;
		});
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			var player = handler.getPlayer();
			if (player.getCommandTags().contains(RECEIVED_SPELLBOOK_TAG)) {
				return;
			}

			ItemStack spellbook = new ItemStack(ModItems.SPELLBOOK);
			if (!player.getInventory().insertStack(spellbook)) {
				player.dropItem(spellbook, false);
			}
			player.addCommandTag(RECEIVED_SPELLBOOK_TAG);
		});
		LOGGER.info("Witch Hat Atelier initialized");
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}
