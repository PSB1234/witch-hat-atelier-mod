package net.oshino.witchhatateliermod.client.drawing.stamp;

import net.minecraft.client.gui.DrawContext;
import net.oshino.witchhatateliermod.drawing.stamp.DrawingStamp;
import net.oshino.witchhatateliermod.drawing.stamp.DrawingStamp.StampPath;
import net.oshino.witchhatateliermod.drawing.stamp.DrawingStamp.StampPoint;

/** Renders normalized drawing stamps into Minecraft GUI coordinates. */
public final class DrawContextStampRenderer {
    private DrawContextStampRenderer() {
    }

    public static void render(DrawContext context, DrawingStamp stamp,
                              int x, int y, int width, int height,
                              int color, int brushSize, float hardness) {
        if (width <= 0 || height <= 0 || brushSize <= 0) {
            throw new IllegalArgumentException("Stamp dimensions and brush size must be positive");
        }

        for (StampPath path : stamp.paths()) {
            StampPoint first = path.points().getFirst();
            int previousX = scale(first.x(), x, width);
            int previousY = scale(first.y(), y, height);
            for (int index = 1; index < path.points().size(); index++) {
                StampPoint point = path.points().get(index);
                int currentX = scale(point.x(), x, width);
                int currentY = scale(point.y(), y, height);
                drawLine(context, previousX, previousY, currentX, currentY, color, brushSize, hardness);
                previousX = currentX;
                previousY = currentY;
            }
        }
    }

    private static int scale(double coordinate, int origin, int size) {
        return origin + (int) Math.round(coordinate * size);
    }

    private static void drawLine(DrawContext context, int startX, int startY, int endX, int endY,
                                 int color, int brushSize, float hardness) {
        int x = startX;
        int y = startY;
        int deltaX = Math.abs(endX - x);
        int deltaY = Math.abs(endY - y);
        int stepX = x < endX ? 1 : -1;
        int stepY = y < endY ? 1 : -1;
        int error = deltaX - deltaY;

        while (true) {
            drawBrush(context, x, y, color, brushSize, hardness);
            if (x == endX && y == endY) {
                break;
            }
            int doubledError = error * 2;
            if (doubledError > -deltaY) {
                error -= deltaY;
                x += stepX;
            }
            if (doubledError < deltaX) {
                error += deltaX;
                y += stepY;
            }
        }
    }

    private static void drawBrush(DrawContext context, int x, int y, int color,
                                  int size, float hardness) {
        float normalizedHardness = Math.clamp(hardness, 0.0F, 1.0F);
        int coreSize = Math.max(1, Math.round(size * normalizedHardness));
        if (coreSize < size) {
            int softAlpha = Math.max(24, Math.round(112 * (1.0F - normalizedHardness)));
            int softColor = (softAlpha << 24) | (color & 0x00FFFFFF);
            int outerOffset = size / 2;
            context.fill(x - outerOffset, y - outerOffset,
                    x - outerOffset + size, y - outerOffset + size, softColor);
        }
        int coreOffset = coreSize / 2;
        context.fill(x - coreOffset, y - coreOffset,
                x - coreOffset + coreSize, y - coreOffset + coreSize, color);
    }
}
