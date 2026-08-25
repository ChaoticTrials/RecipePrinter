package de.melanx.recipeprinter.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.phys.Vec2;
import org.moddingx.libx.render.RenderHelper;

import java.util.List;
import java.util.Optional;

public class Util {

    public static ItemStack getResultItem(Recipe<?> recipe) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            throw new NullPointerException("level must not be null.");
        }

        // Recipe#assemble(T) takes the actual crafting input (e.g. the crafting grid contents), not a
        // registry/level context - passing null there isn't valid for recipe types that inspect it.
        // #display() is the modern, input-independent way to get a representative result for display.
        List<RecipeDisplay> displays = recipe.display();
        if (displays.isEmpty()) {
            return ItemStack.EMPTY;
        }

        return displays.get(0).result().resolveForFirstStack(SlotDisplayContext.fromLevel(level));
    }

    public static boolean isNormalItemCategory(CreativeModeTab category) {
        Optional<ResourceKey<CreativeModeTab>> resourceKey = BuiltInRegistries.CREATIVE_MODE_TAB.getResourceKey(category);
        return resourceKey.filter(creativeModeTabResourceKey -> !category.isAlignedRight() && creativeModeTabResourceKey != CreativeModeTabs.HOTBAR && creativeModeTabResourceKey != CreativeModeTabs.INVENTORY && creativeModeTabResourceKey != CreativeModeTabs.SEARCH).isPresent();
    }

    public static void renderItemCategory(GuiGraphicsExtractor guiGraphics, NonNullList<ItemStack> stacks, int rows, int itemsPerRow, CreativeModeTab category) {
        RenderHelperMod.renderDefaultBackground(guiGraphics, itemsPerRow * 18 + 8, rows * 18 + 24);
        RenderHelperMod.renderItem(guiGraphics, category.getIconItem(), 5, 3);
        guiGraphics.text(Minecraft.getInstance().font, category.getDisplayName().getString(), 25, 8, RenderHelperMod.TEXT_COLOR, false);
        RenderHelper.resetColor();
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < itemsPerRow; x++) {
                int idx = y * itemsPerRow + x;
                RenderHelperMod.renderSlot(guiGraphics, 5 + x * 18, 21 + y * 18);
                if (stacks.size() > idx) {
                    RenderHelperMod.renderItem(guiGraphics, stacks.get(idx), 5 + x * 18, 21 + y * 18);
                }
            }
        }
    }

    public static Vec2 rotatePointAbout(Vec2 in, Vec2 about, double degrees) {
        double rad = degrees * Math.PI / 180.0;
        double newX = Math.cos(rad) * (in.x - about.x) - Math.sin(rad) * (in.y - about.y) + about.x;
        double newY = Math.sin(rad) * (in.x - about.x) + Math.cos(rad) * (in.y - about.y) + about.y;
        return new Vec2((float) newX, (float) newY);
    }
}
