package de.melanx.recipeprinter.jei;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.library.load.registration.RecipeCategoryRegistration;
import net.minecraft.resources.Identifier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@JeiPlugin
@MethodsReturnNonnullByDefault
public class PrinterJEI implements IModPlugin {

    @Nullable
    public static RecipeCategoryRegistration REG = null;

    @Override
    public Identifier getPluginUid() {
        return Identifier.fromNamespaceAndPath("recipeprinter", "fake_plugin");
    }

    @Override
    public void registerCategories(@Nonnull IRecipeCategoryRegistration registration) {
        if (registration instanceof RecipeCategoryRegistration reg) {
            REG = reg;
        }
    }
}
