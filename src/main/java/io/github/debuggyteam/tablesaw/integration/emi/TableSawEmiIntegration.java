package io.github.debuggyteam.tablesaw.integration.emi;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiStack;
import io.github.debuggyteam.tablesaw.TableSaw;
import net.minecraft.util.Identifier;

public class TableSawEmiIntegration implements EmiPlugin {
    public static final Identifier TABLESAW_SPRITE_SHEET = new Identifier(TableSaw.MODID, "textures/gui/emi_simplified_textures.png");
    public static final EmiStack TABLESAW_BLOCK = EmiStack.of(TableSaw.TABLESAW);
    public static final EmiRecipeCategory TABLESAW_CATEGORY = new EmiRecipeCategory(new Identifier(TableSaw.MODID, "tablesaw"), new EmiTexture(TABLESAW_SPRITE_SHEET, 0, 0, 16, 16));
    
    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(TABLESAW_CATEGORY);
        registry.addWorkstation(TABLESAW_CATEGORY, TABLESAW_BLOCK);
    }
}
