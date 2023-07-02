package io.github.debuggyteam.tablesaw.integration.emi;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;

import io.github.debuggyteam.tablesaw.TableSaw;
import io.github.debuggyteam.tablesaw.TableSawRecipes;
import io.github.debuggyteam.tablesaw.api.TableSawRecipe;
import net.minecraft.util.Identifier;

public class TableSawEmiIntegration implements EmiPlugin {
	
	public static final EmiStack TABLESAW_BLOCK = EmiStack.of(TableSaw.TABLESAW);
	public static final EmiRecipeCategory TABLESAW_CATEGORY = new EmiRecipeCategory(new Identifier(TableSaw.MODID, "tablesaw"), EmiStack.of(TableSaw.TABLESAW));
	
	@Override
	public void register(EmiRegistry registry) {
		registry.addCategory(TABLESAW_CATEGORY);
		registry.addWorkstation(TABLESAW_CATEGORY, TABLESAW_BLOCK);
		
		for (TableSawRecipe recipe : TableSawRecipes.serverInstance().queueAll()) {
			registry.addRecipe(new TableSawEmiRecipe(recipe));
		}
	}
}
