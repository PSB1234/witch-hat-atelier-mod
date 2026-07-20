package net.oshino.witchhatateliermod.drawing.stamp;

import net.minecraft.util.Identifier;
import net.oshino.witchhatateliermod.WitchHatAtelierMod;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static net.oshino.witchhatateliermod.drawing.stamp.DrawingStamp.path;
import static net.oshino.witchhatateliermod.drawing.stamp.DrawingStamp.point;

/** Built-in stamp registry shared by drawing features. */
public final class DrawingStamps {
    public static final DrawingStamp WIND_SIGN = new DrawingStamp(
            WitchHatAtelierMod.id("wind_sign"),
            List.of(
                    path(point(0.0, 0.25), point(0.8, 0.25)),
                    path(point(0.2, 0.5), point(1.0, 0.5)),
                    path(point(0.0, 0.75), point(0.75, 0.75))
            )
    );
    public static final DrawingStamp LIGHT_SIGIL = new DrawingStamp(
            WitchHatAtelierMod.id("light_sigil"),
            List.of(
                    path(point(0.5), point(0.5, 1.0)),
                    path(point(0.0, 0.5), point(1.0, 0.5)),
                    path(point(1.0 / 6.0, 1.0 / 6.0), point(5.0 / 6.0, 5.0 / 6.0)),
                    path(point(5.0 / 6.0, 1.0 / 6.0), point(1.0 / 6.0, 5.0 / 6.0))
            )
    );
    public static final DrawingStamp WATER_SIGN = new DrawingStamp(
            WitchHatAtelierMod.id("water_sign"),
            List.of(wavePath(0.25), wavePath(0.5), wavePath(0.75))
    );

    private static final List<DrawingStamp> VALUES = List.of(WIND_SIGN, LIGHT_SIGIL, WATER_SIGN);
    private static final Map<Identifier, DrawingStamp> BY_ID = indexById(VALUES);

    private DrawingStamps() {
    }

    public static List<DrawingStamp> values() {
        return VALUES;
    }

    public static Optional<DrawingStamp> get(Identifier id) {
        return Optional.ofNullable(BY_ID.get(id));
    }

    private static DrawingStamp.StampPath wavePath(double centerY) {
        int segments = 16;
        List<DrawingStamp.StampPoint> points = new ArrayList<>(segments + 1);
        for (int index = 0; index <= segments; index++) {
            double progress = (double) index / segments;
            double y = centerY + Math.sin(Math.PI * 2.0 * progress) * 0.2;
            points.add(new DrawingStamp.StampPoint(progress, y));
        }
        return new DrawingStamp.StampPath(points);
    }

    private static Map<Identifier, DrawingStamp> indexById(List<DrawingStamp> stamps) {
        Map<Identifier, DrawingStamp> indexed = new LinkedHashMap<>();
        for (DrawingStamp stamp : stamps) {
            if (indexed.put(stamp.id(), stamp) != null) {
                throw new IllegalStateException("Duplicate drawing stamp id " + stamp.id());
            }
        }
        return Map.copyOf(indexed);
    }
}
