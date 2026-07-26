package net.oshino.witchhatateliermod.client;

import net.fabricmc.api.ClientModInitializer;
import net.oshino.witchhatateliermod.client.feature.SpellBookFeature;
import net.oshino.witchhatateliermod.client.feature.BlackPixelRenderer;
import net.oshino.witchhatateliermod.client.feature.InkSacCoordinateFeature;
import net.oshino.witchhatateliermod.client.screen.paper.PaperKeybinds;

public class WitchHatAtelierModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
    //Used for adding Drawing feature on overworld to ink sack (temporary placeholder for ink wands).
		InkSacCoordinateFeature.register();
    //Used for adding black pixel over the texture of the block where player interacts using wand black.
		BlackPixelRenderer.register();
    //temporary keybinds
    PaperKeybinds.register();
    //register spellbook screen to spellbook
    SpellBookFeature.register();
	}
}
