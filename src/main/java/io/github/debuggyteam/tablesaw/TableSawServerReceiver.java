package io.github.debuggyteam.tablesaw;

import org.quiltmc.qsl.networking.api.PacketSender;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class TableSawServerReceiver implements ServerPlayNetworking.ChannelReceiver {

	@Override
	public void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		System.out.println("Received tablesaw message.");
    	
    	int message = buf.readVarInt();
    	switch(message) {
    	case TableSaw.MESSAGE_ENGAGE_TABLESAW: {
    		System.out.println("Engaging the tablesaw?");
    		
    		//Get the rest of the data out of the packet
    		final boolean multiCraft = buf.readBoolean();
    		final ItemStack requestedRecipe = buf.readItemStack();
    		
    		//Switch to the server thread
    		server.execute(() -> {
    			
    			//Use final variables captured from the netty thread to perform actions on the server thread
    			if (player.currentScreenHandler instanceof TableSawScreenHandler tableSaw) {
    				tableSaw.tryCraft(requestedRecipe, multiCraft);
    			}
    			
    			//If the screenhandler isn't a tablesaw it might be that we got kicked out of the screen normally;
    			//just quietly drop the message.
    			
    		});
    		return;
    	}
    	default:
    		TableSaw.LOGGER.error("Received unknown command from a tablesaw: 0x{}", Integer.toHexString(message));
    	}
	}

}
