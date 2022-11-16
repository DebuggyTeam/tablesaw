package io.github.debuggyteam.tablesaw.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.render.RenderLayer;

import org.quiltmc.config.api.WrappedConfig;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.config.QuiltConfig;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.block.extensions.api.client.BlockRenderLayerMap;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

import io.github.debuggyteam.tablesaw.TableSaw;
import io.github.debuggyteam.tablesaw.TableSawRecipes;
import io.github.debuggyteam.tablesaw.TableSawScreenHandler;
import io.github.debuggyteam.tablesaw.api.TableSawRecipe;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import static io.github.debuggyteam.tablesaw.TableSaw.TABLESAW;

public class TableSawClient implements ClientModInitializer {
	
	public static ClientSettings config;
	
	@Override
	public void onInitializeClient(ModContainer mod) {
		
		config = QuiltConfig.create(TableSaw.MODID, "client", ClientSettings.class);
		
		
		HandledScreens.register(TableSaw.TABLESAW_SCREEN_HANDLER, (TableSawScreenHandler gui, PlayerInventory inventory, Text title) -> new TableSawScreen(gui, inventory, title));
		
		ClientPlayNetworking.registerGlobalReceiver(TableSaw.TABLESAW_CHANNEL, (client, handler, buf, sender) -> {
			System.out.println("Received tablesaw message from server.");
		});
		
		ClientPlayNetworking.registerGlobalReceiver(TableSaw.RECIPE_CHANNEL, (client, handler, buf, sender) -> {
			// Read data in and freeze it
			final List<TableSawRecipe> recipes = new ArrayList<>();
			int count = buf.readVarInt();
			if (count == 0) {
				/*
				 * We sent a recipes packet with zero recipes in it at the start of recipe sync to let the client know
				 * to zap all recipes. We're the client right now. Zap all recipes from the client thread and exit.
				 */
				client.execute(() -> {
					TableSawRecipes tsr = TableSawRecipes.clientInstance();
					tsr.clearAllRecipes();
					
					if (client.player.currentScreenHandler instanceof TableSawScreenHandler) {
						client.setScreen(null); // Kick the player out of the tablesaw screen
					}
				});
				return;
			}
			
			for(int i = 0; i < count; i++) {
				String id = buf.readString();
				Item inputItem = Registry.ITEM.get(new Identifier(id));
				int quantity = buf.readVarInt();
				ItemStack result = buf.readItemStack();
				
				if (inputItem == Items.AIR) {
					TableSaw.LOGGER.error("Client received a synced recipe with invalid input item \"{}\". Ignoring.", id);
					continue;
				}
				if (quantity == 0) {
					TableSaw.LOGGER.error("Client received a synced recipe requiring zero of input item \"{}\". Ignoring.", id);
					continue;
				}
				
				TableSawRecipe recipe = new TableSawRecipe(inputItem, quantity, result);
				recipes.add(recipe);
			}
			
			// - "recipes" is effectively final here and we promise never to modify it again from the netty thread -
			
			//Switch to client thread
			client.execute(() -> {
				//Apply data to live objects
				TableSawRecipes tsr = TableSawRecipes.clientInstance();
				for(TableSawRecipe sawRecipe : recipes) tsr.registerRecipe(sawRecipe);
			});
		});

		BlockRenderLayerMap.put(RenderLayer.getCutout(), TABLESAW);
	}

}
