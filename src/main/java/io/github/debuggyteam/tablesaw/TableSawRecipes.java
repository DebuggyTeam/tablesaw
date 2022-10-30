package io.github.debuggyteam.tablesaw;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;

import io.github.debuggyteam.tablesaw.api.TableSawRecipe;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;

public class TableSawRecipes {
	
	@Environment(EnvType.CLIENT)
	private static TableSawRecipes CLIENT_INSTANCE = new TableSawRecipes();
	private static TableSawRecipes SERVER_INSTANCE = new TableSawRecipes();
	
	@Environment(EnvType.CLIENT)
	public static TableSawRecipes clientInstance() {
		return CLIENT_INSTANCE;
	}
	
	public static TableSawRecipes serverInstance() {
		return SERVER_INSTANCE;
	}
	
	protected Multimap<Item, TableSawRecipe> recipes = HashMultimap.create();
	
	public List<TableSawRecipe> getRecipes(ItemConvertible item) {
		
		return ImmutableList.copyOf(recipes.get(item.asItem()));
	}
	
	public void registerRecipe(ItemConvertible input, ItemConvertible result) {
		registerRecipe(new TableSawRecipe(input.asItem(), 1, new ItemStack(result)));
	}
	
	public void registerRecipe(ItemConvertible input, int quantity, ItemStack result) {
		registerRecipe(new TableSawRecipe(input.asItem(), quantity, result));
	}
	
	public void registerRecipe(TableSawRecipe recipe) {
		//Remove any recipes with the same output item
		List<TableSawRecipe> toRemove = new ArrayList<>();
		for(TableSawRecipe r : recipes.get(recipe.getInput())) {
			if (r.getResult().getItem() == recipe.getResult().getItem()) {
				toRemove.add(r);
			}
		}
		for(TableSawRecipe r : toRemove) {
			recipes.remove(recipe.getInput(), r);
		}
		
		//Add in the new recipe
		recipes.put(recipe.getInput(), recipe);
	}
	
	
	public void clearAllRecipes() {
		recipes.clear();
	}
	
	@Deprecated
	public void copyFrom(TableSawRecipes other) {
		recipes.clear();
		for(Map.Entry<Item, TableSawRecipe> entry : other.recipes.entries()) {
			recipes.put(entry.getKey(), entry.getValue());
		}
	}
	
	public Deque<TableSawRecipe> queueAll() {
		ArrayDeque<TableSawRecipe> result = new ArrayDeque<>();
		result.addAll(recipes.values());
		return result;
	}

	public void clearRecipesFor(ItemConvertible input) {
		recipes.removeAll(input.asItem());
	}
}
