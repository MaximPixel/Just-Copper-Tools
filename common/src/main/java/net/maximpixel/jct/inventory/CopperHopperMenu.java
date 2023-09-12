package net.maximpixel.jct.inventory;

import net.maximpixel.jct.JustCopperTools;
import net.maximpixel.jct.block.entity.FilterContainer;
import net.maximpixel.jct.block.entity.SimpleFilterContainer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.NotNull;

public class CopperHopperMenu extends HopperMenu {

    protected final FilterContainer filterContainer;

    public CopperHopperMenu(int i, Inventory inventory) {
        this(i, inventory, new SimpleContainer(CONTAINER_SIZE), new SimpleFilterContainer());
    }

    public CopperHopperMenu(int i, Inventory inventory, Container container, FilterContainer filterContainer) {
        super(i, inventory, container);
        this.filterContainer = filterContainer;
    }

    @NotNull
    @Override
    public MenuType<?> getType() {
        return JustCopperTools.COPPER_HOPPER_MENU.get();
    }

    public FilterContainer getFilterContainer() {
        return filterContainer;
    }
}
