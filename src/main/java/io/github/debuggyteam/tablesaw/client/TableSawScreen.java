package io.github.debuggyteam.tablesaw.client;

import java.util.List;

import net.minecraft.client.gui.widget.ButtonListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.sound.SoundManager;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

import com.mojang.blaze3d.systems.RenderSystem;

import io.github.debuggyteam.tablesaw.TableSaw;
import io.github.debuggyteam.tablesaw.TableSawRecipes;
import io.github.debuggyteam.tablesaw.TableSawScreenHandler;
import io.github.debuggyteam.tablesaw.api.TableSawRecipe;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class TableSawScreen extends HandledScreen<TableSawScreenHandler> {
	private static final Identifier TEXTURE = new Identifier("minecraft:textures/gui/container/stonecutter.png");
	private static final Identifier BUTTON_TEXTURE = new Identifier(TableSaw.MODID, "textures/gui/saw_button.png");
	
	private static final int RECIPE_SLOT_Y = 166;
	private static final int INSET_RECIPE_SLOT_Y = 184;
	private static final int HOVERED_RECIPE_SLOT_Y = 202;
	private static final int RECIPE_SLOT_WIDTH = 16;
	private static final int RECIPE_SLOT_HEIGHT = 18;
	
	private static final int RECIPE_GRID_X = 52;
	private static final int RECIPE_GRID_Y = 14;
	private static final int RECIPE_GRID_WIDTH = RECIPE_SLOT_WIDTH*4;
	private static final int RECIPE_GRID_HEIGHT = RECIPE_SLOT_HEIGHT*3;
	
	private static final int SCROLLBAR_X = 119;
	private static final int SCROLLBAR_Y = 15;
	private static final int SCROLLBAR_WIDTH = 12;
	private static final int SCROLLBAR_HEIGHT = 54;
	
	private static final int SCROLLBAR_THUMB_WIDTH = 12;
	private static final int SCROLLBAR_THUMB_HEIGHT = 15;
	private static final int SCROLLBAR_THUMB_X = 176;
	private static final int DISABLED_SCROLLBAR_THUMB_X = 176 + SCROLLBAR_THUMB_WIDTH;
	
	private static final int SAW_BUTTON_X = 143;
	private static final int SAW_BUTTON_Y = 60;
	private static final int SAW_BUTTON_WIDTH = 16;
	private static final int SAW_BUTTON_HEIGHT = 16;
	private static final int SAW_BUTTON_U = 0;
	private static final int SAW_BUTTON_V = 0;
	
	private float scrollAmount = 0f;
	private boolean scrollBarClicked = false;
	private int scrollOffset = 0;
	private int selectedSlot = -1;
	private ItemStack selectedItem = ItemStack.EMPTY;
	
	public TableSawScreen(TableSawScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
		super(screenHandler, playerInventory, text);
		screenHandler.setListenerScreen(this::onContentsChanged);
	}
	
	@Override
	protected void init() {
		super.init();

		ButtonWidget.PressAction onClick = (button) -> {
			if (!selectedItem.isEmpty()) {
				PacketByteBuf buf = PacketByteBufs.create();
				buf.writeVarInt(TableSaw.MESSAGE_ENGAGE_TABLESAW);
				buf.writeBoolean(HandledScreen.hasShiftDown()); // ask the server to multicraft if true
				buf.writeItemStack(selectedItem);

				ClientPlayNetworking.send(TableSaw.TABLESAW_CHANNEL, buf);

				if (this.handler.getSlot(0) != null) {
					MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(TableSaw.TABLESAW_SOUND_EVENT, 1.0F));
				}
			}
		};

		TexturedButtonWidget tablesawUseButton = new TexturedButtonWidget(x + SAW_BUTTON_X, y + SAW_BUTTON_Y, SAW_BUTTON_WIDTH, SAW_BUTTON_HEIGHT, SAW_BUTTON_U, SAW_BUTTON_V, 16, BUTTON_TEXTURE, 16, 32, onClick) {
			@Override
			public void playDownSound(SoundManager soundManager) {
				// 👻
			}
		};

		this.addDrawableChild(tablesawUseButton);
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

		int scrollBarOffset = (int) ((SCROLLBAR_HEIGHT-SCROLLBAR_THUMB_HEIGHT) * this.scrollAmount);
		int scrollThumbImageX = (shouldScroll()) ? SCROLLBAR_THUMB_X : DISABLED_SCROLLBAR_THUMB_X;
		this.drawTexture(matrices, x + SCROLLBAR_X, y + SCROLLBAR_Y + scrollBarOffset, scrollThumbImageX, 0, SCROLLBAR_THUMB_WIDTH, SCROLLBAR_THUMB_HEIGHT);

		this.renderRecipeBackground(matrices, mouseX, mouseY, this.x + RECIPE_GRID_X, this.y + RECIPE_GRID_Y, this.scrollOffset);
		this.renderRecipeIcons(x + RECIPE_GRID_X, y + RECIPE_GRID_Y, this.scrollOffset);
	}

	public void onContentsChanged() {
		if (selectedSlot != -1) {
			List<TableSawRecipe> list = getClientsideRecipes();
			if (selectedSlot >= list.size()) {
				selectedSlot = -1;
				selectedItem = ItemStack.EMPTY;
				scrollAmount = 0f;
				scrollOffset = 0;
				return;
			} else if (ItemStack.areEqual(selectedItem, list.get(selectedSlot).getResult())) {
				//Do not reset the stack
			} else {
				selectedSlot = -1;
				selectedItem = ItemStack.EMPTY;
				scrollAmount = 0f;
				scrollOffset = 0;
				return;
			}
		}
		//TODO: Cache recipe list?
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		scrollBarClicked = false;

		int recipeX = x + RECIPE_GRID_X;
		int recipeY = y + RECIPE_GRID_Y + 1;

		if (mouseX >= recipeX && mouseY >= recipeY) {
			if (mouseX < recipeX + RECIPE_GRID_WIDTH && mouseY < recipeY + RECIPE_GRID_HEIGHT) {
				int gridX = ((int) mouseX - recipeX) / RECIPE_SLOT_WIDTH;
				int gridY = ((int) mouseY - recipeY) / RECIPE_SLOT_HEIGHT;
				if (gridX >= 0 || gridY >= 0 || gridX < 4 || gridY < 3) {
					int clickedSlot = (scrollOffset * 4) + (gridY * 4) + gridX;

					List<TableSawRecipe> list = getClientsideRecipes();
					if (clickedSlot < list.size()) {
						selectedSlot = clickedSlot;
						selectedItem = list.get(selectedSlot).getResult();

						MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
						return true;
					}
				}
			}
		}

		if (shouldScroll() && mouseX >= x + SCROLLBAR_X && mouseY >= y + SCROLLBAR_Y && mouseX < x + SCROLLBAR_X + SCROLLBAR_WIDTH && mouseY < y + SCROLLBAR_Y + SCROLLBAR_HEIGHT) {
			this.scrollBarClicked = true;
		}

		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (this.scrollBarClicked && shouldScroll()) {
			int start = y + SCROLLBAR_Y;
			int trackHeight = SCROLLBAR_HEIGHT - SCROLLBAR_THUMB_HEIGHT;
			this.scrollAmount = ((float) mouseY - start - (SCROLLBAR_THUMB_HEIGHT / 2.0f)) / (float) trackHeight;
			this.scrollAmount = MathHelper.clamp(this.scrollAmount, 0.0F, 1.0F);
			this.scrollOffset = (int) (scrollAmount * getMaxScroll() + 0.5f);
			return true;
		} else {
			return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		if (this.shouldScroll()) {
			int maxScroll = this.getMaxScroll();
			float scrollDelta = (float)amount / (float)maxScroll;
			this.scrollAmount = MathHelper.clamp(this.scrollAmount - scrollDelta, 0.0F, 1.0F);
			this.scrollOffset = (int) (scrollAmount * maxScroll + 0.5f);
		}

		return true;
	}

	private void renderRecipeBackground(MatrixStack matrices, int mouseX, int mouseY, int x, int y, int scrollOffset) {
		List<TableSawRecipe> list = getClientsideRecipes();
		if (list.size()==0) return;

		int curSlot = scrollOffset*4;

		loop:
		for(int yi = 0; yi < 3; yi++) {
			for(int xi = 0; xi < 4; xi++) {
				int slotX = x + (xi * RECIPE_SLOT_WIDTH);
				int slotY = y + (yi * RECIPE_SLOT_HEIGHT) + 1;

				int imageY = RECIPE_SLOT_Y;

				if (ItemStack.areEqual(selectedItem, list.get(curSlot).getResult())) {
					imageY = INSET_RECIPE_SLOT_Y;
				} else {
					if (mouseX >= slotX && mouseY >= slotY && mouseX < slotX + RECIPE_SLOT_WIDTH && mouseY < slotY + RECIPE_SLOT_HEIGHT) {
						imageY = HOVERED_RECIPE_SLOT_Y;
					}
				}

				this.drawTexture(matrices, slotX, slotY, 0, imageY, RECIPE_SLOT_WIDTH, RECIPE_SLOT_HEIGHT);

				curSlot++;
				if (curSlot >= list.size()) break loop;
			}
		}
	}

	private void renderRecipeIcons(int x, int y, int scrollOffset) {
		List<TableSawRecipe> list = getClientsideRecipes();
		if (list.size() == 0) return;

		int curSlot = scrollOffset * 4;

		loop:
		for(int yi = 0; yi < 3; yi++) {
			for(int xi = 0; xi < 4; xi++) {
				TableSawRecipe recipe = list.get(curSlot);
				ItemStack stack = list.get(curSlot).getResult();
				this.client.getItemRenderer().renderInGuiWithOverrides(stack, x + (xi * RECIPE_SLOT_WIDTH), y + (yi * RECIPE_SLOT_HEIGHT) + 2);

				String label = switch( TableSawClient.config.iconRatios ) {
					case NONE -> "";
					case RATIO -> (recipe.getQuantity() < 2) ? null : recipe.getQuantity() + ":" + stack.getCount();
					case STRICT_RATIO -> recipe.getQuantity() + ":" + stack.getCount();
					case OUTPUT_COUNT -> null; //(recipe.getResult().getCount() < 2) ? null : Integer.toString(recipe.getResult().getCount());
				};


				this.itemRenderer.renderGuiItemOverlay(this.textRenderer, stack, x + (xi * RECIPE_SLOT_WIDTH), y + (yi * RECIPE_SLOT_HEIGHT) + 2, label);

				curSlot++;
				if (curSlot >= list.size()) break loop;
			}
		}
	}

	@Override
	protected void drawMouseoverTooltip(MatrixStack matrices, int x, int y) {
		super.drawMouseoverTooltip(matrices, x, y);

		int recipeX = this.x + RECIPE_GRID_X;
		int recipeY = this.y + RECIPE_GRID_Y + 1;

		if (x >= recipeX && y >= recipeY) {
			if (x < recipeX + RECIPE_GRID_WIDTH && y < recipeY + RECIPE_GRID_HEIGHT) {
				int gridX = ((int) x - recipeX) / RECIPE_SLOT_WIDTH;
				int gridY = ((int) y - recipeY) / RECIPE_SLOT_HEIGHT;
				if (gridX >= 0 || gridY >= 0 || gridX < 4 || gridY < 3) {
					int hoveredSlot = (scrollOffset * 4) + (gridY * 4) + gridX;
					List<TableSawRecipe> list = this.getClientsideRecipes();
					if (hoveredSlot >= 0 && hoveredSlot < list.size()) {

						if (TableSawClient.config.ratioTooltip) {
							ItemStack input = this.handler.input.getStack(0);
							ItemStack output = list.get(hoveredSlot).getResult();

							List<Text> text = this.getTooltipFromItem(output);

							int sourceCount = list.get(hoveredSlot).getQuantity();
							Text sourceName = Text.empty().append(input.getName()).formatted(input.getRarity().formatting);
							Text destName = Text.empty().append(output.getName()).formatted(output.getRarity().formatting);

							MutableText recipeLine = Text.translatable("container.tablesaw.tablesaw.ratio", sourceCount, sourceName, output.getCount(), destName);
							text.set(0, Text.empty().append(recipeLine));

							renderTooltip(matrices, text, x, y);
						} else {
							renderTooltip(matrices, list.get(hoveredSlot).getResult(), x, y);
						}

					}
				}
			}
		}
	}

	public int recipeCount() {
		return TableSawRecipes.clientInstance().getRecipes(this.handler.getSlot(0).getStack().getItem()).size();
	}

	public List<TableSawRecipe> getClientsideRecipes() {
		return TableSawRecipes.clientInstance().getRecipes(this.handler.getSlot(0).getStack().getItem());
	}

	private boolean shouldScroll() {
		return recipeCount() > 12;
	}

	protected int getMaxScroll() {
		int baseScroll = (int) Math.ceil(recipeCount() / 4.0); // one scroll increment per line of 4 recipes...
		baseScroll -= 3; if (baseScroll < 0) baseScroll = 0;    // ...minus the first screen.

		return baseScroll;
	}


}
