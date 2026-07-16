package net.oshino.witchhatateliermod.client.screen.spellbook.content;

import net.oshino.witchhatateliermod.client.screen.spellbook.model.GlyphType;
import net.oshino.witchhatateliermod.client.screen.spellbook.model.GuidePage;

import java.util.List;

public final class SpellbookContent {
    private static final List<GuidePage> PAGES = List.of(
            page("welcome",
                    "begin_here",
                    GlyphType.NONE,
                    "welcome.1",
                    "welcome.2"),
            page("getting_started",
                    "begin_here",
                    GlyphType.NONE,
                    "getting_started.1",
                    "getting_started.2"),
            page("basic_principle", "core_concepts", GlyphType.BASIC, "basic_principle.1", "basic_principle.2"),
            page("glyphs", "core_concepts", GlyphType.ANATOMY, "glyphs.1", "glyphs.2", "glyphs.3", "glyphs.4"),
            page("items", "field_guide", GlyphType.NONE, "items.1", "items.2"),
            page("blocks", "field_guide", GlyphType.NONE, "blocks.1"),
            page("mobs", "field_guide", GlyphType.NONE, "mobs.1"),
            page("structures", "field_guide", GlyphType.NONE, "structures.1"),
            page("wind", "magic", GlyphType.WIND, "wind.1", "wind.2"),
            page("light", "magic", GlyphType.LIGHT, "light.1", "light.2"),
            page("water", "magic", GlyphType.WATER, "water.1", "water.2"),
            page("credits", "appendix", GlyphType.NONE, "credits.1", "credits.2")
    );

    private SpellbookContent() {
    }

    public static List<GuidePage> pages() {
        return PAGES;
    }

    private static GuidePage page(String title, String section, GlyphType glyph, String... paragraphs) {
        return new GuidePage(title, section, glyph, List.of(paragraphs));
    }
}
