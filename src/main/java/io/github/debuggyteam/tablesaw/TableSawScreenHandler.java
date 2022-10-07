package io.github.debuggyteam.tablesaw;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CuttingRecipe;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TableSawScreenHandler extends ScreenHandler {
	public static final int INPUT_SLOT = 0;
	public static final int OUTPUT_SLOT = 1;
	
	private static final double MAX_SQUARED_REACH = 6 * 6;
	
	private List<CuttingRecipe> availableRecipes = new ArrayList<>();
	private ScreenHandlerContext context;
	private World world;
	private BlockPos pos;
	//private Slot inputSlot;
	//private Slot outputSlot;
	private Runnable listenerScreen = ()->{};
	public final Inventory input = new SimpleInventory(1) {
		@Override
		public void markDirty() {
			super.markDirty();
			onContentChanged(this);
		}
	};
	public final Inventory output = new SimpleInventory(1) {
		@Override
		public void markDirty() {
			super.markDirty();
			onContentChanged(this);
		}
		
		public boolean canInsert(ItemStack stack) { return false; };
	};

	public TableSawScreenHandler(int syncId, PlayerInventory inventory, ScreenHandlerContext context) {
		super(TableSaw.TABLESAW_SCREEN_HANDLER, syncId);
		this.context = context;
		context.run((world, pos)->{
			this.world = world;
			this.pos = pos;
		});
		
		//this.inputSlot = 
				this.addSlot(new Slot(this.input, 0, 20, 33));
		//this.outputSlot = 
				this.addSlot(new Slot(this.output, 0, 143, 33));
		
		int inventoryWidth = 9;
		for(int yi = 0; yi < 3; ++yi) {
			for(int xi = 0; xi < inventoryWidth; ++xi) {
				this.addSlot(new Slot(inventory, xi + yi * inventoryWidth + inventoryWidth, 8 + xi * 18, 84 + yi * 18));
			}
		}

		for(int xi = 0; xi < inventoryWidth; ++xi) {
			this.addSlot(new Slot(inventory, xi, 8 + xi * 18, 142));
		}
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
	
	public void setListenerScreen(Runnable listener) {
		this.listenerScreen = listener;
	}
	
	@Override
	public void onContentChanged(Inventory inventory) {
		super.onContentChanged(inventory);
		listenerScreen.run();
	}
	
	@Override
	public void close(PlayerEntity player) {
		super.close(player);
		this.output.removeStack(0);
		this.context.run((world, pos) -> this.dropInventory(player, this.input));
	}
}
