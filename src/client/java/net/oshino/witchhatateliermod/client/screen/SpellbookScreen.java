package net.oshino.witchhatateliermod.client.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.oshino.witchhatateliermod.client.screen.spellbook.content.SpellbookContent;
import net.oshino.witchhatateliermod.client.screen.spellbook.view.SpellbookLayout;
import net.oshino.witchhatateliermod.client.screen.spellbook.view.SpellbookRenderer;

public final class SpellbookScreen extends Screen {
    private SpellbookRenderer renderer;
    private int pageIndex;

    public SpellbookScreen() {
        super(Text.translatable("screen.witch-hat-atelier-mod.spellbook"));
    }

    @Override
    protected void init() {
        renderer = new SpellbookRenderer(textRenderer);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderer.render(context, layout(), pageIndex, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false;
        }

        SpellbookLayout layout = layout();
        if (pageIndex == 0) {
            for (int targetPage = 1; targetPage <= SpellbookContent.pages().size(); targetPage++) {
                if (layout.indexEntry(targetPage).contains(mouseX, mouseY)) {
                    pageIndex = targetPage;
                    return true;
                }
            }
        }

        if (pageIndex > 0 && layout.indexLink().contains(mouseX, mouseY)) {
            pageIndex = 0;
            return true;
        }
        if (pageIndex > 0 && layout.previousPage().contains(mouseX, mouseY)) {
            pageIndex--;
            return true;
        }
        if (pageIndex < SpellbookContent.pages().size() && layout.nextPage().contains(mouseX, mouseY)) {
            pageIndex++;
            return true;
        }
        return false;
    }

    private SpellbookLayout layout() {
        return SpellbookLayout.centered(width, height);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
