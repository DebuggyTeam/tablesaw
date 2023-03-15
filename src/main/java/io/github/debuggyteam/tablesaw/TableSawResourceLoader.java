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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import io.github.debuggyteam.tablesaw.api.TableSawAPI;
import io.github.debuggyteam.tablesaw.api.TableSawCompat;
import io.github.debuggyteam.tablesaw.api.TableSawRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class TableSawResourceLoader implements SimpleSynchronousResourceReloader {
	public static final Identifier ID = TableSaw.identifier("recipe_loader");

	@Override
	public @NotNull Identifier getQuiltId() {
		return ID;
	}

	@Override
	public void reload(ResourceManager manager) {
		//We start fresh at every datapack reload
		TableSawRecipes.serverInstance().clearAllRecipes();

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

		//Load data recipes, including our builtins
		Map<Identifier, Resource> tableSawRecipes = manager.findResources("custom_recipes/tablesaw", (id)->id.getPath().endsWith(".json"));
		for(Map.Entry<Identifier, Resource> resource : tableSawRecipes.entrySet()) {

			try {
				String recipeString = new String(resource.getValue().open().readAllBytes(), StandardCharsets.UTF_8);
				JsonElement jsonRoot = JsonParser.parseString(recipeString);
				if (jsonRoot instanceof JsonArray rootArray) {
					for(JsonElement recipeElem : rootArray) {
						if (recipeElem instanceof JsonObject recipeObject) {
							TableSawRecipe recipe = parseRecipe(recipeObject);
							if (recipe != null) {
								TableSawRecipes.serverInstance().registerRecipe(recipe);
							}
						}
					}
				} else if (jsonRoot instanceof JsonObject rootObject) {
					JsonArray recipesArray = getArray(rootObject, "recipes");
					if (recipesArray != null) {

						Boolean replace = getBoolean(rootObject, "replace");
						if (replace == null) replace = Boolean.FALSE;

						List<TableSawRecipe> results = new ArrayList<>();

						for(JsonElement recipeElem : recipesArray) {
							if (recipeElem instanceof JsonObject recipeObject) {
								TableSawRecipe recipe = parseRecipe(recipeObject);
								if (recipe != null) results.add(recipe);
							}
						}

						if (replace) {
							for(TableSawRecipe recipe : results) {
								TableSawRecipes.serverInstance().clearRecipesFor(recipe.getInput());
							}
						}

						for(TableSawRecipe recipe : results) TableSawRecipes.serverInstance().registerRecipe(recipe);
					} else {
						//System.out.println("Parsing single recipe");
						TableSawRecipe recipe = parseRecipe(rootObject);
						if (recipe != null) {
							TableSawRecipes.serverInstance().registerRecipe(recipe);
						}
					}
				} else {
					TableSaw.LOGGER.error("Could not parse recipe(s) from Resource {} - root element needs to be an object or an array.", resource.getKey());
				}
			} catch (IOException e) {
				TableSaw.LOGGER.error("Could not load recipe resource " + resource.getKey(), e);
			}
		}
	}

	/**
	 * Takes a JsonObject and, if possible, creates a TableSawRecipe out of it.
	 * @param obj The object representing a recipe
	 * @return If the JsonObject represents a valid TableSaw recipe, that recipe. If any error happened, returns null with no side effects.
	 */
	protected @Nullable TableSawRecipe parseRecipe(JsonObject obj) {

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
		if (obj == null) return null;

		JsonElement e = obj.get(key);
		if (e instanceof JsonArray array) {
			return array;
		} else {
			return null;
		}
	}

	private @Nullable String getString(@Nullable JsonObject obj, String key) {
		if (obj == null) return null;

		JsonElement e = obj.get(key);
		if (e instanceof JsonPrimitive prim) {
			return (prim.isString()) ? prim.getAsString() : null;
		} else {
			return null;
		}
	}

	private @Nullable Integer getInteger(@Nullable JsonObject obj, String key) {
		if (obj == null) return null;

		JsonElement e = obj.get(key);
		if (e instanceof JsonPrimitive prim) {
			return (prim.isNumber()) ? prim.getAsInt() : null;
		} else {
			return null;
		}
	}

	private @Nullable Boolean getBoolean(@Nullable JsonObject obj, String key) {
		if (obj == null) return null;

		JsonElement e = obj.get(key);
		if (e instanceof JsonPrimitive prim) {
			return (prim.isBoolean()) ? prim.getAsBoolean() : null;
		} else {
			return null;
		}
	}

	private @Nullable JsonObject getObject(@Nullable JsonObject obj, String key) {
		if (obj == null) return null;

		JsonElement e = obj.get(key);
		if (e instanceof JsonObject o) {
			return o;
		} else {
			return null;
		}
	}
}
