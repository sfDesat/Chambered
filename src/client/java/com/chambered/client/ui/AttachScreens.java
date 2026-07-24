package com.chambered.client.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.chambered.attach.AttachContext;
import com.chambered.attach.AttachmentOps;
import com.chambered.attach.SlotType;
import com.chambered.menu.FieldAttachMenu;
import com.chambered.menu.WorkbenchMenu;
import com.chambered.network.ServerboundFieldAttachSelectPayload;
import com.chambered.registry.ModMenuTypes;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.client.renderer.state.gui.GuiItemRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

/**
 * Client screen bindings for attachment / workbench menus.
 */
public final class AttachScreens {
	private AttachScreens() {
	}

	public static void register() {
		MenuScreens.register(ModMenuTypes.FIELD_ATTACH, FieldAttachScreen::new);
		MenuScreens.register(ModMenuTypes.GUN_WORKBENCH, WorkbenchScreen::new);
	}

	/**
	 * Panel-local dim backdrop, 3D gun preview, floating rail slots.
	 * Click a slot to pick from attachments you already carry (no inventory grid).
	 */
	public static class FieldAttachScreen extends AbstractContainerScreen<FieldAttachMenu> {
		private static final Identifier SLOT_SPRITE = Identifier.withDefaultNamespace("container/slot");
		private static final int PANEL = 0xC0181820;
		private static final int PANEL_EDGE = 0xFF3A3A48;
		/** Soft connector — translucent + a shade darker than the old opaque beige. */
		private static final int LINE = 0x68887868;
		private static final int MOUNT_DOT = 0xB0D0C0A0;
		private static final int LABEL = 0xFFE8E0D0;
		private static final int PICKER_ROW = 20;
		private static final int PICKER_WIDTH = 160;
		private static final int PICKER_HEADER = 16;
		private static final int PICKER_PAD_BOTTOM = 4;

		private @Nullable SlotType pickerSlot;
		private final List<PickerEntry> pickerEntries = new ArrayList<>();
		private int pickerX;
		private int pickerY;

		public FieldAttachScreen(FieldAttachMenu menu, Inventory inventory, Component title) {
			super(menu, inventory, title, FieldAttachMenu.PANEL_WIDTH, FieldAttachMenu.PANEL_HEIGHT);
		}

		@Override
		protected void init() {
			super.init();
			// Keep the whole panel on-screen at large GUI scales.
			this.leftPos = Math.max(4, (this.width - this.imageWidth) / 2);
			this.topPos = Math.max(4, (this.height - this.imageHeight) / 2);
			this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
			this.titleLabelY = 6;
		}

		@Override
		public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
			int xo = this.leftPos;
			int yo = this.topPos;

			// Dim only the attach panel — leave the rest of the world undimmed.
			graphics.fill(xo, yo, xo + this.imageWidth, yo + this.imageHeight, PANEL);
			graphics.outline(xo, yo, this.imageWidth, this.imageHeight, PANEL_EDGE);

			drawGunPreview(graphics, xo, yo);
			drawConnectorsAndSlotFrames(graphics, xo, yo);

			if (pickerSlot != null) {
				drawPicker(graphics, mouseX, mouseY);
			}
		}

		private void drawGunPreview(GuiGraphicsExtractor graphics, int xo, int yo) {
			ItemStack gun = menu.gunStack();
			if (gun.isEmpty()) {
				return;
			}

			int cx = xo + FieldAttachMenu.GUN_CENTER_X;
			int cy = yo + FieldAttachMenu.GUN_CENTER_Y;

			TrackingItemStackRenderState state = new TrackingItemStackRenderState();
			Minecraft mc = this.minecraft;
			mc.getItemModelResolver().updateForTopItem(
					state,
					gun,
					ItemDisplayContext.FIXED,
					mc.level,
					mc.player,
					0
			);
			// Force oversized PIP path so hi-res bounds/scale mixins can apply.
			state.setOversizedInGui(true);

			int itemX = cx - 8;
			int itemY = cy - 8;
			graphics.pose().pushMatrix();
			FieldAttachPreview.markNextItemHiResGun();
			graphics.guiRenderState.addItem(new GuiItemRenderState(
					new Matrix3x2f(graphics.pose()),
					state,
					itemX,
					itemY,
					null
			));
			graphics.pose().popMatrix();
		}

		private void drawConnectorsAndSlotFrames(GuiGraphicsExtractor graphics, int xo, int yo) {
			int gunCx = xo + FieldAttachMenu.GUN_CENTER_X;
			int gunCy = yo + FieldAttachMenu.GUN_CENTER_Y;
			ItemStack gun = menu.gunStack();

			for (int i = 0; i < menu.attachmentSlotCount(); i++) {
				Slot slot = menu.slots.get(i);
				if (!(slot instanceof FieldAttachMenu.AttachmentSlot attachSlot)) {
					continue;
				}
				SlotType type = attachSlot.slotType();

				int slotCx = xo + slot.x + 8;
				int slotCy = yo + slot.y + 8;

				int mountX = gunCx + type.mountPointX();
				int mountY = gunCy + type.mountPointY();
				var projected = FieldAttachBoneProjector.projectMount(gun, type, gunCx, gunCy, FieldAttachPreview.GUN_SCALE);
				if (projected.isPresent()) {
					mountX = Math.round(projected.get().x);
					mountY = Math.round(projected.get().y);
				}

				drawConnector(graphics, slotCx, slotCy, mountX, mountY);
				graphics.fill(mountX - 1, mountY - 1, mountX + 2, mountY + 2, MOUNT_DOT);

				graphics.fill(xo + slot.x - 2, yo + slot.y - 2, xo + slot.x + 18, yo + slot.y + 18, PANEL);
				graphics.outline(xo + slot.x - 2, yo + slot.y - 2, 20, 20, PANEL_EDGE);
				graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_SPRITE, xo + slot.x, yo + slot.y, 16, 16);

				Component label = Component.translatable("slot.chambered." + type.getSerializedName());
				int labelX = xo + slot.x + 8 - this.font.width(label) / 2;
				int labelY = type == SlotType.UNDERBARREL ? yo + slot.y + 20 : yo + slot.y - 11;
				graphics.text(this.font, label, labelX, labelY, LABEL, true);
			}
		}

		private void drawPicker(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
			boolean hasMounted = menu.indexOf(pickerSlot) >= 0 && menu.slots.get(menu.indexOf(pickerSlot)).hasItem();
			int extraUnequip = hasMounted ? 1 : 0;
			int totalRows = Math.max(1, pickerEntries.size() + extraUnequip);
			if (pickerEntries.isEmpty() && !hasMounted) {
				totalRows = 1;
			}

			int height = PICKER_HEADER + totalRows * PICKER_ROW + PICKER_PAD_BOTTOM;
			graphics.fill(pickerX, pickerY, pickerX + PICKER_WIDTH, pickerY + height, 0xF0101018);
			graphics.outline(pickerX, pickerY, PICKER_WIDTH, height, PANEL_EDGE);

			Component header = Component.translatable("container.chambered.field_attach.pick",
					Component.translatable("slot.chambered." + pickerSlot.getSerializedName()));
			graphics.text(this.font, header, pickerX + 6, pickerY + 4, LABEL, true);

			int row = 0;
			int y0 = pickerY + PICKER_HEADER;

			if (hasMounted) {
				int ry = y0 + row * PICKER_ROW;
				boolean hover = mouseX >= pickerX && mouseX < pickerX + PICKER_WIDTH && mouseY >= ry && mouseY < ry + PICKER_ROW;
				if (hover) {
					graphics.fill(pickerX + 1, ry, pickerX + PICKER_WIDTH - 1, ry + PICKER_ROW, 0x40FFFFFF);
				}
				graphics.text(this.font, Component.translatable("container.chambered.field_attach.unequip"), pickerX + 6, ry + 6, 0xFFFFAAAA, false);
				row++;
			}

			if (pickerEntries.isEmpty() && !hasMounted) {
				graphics.text(
						this.font,
						Component.translatable("container.chambered.field_attach.empty"),
						pickerX + 6,
						y0 + 6,
						0xFF888888,
						false
				);
				return;
			}

			for (PickerEntry entry : pickerEntries) {
				int ry = y0 + row * PICKER_ROW;
				boolean hover = mouseX >= pickerX && mouseX < pickerX + PICKER_WIDTH && mouseY >= ry && mouseY < ry + PICKER_ROW;
				if (hover) {
					graphics.fill(pickerX + 1, ry, pickerX + PICKER_WIDTH - 1, ry + PICKER_ROW, 0x40FFFFFF);
				}
				graphics.item(entry.stack(), pickerX + 4, ry + 2);
				graphics.text(this.font, entry.stack().getHoverName(), pickerX + 24, ry + 6, LABEL, false);
				row++;
			}
		}

		private static void drawConnector(GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1) {
			int midX = x0 + (x1 - x0) / 2;
			graphics.horizontalLine(Math.min(x0, midX), Math.max(x0, midX), y0, LINE);
			graphics.verticalLine(midX, Math.min(y0, y1), Math.max(y0, y1), LINE);
			graphics.horizontalLine(Math.min(midX, x1), Math.max(midX, x1), y1, LINE);
		}

		@Override
		protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
			// Title sits above the optic slot / gun cluster.
			graphics.text(this.font, this.title, this.titleLabelX, this.titleLabelY, LABEL, true);
		}

		@Override
		public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
			double mx = event.x();
			double my = event.y();

			if (pickerSlot != null) {
				if (handlePickerClick(mx, my)) {
					return true;
				}
				closePicker();
				return true;
			}

			Slot hovered = findAttachmentSlot(mx, my);
			if (hovered instanceof FieldAttachMenu.AttachmentSlot attachSlot && event.button() == 0) {
				openPicker(attachSlot.slotType(), (int) mx, (int) my);
				return true;
			}

			// Click dim backdrop to close.
			if (hasClickedOutside(mx, my, this.leftPos, this.topPos)) {
				this.onClose();
				return true;
			}

			return true;
		}

		private @Nullable Slot findAttachmentSlot(double mx, double my) {
			for (int i = 0; i < menu.attachmentSlotCount(); i++) {
				Slot slot = menu.slots.get(i);
				int x = this.leftPos + slot.x;
				int y = this.topPos + slot.y;
				if (mx >= x && mx < x + 16 && my >= y && my < y + 16) {
					return slot;
				}
			}
			return null;
		}

		private void openPicker(SlotType slotType, int mouseX, int mouseY) {
			this.pickerSlot = slotType;
			this.pickerEntries.clear();

			ItemStack gun = menu.gunStack();
			Inventory inv = menu.playerInventory();
			Set<Identifier> seen = new HashSet<>();
			for (int i = 0; i < inv.getContainerSize(); i++) {
				ItemStack stack = inv.getItem(i);
				if (stack.isEmpty()) {
					continue;
				}
				if (!AttachmentOps.canInstall(AttachContext.FIELD, gun, slotType, stack)) {
					continue;
				}
				// One row per attachment item type — extras in inventory are ignored here.
				Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
				if (!seen.add(id)) {
					continue;
				}
				pickerEntries.add(new PickerEntry(i, stack.copyWithCount(1)));
			}

			int rows = Math.max(1, pickerEntries.size() + (menu.indexOf(slotType) >= 0 && menu.slots.get(menu.indexOf(slotType)).hasItem() ? 1 : 0));
			int pickerHeight = PICKER_HEADER + rows * PICKER_ROW + PICKER_PAD_BOTTOM;
			this.pickerX = Math.min(mouseX + 8, this.width - PICKER_WIDTH - 4);
			this.pickerY = Math.min(mouseY + 8, this.height - pickerHeight - 4);
			this.pickerX = Math.max(4, this.pickerX);
			this.pickerY = Math.max(4, this.pickerY);
		}

		private boolean handlePickerClick(double mx, double my) {
			if (pickerSlot == null) {
				return false;
			}
			int index = menu.indexOf(pickerSlot);
			boolean hasMounted = index >= 0 && menu.slots.get(index).hasItem();
			int y0 = pickerY + PICKER_HEADER;
			int row = 0;

			if (hasMounted) {
				int ry = y0 + row * PICKER_ROW;
				if (mx >= pickerX && mx < pickerX + PICKER_WIDTH && my >= ry && my < ry + PICKER_ROW) {
					sendSelect(pickerSlot, -1);
					closePicker();
					return true;
				}
				row++;
			}

			for (PickerEntry entry : pickerEntries) {
				int ry = y0 + row * PICKER_ROW;
				if (mx >= pickerX && mx < pickerX + PICKER_WIDTH && my >= ry && my < ry + PICKER_ROW) {
					sendSelect(pickerSlot, entry.inventorySlot());
					closePicker();
					return true;
				}
				row++;
			}

			int totalRows = Math.max(1, pickerEntries.size() + (hasMounted ? 1 : 0));
			int height = PICKER_HEADER + totalRows * PICKER_ROW + PICKER_PAD_BOTTOM;
			return mx >= pickerX && mx < pickerX + PICKER_WIDTH
					&& my >= pickerY && my < pickerY + height;
		}

		private void sendSelect(SlotType slot, int inventorySlot) {
			if (ClientPlayNetworking.canSend(ServerboundFieldAttachSelectPayload.TYPE)) {
				ClientPlayNetworking.send(new ServerboundFieldAttachSelectPayload(slot, inventorySlot));
			}
		}

		private void closePicker() {
			pickerSlot = null;
			pickerEntries.clear();
		}

		@Override
		public boolean isPauseScreen() {
			return false;
		}

		private record PickerEntry(int inventorySlot, ItemStack stack) {
		}
	}

	public static class WorkbenchScreen extends AbstractContainerScreen<WorkbenchMenu> {
		private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("chambered", "textures/gui/gun_workbench.png");

		public WorkbenchScreen(WorkbenchMenu menu, Inventory inventory, Component title) {
			super(menu, inventory, title);
		}

		@Override
		protected void init() {
			super.init();
			this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
		}

		@Override
		public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
			super.extractBackground(graphics, mouseX, mouseY, delta);
			int xo = (this.width - this.imageWidth) / 2;
			int yo = (this.height - this.imageHeight) / 2;
			graphics.fill(xo, yo, xo + this.imageWidth, yo + this.imageHeight, 0xFF2A2A32);
			graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, xo, yo, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
		}

		@Override
		protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
			graphics.text(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFFE8E0D0, false);
			float condition = menu.gunCondition();
			Component line = condition >= 0.0f
					? Component.translatable("container.chambered.gun_workbench.condition", String.format("%.0f", condition))
					: Component.translatable("container.chambered.gun_workbench.hint");
			graphics.text(this.font, line, 8, 60, 0xFFC8C0B0, false);
		}
	}
}
