package de.melanx.recipeprinter;

import org.moddingx.libx.annotation.config.RegisterConfig;
import org.moddingx.libx.config.Config;
import org.moddingx.libx.config.validate.IntRange;

@RegisterConfig(client = true)
public class ModConfig {

    @Config("Scale for the images.")
    @IntRange(min = 1)
    public static int scale = 5;

    @Config("The amount of items per row when rendering creative tabs")
    @IntRange(min = 1)
    public static int itemsPerRow = 9;
}
