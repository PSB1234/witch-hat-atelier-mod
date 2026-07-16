package net.oshino.witchhatateliermod.client.screen.spellbook.view;

public record SpellbookLayout(int left, int top) {
    private static final int INDEX_ROWS = 6;

    public static SpellbookLayout centered(int screenWidth, int screenHeight) {
        return new SpellbookLayout(
                (screenWidth - SpellbookTheme.BOOK_WIDTH) / 2,
                (screenHeight - SpellbookTheme.BOOK_HEIGHT) / 2
        );
    }

    public Rect indexEntry(int targetPage) {
        int slot = targetPage - 1;
        int column = slot / INDEX_ROWS;
        int row = slot % INDEX_ROWS;
        return new Rect(left + 20 + column * 215, top + 58 + row * 25, 178, 19);
    }

    public Rect previousPage() {
        return new Rect(left + 17, top + SpellbookTheme.BOOK_HEIGHT - 25, 27, 15);
    }

    public Rect nextPage() {
        return new Rect(left + SpellbookTheme.BOOK_WIDTH - 40, top + SpellbookTheme.BOOK_HEIGHT - 25, 26, 15);
    }

    public Rect indexLink() {
        return new Rect(left + 235, top + SpellbookTheme.BOOK_HEIGHT - 25, 43, 15);
    }
}
