package net.oshino.witchhatateliermod.client.feature;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class BakedModelPixelFeature {
	private static final int VERTEX_STRIDE = 8;
	private static final int POSITION_X = 0;
	private static final int POSITION_Y = 1;
	private static final int POSITION_Z = 2;
	private static final int TEXTURE_U = 4;
	private static final int TEXTURE_V = 5;
	private static final double RAY_EPSILON = 1.0E-7;

	public static Optional<ModelPixelHit> findPixel(MinecraftClient client, BlockHitResult blockHit) {
		List<ModelPixelHit> pixels = findPixels(client, blockHit, 1);
		return pixels.isEmpty() ? Optional.empty() : Optional.of(pixels.getFirst());
	}

	public static List<ModelPixelHit> findPixels(MinecraftClient client, BlockHitResult blockHit, int brushSize) {
		if (brushSize <= 0) {
			throw new IllegalArgumentException("brushSize must be greater than zero");
		}

		if (client.world == null) {
			return List.of();
		}

		BlockPos blockPos = blockHit.getBlockPos();
		BlockState state = client.world.getBlockState(blockPos);
		BakedModel model = client.getBlockRenderManager().getModel(state);
		List<BakedQuad> quads = getRenderedQuads(model, state, blockPos, client.world);

		Vec3d rayStart = client.gameRenderer.getCamera().getPos();
		Vec3d rayDirection = blockHit.getPos().subtract(rayStart).normalize();
		double maxDistance = rayStart.distanceTo(blockHit.getPos()) + 2.0;
		QuadIntersection nearest = null;

		for (int quadIndex = 0; quadIndex < quads.size(); quadIndex++) {
			BakedQuad quad = quads.get(quadIndex);
			QuadVertex[] vertices = readVertices(quad, blockPos);
			for (int[] triangle : List.of(new int[]{0, 1, 2}, new int[]{0, 2, 3})) {
				RayHit hit = intersectTriangle(
						rayStart,
						rayDirection,
						vertices[triangle[0]].position,
						vertices[triangle[1]].position,
						vertices[triangle[2]].position
				);
				if (hit == null || hit.distance > maxDistance || nearest != null && hit.distance >= nearest.distance) {
					continue;
				}

				double u = interpolate(
						vertices[triangle[0]].u, vertices[triangle[1]].u, vertices[triangle[2]].u, hit
				);
				double v = interpolate(
						vertices[triangle[0]].v, vertices[triangle[1]].v, vertices[triangle[2]].v, hit
				);
				nearest = new QuadIntersection(quadIndex, quad, vertices, hit.distance, u, v);
			}
		}

		if (nearest == null) {
			return List.of();
		}

		Sprite sprite = nearest.quad.getSprite();
		int textureWidth = sprite.getContents().getWidth();
		int textureHeight = sprite.getContents().getHeight();
		int centerX = MathHelper.clamp(
				MathHelper.floor(sprite.getFrameFromU((float) nearest.u) * textureWidth), 0, textureWidth - 1
		);
		int centerY = MathHelper.clamp(
				MathHelper.floor(sprite.getFrameFromV((float) nearest.v) * textureHeight), 0, textureHeight - 1
		);
		int startX = centerX - (brushSize - 1) / 2;
		int startY = centerY - (brushSize - 1) / 2;
		List<ModelPixelHit> pixels = new ArrayList<>();
		for (int pixelY = startY; pixelY < startY + brushSize; pixelY++) {
			for (int pixelX = startX; pixelX < startX + brushSize; pixelX++) {
				if (pixelX >= 0 && pixelX < textureWidth && pixelY >= 0 && pixelY < textureHeight) {
					ModelPixelHit pixel = createPixelHit(blockPos, nearest, pixelX, pixelY);
					if (!pixel.polygons().isEmpty()) {
						pixels.add(pixel);
					}
				} else {
					findOverflowPixel(
							client, blockPos, nearest, pixelX, pixelY, textureWidth, textureHeight
					).ifPresent(pixels::add);
				}
			}
		}
		return List.copyOf(pixels);
	}

	private static Optional<ModelPixelHit> findOverflowPixel(
			MinecraftClient client,
			BlockPos originalPos,
			QuadIntersection intersection,
			int pixelX,
			int pixelY,
			int textureWidth,
			int textureHeight
	) {
		Sprite sprite = intersection.quad.getSprite();
		double sampleU = sprite.getFrameU((float) (pixelX + 0.5) / textureWidth);
		double sampleV = sprite.getFrameV((float) (pixelY + 0.5) / textureHeight);
		Vec3d samplePosition = positionForUv(intersection.vertices, sampleU, sampleV);
		if (samplePosition == null) {
			return Optional.empty();
		}

		Direction face = intersection.quad.getFace();
		Vec3d inwardOffset = Vec3d.of(face.getOpposite().getVector()).multiply(0.01);
		BlockPos overflowPos = BlockPos.ofFloored(samplePosition.add(inwardOffset));
		if (overflowPos.equals(originalPos)) {
			return Optional.empty();
		}

		BlockHitResult overflowHit = new BlockHitResult(samplePosition, face, overflowPos, false);
		return findPixel(client, overflowHit);
	}

	private static Vec3d positionForUv(QuadVertex[] vertices, double u, double v) {
		Vec3d position = positionForUv(vertices[0], vertices[1], vertices[2], u, v);
		return position != null ? position : positionForUv(vertices[0], vertices[2], vertices[3], u, v);
	}

	private static Vec3d positionForUv(QuadVertex a, QuadVertex b, QuadVertex c, double u, double v) {
		double denominator = (b.v - c.v) * (a.u - c.u) + (c.u - b.u) * (a.v - c.v);
		if (Math.abs(denominator) < RAY_EPSILON) {
			return null;
		}

		double weightA = ((b.v - c.v) * (u - c.u) + (c.u - b.u) * (v - c.v)) / denominator;
		double weightB = ((c.v - a.v) * (u - c.u) + (a.u - c.u) * (v - c.v)) / denominator;
		double weightC = 1.0 - weightA - weightB;
		return a.position.multiply(weightA).add(b.position.multiply(weightB)).add(c.position.multiply(weightC));
	}

	private static List<BakedQuad> getRenderedQuads(
			BakedModel model,
			BlockState state,
			BlockPos pos,
			ClientWorld world
	) {
		List<BakedQuad> quads = new ArrayList<>();
		Random random = Random.create();
		long seed = state.getRenderingSeed(pos);
		for (Direction direction : Direction.values()) {
			BlockPos adjacentPos = pos.offset(direction);
			if (Block.shouldDrawSide(state, world, pos, direction, adjacentPos)) {
				random.setSeed(seed);
				quads.addAll(model.getQuads(state, direction, random));
			}
		}
		random.setSeed(seed);
		quads.addAll(model.getQuads(state, null, random));
		return quads;
	}

	private static ModelPixelHit createPixelHit(BlockPos blockPos, QuadIntersection intersection, int pixelX, int pixelY) {
		Sprite sprite = intersection.quad.getSprite();
		int textureWidth = sprite.getContents().getWidth();
		int textureHeight = sprite.getContents().getHeight();

		double minU = sprite.getFrameU((float) pixelX / textureWidth);
		double maxU = sprite.getFrameU((float) (pixelX + 1) / textureWidth);
		double minV = sprite.getFrameV((float) pixelY / textureHeight);
		double maxV = sprite.getFrameV((float) (pixelY + 1) / textureHeight);
		List<List<Vec3d>> polygons = new ArrayList<>();

		for (int[] triangle : List.of(new int[]{0, 1, 2}, new int[]{0, 2, 3})) {
			List<QuadVertex> polygon = new ArrayList<>(List.of(
					intersection.vertices[triangle[0]],
					intersection.vertices[triangle[1]],
					intersection.vertices[triangle[2]]
			));
			polygon = clip(polygon, Axis.U, minU, true);
			polygon = clip(polygon, Axis.U, maxU, false);
			polygon = clip(polygon, Axis.V, minV, true);
			polygon = clip(polygon, Axis.V, maxV, false);
			if (polygon.size() >= 3) {
				polygons.add(polygon.stream().map(vertex -> vertex.position).toList());
			}
		}

		return new ModelPixelHit(
				blockPos.toImmutable(), intersection.quadIndex, intersection.quad.getFace(),
				sprite.getContents().getId(), pixelX, pixelY, textureWidth, textureHeight, List.copyOf(polygons)
		);
	}

	private static List<QuadVertex> clip(List<QuadVertex> input, Axis axis, double boundary, boolean keepGreater) {
		if (input.isEmpty()) {
			return input;
		}
		List<QuadVertex> output = new ArrayList<>();
		QuadVertex previous = input.getLast();
		boolean previousInside = inside(previous, axis, boundary, keepGreater);
		for (QuadVertex current : input) {
			boolean currentInside = inside(current, axis, boundary, keepGreater);
			if (currentInside != previousInside) {
				output.add(between(previous, current, axis, boundary));
			}
			if (currentInside) {
				output.add(current);
			}
			previous = current;
			previousInside = currentInside;
		}
		return output;
	}

	private static boolean inside(QuadVertex vertex, Axis axis, double boundary, boolean keepGreater) {
		double value = axis == Axis.U ? vertex.u : vertex.v;
		return keepGreater ? value >= boundary - RAY_EPSILON : value <= boundary + RAY_EPSILON;
	}

	private static QuadVertex between(QuadVertex from, QuadVertex to, Axis axis, double boundary) {
		double fromValue = axis == Axis.U ? from.u : from.v;
		double toValue = axis == Axis.U ? to.u : to.v;
		double amount = (boundary - fromValue) / (toValue - fromValue);
		return new QuadVertex(
				from.position.lerp(to.position, amount),
				MathHelper.lerp(amount, from.u, to.u),
				MathHelper.lerp(amount, from.v, to.v)
		);
	}

	private static QuadVertex[] readVertices(BakedQuad quad, BlockPos blockPos) {
		int[] data = quad.getVertexData();
		int stride = data.length / 4;
		if (stride < VERTEX_STRIDE) {
			throw new IllegalStateException("Unsupported baked-quad vertex format");
		}

		QuadVertex[] vertices = new QuadVertex[4];
		for (int vertex = 0; vertex < 4; vertex++) {
			int offset = vertex * stride;
			vertices[vertex] = new QuadVertex(
					new Vec3d(
							blockPos.getX() + Float.intBitsToFloat(data[offset + POSITION_X]),
							blockPos.getY() + Float.intBitsToFloat(data[offset + POSITION_Y]),
							blockPos.getZ() + Float.intBitsToFloat(data[offset + POSITION_Z])
					),
					Float.intBitsToFloat(data[offset + TEXTURE_U]),
					Float.intBitsToFloat(data[offset + TEXTURE_V])
			);
		}
		return vertices;
	}

	private static RayHit intersectTriangle(Vec3d origin, Vec3d direction, Vec3d a, Vec3d b, Vec3d c) {
		Vec3d edge1 = b.subtract(a);
		Vec3d edge2 = c.subtract(a);
		Vec3d p = direction.crossProduct(edge2);
		double determinant = edge1.dotProduct(p);
		if (Math.abs(determinant) < RAY_EPSILON) {
			return null;
		}

		double inverse = 1.0 / determinant;
		Vec3d t = origin.subtract(a);
		double barycentricB = t.dotProduct(p) * inverse;
		if (barycentricB < 0.0 || barycentricB > 1.0) {
			return null;
		}
		Vec3d q = t.crossProduct(edge1);
		double barycentricC = direction.dotProduct(q) * inverse;
		if (barycentricC < 0.0 || barycentricB + barycentricC > 1.0) {
			return null;
		}
		double distance = edge2.dotProduct(q) * inverse;
		return distance > RAY_EPSILON
				? new RayHit(distance, 1.0 - barycentricB - barycentricC, barycentricB, barycentricC)
				: null;
	}

	private static double interpolate(double a, double b, double c, RayHit hit) {
		return a * hit.weightA + b * hit.weightB + c * hit.weightC;
	}

	public record ModelPixelHit(
			BlockPos blockPos,
			int quadIndex,
			Direction face,
			Identifier textureId,
			int pixelX,
			int pixelY,
			int textureWidth,
			int textureHeight,
			List<List<Vec3d>> polygons
	) {
	}

	private record QuadVertex(Vec3d position, double u, double v) {
	}

	private record RayHit(double distance, double weightA, double weightB, double weightC) {
	}

	private record QuadIntersection(int quadIndex, BakedQuad quad, QuadVertex[] vertices, double distance, double u, double v) {
	}

	private enum Axis {
		U, V
	}
}
