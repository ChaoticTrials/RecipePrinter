package de.melanx.recipeprinter.renderers.vanilla;

import de.melanx.recipeprinter.IRecipeRender;
import de.melanx.recipeprinter.RecipePrinter;
import de.melanx.recipeprinter.util.RenderHelperMod;
import de.melanx.recipeprinter.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import org.joml.Matrix3x2fStack;
import org.moddingx.libx.render.RenderHelper;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class SmeltingRender implements IRecipeRender<SmeltingRecipe> {

    public static final Identifier BACKGROUND_TEXTURE = Identifier.fromNamespaceAndPath("minecraft", "textures/gui/container/furnace.png");

    @Override
    public Class<SmeltingRecipe> getRecipeClass() {
        return SmeltingRecipe.class;
    }

    @Nullable
    @Override
    public RecipeType<? super SmeltingRecipe> getRecipeType() {
        return RecipeType.SMELTING;
    }

    @Override
    public int getRecipeWidth() {
        return 90;
    }

    @Override
    public int getRecipeHeight() {
        return 62;
    }

    @Override
    public void render(SmeltingRecipe recipe, GuiGraphicsExtractor guiGraphics) {
        SmeltingRender.commonRender(recipe, guiGraphics);
        RenderHelperMod.renderItem(guiGraphics, new ItemStack(Items.FURNACE), 5, 41);
    }

    public static void commonRender(AbstractCookingRecipe recipe, GuiGraphicsExtractor guiGraphics) {
        Matrix3x2fStack poseStack = guiGraphics.pose();
        RenderHelperMod.renderBackground(BACKGROUND_TEXTURE, guiGraphics, 51, 12, 90, 62, true);
        poseStack.pushMatrix();
        poseStack.translate(6, 25);
        RenderHelperMod.renderBackground(BACKGROUND_TEXTURE, guiGraphics, 176, 0, 14, 14, false);
        poseStack.popMatrix();
        RenderHelperMod.renderItem(guiGraphics, Util.getResultItem(recipe), 65, 23);
        RenderHelperMod.renderIngredient(guiGraphics, recipe.placementInfo().ingredients().getFirst(), 5, 5);
        MutableComponent time = Component.translatable(RecipePrinter.getInstance().modid + ".time", BigDecimal.valueOf(recipe.cookingTime() / 20d).setScale(2, RoundingMode.HALF_UP).toPlainString());
        guiGraphics.text(Minecraft.getInstance().font, time.getString(), 26, 48, RenderHelperMod.TEXT_COLOR, false);
        RenderHelper.resetColor();
    }
}
