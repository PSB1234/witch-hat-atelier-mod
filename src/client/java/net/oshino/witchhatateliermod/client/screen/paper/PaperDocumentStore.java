package net.oshino.witchhatateliermod.client.screen.paper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Owns all on-disk drawing, PNG, autosave, and checkpoint files. */
public final class PaperDocumentStore {
    private static final DateTimeFormatter CHECKPOINT_TIME = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final int MAX_CHECKPOINTS = 10;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path directory;
    private final Path checkpointsDirectory;

    public PaperDocumentStore(Path gameDirectory) {
        directory = gameDirectory.resolve("witch-hat-atelier-mod").resolve("paper");
        checkpointsDirectory = directory.resolve("checkpoints");
    }

    public Path manualSavePath() {
        return directory.resolve("paper.wha.json");
    }

    public Path autosavePath() {
        return directory.resolve("autosave.wha.json");
    }

    public Path pngPath() {
        return directory.resolve("paper.png");
    }

    public boolean hasAutosave() {
        return Files.isRegularFile(autosavePath());
    }

    public void save(Path path, PaperCanvasState state) throws IOException {
        Files.createDirectories(path.getParent());
        Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
        try (Writer writer = Files.newBufferedWriter(temporary)) {
            GSON.toJson(state, writer);
        }
        try {
            Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public PaperCanvasState load(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path)) {
            PaperCanvasState state = GSON.fromJson(reader, PaperCanvasState.class);
            if (state == null) {
                throw new IOException("Drawing file is empty");
            }
            return state;
        }
    }

    public void checkpoint(PaperCanvasState state) throws IOException {
        Files.createDirectories(checkpointsDirectory);
        Path checkpoint = checkpointsDirectory.resolve(
                "paper-" + CHECKPOINT_TIME.format(LocalDateTime.now()) + ".wha.json");
        save(checkpoint, state);
        pruneCheckpoints();
    }

    public Optional<Path> latestCheckpoint() throws IOException {
        if (!Files.isDirectory(checkpointsDirectory)) {
            return Optional.empty();
        }
        try (var files = Files.list(checkpointsDirectory)) {
            return files.filter(path -> path.getFileName().toString().endsWith(".wha.json"))
                    .max(Comparator.comparingLong(this::lastModified));
        }
    }

    public void ensureDirectory() throws IOException {
        Files.createDirectories(directory);
    }

    private void pruneCheckpoints() throws IOException {
        List<Path> checkpoints;
        try (var files = Files.list(checkpointsDirectory)) {
            checkpoints = files.filter(path -> path.getFileName().toString().endsWith(".wha.json"))
                    .sorted(Comparator.comparingLong(this::lastModified).reversed())
                    .toList();
        }
        for (int index = MAX_CHECKPOINTS; index < checkpoints.size(); index++) {
            Files.deleteIfExists(checkpoints.get(index));
        }
    }

    private long lastModified(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException ignored) {
            return 0L;
        }
    }
}
