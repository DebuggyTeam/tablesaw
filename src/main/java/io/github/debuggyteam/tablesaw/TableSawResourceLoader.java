package io.github.debuggyteam.tablesaw;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.resource.loader.api.reloader.SimpleSynchronousResourceReloader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import io.github.debuggyteam.tablesaw.api.TableSawAPI;
import io.github.debuggyteam.tablesaw.api.TableSawCompat;
import io.github.debuggyteam.tablesaw.api.TableSawRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class TableSawResourceLoader implements SimpleSynchronousResourceReloader {
	public static final Identifier ID = new Identifier(TableSaw.MODID, "recipe_loader");
	
	@Override
	public @NotNull Identifier getQuiltId() {
		return ID;
	}
	
	@Override
	public void reload(ResourceManager manager) {
		TableSawRecipes.serverInstance().clearAllRecipes();
		
		Gson gson = new GsonBuilder().create();
		
		//TODO: Builtins
		
		//TODO: Heuristics
		
		//Create the API implementation
		TableSawAPI api = new TableSawAPI() {
			@Override
			public void registerTableSawRecipe(TableSawRecipe recipe) {
				TableSawRecipes.serverInstance().registerRecipe(recipe);
				
			}
		};
		
		//Call all the API consumers
		for(TableSawCompat compat : QuiltLoader.getEntrypoints(TableSaw.MODID, TableSawCompat.class)) {
			compat.run(api);
		}
		
		//Load data recipes
		Map<Identifier, Resource> tableSawRecipes = manager.findResources("recipes/tablesaw", (id)->id.getPath().endsWith(".json"));
		for(Map.Entry<Identifier, Resource> resource : tableSawRecipes.entrySet()) {
			
			try {
				String recipeString = new String(resource.getValue().open().readAllBytes(), StandardCharsets.UTF_8);
				JsonElement elem = gson.toJsonTree(recipeString);
				if (elem instanceof JsonArray array) {
					//System.out.println("Parsing recipe array");
					for(JsonElement arrayItem : array) {
						if (arrayItem instanceof JsonObject objItem) {
							TableSawRecipe recipe = parseRecipe(objItem);
							if (recipe!=null) {
								TableSawRecipes.serverInstance().registerRecipe(recipe);
							}
						}
					}
				} else if (elem instanceof JsonObject o) {
					JsonArray recipesArray = getArray(o, "recipes");
					if (recipesArray!=null) {
						//System.out.println("Parsing nested recipe array");
						
						Boolean replace = getBoolean(o, "replace");
						if (replace==null) replace = Boolean.FALSE;
						
						List<TableSawRecipe> results = new ArrayList<>();
						//TODO: Parse each result
						for(JsonElement recipeElem : recipesArray) {
							if (recipeElem instanceof JsonObject o2) {
								TableSawRecipe r = parseRecipe(o2);
								if (r!=null) results.add(r);
							}
						}
						
						if (replace) {
							for(TableSawRecipe r : results) {
								TableSawRecipes.serverInstance().clearRecipesFor(r.getInput());
							}
						}
						
						for(TableSawRecipe r : results) TableSawRecipes.serverInstance().registerRecipe(r);
					} else {
						//System.out.println("Parsing single recipe");
						TableSawRecipe recipe = parseRecipe(o);
						if (recipe!=null) {
							TableSawRecipes.serverInstance().registerRecipe(recipe);
						}
					}
				}
				
				//System.out.println("RECIPE: "+recipeString);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	protected TableSawRecipe parseRecipe(JsonObject obj) {
		
		String itemId = null;
		Integer count = null;
		
		JsonObject inputObject = getObject(obj, "input");
		if (inputObject != null) {
			itemId = getString(inputObject, "item");
			count = getInteger(inputObject, "count");
			
			if (itemId == null) return null;
			if (count == null) count = 1;
		} else {
			itemId = getString(obj, "input");
			if (itemId == null) return null;
			count = 1;
		}
		
		Item inputItem = Registry.ITEM.get(new Identifier(itemId));
		if (inputItem == Items.AIR) return null;
		
		ItemStack resultItemStack = ItemStack.EMPTY;
		
		JsonObject resultObject = getObject(obj, "result");
		if (resultObject != null) {
			String resultId = getString(resultObject, "item");
			if (resultId == null) return null;
			Item resultItem = Registry.ITEM.get(new Identifier(resultId));
			if (resultItem == Items.AIR) return null;
			
			Integer resultCount = getInteger(resultObject, "count");
			if (resultCount == null) resultCount = 1;
			
			resultItemStack = new ItemStack(resultItem, resultCount);
			
			//Unpacking the NBT here is extremely experimental
			JsonObject tagObject = getObject(resultObject, "tag");
			if (tagObject != null) {
				try {
					NbtCompound tag = StringNbtReader.parse(tagObject.toString());
					resultItemStack.setNbt(tag);
				} catch (CommandSyntaxException e) {
					e.printStackTrace();
				}
			}
			
		} else {
			String resultId = getString(obj, "result");
			if (resultId == null) return null;
			Item resultItem = Registry.ITEM.get(new Identifier(resultId));
			if (resultItem == Items.AIR) return null;
			
			resultItemStack = new ItemStack(resultItem);
		}
		
		TableSawRecipe result = new TableSawRecipe(inputItem, count, resultItemStack);
		return result;
	}
	
	private @Nullable JsonArray getArray(@Nullable JsonObject obj, String key) {
		if (obj==null) return null;
		
		JsonElement e = obj.get(key);
		if (e instanceof JsonArray array) {
			return array;
		} else {
			return null;
		}
	}
	
	private @Nullable String getString(@Nullable JsonObject obj, String key) {
		if (obj==null) return null;
		
		JsonElement e = obj.get(key);
		if (e instanceof JsonPrimitive prim) {
			return (prim.isString()) ? prim.getAsString() : null;
		} else {
			return null;
		}
	}
	
	private @Nullable Integer getInteger(@Nullable JsonObject obj, String key) {
		if (obj==null) return null;
		
		JsonElement e = obj.get(key);
		if (e instanceof JsonPrimitive prim) {
			return (prim.isNumber()) ? prim.getAsInt() : null;
		} else {
			return null;
		}
	}
	
	private @Nullable Boolean getBoolean(@Nullable JsonObject obj, String key) {
		if (obj==null) return null;
		
		JsonElement e = obj.get(key);
		if (e instanceof JsonPrimitive prim) {
			return (prim.isBoolean()) ? prim.getAsBoolean() : null;
		} else {
			return null;
		}
	}
	
	private @Nullable JsonObject getObject(@Nullable JsonObject obj, String key) {
		if (obj==null) return null;
		
		JsonElement e = obj.get(key);
		if (e instanceof JsonObject o) {
			return o;
		} else {
			return null;
		}
	}
}
