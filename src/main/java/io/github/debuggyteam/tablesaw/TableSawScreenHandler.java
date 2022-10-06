package io.github.debuggyteam.tablesaw;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CuttingRecipe;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TableSawScreenHandler extends ScreenHandler {
	private static final double MAX_SQUARED_REACH = 6 * 6;
	
	private List<CuttingRecipe> availableRecipes = new ArrayList<>();
	private ItemStack inputStack = ItemStack.EMPTY;
	private World world;
	private BlockPos pos;
	
	protected TableSawScreenHandler(int syncId) {
		super(TableSaw.TABLESAW_SCREEN_HANDLER, syncId);
	}

	public TableSawScreenHandler(int syncId, PlayerInventory inventory, ScreenHandlerContext context) {
		super(TableSaw.TABLESAW_SCREEN_HANDLER, syncId);
		context.run((world, pos)->{
			this.world = world;
			this.pos = pos;
		});
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		if (world!=null && pos!=null) {
			if (!player.getWorld().equals(world)) return false;
			if (player.getPos().squaredDistanceTo(pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5)>MAX_SQUARED_REACH) return false;
			if (world.getBlockState(pos).isOf(TableSaw.TABLESAW)) return true;
		}
		
		return true;
	}

	@Override
	public ItemStack transferSlot(PlayerEntity player, int index) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public int getSelectedRecipe() {
		return 0; //this.selectedRecipe.get();
	}

	public List<StonecuttingRecipe> getAvailableRecipes() {
		return ImmutableList.of(); //this.availableRecipes;
	}

	public int getAvailableRecipeCount() {
		return this.availableRecipes.size();
	}

	public boolean canCraft() {
		return false; //return this.inputSlot.hasStack() && !this.availableRecipes.isEmpty();
	}
}
