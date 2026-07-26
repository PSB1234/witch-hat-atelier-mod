package net.oshino.witchhatateliermod.drawing.stamp;

import net.minecraft.util.Identifier;
import net.oshino.witchhatateliermod.WitchHatAtelierMod;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static net.oshino.witchhatateliermod.drawing.stamp.DrawingStamp.curve;
import static net.oshino.witchhatateliermod.drawing.stamp.DrawingStamp.path;
import static net.oshino.witchhatateliermod.drawing.stamp.DrawingStamp.point;

/** Built-in stamp registry shared by drawing features. */
public final class DrawingStamps {
    public static final DrawingStamp COLUMN_SYMBOL = new DrawingStamp(
      WitchHatAtelierMod.id("column_symbol"),
      List.of(
        path(point(0.5, 0.0), point(0.5, 1)),
        path(point(0.25, 1.0), point(0.75, 1.0))
      )
    );
    public static final DrawingStamp DISPERSION_SYMBOL = new DrawingStamp(
      WitchHatAtelierMod.id("dispersion_symbol"),
      List.of(
        curve(point(0.25, 0.25), point(0.5, 0.0), point(0.75, 0.25)),
        path(point(0.25, 0.45), point(0.75, 0.45)),
        path(point(0.5, 0.45), point(0.5, 1))
        )
    );
    public static final DrawingStamp LEVITATION_SYMBOL = new DrawingStamp(
      WitchHatAtelierMod.id("levitation_symbol"),
      List.of(
        path(point(0.25, 0.25), point(0.5, 0.0), point(0.75, 0.25)),
        path(point(0.5, 0.0), point(0.5, 1)),
        path(point(0.25, 1.0), point(0.75, 1.0))
      )
    );
    public static final DrawingStamp WIND_SIGIL = new DrawingStamp(
            WitchHatAtelierMod.id("wind_sigil"),
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
    public static final DrawingStamp WATER_SIGIL = new DrawingStamp(
            WitchHatAtelierMod.id("water_sigil"),
            List.of(wavePath(0.25), wavePath(0.5), wavePath(0.75))
    );

    private static final List<DrawingStamp> VALUES = List.of(WIND_SIGIL, LIGHT_SIGIL, WATER_SIGIL, COLUMN_SYMBOL,DISPERSION_SYMBOL,LEVITATION_SYMBOL);
    private static final Map<Identifier, DrawingStamp> BY_ID = indexById();

    public static List<DrawingStamp> values() {
        return VALUES;
    }

    public static Optional<DrawingStamp> get(Identifier id) {
        return Optional.ofNullable(BY_ID.get(id));
    }

    private static DrawingStamp.StampPath wavePath(double centerY) {
        double curveOffset = Math.min(0.28, Math.min(centerY, 1.0 - centerY));
        return curve(
                point(0.0, centerY),
                point(0.2, centerY + curveOffset),
                point(0.8, centerY - curveOffset),
                point(1.0, centerY)
        );
    }

    private static Map<Identifier, DrawingStamp> indexById() {
        Map<Identifier, DrawingStamp> indexed = new LinkedHashMap<>();
        for (DrawingStamp stamp : DrawingStamps.VALUES) {
            if (indexed.put(stamp.id(), stamp) != null) {
                throw new IllegalStateException("Duplicate drawing stamp id " + stamp.id());
            }
        }
        return Map.copyOf(indexed);
    }
}
