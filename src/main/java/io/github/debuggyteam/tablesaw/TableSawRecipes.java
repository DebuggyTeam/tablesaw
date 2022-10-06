package io.github.debuggyteam.tablesaw;

import java.util.List;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.BlockItem;
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
	
	protected Multimap<ItemConvertible, ItemStack> recipes = HashMultimap.create();
	
	public List<ItemStack> getRecipes(ItemConvertible item) {
		ItemConvertible lookup = getCanonicalForm(item);
		
		return ImmutableList.copyOf(recipes.get(lookup));
	}
	
	public void registerRecipe(ItemConvertible from, ItemStack to) {
		recipes.put(getCanonicalForm(from), to);
	}
	
	public void registerRecipe(ItemConvertible from, ItemStack... to) {
		from = getCanonicalForm(from);
		for(ItemStack stack : to) recipes.put(from, stack);
	}
	
	public void clearAllRecipes() {
		recipes.clear();
	}
	
	private ItemConvertible getCanonicalForm(ItemConvertible item) {
		if (item instanceof BlockItem blockItem) {
			return blockItem.getBlock();
		}
		
		return item;
	}
}
