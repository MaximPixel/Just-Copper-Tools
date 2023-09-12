package net.maximpixel.jct.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.maximpixel.jct.JustCopperTools;
import net.maximpixel.jct.block.entity.FilterContainer;
import net.maximpixel.jct.block.entity.FilterType;
import net.maximpixel.jct.inventory.CopperHopperMenu;
import net.maximpixel.jct.networking.ServerboundCopperHopperPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.HopperScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;

import java.util.List;

@Environment(EnvType.CLIENT)
public class CopperHopperScreen extends HopperScreen {

    public static final ResourceLocation TEXTURE = new ResourceLocation(JustCopperTools.MODID, "textures/gui/container/copper_hopper.png");

    private int filterItemX, filterItemY;
    private int filterTypeSwitchX, filterTypeSwitchY;
    private int nextFilterX, nextFilterY;
    private int prevFilterX, prevFilterY;

    public CopperHopperScreen(HopperMenu hopperMenu, Inventory inventory, Component component) {
        super(hopperMenu, inventory, component);
    }

    @Override
    protected void init() {
        super.init();
        filterItemX = 44 - 20;
        filterItemY = 20;
        filterTypeSwitchX = 0;
        filterTypeSwitchY = 0;
        nextFilterX = 0;
        nextFilterY = -18;
        prevFilterX = 0;
        prevFilterY = 18;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        super.render(guiGraphics, i, j, f);
        FilterType filterType = ((CopperHopperMenu) menu).getFilterContainer().getFilterType();
        if (isHovering(filterTypeSwitchX, filterTypeSwitchY, 16, 16, i, j)) {
            guiGraphics.renderTooltip(font, Component.empty().append(filterType.toString()), i, j);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float f, int i, int j) {
        super.renderBg(guiGraphics, f, i, j);

        ItemStack filterItem = ((CopperHopperMenu) menu).getFilterContainer().getFilterItem();

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(leftPos, topPos, 100F);
        guiGraphics.blit(TEXTURE, filterItemX - 1, filterItemY - 1, 0F, 16F, 18, 18, 64, 64);
        if (filterItem.isEmpty()) {
            guiGraphics.blit(TEXTURE, filterItemX, filterItemY, 0F, 0F, 16, 16, 64, 64);
        }
        guiGraphics.blit(TEXTURE, nextFilterX - 1, nextFilterY - 1, 18F + 16F, 16F, 18, 18, 64, 64);
        guiGraphics.blit(TEXTURE, prevFilterX - 1, prevFilterY - 1, 18F, 16F, 18, 18, 64, 64);
        guiGraphics.pose().popPose();

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(leftPos, topPos, 100F);
        guiGraphics.renderItem(filterItem, filterItemX, filterItemY, filterItemX + filterItemY * imageWidth);
        guiGraphics.renderItemDecorations(font, filterItem, filterItemX, filterItemY, null);
        guiGraphics.pose().popPose();

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(leftPos, topPos, 0F);
        if (isHovering(filterItemX, filterItemY, 16, 16, i, j)) {
            guiGraphics.fillGradient(RenderType.guiOverlay(), filterItemX, filterItemY, filterItemX + 16, filterItemY + 16, -2130706433, -2130706433, 0);
        }
        if (isHovering(filterTypeSwitchX, filterTypeSwitchY, 16, 16, i, j)) {
            guiGraphics.fillGradient(RenderType.guiOverlay(), filterTypeSwitchX, filterTypeSwitchY, filterTypeSwitchX + 16, filterTypeSwitchY + 16, -2130706433, -2130706433, 0);
        }
        FilterType filterType = ((CopperHopperMenu) menu).getFilterContainer().getFilterType();
        guiGraphics.drawString(font, filterType.toString(), 0, 0, 4210752, false);
        guiGraphics.pose().popPose();

        guiGraphics.pose().translate(leftPos, topPos, 100F);
        int x = -5;
        int y = 0;
        for (ItemStack displayItem : filterType.getDisplayItems()) {
            guiGraphics.renderItem(displayItem, x, y, x + y * imageWidth);
            guiGraphics.renderItemDecorations(font, displayItem, x, y, null);
            x += 10;
            y += 0;
        }
        guiGraphics.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        FilterContainer filterContainer = ((CopperHopperMenu) menu).getFilterContainer();

        if (isHovering(filterItemX, filterItemY, 16, 16, d, e)) {
            ItemStack filterItem = menu.getCarried().copy();
            filterItem.setCount(1);
            filterContainer.setFilterItem(filterItem);
            new ServerboundCopperHopperPacket(filterContainer.getFilterItem(), filterContainer.getFilterType()).sendToServer();
            return true;
        }

        if (isHovering(filterTypeSwitchX, filterTypeSwitchY, 16, 16, d, e)) {
            filterContainer.setFilterType(filterContainer.getFilterType().getNext());
            new ServerboundCopperHopperPacket(filterContainer.getFilterItem(), filterContainer.getFilterType()).sendToServer();
            return true;
        }

        return super.mouseClicked(d, e, i);
    }
}
