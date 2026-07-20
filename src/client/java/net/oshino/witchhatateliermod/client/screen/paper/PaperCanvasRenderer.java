package net.oshino.witchhatateliermod.client.screen.paper;

import net.minecraft.client.gui.DrawContext;
import net.oshino.witchhatateliermod.client.drawing.stamp.AwtStampShapeFactory;
import net.oshino.witchhatateliermod.client.drawing.stamp.DrawContextStampRenderer;
import net.oshino.witchhatateliermod.client.screen.paper.PaperCanvas.CanvasPoint;
import net.oshino.witchhatateliermod.client.screen.paper.PaperCanvas.PaperStroke;
import net.oshino.witchhatateliermod.drawing.stamp.DrawingStamp;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/** Converts the canvas's tool commands into clipped pixels on the paper. */
public final class PaperCanvasRenderer {
    private static final int INK = 0xFF17141A;
    private static final int PAPER = 0xFFFFFFFF;
    private static final int PENCIL_SIZE = 6;
    private static final int SHAPE_SIZE = 2;
    private static final int ERASER_SIZE = 10;
    private static final int DEFAULT_STAMP_SIZE = 28;
    private float activeHardness = 1.0F;

    public void render(DrawContext context, PaperCanvas canvas, int x, int y, int width, int height) {
        context.enableScissor(x, y, x + width, y + height);
        for (PaperStroke stroke : canvas.strokes()) {
            drawStroke(context, stroke, x, y);
        }
        context.disableScissor();
    }

    private void drawStroke(DrawContext context, PaperStroke stroke, int originX, int originY) {
        activeHardness = stroke.brush().hardness();
        CanvasPoint start = absolute(stroke.start(), originX, originY);
        CanvasPoint end = absolute(stroke.end(), originX, originY);
        DrawingStamp stamp = stroke.tool().stamp().orElse(null);
        if (stamp != null) {
            StampBounds bounds = stampBounds(start, end);
            DrawContextStampRenderer.render(context, stamp,
                    bounds.x(), bounds.y(), bounds.width(), bounds.height(),
                    INK, SHAPE_SIZE, activeHardness);
            return;
        }

        switch (stroke.tool()) {
            case PENCIL -> drawFreehand(context, stroke.points(), originX, originY, INK, PENCIL_SIZE);
            case ERASER -> drawFreehand(context, stroke.points(), originX, originY, PAPER, ERASER_SIZE);
            case LINE -> drawLine(context, start, end, INK, SHAPE_SIZE);
            case CIRCLE -> drawEllipse(context, start, end);
            case RECTANGLE -> drawRectangle(context, start, end);
            case TRIANGLE -> drawTriangle(context, start, end);
            default -> throw new IllegalStateException("Paper tool has no drawing implementation: " + stroke.tool());
        }
    }

    private void drawFreehand(DrawContext context, List<CanvasPoint> points, int originX, int originY,
                              int color, int brushSize) {
        CanvasPoint previous = absolute(points.getFirst(), originX, originY);
        drawBrush(context, previous.x(), previous.y(), color, brushSize);
        for (int index = 1; index < points.size(); index++) {
            CanvasPoint current = absolute(points.get(index), originX, originY);
            drawLine(context, previous, current, color, brushSize);
            previous = current;
        }
    }

    private void drawRectangle(DrawContext context, CanvasPoint start, CanvasPoint end) {
        int left = Math.min(start.x(), end.x());
        int right = Math.max(start.x(), end.x());
        int top = Math.min(start.y(), end.y());
        int bottom = Math.max(start.y(), end.y());
        drawLine(context, new CanvasPoint(left, top), new CanvasPoint(right, top), INK, SHAPE_SIZE);
        drawLine(context, new CanvasPoint(right, top), new CanvasPoint(right, bottom), INK, SHAPE_SIZE);
        drawLine(context, new CanvasPoint(right, bottom), new CanvasPoint(left, bottom), INK, SHAPE_SIZE);
        drawLine(context, new CanvasPoint(left, bottom), new CanvasPoint(left, top), INK, SHAPE_SIZE);
    }

    private void drawTriangle(DrawContext context, CanvasPoint start, CanvasPoint end) {
        int left = Math.min(start.x(), end.x());
        int right = Math.max(start.x(), end.x());
        int top = Math.min(start.y(), end.y());
        int bottom = Math.max(start.y(), end.y());
        CanvasPoint apex = new CanvasPoint((left + right) / 2, top);
        CanvasPoint bottomLeft = new CanvasPoint(left, bottom);
        CanvasPoint bottomRight = new CanvasPoint(right, bottom);
        drawLine(context, apex, bottomLeft, INK, SHAPE_SIZE);
        drawLine(context, bottomLeft, bottomRight, INK, SHAPE_SIZE);
        drawLine(context, bottomRight, apex, INK, SHAPE_SIZE);
    }

    private void drawEllipse(DrawContext context, CanvasPoint start, CanvasPoint end) {
        double centerX = (start.x() + end.x()) / 2.0;
        double centerY = (start.y() + end.y()) / 2.0;
        double radiusX = Math.abs(end.x() - start.x()) / 2.0;
        double radiusY = Math.abs(end.y() - start.y()) / 2.0;
        if (radiusX < 1.0 && radiusY < 1.0) {
            drawBrush(context, start.x(), start.y(), INK, SHAPE_SIZE);
            return;
        }

        int segments = Math.max(24, (int) Math.ceil(Math.max(radiusX, radiusY) * 1.5));
        CanvasPoint previous = ellipsePoint(centerX, centerY, radiusX, radiusY, 0.0);
        for (int index = 1; index <= segments; index++) {
            double angle = Math.PI * 2.0 * index / segments;
            CanvasPoint current = ellipsePoint(centerX, centerY, radiusX, radiusY, angle);
            drawLine(context, previous, current, INK, SHAPE_SIZE);
            previous = current;
        }
    }

    private StampBounds stampBounds(CanvasPoint start, CanvasPoint end) {
        if (start.equals(end)) {
            int half = DEFAULT_STAMP_SIZE / 2;
            return new StampBounds(start.x() - half, start.y() - half,
                    DEFAULT_STAMP_SIZE, DEFAULT_STAMP_SIZE);
        }

        int left = Math.min(start.x(), end.x());
        int top = Math.min(start.y(), end.y());
        return new StampBounds(left, top,
                Math.max(4, Math.abs(end.x() - start.x())),
                Math.max(4, Math.abs(end.y() - start.y())));
    }

    private void drawLine(DrawContext context, CanvasPoint start, CanvasPoint end, int color, int brushSize) {
        int x = start.x();
        int y = start.y();
        int deltaX = Math.abs(end.x() - x);
        int deltaY = Math.abs(end.y() - y);
        int stepX = x < end.x() ? 1 : -1;
        int stepY = y < end.y() ? 1 : -1;
        int error = deltaX - deltaY;

        while (true) {
            drawBrush(context, x, y, color, brushSize);
            if (x == end.x() && y == end.y()) {
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

    private void drawBrush(DrawContext context, int x, int y, int color, int size) {
        int coreSize = Math.max(1, Math.round(size * activeHardness));
        if (coreSize < size) {
            int softAlpha = Math.max(24, Math.round(112 * (1.0F - activeHardness)));
            int softColor = (softAlpha << 24) | (color & 0x00FFFFFF);
            int outerOffset = size / 2;
            context.fill(x - outerOffset, y - outerOffset,
                    x - outerOffset + size, y - outerOffset + size, softColor);
        }
        int coreOffset = coreSize / 2;
        context.fill(x - coreOffset, y - coreOffset,
                x - coreOffset + coreSize, y - coreOffset + coreSize, color);
    }

    public void exportPng(Path path, PaperCanvas canvas, int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, height);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (PaperStroke stroke : canvas.strokes()) {
                drawExportStroke(graphics, stroke);
            }
        } finally {
            graphics.dispose();
        }
        ImageIO.write(image, "png", path.toFile());
    }

    private void drawExportStroke(Graphics2D graphics, PaperStroke stroke) {
        CanvasPoint start = stroke.start();
        CanvasPoint end = stroke.end();
        DrawingStamp stamp = stroke.tool().stamp().orElse(null);
        Shape shape;
        if (stamp != null) {
            StampBounds bounds = stampBounds(start, end);
            shape = AwtStampShapeFactory.create(stamp,
                    bounds.x(), bounds.y(), bounds.width(), bounds.height());
        } else {
            shape = switch (stroke.tool()) {
                case PENCIL, ERASER -> freehandPath(stroke.points());
                case LINE -> new Line2D.Double(start.x(), start.y(), end.x(), end.y());
                case CIRCLE -> ellipseShape(start, end);
                case RECTANGLE -> rectangleShape(start, end);
                case TRIANGLE -> triangleShape(start, end);
                default -> throw new IllegalStateException(
                        "Paper tool has no export implementation: " + stroke.tool());
            };
        }
        int size = stroke.tool() == PaperTool.ERASER ? ERASER_SIZE
                : stroke.tool() == PaperTool.PENCIL ? PENCIL_SIZE : SHAPE_SIZE;
        Color color = stroke.tool() == PaperTool.ERASER ? Color.WHITE : new Color(INK, true);
        drawSoftStroke(graphics, shape, color, size, stroke.brush().hardness());
    }

    private void drawSoftStroke(Graphics2D graphics, Shape shape, Color color, int size, float hardness) {
        int coreSize = Math.max(1, Math.round(size * hardness));
        if (coreSize < size) {
            graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(),
                    Math.max(24, Math.round(112 * (1.0F - hardness)))));
            graphics.setStroke(new BasicStroke(size, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            graphics.draw(shape);
        }
        graphics.setColor(color);
        graphics.setStroke(new BasicStroke(coreSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        graphics.draw(shape);
    }

    private Shape freehandPath(List<CanvasPoint> points) {
        Path2D path = new Path2D.Double();
        CanvasPoint first = points.getFirst();
        path.moveTo(first.x(), first.y());
        if (points.size() == 1) {
            path.lineTo(first.x() + 0.01, first.y() + 0.01);
        } else {
            for (int index = 1; index < points.size(); index++) {
                CanvasPoint point = points.get(index);
                path.lineTo(point.x(), point.y());
            }
        }
        return path;
    }

    private Shape ellipseShape(CanvasPoint start, CanvasPoint end) {
        int left = Math.min(start.x(), end.x());
        int top = Math.min(start.y(), end.y());
        return new Ellipse2D.Double(left, top,
                Math.max(1, Math.abs(end.x() - start.x())), Math.max(1, Math.abs(end.y() - start.y())));
    }

    private Shape rectangleShape(CanvasPoint start, CanvasPoint end) {
        int left = Math.min(start.x(), end.x());
        int top = Math.min(start.y(), end.y());
        return new Rectangle2D.Double(left, top,
                Math.max(1, Math.abs(end.x() - start.x())), Math.max(1, Math.abs(end.y() - start.y())));
    }

    private Shape triangleShape(CanvasPoint start, CanvasPoint end) {
        int left = Math.min(start.x(), end.x());
        int right = Math.max(start.x(), end.x());
        int top = Math.min(start.y(), end.y());
        int bottom = Math.max(start.y(), end.y());
        Path2D path = new Path2D.Double();
        path.moveTo((left + right) / 2.0, top);
        path.lineTo(left, bottom);
        path.lineTo(right, bottom);
        path.closePath();
        return path;
    }

    private CanvasPoint ellipsePoint(double centerX, double centerY, double radiusX, double radiusY,
                                     double angle) {
        return new CanvasPoint(
                (int) Math.round(centerX + Math.cos(angle) * radiusX),
                (int) Math.round(centerY + Math.sin(angle) * radiusY)
        );
    }

    private CanvasPoint absolute(CanvasPoint point, int originX, int originY) {
        return new CanvasPoint(originX + point.x(), originY + point.y());
    }

    private record StampBounds(int x, int y, int width, int height) {
    }
}
