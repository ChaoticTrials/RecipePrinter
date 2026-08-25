package de.melanx.recipeprinter.util;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.MultiBufferSource;
import org.moddingx.libx.render.target.RenderJob;

import java.util.function.Consumer;

public class PrinterJob implements RenderJob {

    private final int width;
    private final int height;
    private final int scale;
    private final Consumer<GuiGraphicsExtractor> renderFunc;

    public PrinterJob(int width, int height, int scale, Consumer<GuiGraphicsExtractor> renderFunc) {
        this.width = width;
        this.height = height;
        this.scale = Math.max(1, scale);
        this.renderFunc = renderFunc;
    }

    @Override
    public int width() {
        return this.width * this.scale;
    }

    @Override
    public int height() {
        return this.height * this.scale;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer) {
        // All content is drawn via renderGui; this job has no 3D-scene content of its own.
    }

    @Override
    public boolean usesGui() {
        return true;
    }

    @Override
    public void renderGui(GuiGraphicsExtractor guiGraphics) {
        // The bridge's viewport is unscaled (guiScale 1), so we bake this job's scale into the pose
        // stack ourselves; render functions are written in logical (unscaled) pixel coordinates.
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().scale(this.scale, this.scale);
        this.renderFunc.accept(guiGraphics);
        guiGraphics.pose().popMatrix();
    }
}
