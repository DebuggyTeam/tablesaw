package io.github.debuggyteam.tablesaw.api;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class TableSawRecipe {
	protected Item input;
	protected int quantity;
	protected ItemStack result;
	
	public TableSawRecipe(Item input, int quantity, ItemStack result) {
		this.input = input;
		this.quantity = quantity;
		this.result = result;
	}
	
	/**
	 * Gets the input Item that this Recipe uses / consumes
	 * @return this Recipe's input Item
	 */
	public Item getInput() {
		return input;
	}
	
	/**
	 * Gets the number of input items consumed by this Recipe
	 * @return the number of input items that will be consumed by this Recipe
	 */
	public int getQuantity() {
		return quantity;
	}
	
	/**
	 * Returns an ItemStack representing the output result of this Recipe. <em>Do not modify this ItemStack!</em> Make a
	 * copy instead!
	 * @return The result of crafting this Recipe
	 */
	public ItemStack getResult() {
		return result;
	}
}
