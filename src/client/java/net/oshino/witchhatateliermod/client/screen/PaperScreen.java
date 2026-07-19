package net.oshino.witchhatateliermod.client.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public final class PaperScreen extends Screen {
    private static final Text PLACEHOLDER_TEXT = Text.translatable("screen.witch-hat-atelier-mod.paper.placeholder");

    public PaperScreen() {
        super(Text.translatable("screen.witch-hat-atelier-mod.paper"));
    }
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 2 - 10, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, PLACEHOLDER_TEXT, width / 2, height / 2 + 10, 0xA0A0A0);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
