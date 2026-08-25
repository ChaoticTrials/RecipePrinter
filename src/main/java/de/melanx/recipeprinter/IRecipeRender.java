package de.melanx.recipeprinter;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import javax.annotation.Nullable;

public interface IRecipeRender<T extends Recipe<?>> {

    Class<T> getRecipeClass();

    @Nullable
    RecipeType<? super T> getRecipeType();

    int getRecipeWidth();

    int getRecipeHeight();

    void render(T recipe, GuiGraphicsExtractor guiGraphics);

    default double getScaleFactor() {
        return ModConfig.scale;
    }
}
