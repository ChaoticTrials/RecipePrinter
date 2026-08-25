package de.melanx.recipeprinter.util;

import de.melanx.recipeprinter.RecipePrinter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import org.moddingx.libx.render.RenderHelper;
import org.moddingx.libx.render.RenderHelperFluid;

import java.awt.Color;

public class RenderHelperMod {

    public static final int COLOR_GUI_BACKGROUND = 0xFFC6C6C6;
    public static final int COLOR_GUI_BACKGROUND_FRAME_INNER_BR = 0xFFB3B3B3;
    public static final int COLOR_GUI_BACKGROUND_FRAME_INNER_TL = 0xFFD8D8D8;
    public static final int COLOR_GUI_BACKGROUND_FRAME_OUTER = 0xFF999999;

    public static final int TEXT_COLOR = Color.DARK_GRAY.getRGB();
    public static final Identifier TEXTURE_ICONS = Identifier.fromNamespaceAndPath(RecipePrinter.getInstance().modid, "textures/gui/icons.png");

    public static void renderBackground(Identifier texture, GuiGraphicsExtractor guiGraphics, int x, int y, int width, int height, int textureWidth, int textureHeight, boolean includeFrame) {
        RenderHelper.renderGuiBackground(RenderPipelines.GUI_TEXTURED, guiGraphics, x, y, width, height);
        RenderHelper.resetColor();
        if (includeFrame) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, 2, 2, x + 2, y + 2, width - 4, height - 4, textureWidth, textureHeight);
            applyFrame(guiGraphics, width, height);
        } else {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, 0, 0, x, y, width, height, textureWidth, textureHeight);
        }
        guiGraphics.pose().translate(0, 0);
    }

    public static void renderBackground(Identifier texture, GuiGraphicsExtractor guiGraphics, int x, int y, int width, int height, boolean includeFrame) {
        RenderHelperMod.renderBackground(texture, guiGraphics, x, y, width, height, 256, 256, includeFrame);
    }

    public static void renderDefaultBackground(GuiGraphicsExtractor guiGraphics, int width, int height) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, RenderHelper.TEXTURE_WHITE, 2, 2, 0, 0, width - 4, height - 4, 256, 256, COLOR_GUI_BACKGROUND);

        applyFrame(guiGraphics, width, height);

        guiGraphics.pose().translate(0, 0);
    }

    private static void applyFrame(GuiGraphicsExtractor guiGraphics, int width, int height) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, RenderHelper.TEXTURE_WHITE, 2, 1, 0, 0, width - 4, 1, 256, 256, COLOR_GUI_BACKGROUND_FRAME_INNER_TL);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, RenderHelper.TEXTURE_WHITE, 1, 2, 0, 0, 1, height - 4, 256, 256, COLOR_GUI_BACKGROUND_FRAME_INNER_TL);

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, RenderHelper.TEXTURE_WHITE, 2, height - 2, 0, 0, width - 4, 1, 256, 256, COLOR_GUI_BACKGROUND_FRAME_INNER_BR);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, RenderHelper.TEXTURE_WHITE, width - 2, 2, 0, 0, 1, height - 4, 256, 256, COLOR_GUI_BACKGROUND_FRAME_INNER_BR);

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, RenderHelper.TEXTURE_WHITE, 1, 1, 0, 0, 1, 1, 256, 256, COLOR_GUI_BACKGROUND_FRAME_OUTER);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, RenderHelper.TEXTURE_WHITE, 1, height - 2, 0, 0, 1, 1, 256, 256, COLOR_GUI_BACKGROUND_FRAME_OUTER);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, RenderHelper.TEXTURE_WHITE, width - 2, 1, 0, 0, 1, 1, 256, 256, COLOR_GUI_BACKGROUND_FRAME_OUTER);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, RenderHelper.TEXTURE_WHITE, width - 2, height - 2, 0, 0, 1, 1, 256, 256, COLOR_GUI_BACKGROUND_FRAME_OUTER);

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, RenderHelper.TEXTURE_WHITE, 2, 0, 0, 0, width - 4, 1, 256, 256, COLOR_GUI_BACKGROUND_FRAME_OUTER);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, RenderHelper.TEXTURE_WHITE, 0, 2, 0, 0, 1, height - 4, 256, 256, COLOR_GUI_BACKGROUND_FRAME_OUTER);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, RenderHelper.TEXTURE_WHITE, 2, height - 1, 0, 0, width - 4, 1, 256, 256, COLOR_GUI_BACKGROUND_FRAME_OUTER);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, RenderHelper.TEXTURE_WHITE, width - 1, 2, 0, 0, 1, height - 4, 256, 256, COLOR_GUI_BACKGROUND_FRAME_OUTER);
    }

    public static void render(OverlayIcon icon, GuiGraphicsExtractor guiGraphics, int x, int y) {
        RenderHelperMod.render(icon, guiGraphics, x, y, icon.width, icon.height);
    }

    public static void render(OverlayIcon icon, GuiGraphicsExtractor guiGraphics, int x, int y, int width, int height) {
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(x, y);
        guiGraphics.pose().scale((float) width / icon.width, (float) height / icon.height);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, icon.texture, 0, 0, icon.u, icon.v, icon.width, icon.height, 256, 256);
        guiGraphics.pose().popMatrix();
    }

    public static void renderSlot(GuiGraphicsExtractor guiGraphics, int x, int y) {
        render(OverlayIcon.SLOT, guiGraphics, x - 1, y - 1);
    }

    public static void renderBigSlot(GuiGraphicsExtractor guiGraphics, int x, int y) {
        render(OverlayIcon.BIG_SLOT, guiGraphics, x - 5, y - 5);
    }

    public static void renderItem(GuiGraphicsExtractor guiGraphics, ItemStack stack, int x, int y) {
        guiGraphics.item(stack, x, y);
        guiGraphics.itemDecorations(Minecraft.getInstance().font, stack, x, y);
    }

    public static void renderIngredient(GuiGraphicsExtractor guiGraphics, Ingredient ingredient, int x, int y) {
        if (!ingredient.isEmpty()) {
            // Ingredient#getValues() throws for NeoForge custom ingredients (e.g. the chest boat
            // recipes' compound ingredient); #items() is the generic accessor that works for both.
            ingredient.items().findFirst().ifPresent(item -> RenderHelperMod.renderItem(guiGraphics, new ItemStack(item), x, y));
        }
    }

    public static void renderBlockState(GuiGraphicsExtractor guiGraphics, BlockState state, int x, int y) {
        //noinspection deprecation
        if (!state.getFluidState().isEmpty() && state.liquid()) {
            RenderHelperFluid.renderFluid(guiGraphics, new FluidStack(state.getFluidState().getType(), 1000), x, y, 16, 16);
        } else {
            // noinspection ConstantConditions
            RenderHelperMod.renderItem(guiGraphics, state.getBlock().getCloneItemStack(Minecraft.getInstance().level, BlockPos.ZERO, state, true, Minecraft.getInstance().player), x, y);
        }
    }
}
