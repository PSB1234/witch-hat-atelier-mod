package net.oshino.witchhatateliermod.client;

import net.fabricmc.api.ClientModInitializer;
import net.oshino.witchhatateliermod.client.feature.BlackPixelRenderer;
import net.oshino.witchhatateliermod.client.feature.InkSacCoordinateFeature;

public class WitchHatAtelierModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		InkSacCoordinateFeature.register();
		BlackPixelRenderer.register();
	}
}
