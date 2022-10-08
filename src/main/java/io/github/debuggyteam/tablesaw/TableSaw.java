package io.github.debuggyteam.tablesaw;

import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceType;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;
import org.quiltmc.qsl.resource.loader.api.ResourceLoaderEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TableSaw implements ModInitializer {
	public static final String MODID = "tablesaw";
	public static final Identifier TABLESAW_CHANNEL = new Identifier(MODID, "tablesaw");  // S <-> C
	public static final Identifier RECIPE_CHANNEL = new Identifier(MODID, "recipe_sync"); // S -> C only
	public static final int MESSAGE_ENGAGE_TABLESAW = 0x120000;
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod name as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("TableSaw");

    public static final TableSawBlock TABLESAW = new TableSawBlock(QuiltBlockSettings.of(Material.WOOD).nonOpaque());
    public static final ScreenHandlerType<TableSawScreenHandler> TABLESAW_SCREEN_HANDLER = new ScreenHandlerType<>((syncId, inventory) -> new TableSawScreenHandler(syncId, inventory, ScreenHandlerContext.EMPTY));

    @Override
    public void onInitialize(ModContainer mod) {
        LOGGER.info("Hello Quilt world from {}!", mod.metadata().name());

        Registry.register(Registry.SCREEN_HANDLER, new Identifier(MODID, "tablesaw"), TABLESAW_SCREEN_HANDLER);
        
        Registry.register(Registry.BLOCK, new Identifier(MODID, "tablesaw"), TABLESAW);
        Registry.register(Registry.ITEM, new Identifier(MODID, "tablesaw"),
                new BlockItem(TABLESAW, new QuiltItemSettings().group(ItemGroup.DECORATIONS)));
        
        TableSawRecipes recipes = TableSawRecipes.serverInstance();
        recipes.registerRecipe(Blocks.OAK_PLANKS, 1, new ItemStack(Blocks.OAK_STAIRS));
        recipes.registerRecipe(Blocks.OAK_PLANKS, 1, new ItemStack(Blocks.OAK_FENCE));
        recipes.registerRecipe(Blocks.OAK_PLANKS, 1, new ItemStack(Blocks.OAK_DOOR));
        recipes.registerRecipe(Blocks.OAK_PLANKS, 1, new ItemStack(Blocks.OAK_BUTTON));
        recipes.registerRecipe(Blocks.OAK_PLANKS, 1, new ItemStack(Blocks.OAK_FENCE_GATE));
        recipes.registerRecipe(Blocks.OAK_PLANKS, 1, new ItemStack(Blocks.OAK_PRESSURE_PLATE));
        recipes.registerRecipe(Blocks.OAK_PLANKS, 1, new ItemStack(Blocks.OAK_SIGN));
        recipes.registerRecipe(Blocks.OAK_PLANKS, 1, new ItemStack(Blocks.OAK_SLAB));
        recipes.registerRecipe(Blocks.OAK_PLANKS, 1, new ItemStack(Blocks.OAK_TRAPDOOR));
        //Silly blocks to force the recipes to scroll
        recipes.registerRecipe(Blocks.OAK_PLANKS, 1, new ItemStack(Blocks.BARREL));
        recipes.registerRecipe(Blocks.OAK_PLANKS, 1, new ItemStack(Blocks.COMPOSTER));
        recipes.registerRecipe(Blocks.OAK_PLANKS, 1, new ItemStack(Blocks.BEEHIVE));
        recipes.registerRecipe(Blocks.OAK_PLANKS, 1, new ItemStack(Blocks.CHEST));
        
        ServerPlayNetworking.registerGlobalReceiver(TABLESAW_CHANNEL, new TableSawServerReceiver());
        
        ResourceLoader.get(ResourceType.SERVER_DATA).registerReloader(new TableSawResourceLoader());
    }
}
