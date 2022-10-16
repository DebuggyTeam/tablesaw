package io.github.debuggyteam.tablesaw.client;

import java.util.ArrayList;
import java.util.List;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
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

public class TableSawClient implements ClientModInitializer {

	@Override
	public void onInitializeClient(ModContainer mod) {
		HandledScreens.register(TableSaw.TABLESAW_SCREEN_HANDLER, (TableSawScreenHandler gui, PlayerInventory inventory, Text title) -> new TableSawScreen(gui, inventory, title));
		
		ClientPlayNetworking.registerGlobalReceiver(TableSaw.TABLESAW_CHANNEL, (client, handler, buf, sender) -> {
			System.out.println("Received tablesaw message from server.");
		});
		
		ClientPlayNetworking.registerGlobalReceiver(TableSaw.RECIPE_CHANNEL, (client, handler, buf, sender) -> {
			// Read data in and freeze it
			final List<TableSawRecipe> recipes = new ArrayList<>();
			int count = buf.readVarInt();
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
				for(TableSawRecipe r : recipes) tsr.registerRecipe(r);
			});
		});
	}

}
