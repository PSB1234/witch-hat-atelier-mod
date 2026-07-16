package net.oshino.witchhatateliermod.client.feature;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class BlackPixelRenderer {
	private static final double SURFACE_OFFSET = 0.001;
	private static final Map<PixelKey, BakedModelPixelFeature.ModelPixelHit> PIXELS = new LinkedHashMap<>();

	private BlackPixelRenderer() {
	}

	public static void register() {
		WorldRenderEvents.AFTER_ENTITIES.register(context -> {
			MatrixStack matrices = context.matrixStack();
			VertexConsumerProvider consumers = context.consumers();
			if (matrices == null || consumers == null || PIXELS.isEmpty()) {
				return;
			}

			Vec3d camera = context.camera().getPos();
			Matrix4f matrix = matrices.peek().getPositionMatrix();
			VertexConsumer vertices = consumers.getBuffer(RenderLayer.getDebugQuads());
			for (BakedModelPixelFeature.ModelPixelHit pixel : PIXELS.values()) {
				renderPixel(vertices, matrix, camera, pixel);
			}
		});
	}

	public static void addPixel(BakedModelPixelFeature.ModelPixelHit pixel) {
		PixelKey key = new PixelKey(
				pixel.blockPos(), pixel.quadIndex(), pixel.textureId(), pixel.pixelX(), pixel.pixelY()
		);
		PIXELS.put(key, pixel);
	}

	private static void renderPixel(
			VertexConsumer vertices,
			Matrix4f matrix,
			Vec3d camera,
			BakedModelPixelFeature.ModelPixelHit pixel
	) {
		Vec3d offset = Vec3d.of(pixel.face().getVector()).multiply(SURFACE_OFFSET);
		for (List<Vec3d> polygon : pixel.polygons()) {
			Vec3d first = polygon.getFirst().add(offset).subtract(camera);
			for (int index = 1; index < polygon.size() - 1; index++) {
				Vec3d second = polygon.get(index).add(offset).subtract(camera);
				Vec3d third = polygon.get(index + 1).add(offset).subtract(camera);
				// Debug quads use four vertices; repeating the final vertex produces one triangle.
				vertex(vertices, matrix, first);
				vertex(vertices, matrix, second);
				vertex(vertices, matrix, third);
				vertex(vertices, matrix, third);
			}
		}
	}

	private static void vertex(VertexConsumer vertices, Matrix4f matrix, Vec3d point) {
		vertices.vertex(matrix, (float) point.x, (float) point.y, (float) point.z)
				.color(0, 0, 0, 255);
	}

	private record PixelKey(
			BlockPos blockPos,
			int quadIndex,
			Identifier textureId,
			int pixelX,
			int pixelY
	) {
	}
}
