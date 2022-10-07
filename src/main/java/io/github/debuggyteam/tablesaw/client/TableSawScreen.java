package io.github.debuggyteam.tablesaw.client;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import io.github.debuggyteam.tablesaw.TableSawRecipes;
import io.github.debuggyteam.tablesaw.TableSawScreenHandler;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class TableSawScreen extends HandledScreen<TableSawScreenHandler> {
	
	
	private static final Identifier TEXTURE = new Identifier("minecraft:textures/gui/container/stonecutter.png");
	
	private static final int RECIPE_GRID_X = 52;
	private static final int RECIPE_GRID_Y = 14;
	
	private static final int RECIPE_SLOT_Y = 166;
	private static final int INSET_RECIPE_SLOT_Y = 184;
	private static final int HOVERED_RECIPE_SLOT_Y = 202;
	private static final int RECIPE_SLOT_WIDTH = 16;
	private static final int RECIPE_SLOT_HEIGHT = 18;
	
	private static final int SCROLLBAR_START_X = 119;
	private static final int SCROLLBAR_START_Y = 15;
	private static final int SCROLLBAR_WIDTH = 12;
	private static final int SCROLLBAR_HEIGHT = 54;
	
	private static final int SCROLLBAR_THUMB_X = 176;
	private static final int SCROLLBAR_THUMB_WIDTH = 12;
	private static final int SCROLLBAR_THUMB_HEIGHT = 15;
	
	private float scrollAmount;
	private boolean mouseClicked;
	private int scrollOffset;
	private boolean canCraft;
	
	public TableSawScreen(TableSawScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
		super(screenHandler, playerInventory, text);
		screenHandler.setListenerScreen(this::onContentsChanged);
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		super.render(matrices, mouseX, mouseY, delta);
		this.drawMouseoverTooltip(matrices, mouseX, mouseY);
	}
	
	@Override
	protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
		this.renderBackground(matrices);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, TEXTURE);
		this.drawTexture(matrices, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);
		
		int scrollBarOffset = (int) (41 * this.scrollAmount);
		this.drawTexture(matrices, x + 119, y + 15 + scrollBarOffset, 176 + (this.shouldScroll() ? 0 : 12), 0, 12, 15);
		
		this.renderRecipeBackground(matrices, mouseX, mouseY, this.x + RECIPE_GRID_X, this.y + RECIPE_GRID_Y, this.scrollOffset);
		this.renderRecipeIcons(x + RECIPE_GRID_X, y + RECIPE_GRID_Y, this.scrollOffset);
	}
	
	public void onContentsChanged() {
		//TODO: Cache recipe list
	}
	
	private void renderRecipeBackground(MatrixStack matrices, int mouseX, int mouseY, int x, int y, int scrollOffset) {
		int recipeCount = recipeCount();
		if (recipeCount==0) return;
		
		int curSlot = 0;
		
		loop:
		for(int yi=0; yi<3; yi++) {
			for(int xi=0; xi<4; xi++) {
				//TODO: Decide which texture this slot has, e.g. selected, hovered
				int slotX = x+(xi*RECIPE_SLOT_WIDTH);
				int slotY = y+(yi*RECIPE_SLOT_HEIGHT)+1;
				
				int imageY = RECIPE_SLOT_Y;
				if (mouseX>=slotX && mouseY>=slotY && mouseX<slotX+RECIPE_SLOT_WIDTH && mouseY<slotY+RECIPE_SLOT_HEIGHT) {
					imageY = HOVERED_RECIPE_SLOT_Y;
				}
				
				this.drawTexture(matrices, slotX, slotY, 0, imageY, RECIPE_SLOT_WIDTH, RECIPE_SLOT_HEIGHT);
				
				curSlot++;
				if (curSlot>=recipeCount) break loop;
			}
		}
	}
	
	private void renderRecipeIcons(int x, int y, int scrollOffset) {
		List<ItemStack> list = getClientsideRecipes();
		if (list.size()==0) return;
		
		int curSlot = 0;
		
		loop:
		for(int yi=0; yi<3; yi++) {
			for(int xi=0; xi<4; xi++) {
				this.client.getItemRenderer().renderInGuiWithOverrides(list.get(curSlot), x+(xi*RECIPE_SLOT_WIDTH), y+(yi*RECIPE_SLOT_HEIGHT)+2);
				
				curSlot++;
				if (curSlot>=list.size()) break loop;
			}
		}

	}

	@Override
	protected void drawMouseoverTooltip(MatrixStack matrices, int x, int y) {
		super.drawMouseoverTooltip(matrices, x, y);
		
		
		
		if (this.canCraft) {
			int i = this.x + 52;
			int j = this.y + 14;
			int k = this.scrollOffset + 12;
			List<ItemStack> list = this.getClientsideRecipes();

			for(int l = this.scrollOffset; l < k && l < recipeCount(); ++l) {
				int m = l - this.scrollOffset;
				int n = i + m % 4 * 16;
				int o = j + m / 4 * 18 + 2;
				if (x >= n && x < n + 16 && y >= o && y < o + 18) {
					this.renderTooltip(matrices, (list.get(l)), x, y);
				}
			}
		}
	}
	
	public int recipeCount() {
		return TableSawRecipes.clientInstance().getRecipes(this.handler.getSlot(0).getStack().getItem()).size();
	}
	
	public List<ItemStack> getClientsideRecipes() {
		return TableSawRecipes.clientInstance().getRecipes(this.handler.getSlot(0).getStack().getItem());
	}
	
	private boolean shouldScroll() {
		return recipeCount() > 12;
	}

	protected int getMaxScroll() {
		int baseScroll = (int) Math.ceil(recipeCount() / 4.0); // one scroll increment per line of 4 recipes...
		baseScroll -= 12; if (baseScroll<0) baseScroll = 0;    // ...minus the first screen.
		
		return baseScroll;
	}
	
	
}
