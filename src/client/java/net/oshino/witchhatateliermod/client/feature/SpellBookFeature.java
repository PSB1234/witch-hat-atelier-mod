package net.oshino.witchhatateliermod.client.feature;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.TypedActionResult;
import net.oshino.witchhatateliermod.client.screen.SpellbookScreen;
import net.oshino.witchhatateliermod.item.ModItems;

public class SpellBookFeature {
  public static void register(){
    UseItemCallback.EVENT.register((player, world, hand) -> {
      if (world.isClient && player.getStackInHand(hand).isOf(ModItems.SPELLBOOK)) {
        MinecraftClient.getInstance().setScreen(new SpellbookScreen());
        return TypedActionResult.success(player.getStackInHand(hand), true);
      }
      return TypedActionResult.pass(player.getStackInHand(hand));
    });
  }
}
