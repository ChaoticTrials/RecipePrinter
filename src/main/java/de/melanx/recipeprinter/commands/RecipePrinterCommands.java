package de.melanx.recipeprinter.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.melanx.recipeprinter.RecipePrinter;
import de.melanx.recipeprinter.RecipeRenderers;
import de.melanx.recipeprinter.util.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class RecipePrinterCommands {

    public static final SuggestionProvider<CommandSourceStack> TABS = ((context, builder) -> SharedSuggestionProvider
            .suggest(CreativeModeTabs.allTabs().stream().filter(Util::isNormalItemCategory).map(group -> ("\"" + group.getDisplayName().getString() + "\"")), builder));
    public static final SuggestionProvider<CommandSourceStack> RECIPE_TYPES = ((context, builder) -> {
        Optional<Registry<RecipeType<?>>> registry = context.getSource().getServer().registryAccess().registry(Registries.RECIPE_TYPE);
        return registry.map(recipeTypes -> SharedSuggestionProvider.suggest(recipeTypes.stream().filter(RecipeRenderers::isSupported).map(Object::toString), builder)).orElse(null);
    });
    public static final SuggestionProvider<CommandSourceStack> RECIPES = (context, builder) ->
            suggestRecipes(context.getSource().getRecipeNames(), builder);

    public static void register(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> hand = Commands.literal("hand")
                .executes(new HandCommand(false));

        if (ModList.get().isLoaded("jei")) {
            hand = hand.then(Commands.literal("fromJei").executes(new HandCommand(true)));
        }

        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal(RecipePrinter.getInstance().modid)
                .then(Commands.literal("recipe")
                        .then(Commands.argument("recipes", StringArgumentType.greedyString())
                                .suggests(RECIPES)
                                .executes(new RecipeCommand())))
                .then(Commands.literal("recipeType")
                        .then(Commands.argument("type", StringArgumentType.word()).suggests(RECIPE_TYPES)
                                .executes(new RecipeTypeCommand())))
                .then(Commands.literal("itemgroup")
                        .then(Commands.argument("group", StringArgumentType.string()).suggests(TABS)
                                .executes(new ItemCategoryCommand()))
                )
                .then(hand);

        if (ModList.get().isLoaded("jei")) {
            builder = builder.then(Commands.literal("fromJei").then(Commands.argument("recipes", StringArgumentType.greedyString())
                    .suggests(RECIPES).executes(new JeiCommand())));
        }

        event.getDispatcher().register(builder);
    }

    private static CompletableFuture<Suggestions> suggestRecipes(
            Stream<ResourceLocation> recipeNames, SuggestionsBuilder builder) {
        List<ResourceLocation> recipes = recipeNames
                .distinct()
                .sorted(Comparator.comparing(ResourceLocation::toString))
                .toList();
        String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

        if ("*".startsWith(remaining)) {
            builder.suggest("*");
        }

        recipes.stream()
                .map(ResourceLocation::getNamespace)
                .distinct()
                .map(namespace -> namespace + ":*")
                .filter(wildcard -> wildcard.startsWith(remaining))
                .forEach(builder::suggest);

        return SharedSuggestionProvider.suggestResource(recipes, builder);
    }
}
