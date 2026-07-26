package net.oshino.witchhatateliermod.client.screen.paper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

/** A drawing session backed by the specific paper item that opened the screen. */
public final class PaperWorkspace {
    private static final Logger LOGGER = LoggerFactory.getLogger("witch-hat-atelier-mod/paper");
    private static final long AUTOSAVE_INTERVAL_MS = 30_000L;

    private final PaperCanvas canvas = new PaperCanvas();
    private final PaperCanvasRenderer renderer = new PaperCanvasRenderer();
    private final PaperDocumentStore store;
    private final Consumer<Document> saveConsumer;
    private BrushSettings brush = BrushSettings.DEFAULT;
    private String title;
    private int canvasWidth = 420;
    private int canvasHeight = 280;
    private long settingsRevision;
    private long savedCanvasRevision;
    private long savedSettingsRevision;
    private long lastAutosaveAt = System.currentTimeMillis();

    public PaperWorkspace(Path gameDirectory, String title, String drawing, Consumer<Document> saveConsumer) {
        store = new PaperDocumentStore(gameDirectory);
        this.title = title;
        this.saveConsumer = saveConsumer;
        if (!drawing.isBlank()) {
            try {
                apply(store.deserialize(drawing));
            } catch (IOException | IllegalArgumentException exception) {
                LOGGER.warn("Could not read drawing from paper item", exception);
            }
        }
        markSaved();
    }

    public PaperCanvas canvas() {
        return canvas;
    }

    public PaperCanvasRenderer renderer() {
        return renderer;
    }

    public BrushSettings brush() {
        return brush;
    }

    public String title() {
        return title;
    }

    public void rename(String title) {
        String trimmed = title.trim();
        if (!this.title.equals(trimmed)) {
            this.title = trimmed;
            settingsRevision++;
        }
    }

    public void updateCanvasSize(int width, int height) {
        canvasWidth = Math.max(1, width);
        canvasHeight = Math.max(1, height);
    }

    public void cycleHardness() {
        brush = brush.nextHardness();
        settingsRevision++;
    }

    public void cycleStabilization() {
        brush = brush.nextStabilization();
        settingsRevision++;
    }

    public String save() {
        canvas.finishActiveStroke();
        saveDocument();
        return "Saved to this paper";
    }

    public String load() {
        return "This paper's saved drawing is already open";
    }

    public String exportPng() {
        try {
            Files.createDirectories(store.exportPath().getParent());
            renderer.exportPng(store.exportPath(), canvas, canvasWidth, canvasHeight);
            return "Exported paper.png";
        } catch (IOException exception) {
            LOGGER.error("Could not export paper PNG", exception);
            return "PNG export failed: " + exception.getMessage();
        }
    }

    public void tick() {
        long now = System.currentTimeMillis();
        if (isDirty() && now - lastAutosaveAt >= AUTOSAVE_INTERVAL_MS) {
            saveDocument();
            lastAutosaveAt = now;
        }
    }

    public void saveOnClose() {
        canvas.finishActiveStroke();
        if (isDirty()) {
            saveDocument();
        }
    }

    private void saveDocument() {
        try {
            saveConsumer.accept(new Document(title, store.serialize(snapshot())));
            markSaved();
        } catch (RuntimeException exception) {
            LOGGER.error("Could not save paper drawing", exception);
        }
    }

    private PaperCanvasState snapshot() {
        return canvas.snapshot(canvasWidth, canvasHeight, brush);
    }

    private void apply(PaperCanvasState state) {
        canvas.load(state);
        brush = state.brush();
        canvasWidth = Math.max(1, state.width());
        canvasHeight = Math.max(1, state.height());
        settingsRevision++;
    }

    private boolean isDirty() {
        return canvas.revision() != savedCanvasRevision || settingsRevision != savedSettingsRevision;
    }

    private void markSaved() {
        savedCanvasRevision = canvas.revision();
        savedSettingsRevision = settingsRevision;
    }

    public record Document(String title, String drawing) {
    }
}
