package net.oshino.witchhatateliermod.paper;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;

import java.util.Optional;
import java.util.UUID;

/** The persistent, stack-specific document carried by a drawn paper item. */
public final class PaperDocumentItemData {
    private static final String ROOT_KEY = "witch_hat_atelier_paper";
    private static final String ID_KEY = "id";
    private static final String TITLE_KEY = "title";
    private static final String DRAWING_KEY = "drawing";

    private PaperDocumentItemData() {
    }

    public static Optional<Document> read(ItemStack stack) {
        NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData == null) {
            return Optional.empty();
        }
        NbtCompound root = customData.copyNbt();
        if (!root.contains(ROOT_KEY, NbtElement.COMPOUND_TYPE)) {
            return Optional.empty();
        }
        NbtCompound paper = (NbtCompound) root.get(ROOT_KEY);
        if (paper == null || !paper.contains(DRAWING_KEY, NbtElement.STRING_TYPE)) {
            return Optional.empty();
        }
        return Optional.of(new Document(paper.getString(ID_KEY), paper.getString(TITLE_KEY), paper.getString(DRAWING_KEY)));
    }

    public static void write(ItemStack stack, String title, String drawing) {
        String id = read(stack).map(Document::id).filter(value -> !value.isBlank())
                .orElseGet(() -> UUID.randomUUID().toString());
        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, root -> {
            NbtCompound paper = new NbtCompound();
            paper.putString(ID_KEY, id);
            paper.putString(TITLE_KEY, title);
            paper.putString(DRAWING_KEY, drawing);
            root.put(ROOT_KEY, paper);
        });
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(title));
    }

    public record Document(String id, String title, String drawing) {
    }
}
