package io.github.debuggyteam.tablesaw;

import java.util.List;

import io.github.debuggyteam.tablesaw.api.TableSawRecipe;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TableSawScreenHandler extends ScreenHandler {
	public static final int INPUT_SLOT = 0;
	public static final int OUTPUT_SLOT = 1;
	
	private static final double MAX_SQUARED_REACH = 6 * 6;
	
	private ScreenHandlerContext context;
	private World world;
	private BlockPos pos;
	private Runnable listenerScreen = () -> {};
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
			for(int xi = 0; xi < inventoryWidth; xi++) {
				this.addSlot(new Slot(inventory, xi + yi * inventoryWidth + inventoryWidth, 8 + xi * 18, 84 + yi * 18));
			}
		}

		for(int xi = 0; xi < inventoryWidth; xi++) {
			this.addSlot(new Slot(inventory, xi, 8 + xi * 18, 142));
		}
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		if (world != null && pos != null) {
			if (!player.getWorld().equals(world)) return false;
			if (player.getPos().squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > MAX_SQUARED_REACH) return false;
			if (world.getBlockState(pos).isOf(TableSaw.TABLESAW)) return true;
		}
		
		return true;
	}

	@Override
	public ItemStack transferSlot(PlayerEntity player, int index) {
		ItemStack result = ItemStack.EMPTY;
		Slot slot = slots.get(index);
		if (slot != null && slot.hasStack()) {
			ItemStack initial = slot.getStack();
			result = initial.copy();
			if (index == 0) {
				if (!this.insertItem(result, 2, 38, false)) {
					return ItemStack.EMPTY;
				}
			} else if (index == 1) {
				if (!this.insertItem(result, 2, 38, false)) {
					return ItemStack.EMPTY;
				}
			} else if (index >= 2 && index < 29) {
				if (!this.insertItem(result, 29, 38, false)) {
					return ItemStack.EMPTY;
				}
			} else if (index >= 29 && index < 38 && !this.insertItem(result, 2, 29, false)) {
				return ItemStack.EMPTY;
			}
			
			if (result.isEmpty()) {
				slot.setStack(ItemStack.EMPTY);
			}
			
			slot.markDirty();
			if (result.getCount() == initial.getCount()) {
				return ItemStack.EMPTY;
			}
			
			slot.onTakeItem(player, result);
			this.sendContentUpdates();
		}
		
		return result;
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

	public void tryCraft(ItemStack stack, boolean multiCraft) {
		List<TableSawRecipe> recipes = TableSawRecipes.serverInstance().getRecipes(this.input.getStack(0).getItem());
		for(TableSawRecipe recipe : recipes) {
			if (ItemStack.areEqual(recipe.getResult(), stack)) {
				
				/* This is a complex interlocking series of steps to verify the input count, that the
				 * output can fit in the destination slot, and then deduct the inputs, and *then* insert the results.
				 */
				
				int availableInputQuantity = this.input.getStack(0).getCount();
				int availableCraftsFromSource = availableInputQuantity / recipe.getQuantity();
				if (availableCraftsFromSource <= 0) return; //We don't have enough input
				
				ItemStack destination = this.output.getStack(0);
				if (!(destination.isEmpty() || ItemStack.canCombine(recipe.getResult(), destination))) {
					return; //We can't put the items in the output slot
				}
				
				int availableRoom = destination.isEmpty() ? recipe.getResult().getMaxCount() : destination.getMaxCount() - destination.getCount();
				int destinationCraftableQuantity = availableRoom / recipe.getResult().getCount();
				
				if (destinationCraftableQuantity <= 0) return; //Not enough room in the output slot
				
				int toCraft = (multiCraft) ? Math.min(availableCraftsFromSource, destinationCraftableQuantity) : 1;
				
				for(int i=0; i<toCraft; i++) {
					input.removeStack(0, recipe.getQuantity());
					ItemStack outputStack = this.output.getStack(0);
					if (outputStack.isEmpty()) {
						output.setStack(0, recipe.getResult().copy());
					} else {
						outputStack.setCount(outputStack.getCount() + recipe.getResult().getCount());
						output.setStack(0, outputStack);
					}
				}
				
				//Shouldn't be needed but enable if sync gets funky
				/*
				context.run((world, pos) -> {
					world.markDirty(pos);
				});*/
				
				return;
			}
		}
	}
	
}
