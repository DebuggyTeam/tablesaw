package io.github.debuggyteam.tablesaw;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class TableSawScreen extends HandledScreen<TableSawScreenHandler> {
	private static final Identifier TEXTURE = new Identifier("minecraft:textures/gui/container/stonecutter.png");
	
	private float scrollAmount;
	private boolean mouseClicked;
	private int scrollOffset;
	private boolean canCraft;
	
	public TableSawScreen(TableSawScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
		super(screenHandler, playerInventory, text);
		// TODO Auto-generated constructor stub
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
		int k = (int)(41.0F * this.scrollAmount);
		this.drawTexture(matrices, x + 119, y + 15 + k, 176 + (this.shouldScroll() ? 0 : 12), 0, 12, 15);
		int l = this.x + 52;
		int m = this.y + 14;
		int n = this.scrollOffset + 12;
		this.renderRecipeBackground(matrices, mouseX, mouseY, l, m, n);
		//this.renderRecipeIcons(l, m, n);
	}
	
	private void renderRecipeBackground(MatrixStack matrices, int mouseX, int mouseY, int x, int y, int scrollOffset) {
		for(int i = this.scrollOffset; i < scrollOffset && i < this.handler.getAvailableRecipeCount(); ++i) {
			int j = i - this.scrollOffset;
			int k = x + j % 4 * 16;
			int l = j / 4;
			int m = y + l * 18 + 2;
			int n = this.backgroundHeight;
			if (i == this.handler.getSelectedRecipe()) {
				n += 18;
			} else if (mouseX >= k && mouseY >= m && mouseX < k + 16 && mouseY < m + 18) {
				n += 36;
			}

			this.drawTexture(matrices, k, m - 1, 0, n, 16, 18);
		}

	}

	@Override
	protected void drawMouseoverTooltip(MatrixStack matrices, int x, int y) {
		super.drawMouseoverTooltip(matrices, x, y);
		if (this.canCraft) {
			int i = this.x + 52;
			int j = this.y + 14;
			int k = this.scrollOffset + 12;
			List<StonecuttingRecipe> list = this.handler.getAvailableRecipes();

			for(int l = this.scrollOffset; l < k && l < this.handler.getAvailableRecipeCount(); ++l) {
				int m = l - this.scrollOffset;
				int n = i + m % 4 * 16;
				int o = j + m / 4 * 18 + 2;
				if (x >= n && x < n + 16 && y >= o && y < o + 18) {
					this.renderTooltip(matrices, ((StonecuttingRecipe)list.get(l)).getOutput(), x, y);
				}
			}
		}
	}
	
	private boolean shouldScroll() {
		return this.canCraft && this.handler.getAvailableRecipeCount() > 12;
	}

	protected int getMaxScroll() {
		return (this.handler.getAvailableRecipeCount() + 4 - 1) / 4 - 3;
	}
}
