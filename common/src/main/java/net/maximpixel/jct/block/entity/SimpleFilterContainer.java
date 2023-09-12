package net.maximpixel.jct.block.entity;

import net.minecraft.world.item.ItemStack;

public class SimpleFilterContainer implements FilterContainer {

    protected ItemStack filterItem = ItemStack.EMPTY;
    protected FilterType filterType = FilterTypes.NONE;

    @Override
    public ItemStack getFilterItem() {
        return filterItem;
    }

    @Override
    public FilterType getFilterType() {
        return filterType;
    }

    @Override
    public void setFilterItem(ItemStack filterItem) {
        this.filterItem = filterItem;
    }

    @Override
    public void setFilterType(FilterType filterType) {
        this.filterType = filterType;
    }
}
