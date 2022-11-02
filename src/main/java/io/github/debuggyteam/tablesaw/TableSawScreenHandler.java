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
	public static final int FIRST_INVENTORY_SLOT = 2;
	public static final int LAST_INVENTORY_SLOT = 28;
	public static final int FIRST_HOTBAR_SLOT = 29;
	public static final int LAST_HOTBAR_SLOT = 37;
	
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
	
	public void setListenerScreen(Runnable listener) {
		this.listenerScreen = listener;
	}
	
	@Override
	public void onContentChanged(Inventory inventory) {
		super.onContentChanged(inventory);
		listenerScreen.run();
	}

	@Override
	public ItemStack quickTransfer(PlayerEntity player, int fromIndex) {
		ItemStack result = ItemStack.EMPTY;
		Slot slot = slots.get(fromIndex);
		if (slot != null && slot.hasStack()) {
			ItemStack initial = slot.getStack();
			result = initial.copy();
			
			// Descriptive constants used here where possible. Could still use a total rewrite but the behavior is good.
			
			// Note: insertItem's "to" param is [seemingly] intentionally off-by-one. Thanks Mojang.
			// To insert into only slot 2, DO:
			//     insertItem(result, 2, 3, false)
			// DO NOT:
			//     insertItem(result, 2, 2, false)
			// For this reason you'll see a lot of + 1's for clarity.
			
			if (fromIndex == INPUT_SLOT) {
				// Try to move the item from the input slot back to the inventory, or failing that, the hotbar.
				
				if (!this.insertItem(result, FIRST_INVENTORY_SLOT, LAST_HOTBAR_SLOT + 1, false)) {
					return ItemStack.EMPTY;
				}
			} else if (fromIndex == OUTPUT_SLOT) {
				// Try to move the item from the output slot into the inventory, or the hotbar if no inventory slots are available.
				
				if (!this.insertItem(result, FIRST_INVENTORY_SLOT, LAST_HOTBAR_SLOT + 1, false)) {
					return ItemStack.EMPTY;
				}
			} else if (fromIndex >= FIRST_INVENTORY_SLOT && fromIndex <= LAST_HOTBAR_SLOT) {
				// Item is coming from the inventory or hotbar, so try putting it into the input slot first.
				
				if (this.insertItem(result, INPUT_SLOT, INPUT_SLOT + 1, false)) {
					// Success!
					if (result.isEmpty()) {
						//Very important to prevent dupes
						slot.setStack(ItemStack.EMPTY);
					}
					
					//System.out.println("Count remaining: " + result.getCount() + " initial: " + initial.getCount());
					slot.markDirty();
					if (result.getCount() == initial.getCount()) {
						return ItemStack.EMPTY;
					}
					
					slot.onTakeItem(player, result);
					slot.setStack(result);
					this.sendContentUpdates();
					return result;
				} else {
					// Swap between the inventory and the hotbar since the input slot is occupied
					
					if (fromIndex >= FIRST_INVENTORY_SLOT && fromIndex <= LAST_INVENTORY_SLOT) {
						if (!this.insertItem(result, FIRST_HOTBAR_SLOT, LAST_HOTBAR_SLOT + 1, false)) {
							return ItemStack.EMPTY;
						}
					} else if (fromIndex >= FIRST_HOTBAR_SLOT && fromIndex <= LAST_HOTBAR_SLOT && !this.insertItem(result, FIRST_INVENTORY_SLOT, LAST_INVENTORY_SLOT + 1, false)) {
						return ItemStack.EMPTY;
					}
				}
			}
			
			// Replace zero-count result stacks with the EMPTY itemstack to help consistency
			if (result.isEmpty()) {
				slot.setStack(ItemStack.EMPTY);
			}
			
			// Mark the slot dirty and duck out if nothing happened
			slot.markDirty();
			if (result.getCount() == initial.getCount()) {
				return ItemStack.EMPTY;
			}
			
			// Fire take-item events which can be important if e.g. the output slot goes back to stonecutter style.
			slot.onTakeItem(player, result);
			// Notify inventory listeners of any changes
			this.sendContentUpdates();
		}
		
		return result;
	}

	@Override
	public void close(PlayerEntity player) {
		super.close(player);
		this.context.run((world, pos) -> this.dropInventory(player, this.input));
		this.context.run((world, pos) -> this.dropInventory(player, this.output));
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
