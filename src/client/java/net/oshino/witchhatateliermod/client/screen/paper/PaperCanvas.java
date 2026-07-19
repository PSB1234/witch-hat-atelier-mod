package net.oshino.witchhatateliermod.client.screen.paper;

import net.oshino.witchhatateliermod.client.screen.paper.PaperCanvasState.PointState;
import net.oshino.witchhatateliermod.client.screen.paper.PaperCanvasState.StrokeState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/** Stores paper-relative drawing commands and their undo/redo history. */
public final class PaperCanvas {
    private static final int MAX_HISTORY = 100;

    private final List<PaperStroke> strokes = new ArrayList<>();
    private final Deque<List<PaperStroke>> undoHistory = new ArrayDeque<>();
    private final Deque<List<PaperStroke>> redoHistory = new ArrayDeque<>();
    private PaperStroke activeStroke;
    private List<PaperStroke> stateBeforeStroke;
    private long revision;

    public void beginStroke(PaperTool tool, BrushSettings brush, int x, int y) {
        if (activeStroke != null) {
            return;
        }
        stateBeforeStroke = copyStrokes(strokes);
        activeStroke = new PaperStroke(tool, brush, new CanvasPoint(x, y));
        strokes.add(activeStroke);
    }

    public void continueStroke(int x, int y) {
        if (activeStroke != null) {
            activeStroke.moveTo(new CanvasPoint(x, y));
        }
    }

    public void endStroke(int x, int y) {
        if (activeStroke == null) {
            return;
        }
        activeStroke.moveTo(new CanvasPoint(x, y));
        activeStroke = null;
        pushUndo(stateBeforeStroke);
        stateBeforeStroke = null;
        redoHistory.clear();
        revision++;
    }

    public boolean isDrawing() {
        return activeStroke != null;
    }

    public void finishActiveStroke() {
        if (activeStroke != null) {
            CanvasPoint end = activeStroke.end();
            endStroke(end.x(), end.y());
        }
    }

    public boolean canUndo() {
        return !undoHistory.isEmpty();
    }

    public boolean canRedo() {
        return !redoHistory.isEmpty();
    }

    public void undo() {
        if (!canUndo() || activeStroke != null) {
            return;
        }
        redoHistory.push(copyStrokes(strokes));
        restoreStrokes(undoHistory.pop());
        revision++;
    }

    public void redo() {
        if (!canRedo() || activeStroke != null) {
            return;
        }
        pushUndo(copyStrokes(strokes));
        restoreStrokes(redoHistory.pop());
        revision++;
    }

    public void clear() {
        if (strokes.isEmpty()) {
            return;
        }
        pushUndo(copyStrokes(strokes));
        strokes.clear();
        activeStroke = null;
        stateBeforeStroke = null;
        redoHistory.clear();
        revision++;
    }

    public PaperCanvasState snapshot(int width, int height, BrushSettings brush) {
        List<StrokeState> savedStrokes = strokes.stream().map(PaperStroke::toState).toList();
        return new PaperCanvasState(PaperCanvasState.CURRENT_FORMAT_VERSION, width, height, brush, savedStrokes);
    }

    public void load(PaperCanvasState state) {
        if (state.formatVersion() > PaperCanvasState.CURRENT_FORMAT_VERSION) {
            throw new IllegalArgumentException("Unsupported paper format version " + state.formatVersion());
        }
        List<PaperStroke> loaded = state.strokes().stream()
                .filter(stroke -> stroke.tool() != null && !stroke.points().isEmpty())
                .map(PaperStroke::fromState)
                .toList();
        if (!strokes.isEmpty()) {
            pushUndo(copyStrokes(strokes));
        }
        restoreStrokes(loaded);
        activeStroke = null;
        stateBeforeStroke = null;
        redoHistory.clear();
        revision++;
    }

    public long revision() {
        return revision;
    }

    List<PaperStroke> strokes() {
        return Collections.unmodifiableList(strokes);
    }

    private void pushUndo(List<PaperStroke> state) {
        if (state == null) {
            return;
        }
        undoHistory.push(state);
        while (undoHistory.size() > MAX_HISTORY) {
            undoHistory.removeLast();
        }
    }

    private void restoreStrokes(List<PaperStroke> state) {
        strokes.clear();
        strokes.addAll(copyStrokes(state));
    }

    private static List<PaperStroke> copyStrokes(List<PaperStroke> source) {
        return source.stream().map(PaperStroke::copy).toList();
    }

    record CanvasPoint(int x, int y) {
    }

    static final class PaperStroke {
        private final PaperTool tool;
        private final BrushSettings brush;
        private final List<CanvasPoint> points = new ArrayList<>();
        private double filteredX;
        private double filteredY;

        private PaperStroke(PaperTool tool, BrushSettings brush, CanvasPoint start) {
            this.tool = tool;
            this.brush = brush;
            this.filteredX = start.x();
            this.filteredY = start.y();
            points.add(start);
        }

        private void moveTo(CanvasPoint point) {
            CanvasPoint next = tool.isFreehand() ? stabilized(point) : point;
            CanvasPoint last = points.getLast();
            if (last.equals(next)) {
                return;
            }
            if (tool.isFreehand()) {
                points.add(next);
            } else if (points.size() == 1) {
                points.add(next);
            } else {
                points.set(1, next);
            }
        }

        private CanvasPoint stabilized(CanvasPoint point) {
            double response = 1.0 / (brush.stabilization() + 1.0);
            filteredX += (point.x() - filteredX) * response;
            filteredY += (point.y() - filteredY) * response;
            return new CanvasPoint((int) Math.round(filteredX), (int) Math.round(filteredY));
        }

        private PaperStroke copy() {
            PaperStroke copy = new PaperStroke(tool, brush, points.getFirst());
            copy.points.clear();
            copy.points.addAll(points);
            copy.filteredX = filteredX;
            copy.filteredY = filteredY;
            return copy;
        }

        private StrokeState toState() {
            return new StrokeState(tool, brush,
                    points.stream().map(point -> new PointState(point.x(), point.y())).toList());
        }

        private static PaperStroke fromState(StrokeState state) {
            PointState first = state.points().getFirst();
            PaperStroke stroke = new PaperStroke(state.tool(), state.brush(),
                    new CanvasPoint(first.x(), first.y()));
            for (int index = 1; index < state.points().size(); index++) {
                PointState point = state.points().get(index);
                stroke.points.add(new CanvasPoint(point.x(), point.y()));
            }
            CanvasPoint last = stroke.points.getLast();
            stroke.filteredX = last.x();
            stroke.filteredY = last.y();
            return stroke;
        }

        PaperTool tool() {
            return tool;
        }

        BrushSettings brush() {
            return brush;
        }

        List<CanvasPoint> points() {
            return points;
        }

        CanvasPoint start() {
            return points.getFirst();
        }

        CanvasPoint end() {
            return points.getLast();
        }
    }
}
