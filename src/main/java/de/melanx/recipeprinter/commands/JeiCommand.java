package de.melanx.recipeprinter.commands;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import de.melanx.recipeprinter.ModConfig;
import de.melanx.recipeprinter.RecipePrinter;
import de.melanx.recipeprinter.jei.PrinterJEI;
import de.melanx.recipeprinter.util.PrinterJob;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.drawable.IScalableDrawable;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.elements.DrawableBlank;
import mezz.jei.library.focus.FocusGroup;
import mezz.jei.library.gui.recipes.RecipeLayout;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.moddingx.libx.render.target.ImageHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class JeiCommand implements Command<CommandSourceStack> {

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        RecipeSelector sel = new RecipeSelector(StringArgumentType.getString(context, "recipes"));
        //noinspection ConstantConditions
        RecipeManager rm = Minecraft.getInstance().getSingleplayerServer().getRecipeManager();
        List<ResourceLocation> recipes = sel.getRecipes(rm);
        return JeiCommand.render(context, recipes);
    }

    public static int render(CommandContext<CommandSourceStack> context, List<ResourceLocation> recipes) {
        if (PrinterJEI.REG == null) {
            context.getSource().sendFailure(Component.literal("JEI recipe categories are not available yet."));
            return 0;
        }

        //noinspection ConstantConditions
        RecipeManager rm = Minecraft.getInstance().getSingleplayerServer().getRecipeManager();
        List<IRecipeCategory<?>> categories = PrinterJEI.REG.getRecipeCategories();

        int rendered = 0;
        Set<ResourceLocation> failedRecipes = new LinkedHashSet<>();
        for (ResourceLocation rl : recipes) {
            RecipeHolder<?> recipeHolder = rm.byKey(rl).orElse(null);
            if (recipeHolder == null) {
                failedRecipes.add(rl);
                continue;
            }

            ResourceLocation recipeType = BuiltInRegistries.RECIPE_TYPE.getKey(recipeHolder.value().getType());
            boolean recipeRendered = false;
            for (IRecipeCategory<?> recipeCategory : categories) {
                if (!recipeCategory.getRecipeType().getUid().equals(recipeType)) {
                    continue;
                }

                try {
                    Optional<IRecipeLayoutDrawable<Object>> optLayout = JeiCommand.createLayout(recipeCategory, recipeHolder);
                    if (optLayout.isEmpty()) {
                        RecipePrinter.getInstance().logger.error("JEI could not create a layout for recipe {}", recipeHolder.id());
                        continue;
                    }

                    Path path = context.getSource().getServer().getServerDirectory()
                            .resolve(RecipePrinter.getInstance().modid)
                            .resolve("jei")
                            .resolve(rl.getNamespace())
                            .resolve(recipeCategory.getClass().getSimpleName())
                            .resolve(rl.getPath().replaceAll("([^/]*/)*", "") + ".png");
                    if (!Files.exists(path.getParent())) {
                        Files.createDirectories(path.getParent());
                    }

                    IRecipeLayoutDrawable<Object> layout = optLayout.get();
                    CompletableFuture<NativeImage> img = ImageHelper.render(new PrinterJob(recipeCategory.getWidth() + 8, recipeCategory.getHeight() + 8, ModConfig.scale,
                            guiGraphics -> {
                                RecipePrinter.getInstance().logger.debug("Printing {} {}", recipeCategory.getRecipeType().getUid(), recipeHolder.id());
                                guiGraphics.pose().translate(4, 4, 0);
                                layout.drawRecipe(guiGraphics, -10, -10);
                            }));
                    img.get().writeToFile(path);
                    rendered++;
                    recipeRendered = true;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    RecipePrinter.getInstance().logger.error("Could not print recipe {}: {}", recipeHolder.id(), e.getMessage());
                    break;
                } catch (IOException | ExecutionException | RuntimeException e) {
                    RecipePrinter.getInstance().logger.error("Could not print recipe {}: {}", recipeHolder.id(), e.getMessage());
                }

            }

            if (!recipeRendered) {
                failedRecipes.add(recipeHolder.id());
            }
        }

        if (rendered == 0) {
            context.getSource().sendFailure(Component.literal("No JEI recipe layouts were rendered."));
        } else {
            int renderedCount = rendered;
            context.getSource().sendSuccess(() -> Component.literal("Successfully printed " + renderedCount + " JEI recipe layouts."), false);
        }

        if (!failedRecipes.isEmpty()) {
            context.getSource().sendFailure(Component.literal("Failed to render " + failedRecipes.size() + " recipes:"));
            for (ResourceLocation failedRecipe : failedRecipes) {
                context.getSource().sendFailure(Component.literal("- " + failedRecipe));
            }
        }

        return rendered;
    }

    private static Optional<IRecipeLayoutDrawable<Object>> createLayout(IRecipeCategory<?> category, RecipeHolder<?> recipe) {
        IScalableDrawable recipeBackground;
        int borderPadding;
        if (category.needsRecipeBorder()) {
            recipeBackground = Internal.getTextures().getRecipeBackground();
            borderPadding = 4;
        } else {
            recipeBackground = DrawableBlank.EMPTY;
            borderPadding = 0;
        }

        //noinspection unchecked
        return RecipeLayout.create((IRecipeCategory<Object>) category, Collections.emptyList(), recipe, FocusGroup.EMPTY,
                PrinterJEI.REG.getJeiHelpers().getIngredientManager(), recipeBackground, borderPadding);
    }
}
