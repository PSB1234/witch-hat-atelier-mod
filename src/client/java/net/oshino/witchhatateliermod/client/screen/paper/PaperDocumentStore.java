package net.oshino.witchhatateliermod.client.screen.paper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Serializes a drawing for storage in an individual paper item's custom data.
 */
public record PaperDocumentStore(Path exportPath) {
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  public PaperDocumentStore(Path exportPath) {
    this.exportPath = exportPath.resolve("witch-hat-atelier-mod").resolve("paper.png");
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
}
