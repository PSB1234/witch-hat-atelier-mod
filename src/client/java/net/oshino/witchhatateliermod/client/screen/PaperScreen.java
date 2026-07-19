package net.oshino.witchhatateliermod.client.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.oshino.witchhatateliermod.client.screen.paper.BrushSettings;
import net.oshino.witchhatateliermod.client.screen.paper.PaperCanvas;
import net.oshino.witchhatateliermod.client.screen.paper.PaperCanvasRenderer;
import net.oshino.witchhatateliermod.client.screen.paper.PaperTool;
import net.oshino.witchhatateliermod.client.screen.paper.PaperWorkspace;
import org.lwjgl.glfw.GLFW;

public final class PaperScreen extends Screen {
    private static final int MAX_PAPER_WIDTH = 420;
    private static final int MAX_PAPER_HEIGHT = 280;
    private static final int TOOLBAR_WIDTH = 71;
    private static final int CONTENT_GAP = 12;
    private static final int ICON_BUTTON_SIZE = 26;
    private static final int ICON_BUTTON_GAP = 3;
    private static final int ACTION_BAR_HEIGHT = 22;
    private static final int ACTION_BAR_GAP = 6;
    private static final int ACTION_BUTTON_WIDTH = 31;
    private static final int ACTION_BUTTON_HEIGHT = 18;
    private static final int ACTION_BUTTON_GAP = 3;
    private static final int ACTION_BUTTON_COUNT = 6;

    private static final int PANEL = 0xF21B171C;
    private static final int PANEL_BORDER = 0xFF6D5A54;
    private static final int BUTTON = 0xFF302830;
    private static final int BUTTON_HOVER = 0xFF493C47;
    private static final int BUTTON_SELECTED = 0xFF395A7A;
    private static final int CLEAR_BUTTON = 0xFF593033;
    private static final int PAPER_BORDER = 0xFF8D8982;
    private static final int PAPER_SHADOW = 0x88000000;
    private static final int TEXT = 0xFFF0E5D0;

    private static final PaperTool[] SIGILS = {
            PaperTool.WIND_SIGN, PaperTool.LIGHT_SIGIL, PaperTool.WATER_SIGN
    };
    private static final PaperTool[] SHAPES = {
            PaperTool.LINE, PaperTool.CIRCLE, PaperTool.RECTANGLE, PaperTool.TRIANGLE
    };

    private PaperTool selectedTool = PaperTool.PENCIL;
    private boolean sigilsExpanded = true;
    private final PaperWorkspace workspace;
    private final PaperCanvas canvas;
    private final PaperCanvasRenderer canvasRenderer;
    private Text statusMessage;
    private long statusMessageUntil;

    public PaperScreen(PaperWorkspace workspace) {
        super(Text.translatable("screen.witch-hat-atelier-mod.paper"));
        this.workspace = workspace;
        this.canvas = workspace.canvas();
        this.canvasRenderer = workspace.renderer();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        renderBackground(context, mouseX, mouseY, delta);

        PaperLayout layout = layout();
        workspace.updateCanvasSize(layout.paper().width(), layout.paper().height());
        drawPaper(context, layout.paper());
        canvasRenderer.render(context, canvas, layout.paper().x(), layout.paper().y(),
                layout.paper().width(), layout.paper().height());
        drawToolbar(context, layout.toolbar(), mouseX, mouseY);
        drawActionBar(context, layout.actions(), mouseX, mouseY);

        if (statusMessage != null && System.currentTimeMillis() < statusMessageUntil) {
            context.drawCenteredTextWithShadow(textRenderer, statusMessage,
                    layout.paper().x() + layout.paper().width() / 2, layout.paper().bottom() + 7, TEXT);
        }

        Text tooltip = hoveredTooltip(layout, mouseX, mouseY);
        if (tooltip != null) {
            context.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        PaperLayout layout = layout();
        for (int index = 0; index < ACTION_BUTTON_COUNT; index++) {
            if (actionButton(layout.actions(), index).contains(mouseX, mouseY)) {
                runAction(index);
                return true;
            }
        }
        if (hardnessButton(layout.actions()).contains(mouseX, mouseY)) {
            workspace.cycleHardness();
            return true;
        }
        if (stabilizationButton(layout.actions()).contains(mouseX, mouseY)) {
            workspace.cycleStabilization();
            return true;
        }

        Bounds toolbar = layout.toolbar();
        if (primaryButton(toolbar, 0).contains(mouseX, mouseY)) {
            selectedTool = PaperTool.PENCIL;
            return true;
        }
        if (primaryButton(toolbar, 1).contains(mouseX, mouseY)) {
            selectedTool = PaperTool.ERASER;
            return true;
        }
        if (primaryButton(toolbar, 2).contains(mouseX, mouseY)) {
            canvas.clear();
            return true;
        }

        if (sigilsHeader(toolbar).contains(mouseX, mouseY)) {
            sigilsExpanded = !sigilsExpanded;
            return true;
        }

        if (sigilsExpanded) {
            for (int index = 0; index < SIGILS.length; index++) {
                if (sigilButton(toolbar, index).contains(mouseX, mouseY)) {
                    selectedTool = SIGILS[index];
                    return true;
                }
            }
        }

        for (int index = 0; index < SHAPES.length; index++) {
            if (shapeButton(toolbar, index).contains(mouseX, mouseY)) {
                selectedTool = SHAPES[index];
                return true;
            }
        }

        if (layout.paper().contains(mouseX, mouseY)) {
            canvas.beginStroke(selectedTool, workspace.brush(),
                    localX(layout.paper(), mouseX), localY(layout.paper(), mouseY));
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0 && canvas.isDrawing()) {
            Bounds paper = layout().paper();
            canvas.continueStroke(localX(paper, mouseX), localY(paper, mouseY));
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && canvas.isDrawing()) {
            Bounds paper = layout().paper();
            canvas.endStroke(localX(paper, mouseX), localY(paper, mouseY));
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (Screen.hasControlDown()) {
            if (keyCode == GLFW.GLFW_KEY_Z) {
                if (Screen.hasShiftDown()) {
                    canvas.redo();
                } else {
                    canvas.undo();
                }
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_Y) {
                canvas.redo();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_S) {
                showStatus(workspace.save());
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_O) {
                showStatus(workspace.load());
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_P) {
                showStatus(workspace.exportPng());
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void drawPaper(DrawContext context, Bounds paper) {
        context.fill(paper.x() + 4, paper.y() + 5,
                paper.right() + 4, paper.bottom() + 5, PAPER_SHADOW);
        context.fill(paper.x() - 1, paper.y() - 1,
                paper.right() + 1, paper.bottom() + 1, PAPER_BORDER);
        context.fill(paper.x(), paper.y(), paper.right(), paper.bottom(), 0xFFFFFFFF);
    }

    private void drawToolbar(DrawContext context, Bounds toolbar, int mouseX, int mouseY) {
        context.fill(toolbar.x(), toolbar.y(), toolbar.right(), toolbar.bottom(), PANEL_BORDER);
        context.fill(toolbar.x() + 1, toolbar.y() + 1, toolbar.right() - 1, toolbar.bottom() - 1, PANEL);

        drawIconButton(context, primaryButton(toolbar, 0), PaperTool.PENCIL,
                selectedTool == PaperTool.PENCIL, false, mouseX, mouseY);
        drawIconButton(context, primaryButton(toolbar, 1), PaperTool.ERASER,
                selectedTool == PaperTool.ERASER, false, mouseX, mouseY);
        drawIconButton(context, primaryButton(toolbar, 2), null,
                false, true, mouseX, mouseY);

        Bounds header = sigilsHeader(toolbar);
        drawSigilsHeader(context, header, mouseX, mouseY);

        if (sigilsExpanded) {
            for (int index = 0; index < SIGILS.length; index++) {
                PaperTool sigil = SIGILS[index];
                drawIconButton(context, sigilButton(toolbar, index), sigil,
                        selectedTool == sigil, false, mouseX, mouseY);
            }
        }

        for (int index = 0; index < SHAPES.length; index++) {
            PaperTool shape = SHAPES[index];
            drawIconButton(context, shapeButton(toolbar, index), shape,
                    selectedTool == shape, false, mouseX, mouseY);
        }
    }

    private void drawActionBar(DrawContext context, Bounds actions, int mouseX, int mouseY) {
        context.fill(actions.x(), actions.y(), actions.right(), actions.bottom(), PANEL_BORDER);
        context.fill(actions.x() + 1, actions.y() + 1, actions.right() - 1, actions.bottom() - 1, PANEL);

        String[] labels = {"Undo", "Redo", "Save", "Load", "PNG", "Clear"};
        for (int index = 0; index < labels.length; index++) {
            Bounds button = actionButton(actions, index);
            boolean enabled = index != 0 || canvas.canUndo();
            enabled = enabled && (index != 1 || canvas.canRedo());
            drawTextButton(context, button, labels[index], enabled, mouseX, mouseY);
        }

        BrushSettings brush = workspace.brush();
        drawTextButton(context, hardnessButton(actions), "H " + brush.hardnessPercent() + "%",
                true, mouseX, mouseY);
        drawTextButton(context, stabilizationButton(actions), "Smooth " + brush.stabilization(),
                true, mouseX, mouseY);
    }

    private void drawTextButton(DrawContext context, Bounds bounds, String label, boolean enabled,
                                int mouseX, int mouseY) {
        int color = !enabled ? 0xFF252126 : bounds.contains(mouseX, mouseY) ? BUTTON_HOVER : BUTTON;
        context.fill(bounds.x(), bounds.y(), bounds.right(), bounds.bottom(), color);
        int textColor = enabled ? TEXT : 0xFF766C6D;
        context.drawCenteredTextWithShadow(textRenderer, label,
                bounds.x() + bounds.width() / 2, bounds.y() + 5, textColor);
    }

    private void runAction(int index) {
        switch (index) {
            case 0 -> canvas.undo();
            case 1 -> canvas.redo();
            case 2 -> showStatus(workspace.save());
            case 3 -> showStatus(workspace.load());
            case 4 -> showStatus(workspace.exportPng());
            case 5 -> canvas.clear();
            default -> throw new IllegalArgumentException("Unknown paper action " + index);
        }
    }

    private void showStatus(String message) {
        statusMessage = Text.literal(message);
        statusMessageUntil = System.currentTimeMillis() + 3_000L;
    }

    private void drawIconButton(DrawContext context, Bounds bounds, PaperTool tool, boolean selected,
                                boolean clearButton, int mouseX, int mouseY) {
        boolean hovered = bounds.contains(mouseX, mouseY);
        int color = selected ? BUTTON_SELECTED : clearButton ? CLEAR_BUTTON : hovered ? BUTTON_HOVER : BUTTON;
        context.fill(bounds.x(), bounds.y(), bounds.right(), bounds.bottom(), color);
        context.drawHorizontalLine(bounds.x(), bounds.right() - 1, bounds.y(), PANEL_BORDER);
        context.drawVerticalLine(bounds.x(), bounds.y(), bounds.bottom() - 1, PANEL_BORDER);

        int iconX = bounds.x() + (bounds.width() - 24) / 2;
        int iconY = bounds.y() + (bounds.height() - 24) / 2;
        drawPixelIcon(context, iconX, iconY, tool, TEXT);
    }

    private void drawSigilsHeader(DrawContext context, Bounds bounds, int mouseX, int mouseY) {
        int color = bounds.contains(mouseX, mouseY) ? BUTTON_HOVER : BUTTON;
        context.fill(bounds.x(), bounds.y(), bounds.right(), bounds.bottom(), color);
        context.drawHorizontalLine(bounds.x(), bounds.right() - 1, bounds.y(), PANEL_BORDER);
        context.drawVerticalLine(bounds.x(), bounds.y(), bounds.bottom() - 1, PANEL_BORDER);

        drawSigilsCategoryIcon(context, bounds.x() + 8, bounds.y() + 6, TEXT);
        drawChevron(context, bounds.right() - 13, bounds.y() + 8, sigilsExpanded, TEXT);
    }

    private Text hoveredTooltip(PaperLayout layout, double mouseX, double mouseY) {
        Bounds actions = layout.actions();
        String[] actionKeys = {"undo", "redo", "save", "load", "export", "clear"};
        for (int index = 0; index < actionKeys.length; index++) {
            if (actionButton(actions, index).contains(mouseX, mouseY)) {
                return Text.translatable("screen.witch-hat-atelier-mod.paper.action." + actionKeys[index]);
            }
        }
        if (hardnessButton(actions).contains(mouseX, mouseY)) {
            return Text.translatable("screen.witch-hat-atelier-mod.paper.brush.hardness",
                    workspace.brush().hardnessPercent());
        }
        if (stabilizationButton(actions).contains(mouseX, mouseY)) {
            return Text.translatable("screen.witch-hat-atelier-mod.paper.brush.smoothing",
                    workspace.brush().stabilization());
        }

        Bounds toolbar = layout.toolbar();
        if (primaryButton(toolbar, 0).contains(mouseX, mouseY)) {
            return PaperTool.PENCIL.label();
        }
        if (primaryButton(toolbar, 1).contains(mouseX, mouseY)) {
            return PaperTool.ERASER.label();
        }
        if (primaryButton(toolbar, 2).contains(mouseX, mouseY)) {
            return Text.translatable("screen.witch-hat-atelier-mod.paper.clear");
        }
        if (sigilsHeader(toolbar).contains(mouseX, mouseY)) {
            return Text.translatable("screen.witch-hat-atelier-mod.paper.sigils");
        }
        if (sigilsExpanded) {
            for (int index = 0; index < SIGILS.length; index++) {
                if (sigilButton(toolbar, index).contains(mouseX, mouseY)) {
                    return SIGILS[index].label();
                }
            }
        }
        for (int index = 0; index < SHAPES.length; index++) {
            if (shapeButton(toolbar, index).contains(mouseX, mouseY)) {
                return SHAPES[index].label();
            }
        }
        return null;
    }

    private PaperLayout layout() {
        int availableWidth = Math.max(1, width - 24);
        int paperWidth = Math.clamp(availableWidth - TOOLBAR_WIDTH - CONTENT_GAP, 48, MAX_PAPER_WIDTH);
        int paperHeight = Math.clamp(height - 32 - ACTION_BAR_HEIGHT - ACTION_BAR_GAP, 1, MAX_PAPER_HEIGHT);
        int contentWidth = TOOLBAR_WIDTH + CONTENT_GAP + paperWidth;
        int left = (width - contentWidth) / 2;
        int top = (height - paperHeight - ACTION_BAR_HEIGHT - ACTION_BAR_GAP) / 2;
        int paperX = left + TOOLBAR_WIDTH + CONTENT_GAP;
        int paperY = top + ACTION_BAR_HEIGHT + ACTION_BAR_GAP;

        return new PaperLayout(
                new Bounds(left, paperY, TOOLBAR_WIDTH, paperHeight),
                new Bounds(paperX, paperY, paperWidth, paperHeight),
                new Bounds(paperX, top, paperWidth, ACTION_BAR_HEIGHT)
        );
    }

    private Bounds actionButton(Bounds actions, int index) {
        return new Bounds(actions.x() + 2 + index * (ACTION_BUTTON_WIDTH + ACTION_BUTTON_GAP),
                actions.y() + 2, ACTION_BUTTON_WIDTH, ACTION_BUTTON_HEIGHT);
    }

    private Bounds hardnessButton(Bounds actions) {
        int start = actions.x() + 2 + ACTION_BUTTON_COUNT * (ACTION_BUTTON_WIDTH + ACTION_BUTTON_GAP);
        int available = Math.max(40, actions.right() - start - 4);
        int width = Math.max(20, (available - ACTION_BUTTON_GAP) / 2);
        return new Bounds(start, actions.y() + 2, width, ACTION_BUTTON_HEIGHT);
    }

    private Bounds stabilizationButton(Bounds actions) {
        Bounds hardness = hardnessButton(actions);
        return new Bounds(hardness.right() + ACTION_BUTTON_GAP, actions.y() + 2,
                hardness.width(), ACTION_BUTTON_HEIGHT);
    }

    private Bounds primaryButton(Bounds toolbar, int index) {
        int column = index % 2;
        int row = index / 2;
        return new Bounds(toolbar.x() + 8 + column * (ICON_BUTTON_SIZE + ICON_BUTTON_GAP),
                toolbar.y() + 5 + row * (ICON_BUTTON_SIZE + ICON_BUTTON_GAP),
                ICON_BUTTON_SIZE, ICON_BUTTON_SIZE);
    }

    private Bounds sigilsHeader(Bounds toolbar) {
        return new Bounds(toolbar.x() + 6, toolbar.y() + 64, toolbar.width() - 12, 20);
    }

    private Bounds sigilButton(Bounds toolbar, int index) {
        Bounds header = sigilsHeader(toolbar);
        int column = index % 2;
        int row = index / 2;
        return new Bounds(toolbar.x() + 8 + column * (ICON_BUTTON_SIZE + ICON_BUTTON_GAP),
                header.bottom() + 5 + row * (ICON_BUTTON_SIZE + ICON_BUTTON_GAP),
                ICON_BUTTON_SIZE, ICON_BUTTON_SIZE);
    }

    private Bounds shapeButton(Bounds toolbar, int index) {
        Bounds header = sigilsHeader(toolbar);
        int shapeTop = sigilsExpanded ? sigilButton(toolbar, SIGILS.length - 1).bottom() + 5 : header.bottom() + 5;
        int gap = 3;
        int width = (header.width() - gap) / 2;
        int column = index % 2;
        int row = index / 2;
        return new Bounds(header.x() + column * (width + gap),
                shapeTop + row * (ICON_BUTTON_SIZE + ICON_BUTTON_GAP), width, ICON_BUTTON_SIZE);
    }

    private int localX(Bounds paper, double mouseX) {
        return Math.clamp((int) Math.round(mouseX) - paper.x(), 0, paper.width() - 1);
    }

    private int localY(Bounds paper, double mouseY) {
        return Math.clamp((int) Math.round(mouseY) - paper.y(), 0, paper.height() - 1);
    }

    private void drawPixelIcon(DrawContext context, int x, int y, PaperTool tool, int color) {
        if (tool == null) {
            pixel(context, x, y, 8, 6, 8, 2, color);
            pixel(context, x, y, 10, 4, 4, 2, color);
            pixel(context, x, y, 7, 9, 2, 10, color);
            pixel(context, x, y, 15, 9, 2, 10, color);
            pixel(context, x, y, 9, 18, 6, 2, color);
            pixel(context, x, y, 11, 10, 2, 6, color);
            return;
        }

        switch (tool) {
            case PENCIL -> {
                pixel(context, x, y, 4, 18, 3, 3, color);
                pixel(context, x, y, 6, 15, 5, 5, color);
                pixel(context, x, y, 9, 12, 5, 5, color);
                pixel(context, x, y, 12, 9, 5, 5, color);
                pixel(context, x, y, 15, 6, 5, 5, color);
                pixel(context, x, y, 18, 5, 3, 3, color);
            }
            case ERASER -> {
                pixel(context, x, y, 5, 7, 14, 2, color);
                pixel(context, x, y, 5, 17, 14, 2, color);
                pixel(context, x, y, 5, 9, 2, 8, color);
                pixel(context, x, y, 17, 9, 2, 8, color);
                pixel(context, x, y, 12, 9, 2, 8, color);
            }
            case WIND_SIGN -> {
                pixel(context, x, y, 4, 6, 13, 2, color);
                pixel(context, x, y, 17, 8, 3, 2, color);
                pixel(context, x, y, 7, 11, 12, 2, color);
                pixel(context, x, y, 4, 16, 11, 2, color);
                pixel(context, x, y, 15, 14, 4, 2, color);
            }
            case LIGHT_SIGIL -> {
                pixel(context, x, y, 11, 3, 2, 18, color);
                pixel(context, x, y, 3, 11, 18, 2, color);
                pixel(context, x, y, 7, 7, 3, 3, color);
                pixel(context, x, y, 14, 7, 3, 3, color);
                pixel(context, x, y, 7, 14, 3, 3, color);
                pixel(context, x, y, 14, 14, 3, 3, color);
            }
            case WATER_SIGN -> {
                pixel(context, x, y, 3, 7, 4, 2, color);
                pixel(context, x, y, 7, 9, 4, 2, color);
                pixel(context, x, y, 11, 7, 4, 2, color);
                pixel(context, x, y, 15, 9, 5, 2, color);
                pixel(context, x, y, 3, 14, 4, 2, color);
                pixel(context, x, y, 7, 16, 4, 2, color);
                pixel(context, x, y, 11, 14, 4, 2, color);
                pixel(context, x, y, 15, 16, 5, 2, color);
            }
            case LINE -> {
                pixel(context, x, y, 4, 18, 4, 2, color);
                pixel(context, x, y, 7, 15, 4, 2, color);
                pixel(context, x, y, 10, 12, 4, 2, color);
                pixel(context, x, y, 13, 9, 4, 2, color);
                pixel(context, x, y, 16, 6, 4, 2, color);
            }
            case CIRCLE -> {
                pixel(context, x, y, 8, 4, 8, 2, color);
                pixel(context, x, y, 5, 6, 3, 2, color);
                pixel(context, x, y, 16, 6, 3, 2, color);
                pixel(context, x, y, 3, 8, 2, 8, color);
                pixel(context, x, y, 19, 8, 2, 8, color);
                pixel(context, x, y, 5, 16, 3, 2, color);
                pixel(context, x, y, 16, 16, 3, 2, color);
                pixel(context, x, y, 8, 18, 8, 2, color);
            }
            case RECTANGLE -> {
                pixel(context, x, y, 4, 6, 16, 2, color);
                pixel(context, x, y, 4, 16, 16, 2, color);
                pixel(context, x, y, 4, 8, 2, 8, color);
                pixel(context, x, y, 18, 8, 2, 8, color);
            }
            case TRIANGLE -> {
                pixel(context, x, y, 11, 4, 2, 2, color);
                pixel(context, x, y, 9, 6, 2, 4, color);
                pixel(context, x, y, 13, 6, 2, 4, color);
                pixel(context, x, y, 7, 10, 2, 4, color);
                pixel(context, x, y, 15, 10, 2, 4, color);
                pixel(context, x, y, 5, 14, 2, 5, color);
                pixel(context, x, y, 17, 14, 2, 5, color);
                pixel(context, x, y, 5, 18, 14, 2, color);
            }
        }
    }

    private static void drawChevron(DrawContext context, int x, int y, boolean expanded, int color) {
        if (expanded) {
            context.fill(x, y, x + 2, y + 2, color);
            context.fill(x + 6, y, x + 8, y + 2, color);
            context.fill(x + 2, y + 2, x + 6, y + 4, color);
        } else {
            context.fill(x + 2, y, x + 6, y + 2, color);
            context.fill(x, y + 2, x + 2, y + 4, color);
            context.fill(x + 6, y + 2, x + 8, y + 4, color);
        }
    }

    private static void drawSigilsCategoryIcon(DrawContext context, int x, int y, int color) {
        context.fill(x, y, x + 4, y + 4, color);
        context.fill(x + 6, y, x + 10, y + 4, color);
        context.fill(x + 3, y + 6, x + 7, y + 10, color);
    }

    private static void pixel(DrawContext context, int originX, int originY,
                              int x, int y, int width, int height, int color) {
        context.fill(originX + x, originY + y,
                originX + x + width, originY + y + height, color);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void removed() {
        canvas.finishActiveStroke();
        super.removed();
    }

    private record PaperLayout(Bounds toolbar, Bounds paper, Bounds actions) {
    }

    private record Bounds(int x, int y, int width, int height) {
        private int right() {
            return x + width;
        }

        private int bottom() {
            return y + height;
        }

        private boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX < right() && mouseY >= y && mouseY < bottom();
        }
    }
}
