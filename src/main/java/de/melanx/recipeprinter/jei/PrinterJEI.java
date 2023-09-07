package de.melanx.recipeprinter.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.library.load.registration.RecipeCategoryRegistration;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@JeiPlugin
@MethodsReturnNonnullByDefault
public class PrinterJEI implements IModPlugin {

    @Nullable
    public static RecipeCategoryRegistration REG = null;

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath("recipeprinter", "fake_plugin");
    }

    @Override
    public void registerCategories(@Nonnull IRecipeCategoryRegistration registration) {
        if (registration instanceof RecipeCategoryRegistration reg) {
            REG = reg;
        }
    }
}
