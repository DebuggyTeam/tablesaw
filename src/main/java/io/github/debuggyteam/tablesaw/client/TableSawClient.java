package io.github.debuggyteam.tablesaw.client;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

import io.github.debuggyteam.tablesaw.TableSaw;
import io.github.debuggyteam.tablesaw.TableSawRecipes;
import io.github.debuggyteam.tablesaw.TableSawScreenHandler;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public class TableSawClient implements ClientModInitializer {

	@Override
	public void onInitializeClient(ModContainer mod) {
		HandledScreens.register(TableSaw.TABLESAW_SCREEN_HANDLER, (TableSawScreenHandler gui, PlayerInventory inventory, Text title) -> new TableSawScreen(gui, inventory, title));
		
		
		TableSawRecipes.clientInstance().copyFrom(TableSawRecipes.serverInstance()); //For testing! Remove once sync is implemented!
	}

}
