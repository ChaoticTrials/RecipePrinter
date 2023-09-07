package de.melanx.recipeprinter;

import de.melanx.recipeprinter.commands.RecipePrinterCommands;
import de.melanx.recipeprinter.renderers.vanilla.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.moddingx.libx.mod.ModX;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

@Mod(value = "recipeprinter", dist = Dist.CLIENT)
public final class RecipePrinter extends ModX {

    private static RecipePrinter instance;
    public final Logger logger = LoggerFactory.getLogger(RecipePrinter.class);

    public RecipePrinter() {
        instance = this;

        NeoForge.EVENT_BUS.addListener(RecipePrinterCommands::register);
    }

    @Nonnull
    public static RecipePrinter getInstance() {
        return instance;
    }

    @Override
    protected void setup(FMLCommonSetupEvent event) {
        RecipeRenderers.registerRecipeRender(new ShapelessRender());
        RecipeRenderers.registerRecipeRender(new ShapedRender());
        RecipeRenderers.registerRecipeRender(new SmeltingRender());
        RecipeRenderers.registerRecipeRender(new BlastingRender());
        RecipeRenderers.registerRecipeRender(new SmokingRender());
        RecipeRenderers.registerRecipeRender(new CampfireRender());
        RecipeRenderers.registerRecipeRender(new SmithingTransformRender());
        RecipeRenderers.registerRecipeRender(new StonecuttingRender());
    }

    @Override
    protected void clientSetup(FMLClientSetupEvent event) {
        // NO-OP
    }
}
