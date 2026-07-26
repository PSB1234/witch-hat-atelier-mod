package net.oshino.witchhatateliermod.client.screen.paper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Path;

/** Serializes a drawing for storage in an individual paper item's custom data. */
public final class PaperDocumentStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path exportPath;

    public PaperDocumentStore(Path gameDirectory) {
        exportPath = gameDirectory.resolve("witch-hat-atelier-mod").resolve("paper.png");
    }

    public String serialize(PaperCanvasState state) {
        return GSON.toJson(state);
    }

    public PaperCanvasState deserialize(String document) throws IOException {
        PaperCanvasState state = GSON.fromJson(document, PaperCanvasState.class);
        if (state == null) {
            throw new IOException("Drawing data is empty");
        }
        return state;
    }

    public Path exportPath() {
        return exportPath;
    }
}
