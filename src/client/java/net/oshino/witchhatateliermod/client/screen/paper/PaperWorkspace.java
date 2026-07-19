package net.oshino.witchhatateliermod.client.screen.paper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Long-lived client drawing session with persistence and automatic recovery. */
public final class PaperWorkspace {
    private static final Logger LOGGER = LoggerFactory.getLogger("witch-hat-atelier-mod/paper");
    private static final long AUTOSAVE_INTERVAL_MS = 30_000L;
    private static final long CHECKPOINT_INTERVAL_MS = 5 * 60_000L;

    private final PaperCanvas canvas = new PaperCanvas();
    private final PaperCanvasRenderer renderer = new PaperCanvasRenderer();
    private final PaperDocumentStore store;
    private BrushSettings brush = BrushSettings.DEFAULT;
    private int canvasWidth = 420;
    private int canvasHeight = 280;
    private long settingsRevision;
    private long autosavedCanvasRevision;
    private long autosavedSettingsRevision;
    private long checkpointCanvasRevision;
    private long checkpointSettingsRevision;
    private long lastAutosaveAt = System.currentTimeMillis();
    private long lastCheckpointAt = System.currentTimeMillis();

    public PaperWorkspace(Path gameDirectory) {
        store = new PaperDocumentStore(gameDirectory);
        restoreAutosave();
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
        try {
            PaperCanvasState state = snapshot();
            store.save(store.manualSavePath(), state);
            store.save(store.autosavePath(), state);
            markAutosaved();
            return "Saved paper.wha.json";
        } catch (IOException exception) {
            LOGGER.error("Could not save paper drawing", exception);
            return "Save failed: " + exception.getMessage();
        }
    }

    public String load() {
        if (!Files.isRegularFile(store.manualSavePath())) {
            return "No paper.wha.json save found";
        }
        try {
            apply(store.load(store.manualSavePath()));
            store.save(store.autosavePath(), snapshot());
            markAutosaved();
            markCheckpointed();
            return "Loaded paper.wha.json";
        } catch (IOException | IllegalArgumentException exception) {
            LOGGER.error("Could not load paper drawing", exception);
            return "Load failed: " + exception.getMessage();
        }
    }

    public String exportPng() {
        try {
            store.ensureDirectory();
            renderer.exportPng(store.pngPath(), canvas, canvasWidth, canvasHeight);
            return "Exported paper.png";
        } catch (IOException exception) {
            LOGGER.error("Could not export paper PNG", exception);
            return "PNG export failed: " + exception.getMessage();
        }
    }

    public void tick() {
        long now = System.currentTimeMillis();
        if (isAutosaveDirty() && now - lastAutosaveAt >= AUTOSAVE_INTERVAL_MS) {
            autosave();
            lastAutosaveAt = now;
        }
        if (isCheckpointDirty() && now - lastCheckpointAt >= CHECKPOINT_INTERVAL_MS) {
            checkpoint();
            lastCheckpointAt = now;
        }
    }

    public void saveOnShutdown() {
        canvas.finishActiveStroke();
        if (isAutosaveDirty()) {
            autosave();
        }
    }

    private void restoreAutosave() {
        if (!store.hasAutosave()) {
            markAutosaved();
            markCheckpointed();
            return;
        }
        try {
            apply(store.load(store.autosavePath()));
            markAutosaved();
            markCheckpointed();
        } catch (IOException | IllegalArgumentException exception) {
            LOGGER.error("Could not restore paper autosave", exception);
            restoreLatestCheckpoint();
        }
    }

    private void restoreLatestCheckpoint() {
        try {
            var latest = store.latestCheckpoint();
            if (latest.isPresent()) {
                apply(store.load(latest.get()));
                markAutosaved();
                markCheckpointed();
                LOGGER.info("Recovered paper drawing from {}", latest.get().getFileName());
            }
        } catch (IOException | IllegalArgumentException exception) {
            LOGGER.error("Could not restore latest paper checkpoint", exception);
        }
    }

    private void autosave() {
        try {
            store.save(store.autosavePath(), snapshot());
            markAutosaved();
        } catch (IOException exception) {
            LOGGER.error("Could not autosave paper drawing", exception);
        }
    }

    private void checkpoint() {
        try {
            store.checkpoint(snapshot());
            markCheckpointed();
        } catch (IOException exception) {
            LOGGER.error("Could not checkpoint paper drawing", exception);
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

    private boolean isAutosaveDirty() {
        return canvas.revision() != autosavedCanvasRevision || settingsRevision != autosavedSettingsRevision;
    }

    private boolean isCheckpointDirty() {
        return canvas.revision() != checkpointCanvasRevision || settingsRevision != checkpointSettingsRevision;
    }

    private void markAutosaved() {
        autosavedCanvasRevision = canvas.revision();
        autosavedSettingsRevision = settingsRevision;
    }

    private void markCheckpointed() {
        checkpointCanvasRevision = canvas.revision();
        checkpointSettingsRevision = settingsRevision;
    }
}
