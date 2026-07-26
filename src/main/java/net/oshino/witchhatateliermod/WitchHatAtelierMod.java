package net.oshino.witchhatateliermod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.Hand;
import net.oshino.witchhatateliermod.item.ModItems;
import net.oshino.witchhatateliermod.network.PaperSavePayload;
import net.oshino.witchhatateliermod.paper.PaperDocumentItemData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WitchHatAtelierMod implements ModInitializer {
	public static final String MOD_ID = "witch-hat-atelier-mod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static final String RECEIVED_SPELLBOOK_TAG = MOD_ID + ".received_spellbook";

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.playC2S().register(PaperSavePayload.ID, PaperSavePayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(PaperSavePayload.ID, (payload, context) -> {
			Hand hand = payload.mainHand() ? Hand.MAIN_HAND : Hand.OFF_HAND;
			ItemStack heldPaper = context.player().getStackInHand(hand);
			if (!heldPaper.isOf(Items.PAPER)) {
				return;
			}

			// A customized stack must contain one paper, leaving the untouched papers stackable.
			if (heldPaper.getCount() > 1) {
				ItemStack remainder = heldPaper.copy();
				remainder.decrement(1);
				heldPaper = heldPaper.copyWithCount(1);
				context.player().setStackInHand(hand, heldPaper);
				if (!context.player().getInventory().insertStack(remainder)) {
					context.player().dropItem(remainder, false);
				}
			}

			String title = payload.title().trim();
			if (title.isEmpty()) {
				title = PaperDocumentItemData.read(heldPaper).map(PaperDocumentItemData.Document::title)
						.filter(existing -> !existing.isBlank()).orElse("Untitled drawing");
			}
			PaperDocumentItemData.write(heldPaper, title, payload.drawing());
		});
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
