package net.oshino.witchhatateliermod.client.screen.paper;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.oshino.witchhatateliermod.client.screen.PaperScreen;
import org.lwjgl.glfw.GLFW;

public class PaperKeybinds {

  private static final KeyBinding cycleHardnessKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
      "key.witch-hat-atelier-mod.paper.hardness", InputUtil.Type.KEYSYM,
      GLFW.GLFW_KEY_H, "key.category.witch-hat-atelier-mod"));
  private static final KeyBinding cycleSmoothingKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
      "key.witch-hat-atelier-mod.paper.smoothing", InputUtil.Type.KEYSYM,
      GLFW.GLFW_KEY_M, "key.category.witch-hat-atelier-mod"));
  private static final MinecraftClient  client = MinecraftClient.getInstance();
  private static final PaperWorkspace paperWorkspace = new PaperWorkspace(client.runDirectory.toPath());



  public static void register() {
    ClientTickEvents.END_CLIENT_TICK.register(tickingClient -> {
      paperWorkspace.tick();
      while (cycleHardnessKey.wasPressed()) {
        if (tickingClient.currentScreen instanceof PaperScreen paperScreen) {
          paperScreen.cycleBrushHardness();
        }
      }
      while (cycleSmoothingKey.wasPressed()) {
        if (tickingClient.currentScreen instanceof PaperScreen paperScreen) {
          paperScreen.cycleBrushSmoothing();
        }
      }
    });
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
  }
}
