package io.github.debuggyteam.tablesaw;

import io.github.debuggyteam.tablesaw.api.TableSawRecipe;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;

import java.util.Deque;

public class TableSawRecipeSync {
	/**
	 * Sync all current TableSaw recipes to a client, clearing any existing recipes they had.
	 *
	 * <p>Note: This is a very heavy operation network-wise, and will kick the player out of the tablesaw gui if they're
	 * in one. Do not do this if you don't have to.
	 * @param server The server to send the packets from.
	 * @param player The player to update recipes for.
	 */
	public static void syncFromServer(MinecraftServer server, ServerPlayerEntity player) {
		Deque<TableSawRecipe> list = TableSawRecipes.serverInstance().queueAll();
		
		//Send an initial zero message to re-clear the recipe list
		PacketByteBuf firstBuf = PacketByteBufs.create();
		firstBuf.writeVarInt(0);
		ServerPlayNetworking.send(player, TableSaw.RECIPE_CHANNEL, firstBuf);
		
		//Batch recipes in packets of 100 a pop
		while(list.size() > 100) {
			PacketByteBuf buf = PacketByteBufs.create();
			buf.writeVarInt(100);
			for(int i = 0; i < 100; i++) {
				writeRecipe(list.pop(), buf);
			}
			
			ServerPlayNetworking.send(player, TableSaw.RECIPE_CHANNEL, buf);
		}
		
		//cleanup any additional recipes (< 100) in a single packet
		PacketByteBuf lastBuf = PacketByteBufs.create();
		lastBuf.writeVarInt(list.size());
		while(!list.isEmpty()) {
			writeRecipe(list.pop(), lastBuf);
		}
		ServerPlayNetworking.send(player, TableSaw.RECIPE_CHANNEL, lastBuf);
	}
	
	/** Writes a recipe to a PacketByteBuf */
	private static void writeRecipe(TableSawRecipe recipe, PacketByteBuf buf) {
		String inputId = Registries.ITEM.getId(recipe.getInput()).toString();
		buf.writeString(inputId);
		buf.writeVarInt(recipe.getQuantity());
		buf.writeItemStack(recipe.getResult());
	}
}
