package net.oshino.witchhatateliermod.client.drawing.stamp;

import net.oshino.witchhatateliermod.drawing.stamp.DrawingStamp;
import net.oshino.witchhatateliermod.drawing.stamp.DrawingStamp.StampPath;
import net.oshino.witchhatateliermod.drawing.stamp.DrawingStamp.StampPoint;

import java.awt.Shape;
import java.awt.geom.Path2D;

/** Creates Java2D shapes from the same normalized geometry used by GUI stamps. */
public final class AwtStampShapeFactory {
    private AwtStampShapeFactory() {
    }

    public static Shape create(DrawingStamp stamp, int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Stamp dimensions must be positive");
        }

        Path2D path = new Path2D.Double();
        for (StampPath stampPath : stamp.paths()) {
            StampPoint first = stampPath.points().getFirst();
            path.moveTo(scale(first.x(), x, width), scale(first.y(), y, height));
            for (int index = 1; index < stampPath.points().size(); index++) {
                StampPoint point = stampPath.points().get(index);
                path.lineTo(scale(point.x(), x, width), scale(point.y(), y, height));
            }
        }
        return path;
    }

    private static double scale(double coordinate, int origin, int size) {
        return origin + coordinate * size;
    }
}
