package net.oshino.witchhatateliermod.client.screen.paper;

import java.util.List;

public record PaperCanvasState(int formatVersion, int width, int height, BrushSettings brush,
                               List<StrokeState> strokes) {
    public static final int CURRENT_FORMAT_VERSION = 1;

    public PaperCanvasState {
        brush = brush == null ? BrushSettings.DEFAULT : brush;
        strokes = strokes == null ? List.of() : List.copyOf(strokes);
    }

    public record StrokeState(PaperTool tool, BrushSettings brush, List<PointState> points) {
        public StrokeState {
            brush = brush == null ? BrushSettings.DEFAULT : brush;
            points = points == null ? List.of() : List.copyOf(points);
        }
    }

    public record PointState(int x, int y) {
    }
}
