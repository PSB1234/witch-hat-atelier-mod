package net.oshino.witchhatateliermod.drawing.stamp;

import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/** Backend-neutral drawing geometry expressed in normalized coordinates. */
public record DrawingStamp(Identifier id, List<StampPath> paths) {
    public DrawingStamp {
        Objects.requireNonNull(id, "id");
        paths = List.copyOf(paths);
        if (paths.isEmpty()) {
            throw new IllegalArgumentException("A drawing stamp must contain at least one path");
        }
    }

    /** Creates one continuous path from two or more normalized points. */
    public static StampPath path(StampPoint... points) {
        return new StampPath(Arrays.asList(points));
    }

    public static StampPoint point(double x, double y) {
        return new StampPoint(x, y);
    }

    /** Creates a point whose y coordinate defaults to zero. */
    public static StampPoint point(double x) {
        return point(x, 0.0);
    }

    public record StampPath(List<StampPoint> points) {
        public StampPath {
            points = List.copyOf(points);
            if (points.size() < 2) {
                throw new IllegalArgumentException("A stamp path must contain at least two points");
            }
        }
    }

    public record StampPoint(double x, double y) {
        public StampPoint {
            if (!Double.isFinite(x) || !Double.isFinite(y)
                    || x < 0.0 || x > 1.0 || y < 0.0 || y > 1.0) {
                throw new IllegalArgumentException("Stamp coordinates must be finite and normalized to 0..1");
            }
        }
    }
}
