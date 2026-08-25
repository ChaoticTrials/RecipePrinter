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
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.elements.DrawableBlank;
import mezz.jei.library.focus.FocusGroup;
import mezz.jei.library.gui.recipes.RecipeLayout;
import mezz.jei.library.ingredients.IIngredientManagerInternal;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
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
        List<RecipeHolder<?>> recipes = sel.getRecipes(rm);
        return JeiCommand.render(context, recipes);
    }

    public static int render(CommandContext<CommandSourceStack> context, List<RecipeHolder<?>> recipes) {
        if (PrinterJEI.REG == null) {
            context.getSource().sendFailure(Component.literal("JEI recipe categories are not available yet."));
            return 0;
        }

        List<IRecipeCategory<?>> categories = PrinterJEI.REG.getRecipeCategories();
        IFocusFactory focusFactory = PrinterJEI.REG.getJeiHelpers().getFocusFactory();

        int rendered = 0;
        Set<Identifier> failedRecipes = new LinkedHashSet<>();
        for (RecipeHolder<?> recipeHolder : recipes) {
            if (recipeHolder == null) {
                continue;
            }

            Identifier recipeType = BuiltInRegistries.RECIPE_TYPE.getKey(recipeHolder.value().getType());
            boolean recipeRendered = false;
            for (IRecipeCategory<?> recipeCategory : categories) {
                if (!recipeCategory.getRecipeType().getUid().equals(recipeType)) {
                    continue;
                }

                try {
                    // Build once, unfocused, purely to discover each slot's candidate ingredients and their order.
                    Optional<IRecipeLayoutDrawable<Object>> discoveryOpt = JeiCommand.createLayout(recipeCategory, recipeHolder, FocusGroup.EMPTY);
                    if (discoveryOpt.isEmpty()) {
                        RecipePrinter.getInstance().logger.error("JEI could not create a layout for recipe {}", recipeHolder.id());
                        continue;
                    }

                    // Any slot with more than one candidate ingredient (e.g. a tag) gets pinned to its first
                    // candidate via a focus, so the displayed item is deterministic instead of relying on
                    // JEI's own (wall-clock-based) cycling, which could show any tag member.
                    List<IFocus<?>> pins = new ArrayList<>();
                    for (IRecipeSlotView slotView : discoveryOpt.get().getRecipeSlotsView().getSlotViews()) {
                        List<ITypedIngredient<?>> candidates = slotView.getAllIngredientsList().stream().filter(Objects::nonNull).toList();
                        if (candidates.size() > 1) {
                            //noinspection unchecked,rawtypes
                            pins.add(focusFactory.createFocus(slotView.getRole(), (ITypedIngredient) candidates.getFirst()));
                        }
                    }

                    IRecipeLayoutDrawable<Object> layout;
                    if (pins.isEmpty()) {
                        layout = discoveryOpt.get();
                    } else {
                        IFocusGroup focusGroup = focusFactory.createFocusGroup(pins);
                        Optional<IRecipeLayoutDrawable<Object>> pinnedOpt = JeiCommand.createLayout(recipeCategory, recipeHolder, focusGroup);
                        if (pinnedOpt.isEmpty()) {
                            continue;
                        }
                        layout = pinnedOpt.get();
                    }

                    Path path = context.getSource().getServer().getServerDirectory()
                            .resolve(RecipePrinter.getInstance().modid)
                            .resolve("jei")
                            .resolve(recipeHolder.id().identifier().getNamespace())
                            .resolve(recipeCategory.getClass().getSimpleName())
                            .resolve(recipeHolder.id().identifier().getPath().replaceAll("([^/]*/)*", "") + ".png");
                    if (!Files.exists(path.getParent())) {
                        Files.createDirectories(path.getParent());
                    }

                    CompletableFuture<NativeImage> img = ImageHelper.render(new PrinterJob(recipeCategory.getWidth() + 8, recipeCategory.getHeight() + 8, ModConfig.scale,
                            guiGraphics -> {
                                RecipePrinter.getInstance().logger.debug("Printing {} {}", recipeCategory.getRecipeType().getUid(), recipeHolder.id());
                                guiGraphics.pose().translate(4, 4);
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
                failedRecipes.add(recipeHolder.id().identifier());
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
            for (Identifier failedRecipe : failedRecipes) {
                context.getSource().sendFailure(Component.literal("- " + failedRecipe));
            }
        }

        return rendered;
    }

    private static Optional<IRecipeLayoutDrawable<Object>> createLayout(IRecipeCategory<?> category, RecipeHolder<?> recipe, IFocusGroup focusGroup) {
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
        IRecipeCategory<Object> objCategory = (IRecipeCategory<Object>) category;
        // The public IIngredientManager returned here is always also the internal one RecipeLayout needs;
        // JEI just doesn't expose that supertype through the public IJeiHelpers API.
        IIngredientManagerInternal ingredientManager = (IIngredientManagerInternal) PrinterJEI.REG.getJeiHelpers().getIngredientManager();
        return RecipeLayout.create(objCategory, Collections.emptyList(), recipe, focusGroup,
                ingredientManager, recipeBackground, borderPadding, SlotDisplayContext.fromLevel(Minecraft.getInstance().level));
    }
}
