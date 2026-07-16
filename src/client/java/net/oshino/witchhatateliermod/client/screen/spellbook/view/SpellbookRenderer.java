package net.oshino.witchhatateliermod.client.screen.spellbook.view;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.oshino.witchhatateliermod.client.screen.spellbook.content.SpellbookContent;
import net.oshino.witchhatateliermod.client.screen.spellbook.model.GlyphType;
import net.oshino.witchhatateliermod.client.screen.spellbook.model.GuidePage;

import java.util.List;

public final class SpellbookRenderer {
    private final TextRenderer textRenderer;
    private final List<GuidePage> pages;

    public SpellbookRenderer(TextRenderer textRenderer) {
        this.textRenderer = textRenderer;
        this.pages = SpellbookContent.pages();
    }

    public void render(DrawContext context, SpellbookLayout layout, int pageIndex, int mouseX, int mouseY) {
        drawBook(context, layout);
        if (pageIndex == 0) {
            drawIndex(context, layout, mouseX, mouseY);
        } else {
            drawEntry(context, layout, pages.get(pageIndex - 1));
        }
        drawPageControls(context, layout, pageIndex, mouseX, mouseY);
    }

    private void drawBook(DrawContext context, SpellbookLayout layout) {
        int left = layout.left();
        int top = layout.top();
        context.fill(left - 5, top - 5, left + SpellbookTheme.BOOK_WIDTH + 5, top + SpellbookTheme.BOOK_HEIGHT + 5, SpellbookTheme.COVER_EDGE);
        context.fill(left, top, left + SpellbookTheme.BOOK_WIDTH, top + SpellbookTheme.BOOK_HEIGHT, SpellbookTheme.COVER);
        context.fill(left + 7, top + 7, left + 212, top + SpellbookTheme.BOOK_HEIGHT - 7, SpellbookTheme.VELLUM_SHADOW);
        context.fill(left + 10, top + 7, left + 209, top + SpellbookTheme.BOOK_HEIGHT - 10, SpellbookTheme.VELLUM);
        context.fill(left + 218, top + 7, left + SpellbookTheme.BOOK_WIDTH - 7, top + SpellbookTheme.BOOK_HEIGHT - 7, SpellbookTheme.VELLUM_SHADOW);
        context.fill(left + 221, top + 7, left + SpellbookTheme.BOOK_WIDTH - 10, top + SpellbookTheme.BOOK_HEIGHT - 10, SpellbookTheme.VELLUM);
        context.fill(left + 209, top + 7, left + 221, top + SpellbookTheme.BOOK_HEIGHT - 7, 0xFF80613F);
        context.drawVerticalLine(left + 214, top + 11, top + SpellbookTheme.BOOK_HEIGHT - 12, 0xFF3B2822);
    }

    private void drawIndex(DrawContext context, SpellbookLayout layout, int mouseX, int mouseY) {
        int left = layout.left();
        int top = layout.top();
        drawCentered(context, Text.translatable("guide.witch-hat-atelier-mod.contents"), left + SpellbookTheme.BOOK_WIDTH / 2, top + 17, SpellbookTheme.MAGIC_INK);
        drawCentered(context, Text.translatable("guide.witch-hat-atelier-mod.text.tagline"), left + SpellbookTheme.BOOK_WIDTH / 2, top + 32, SpellbookTheme.FADED_INK);
        context.drawHorizontalLine(left + 24, left + SpellbookTheme.BOOK_WIDTH - 24, top + 46, SpellbookTheme.MAGIC_INK);

        for (int targetPage = 1; targetPage <= pages.size(); targetPage++) {
            GuidePage page = pages.get(targetPage - 1);
            Rect hitbox = layout.indexEntry(targetPage);
            int x = hitbox.x() + 4;
            int y = hitbox.y() + 4;
            boolean hovered = hitbox.contains(mouseX, mouseY);
            if (hovered) {
                context.fill(hitbox.x(), hitbox.y(), hitbox.x() + hitbox.width(), hitbox.y() + hitbox.height(), SpellbookTheme.HOVER);
            }
            context.drawText(textRenderer, roman(targetPage), x, y, SpellbookTheme.FADED_INK, false);
            context.drawText(textRenderer, title(page), x + 24, y, hovered ? SpellbookTheme.MAGIC_INK : SpellbookTheme.INK, false);
            context.drawHorizontalLine(x + 24, x + 170, y + 12, SpellbookTheme.VELLUM_SHADOW);
        }
    }

    private void drawEntry(DrawContext context, SpellbookLayout layout, GuidePage page) {
        int left = layout.left();
        int top = layout.top();
        int leftX = left + 24;
        int rightX = left + 238;
        int columnWidth = 169;

        context.drawText(textRenderer, section(page).getString().toUpperCase(), leftX, top + 17, SpellbookTheme.FADED_INK, false);
        context.drawText(textRenderer, title(page), leftX, top + 33, SpellbookTheme.MAGIC_INK, false);
        context.drawHorizontalLine(leftX, left + 195, top + 46, SpellbookTheme.MAGIC_INK);
        context.drawText(textRenderer, Text.translatable("guide.witch-hat-atelier-mod.field_note"), rightX, top + 17, SpellbookTheme.FADED_INK, false);
        context.drawHorizontalLine(rightX, left + SpellbookTheme.BOOK_WIDTH - 24, top + 30, SpellbookTheme.VELLUM_SHADOW);

        int split = (page.paragraphKeys().size() + 1) / 2;
        int leftY = top + 57;
        int rightY = top + 42;
        for (int i = 0; i < page.paragraphKeys().size(); i++) {
            Text paragraph = Text.translatable("guide.witch-hat-atelier-mod.text." + page.paragraphKeys().get(i));
            if (i < split) {
                leftY = drawParagraph(context, paragraph, leftX, leftY, columnWidth) + 7;
            } else {
                rightY = drawParagraph(context, paragraph, rightX, rightY, columnWidth) + 7;
            }
        }

        if (page.glyph() != GlyphType.NONE) {
            drawGlyph(context, left + 322, top + 171, page.glyph());
        }
    }

    private void drawGlyph(DrawContext context, int cx, int cy, GlyphType type) {
        int color = SpellbookTheme.MAGIC_INK;
        int radius = 27;
        context.drawHorizontalLine(cx - radius, cx + radius, cy - radius, color);
        context.drawHorizontalLine(cx - radius, cx + radius, cy + radius, color);
        context.drawVerticalLine(cx - radius, cy - radius, cy + radius, color);
        context.drawVerticalLine(cx + radius, cy - radius, cy + radius, color);
        context.drawHorizontalLine(cx - 19, cx + 19, cy, color);
        context.drawVerticalLine(cx, cy - 19, cy + 19, color);

        switch (type) {
            case BASIC, ANATOMY -> {
                context.drawHorizontalLine(cx - 14, cx + 14, cy - 13, color);
                context.drawVerticalLine(cx - 14, cy - 13, cy + 13, color);
                context.drawHorizontalLine(cx - 14, cx + 14, cy + 13, color);
                context.drawVerticalLine(cx + 14, cy - 13, cy + 13, color);
            }
            case WIND -> {
                context.drawHorizontalLine(cx - 17, cx + 10, cy - 10, color);
                context.drawHorizontalLine(cx - 10, cx + 17, cy + 10, color);
            }
            case LIGHT -> context.fill(cx - 5, cy - 5, cx + 6, cy + 6, color);
            case WATER -> {
                context.drawVerticalLine(cx - 11, cy - 15, cy + 15, color);
                context.drawVerticalLine(cx + 11, cy - 15, cy + 15, color);
            }
            case NONE -> { }
        }
    }

    private void drawPageControls(DrawContext context, SpellbookLayout layout, int pageIndex, int mouseX, int mouseY) {
        int left = layout.left();
        int y = layout.top() + SpellbookTheme.BOOK_HEIGHT - 22;
        if (pageIndex > 0) {
            context.drawText(textRenderer, Text.translatable("guide.witch-hat-atelier-mod.index_link"), layout.indexLink().x() + 3, y,
                    layout.indexLink().contains(mouseX, mouseY) ? SpellbookTheme.MAGIC_INK : SpellbookTheme.FADED_INK, false);
            context.drawText(textRenderer, "<", layout.previousPage().x() + 7, y,
                    layout.previousPage().contains(mouseX, mouseY) ? SpellbookTheme.MAGIC_INK : SpellbookTheme.FADED_INK, false);
        }
        if (pageIndex < pages.size()) {
            context.drawText(textRenderer, ">", layout.nextPage().x() + 10, y,
                    layout.nextPage().contains(mouseX, mouseY) ? SpellbookTheme.MAGIC_INK : SpellbookTheme.FADED_INK, false);
        }
        drawCentered(context, Text.literal((pageIndex + 1) + " / " + (pages.size() + 1)), left + 107, y, SpellbookTheme.FADED_INK);
    }

    private int drawParagraph(DrawContext context, Text paragraph, int x, int y, int width) {
        for (OrderedText line : textRenderer.wrapLines(paragraph, width)) {
            context.drawText(textRenderer, line, x, y, SpellbookTheme.INK, false);
            y += 10;
        }
        return y;
    }

    private void drawCentered(DrawContext context, Text text, int centerX, int y, int color) {
        context.drawText(textRenderer, text, centerX - textRenderer.getWidth(text) / 2, y, color, false);
    }

    private static Text title(GuidePage page) {
        return Text.translatable("guide.witch-hat-atelier-mod.page." + page.titleKey());
    }

    private static Text section(GuidePage page) {
        return Text.translatable("guide.witch-hat-atelier-mod.section." + page.sectionKey());
    }

    private static String roman(int number) {
        return switch (number) {
            case 1 -> "I"; case 2 -> "II"; case 3 -> "III"; case 4 -> "IV";
            case 5 -> "V"; case 6 -> "VI"; case 7 -> "VII"; case 8 -> "VIII";
            case 9 -> "IX"; case 10 -> "X"; case 11 -> "XI"; default -> "XII";
        };
    }
}
