package io.github.debuggyteam.tablesaw;

import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TableSaw implements ModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod name as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("TableSaw");

    public static final TableSawBlock TABLESAW = new TableSawBlock(QuiltBlockSettings.of(Material.WOOD).nonOpaque());
    public static final ScreenHandlerType<TableSawScreenHandler> TABLESAW_SCREEN_HANDLER = new ScreenHandlerType<>((syncId, inventory) -> new TableSawScreenHandler(syncId, inventory, ScreenHandlerContext.EMPTY));

    @Override
    public void onInitialize(ModContainer mod) {
        LOGGER.info("Hello Quilt world from {}!", mod.metadata().name());

        Registry.register(Registry.SCREEN_HANDLER, new Identifier("tablesaw", "tablesaw"), TABLESAW_SCREEN_HANDLER);
        
        Registry.register(Registry.BLOCK, new Identifier("tablesaw", "tablesaw"), TABLESAW);
        Registry.register(Registry.ITEM, new Identifier("tablesaw", "tablesaw"),
                new BlockItem(TABLESAW, new QuiltItemSettings().group(ItemGroup.DECORATIONS)));
        
        TableSawRecipes.serverInstance().registerRecipe(Blocks.OAK_PLANKS, new ItemStack(Blocks.OAK_STAIRS));
        TableSawRecipes.serverInstance().registerRecipe(Blocks.OAK_PLANKS, new ItemStack(Blocks.OAK_FENCE));
        TableSawRecipes.serverInstance().registerRecipe(Blocks.OAK_PLANKS, new ItemStack(Blocks.OAK_DOOR));
        TableSawRecipes.serverInstance().registerRecipe(Blocks.OAK_PLANKS, new ItemStack(Blocks.OAK_BUTTON));
        TableSawRecipes.serverInstance().registerRecipe(Blocks.OAK_PLANKS, new ItemStack(Blocks.OAK_FENCE_GATE));
        TableSawRecipes.serverInstance().registerRecipe(Blocks.OAK_PLANKS, new ItemStack(Blocks.OAK_PRESSURE_PLATE));
        TableSawRecipes.serverInstance().registerRecipe(Blocks.OAK_PLANKS, new ItemStack(Blocks.OAK_SIGN));
        TableSawRecipes.serverInstance().registerRecipe(Blocks.OAK_PLANKS, new ItemStack(Blocks.OAK_SLAB));
        TableSawRecipes.serverInstance().registerRecipe(Blocks.OAK_PLANKS, new ItemStack(Blocks.OAK_TRAPDOOR));
        //Silly blocks to force the recipes to scroll
        TableSawRecipes.serverInstance().registerRecipe(Blocks.OAK_PLANKS, new ItemStack(Blocks.BARREL));
        TableSawRecipes.serverInstance().registerRecipe(Blocks.OAK_PLANKS, new ItemStack(Blocks.COMPOSTER));
        TableSawRecipes.serverInstance().registerRecipe(Blocks.OAK_PLANKS, new ItemStack(Blocks.BEEHIVE));
        TableSawRecipes.serverInstance().registerRecipe(Blocks.OAK_PLANKS, new ItemStack(Blocks.CHEST));
    }
}
