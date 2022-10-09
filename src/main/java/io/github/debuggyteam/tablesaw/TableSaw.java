package io.github.debuggyteam.tablesaw;

import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.resource.ResourceType;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Deque;
import java.util.List;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.ServerPlayConnectionEvents;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.debuggyteam.tablesaw.api.TableSawRecipe;

public class TableSaw implements ModInitializer {
    public static final String MODID = "tablesaw";
    public static final Identifier TABLESAW_CHANNEL = identifier("tablesaw");  // S <-> C
    public static final Identifier RECIPE_CHANNEL = identifier("recipe_sync"); // S -> C only
    public static final int MESSAGE_ENGAGE_TABLESAW = 0x120000;

    public static final Logger LOGGER = LoggerFactory.getLogger("TableSaw");

    public static final TableSawBlock TABLESAW = new TableSawBlock(QuiltBlockSettings.of(Material.WOOD).nonOpaque());
    public static final ScreenHandlerType<TableSawScreenHandler> TABLESAW_SCREEN_HANDLER = new ScreenHandlerType<>((syncId, inventory) -> new TableSawScreenHandler(syncId, inventory, ScreenHandlerContext.EMPTY));

    /** Creates an identifier with this mod as the namespace */
    public static Identifier identifier(String path) {
    	return new Identifier(MODID, path);
    }
    
    @Override
    public void onInitialize(ModContainer mod) {
        LOGGER.info("Hello Quilt world from {}!", mod.metadata().name());

        Registry.register(Registry.SCREEN_HANDLER, new Identifier(MODID, "tablesaw"), TABLESAW_SCREEN_HANDLER);
        
        Registry.register(Registry.BLOCK, new Identifier(MODID, "tablesaw"), TABLESAW);
        Registry.register(Registry.ITEM, new Identifier(MODID, "tablesaw"),
                new BlockItem(TABLESAW, new QuiltItemSettings().group(ItemGroup.DECORATIONS)));
        
        ServerPlayNetworking.registerGlobalReceiver(TABLESAW_CHANNEL, new TableSawServerReceiver());
        
        ResourceLoader.get(ResourceType.SERVER_DATA).registerReloader(new TableSawResourceLoader());
        
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            Deque<TableSawRecipe> list = TableSawRecipes.serverInstance().queueAll();
            
            //Batch recipes in packets of 100 a pop
            while(list.size() > 100) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeVarInt(100);
                for(int i = 0; i < 100; i++) {
                	writeRecipe(list.pop(), buf);
                }
                
                ServerPlayNetworking.send(handler.getPlayer(), RECIPE_CHANNEL, buf);
            }
            
            //cleanup any additional in a single packet
            PacketByteBuf lastBuf = PacketByteBufs.create();
            lastBuf.writeVarInt(list.size());
            while(!list.isEmpty()) {
            	writeRecipe(list.pop(), lastBuf);
            }
            ServerPlayNetworking.send(handler.getPlayer(), RECIPE_CHANNEL, lastBuf);
        });
    }
    
    /** Writes a recipe to a PacketByteBuf */
    private void writeRecipe(TableSawRecipe recipe, PacketByteBuf buf) {
        String inputId = Registry.ITEM.getId(recipe.getInput()).toString();
        buf.writeString(inputId);
        buf.writeVarInt(recipe.getQuantity());
        buf.writeItemStack(recipe.getResult());
    }
}
