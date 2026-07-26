package net.oshino.witchhatateliermod.client.screen.paper;

import net.minecraft.text.Text;
import net.oshino.witchhatateliermod.drawing.stamp.DrawingStamp;
import net.oshino.witchhatateliermod.drawing.stamp.DrawingStamps;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public enum PaperTool {
    PENCIL("pencil", ToolKind.FREEHAND),
    ERASER("eraser", ToolKind.FREEHAND),
    WIND_SIGIL("wind_sigil", DrawingStamps.WIND_SIGIL),
    LIGHT_SIGIL("light_sigil", DrawingStamps.LIGHT_SIGIL),
    WATER_SIGIL("water_sigil", DrawingStamps.WATER_SIGIL),
    COLUMN_SYMBOL("column_symbol", ToolKind.SYMBOL, DrawingStamps.COLUMN_SYMBOL),
    DISPERSION_SYMBOL("dispersion_symbol", ToolKind.SYMBOL, DrawingStamps.DISPERSION_SYMBOL),
    LEVITATION_SYMBOL("levitation_symbol", ToolKind.SYMBOL, DrawingStamps.LEVITATION_SYMBOL),
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

    /**
     * Add a stamp-backed enum entry above, and it is automatically shown in the Sigils toolbar.
     */
    public static List<PaperTool> sigils() {
        return byKind(ToolKind.SIGIL);
    }

    public static List<PaperTool> shapes() {
        return byKind(ToolKind.SHAPE);
    }

    /** Add a stamp-backed enum entry with {@link ToolKind#SYMBOL} to show it in Symbols. */
    public static List<PaperTool> symbols() {
        return byKind(ToolKind.SYMBOL);
    }

    boolean isFreehand() {
        return kind == ToolKind.FREEHAND;
    }

    public Optional<DrawingStamp> stamp() {
        return stamp;
    }

    private static List<PaperTool> byKind(ToolKind kind) {
        return Arrays.stream(values()).filter(tool -> tool.kind == kind).toList();
    }

    private enum ToolKind {
        FREEHAND,
        SHAPE,
        SIGIL,
        SYMBOL
    }
}
