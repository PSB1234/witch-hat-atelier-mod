package net.oshino.witchhatateliermod.client.screen.paper;

import net.minecraft.text.Text;
import net.oshino.witchhatateliermod.drawing.stamp.DrawingStamp;
import net.oshino.witchhatateliermod.drawing.stamp.DrawingStamps;

import java.util.Optional;

public enum PaperTool {
    PENCIL("pencil", ToolKind.FREEHAND),
    ERASER("eraser", ToolKind.FREEHAND),
    WIND_SIGN("wind_sign", DrawingStamps.WIND_SIGN),
    LIGHT_SIGIL("light_sigil", DrawingStamps.LIGHT_SIGIL),
    WATER_SIGN("water_sign", DrawingStamps.WATER_SIGN),
    LINE("line", ToolKind.SHAPE),
    CIRCLE("circle", ToolKind.SHAPE),
    RECTANGLE("rectangle", ToolKind.SHAPE),
    TRIANGLE("triangle", ToolKind.SHAPE);

    private final String translationKey;
    private final ToolKind kind;
    private final Optional<DrawingStamp> stamp;

    PaperTool(String translationKey, ToolKind kind) {
        this(translationKey, kind, null);
    }

    PaperTool(String translationKey, DrawingStamp stamp) {
        this(translationKey, ToolKind.SIGIL, stamp);
    }

    PaperTool(String translationKey, ToolKind kind, DrawingStamp stamp) {
        this.translationKey = translationKey;
        this.kind = kind;
        this.stamp = Optional.ofNullable(stamp);
    }

    public Text label() {
        return Text.translatable("screen.witch-hat-atelier-mod.paper.tool." + translationKey);
    }

    boolean isFreehand() {
        return kind == ToolKind.FREEHAND;
    }

    public Optional<DrawingStamp> stamp() {
        return stamp;
    }

    private enum ToolKind {
        FREEHAND,
        SHAPE,
        SIGIL
    }
}
