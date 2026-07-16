package net.oshino.witchhatateliermod.client.screen.spellbook.model;

import java.util.List;

public record GuidePage(String titleKey, String sectionKey, GlyphType glyph, List<String> paragraphKeys) {
    public GuidePage {
        paragraphKeys = List.copyOf(paragraphKeys);
    }
}
