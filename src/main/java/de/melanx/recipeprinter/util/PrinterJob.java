package de.melanx.recipeprinter.util;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.moddingx.libx.render.target.RenderJob;

import java.util.function.Consumer;

public class PrinterJob implements RenderJob {

    private final int width;
    private final int height;
    private final int scale;
    private final Consumer<GuiGraphics> renderFunc;

    public PrinterJob(int width, int height, int scale, Consumer<GuiGraphics> renderFunc) {
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
    public Matrix4f setupModelViewMatrix() {
        return new Matrix4f().translate(0, 0, -(1000 - GuiGraphics.MIN_GUI_Z));
    }

    @Override
    public void render(PoseStack ignored, MultiBufferSource buffer) {
        MultiBufferSource.BufferSource bs = (MultiBufferSource.BufferSource) buffer;

        // ✅ Use an ortho that actually contains the Z you draw at (0..100)
        Matrix4f proj = new Matrix4f().setOrtho(0f, (float) this.width, (float) this.height, 0f, GuiGraphics.MIN_GUI_Z, GuiGraphics.MAX_GUI_Z);
        RenderSystem.setProjectionMatrix(proj, VertexSorting.ORTHOGRAPHIC_Z);

        Matrix4fStack mv = RenderSystem.getModelViewStack();
        mv.identity();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        Lighting.setupFor3DItems();

        GuiGraphics guiGraphics = new GuiGraphics(Minecraft.getInstance(), bs);

        this.renderFunc.accept(guiGraphics);
        guiGraphics.flush();
    }
}
