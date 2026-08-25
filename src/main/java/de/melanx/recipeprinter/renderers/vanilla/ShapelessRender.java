package de.melanx.recipeprinter.renderers.vanilla;

import de.melanx.recipeprinter.IRecipeRender;
import de.melanx.recipeprinter.util.OverlayIcon;
import de.melanx.recipeprinter.util.RenderHelperMod;
import de.melanx.recipeprinter.util.Util;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import org.joml.Matrix3x2fStack;

import javax.annotation.Nullable;
import java.util.List;

public class ShapelessRender implements IRecipeRender<ShapelessRecipe> {

    public static final Identifier BACKGROUND_TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/crafting_table.png");

    @Override
    public Class<ShapelessRecipe> getRecipeClass() {
        return ShapelessRecipe.class;
    }

    @Nullable
    @Override
    public RecipeType<? super ShapelessRecipe> getRecipeType() {
        return RecipeType.CRAFTING;
    }

    @Override
    public int getRecipeWidth() {
        return 124;
    }

    @Override
    public int getRecipeHeight() {
        return 62;
    }

    @Override
    public void render(ShapelessRecipe recipe, GuiGraphicsExtractor guiGraphics) {
        Matrix3x2fStack poseStack = guiGraphics.pose();
        RenderHelperMod.renderBackground(BACKGROUND_TEXTURE, guiGraphics, 25, 12, 124, 62, true);
        poseStack.pushMatrix();
        poseStack.translate(108, 4);
        poseStack.scale(1f / 3, 1f / 3);
        RenderHelperMod.render(OverlayIcon.SHAPELESS, guiGraphics, 0, 0);
        poseStack.popMatrix();
        RenderHelperMod.renderItem(guiGraphics, Util.getResultItem(recipe), 99, 23);
        List<Ingredient> ingredients = recipe.placementInfo().ingredients();
        int max = ingredients.size() > 4 ? 3 : 2;
        for (int x = 0; x < max; x++) {
            for (int y = 0; y < max; y++) {
                int idx = x + (max * y);
                if (ingredients.size() == 1) {
                    RenderHelperMod.renderIngredient(guiGraphics, ingredients.get(idx), 23, 23);
                    return;
                } else if (idx < ingredients.size()) {
                    RenderHelperMod.renderIngredient(guiGraphics, ingredients.get(idx), 5 + (x * 18), 5 + (y * 18));
                }
            }
        }
    }
}
