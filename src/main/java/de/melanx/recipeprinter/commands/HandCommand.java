package de.melanx.recipeprinter.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;

import java.util.List;

public record HandCommand(boolean fromJei) implements Command<CommandSourceStack> {

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ItemStack heldItem = context.getSource().getPlayerOrException().getMainHandItem();
        if (heldItem.isEmpty()) {
            context.getSource().sendFailure(Component.literal("The main hand is empty."));
            return 0;
        }

        RecipeManager recipeManager = context.getSource().getServer().getRecipeManager();
        ServerLevel level = context.getSource().getLevel();
        ContextMap displayContext = SlotDisplayContext.fromLevel(level);

        List<RecipeHolder<?>> recipes = recipeManager.getRecipes().stream()
                .filter(holder -> holder.value().display().stream()
                        .flatMap(display -> display.result().resolveForStacks(displayContext).stream())
                        .anyMatch(stack -> ItemStack.isSameItem(heldItem, stack)))
                .toList();

        if (recipes.isEmpty()) {
            context.getSource().sendFailure(Component.literal("No recipes found for ").append(heldItem.getHoverName()).append("."));
            return 0;
        }

        return this.fromJei
                ? JeiCommand.render(context, recipes)
                : RecipeCommand.render(context, recipes);
    }
}
