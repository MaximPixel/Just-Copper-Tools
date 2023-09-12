package net.maximpixel.jct.block.entity;

import net.minecraft.world.item.ItemStack;

public interface FilterContainer {

    ItemStack getFilterItem();

    FilterType getFilterType();

    void setFilterItem(ItemStack filterItem);

    void setFilterType(FilterType filterType);
}
