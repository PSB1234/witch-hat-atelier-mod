package net.oshino.witchhatateliermod.client.screen.paper;

public record BrushSettings(float hardness, int stabilization) {
    public static final BrushSettings DEFAULT = new BrushSettings(1.0F, 2);

    public BrushSettings {
        hardness = Math.clamp(hardness, 0.25F, 1.0F);
        stabilization = Math.clamp(stabilization, 0, 4);
    }

    public BrushSettings nextHardness() {
        float next = hardness >= 1.0F ? 0.25F : hardness + 0.25F;
        return new BrushSettings(next, stabilization);
    }

    public BrushSettings nextStabilization() {
        return new BrushSettings(hardness, (stabilization + 1) % 5);
    }

    public int hardnessPercent() {
        return Math.round(hardness * 100.0F);
    }
}
