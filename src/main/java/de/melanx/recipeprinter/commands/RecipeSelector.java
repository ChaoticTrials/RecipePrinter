package de.melanx.recipeprinter.commands;

import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.List;
import java.util.stream.Collectors;

public class RecipeSelector {

    private final String namespace; // Null for wildcard
    private final String path; // Null for wildcard

    public RecipeSelector(String namespace, String path) {
        this.namespace = namespace;
        this.path = path;
    }

    public RecipeSelector(String selector) {
        String namespace;
        String path;
        if (selector.contains(":")) {
            namespace = selector.substring(0, selector.indexOf(':')).trim();
            path = selector.substring(selector.indexOf(':') + 1).trim();
        } else {
            namespace = "minecraft";
            path = selector.trim();
        }
        if (namespace.equals("*")) {
            namespace = null;
        }
        if (path.equals("*")) {
            path = null;
        }
        if (selector.trim().equals("*")) {
            namespace = null;
            path = null;
        }
        this.namespace = namespace;
        this.path = path;
    }

    public List<RecipeHolder<?>> getRecipes(RecipeManager mgr) {
        return mgr.getRecipes().stream().filter(recipeHolder -> (this.namespace == null || recipeHolder.id().identifier().getNamespace().equalsIgnoreCase(this.namespace))
                && (this.path == null || recipeHolder.id().identifier().getPath().equalsIgnoreCase(this.path))).collect(Collectors.toList());
    }
}
