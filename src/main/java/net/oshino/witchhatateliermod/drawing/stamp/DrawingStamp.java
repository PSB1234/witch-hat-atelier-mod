package net.oshino.witchhatateliermod.drawing.stamp;

import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/** Backend-neutral drawing geometry expressed in normalized coordinates. */
public record DrawingStamp(Identifier id, List<StampPath> paths) {
    private static final int DEFAULT_CURVE_SEGMENTS = 24;
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

    /**
     * Creates a smooth Bézier path from normalized control points, sampled into drawable line segments.
     * Three points make a quadratic curve; four points make a cubic curve.
     */
    public static StampPath curve(StampPoint... controlPoints) {
        return curve(DEFAULT_CURVE_SEGMENTS, controlPoints);
    }

    /** Creates a smooth Bézier path with an explicit number of sampled line segments. */
    public static StampPath curve(int segments, StampPoint... controlPoints) {
        if (segments < 1) {
            throw new IllegalArgumentException("A curve must have at least one segment");
        }
        if (controlPoints.length < 3) {
            throw new IllegalArgumentException("A curve must have at least three control points");
        }

        List<StampPoint> points = new java.util.ArrayList<>(segments + 1);
        for (int index = 0; index <= segments; index++) {
            points.add(evaluateBezier(controlPoints, (double) index / segments));
        }
        return new StampPath(points);
    }

    public static StampPoint point(double x, double y) {
        return new StampPoint(x, y);
    }

    /** Creates a point whose y coordinate defaults to zero. */
    public static StampPoint point(double x) {
        return point(x, 0.0);
    }

    private static StampPoint evaluateBezier(StampPoint[] controlPoints, double progress) {
        double[] x = new double[controlPoints.length];
        double[] y = new double[controlPoints.length];
        for (int index = 0; index < controlPoints.length; index++) {
            x[index] = controlPoints[index].x();
            y[index] = controlPoints[index].y();
        }

        for (int count = controlPoints.length - 1; count > 0; count--) {
            for (int index = 0; index < count; index++) {
                x[index] = x[index] + (x[index + 1] - x[index]) * progress;
                y[index] = y[index] + (y[index + 1] - y[index]) * progress;
            }
        }
        return point(x[0], y[0]);
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
