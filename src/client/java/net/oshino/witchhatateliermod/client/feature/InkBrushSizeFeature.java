package net.oshino.witchhatateliermod.client.feature;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;

public final class InkBrushSizeFeature {
	private static final int MIN_SIZE = 1;
	private static final int MAX_SIZE = 64;
	private static int size = MIN_SIZE;

	private InkBrushSizeFeature() {
	}

	public static int getSize() {
		return size;
	}

	public static boolean handleScroll(double verticalAmount) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (verticalAmount == 0.0
				|| client.currentScreen != null
				|| client.player == null
				|| !Screen.hasShiftDown()
				|| !isHoldingInkSac(client)) {
			return false;
		}

		int change = verticalAmount > 0.0 ? 1 : -1;
		size = MathHelper.clamp(size + change, MIN_SIZE, MAX_SIZE);
		client.player.sendMessage(Text.literal("Ink brush: " + size + " × " + size + " pixels"), true);
		return true;
	}

	private static boolean isHoldingInkSac(MinecraftClient client) {
        assert client.player != null;
        return client.player.getStackInHand(Hand.MAIN_HAND).isOf(Items.INK_SAC)
				|| client.player.getStackInHand(Hand.OFF_HAND).isOf(Items.INK_SAC);
	}
}
