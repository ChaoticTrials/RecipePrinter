package de.melanx.recipeprinter.renderers.vanilla;

import de.melanx.recipeprinter.IRecipeRender;
import de.melanx.recipeprinter.util.RenderHelperMod;
import de.melanx.recipeprinter.util.Util;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class ShapedRender implements IRecipeRender<ShapedRecipe> {

    @Override
    public Class<ShapedRecipe> getRecipeClass() {
        return ShapedRecipe.class;
    }

    @Nullable
    @Override
    public RecipeType<? super ShapedRecipe> getRecipeType() {
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
    public void render(ShapedRecipe recipe, GuiGraphicsExtractor guiGraphics) {
        RenderHelperMod.renderBackground(ShapelessRender.BACKGROUND_TEXTURE, guiGraphics, 25, 12, 124, 62, true);
        RenderHelperMod.renderItem(guiGraphics, Util.getResultItem(recipe), 99, 23);
        List<Optional<Ingredient>> ingredients = recipe.getIngredients();
        for (int x = 0; x < recipe.getWidth(); x++) {
            for (int y = 0; y < recipe.getHeight(); y++) {
                int idx = x + (recipe.getWidth() * y);
                if (idx < ingredients.size()) {
                    int finalX = x;
                    int finalY = y;
                    ingredients.get(idx).ifPresent(ingredient -> RenderHelperMod.renderIngredient(guiGraphics, ingredient, 5 + (finalX * 18), 5 + (finalY * 18)));
                }
            }
        }
    }
}
