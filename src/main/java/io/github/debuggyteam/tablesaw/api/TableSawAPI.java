package io.github.debuggyteam.tablesaw.api;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * To use this API without creating a dependency on TableSaw, register the entrypoint "tablesaw" with an implementation
 * of {@link TableSawCompat}.
 */
public interface TableSawAPI {
	/**
	 * Registers a recipe for the Table Saw.
	 * @param input    the input item to be taken from the left slot
	 * @param quantity the number of input items that will be consumed per craft
	 * @param result   the item to be produced by the recipe and placed in the right slot
	 */
	public default void registerTableSawRecipe(Item input, int quantity, ItemStack result) {
		registerTableSawRecipe(new TableSawRecipe(input, quantity, result));
	}
	
	/**
	 * Registers a recipe for the Table Saw.
	 * @param recipe the Recipe to register.
	 */
	public void registerTableSawRecipe(TableSawRecipe recipe);
}
