package net.maximpixel.jct.block.entity;

import net.minecraft.world.item.ItemStack;

import java.util.Collection;

public interface FilterType {
    boolean filter(ItemStack filterItem, ItemStack stack);
    Collection<ItemStack> getDisplayItems();
    FilterType getNext();
    FilterType getPrev();
}
