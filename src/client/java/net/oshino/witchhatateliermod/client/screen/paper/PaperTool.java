package net.oshino.witchhatateliermod.client.screen.paper;

import net.minecraft.text.Text;

public enum PaperTool {
    PENCIL("pencil", ToolKind.FREEHAND),
    ERASER("eraser", ToolKind.FREEHAND),
    WIND_SIGN("wind_sign", ToolKind.SIGIL),
    LIGHT_SIGIL("light_sigil", ToolKind.SIGIL),
    WATER_SIGN("water_sign", ToolKind.SIGIL),
    LINE("line", ToolKind.SHAPE),
    CIRCLE("circle", ToolKind.SHAPE),
    RECTANGLE("rectangle", ToolKind.SHAPE),
    TRIANGLE("triangle", ToolKind.SHAPE);

    private final String translationKey;
    private final ToolKind kind;

    PaperTool(String translationKey, ToolKind kind) {
        this.translationKey = translationKey;
        this.kind = kind;
    }

    public Text label() {
        return Text.translatable("screen.witch-hat-atelier-mod.paper.tool." + translationKey);
    }

    boolean isFreehand() {
        return kind == ToolKind.FREEHAND;
    }

    private enum ToolKind {
        FREEHAND,
        SHAPE,
        SIGIL
    }
}
