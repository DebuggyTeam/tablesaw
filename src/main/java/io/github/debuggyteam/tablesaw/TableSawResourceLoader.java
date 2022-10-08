package io.github.debuggyteam.tablesaw;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.jetbrains.annotations.NotNull;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.resource.loader.api.reloader.SimpleSynchronousResourceReloader;

import io.github.debuggyteam.tablesaw.api.TableSawAPI;
import io.github.debuggyteam.tablesaw.api.TableSawCompat;
import io.github.debuggyteam.tablesaw.api.TableSawRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

public class TableSawResourceLoader implements SimpleSynchronousResourceReloader {
	public static final Identifier ID = new Identifier(TableSaw.MODID, "recipe_loader");
	
	@Override
	public @NotNull Identifier getQuiltId() {
		return ID;
	}
	
	@Override
	public void reload(ResourceManager manager) {
		
		
		//Create the API implementation
		TableSawAPI api = new TableSawAPI() {
			@Override
			public void registerTableSawRecipe(TableSawRecipe recipe) {
				TableSawRecipes.serverInstance().registerRecipe(recipe);
				
			}
		};
		
		for(TableSawCompat compat : QuiltLoader.getEntrypoints(TableSaw.MODID, TableSawCompat.class)) {
			
		}
	}

	

}
