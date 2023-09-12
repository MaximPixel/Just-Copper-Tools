package net.maximpixel.jct.block.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.*;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public enum FilterTypes implements FilterType {
    NONE {
        @Override
        public boolean filter(ItemStack filterItem, ItemStack stack) {
            return false;
        }

        @Override
        public Collection<ItemStack> getDisplayItems() {
            return List.of(new ItemStack(Items.BARRIER));
        }
    },
    ITEM {
        @Override
        public boolean filter(ItemStack filterItem, ItemStack stack) {
            return filterItem.is(stack.getItem());
        }

        @Override
        public Collection<ItemStack> getDisplayItems() {
            return List.of(new ItemStack(Items.IRON_INGOT), new ItemStack(Items.IRON_INGOT));
        }
    },
    DAMAGE_BOOLEAN {
        @Override
        public boolean filter(ItemStack filterItem, ItemStack stack) {
            return filterItem.isDamaged() == stack.isDamaged();
        }

        @Override
        public Collection<ItemStack> getDisplayItems() {
            ItemStack first = new ItemStack(Items.WOODEN_PICKAXE);
            first.setDamageValue(20);
            ItemStack second = new ItemStack(Items.DIAMOND_SWORD);
            first.setDamageValue(1);
            return List.of(first, second);
        }
    },
    DAMAGE {
        @Override
        public boolean filter(ItemStack filterItem, ItemStack stack) {
            return filterItem.getDamageValue() == stack.getDamageValue();
        }

        @Override
        public Collection<ItemStack> getDisplayItems() {
            ItemStack first = new ItemStack(Items.WOODEN_PICKAXE);
            first.setDamageValue(1);
            ItemStack second = new ItemStack(Items.DIAMOND_SWORD);
            first.setDamageValue(1);
            return List.of(first, second);
        }
    },
    SAME_NBT {
        @Override
        public boolean filter(ItemStack filterItem, ItemStack stack) {
            CompoundTag filterTag = filterItem.getTag();
            CompoundTag stackTag = stack.getTag();
            if (filterTag == null) {
                return stackTag == null;
            }
            return filterItem.getTag().equals(stack.getTag());
        }

        @Override
        public Collection<ItemStack> getDisplayItems() {
            return List.of(new ItemStack(Items.ENCHANTED_BOOK), new ItemStack(Items.ENCHANTED_BOOK));
        }
    },
    SAME_TAG {
        @Override
        public boolean filter(ItemStack filterItem, ItemStack stack) {
            return filterItem.getTags().anyMatch(filterItemTagKey -> stack.getTags().anyMatch(stackItemTagKey -> stackItemTagKey == filterItemTagKey));
        }

        @Override
        public Collection<ItemStack> getDisplayItems() {
            return List.of(new ItemStack(Items.OAK_PLANKS), new ItemStack(Items.BAMBOO_PLANKS));
        }
    },
    CLASS {
        @Override
        public boolean filter(ItemStack filterItem, ItemStack stack) {
            Class<? extends Item> filterClass = filterItem.getItem().getClass();
            Class<? extends Item> stackClass = filterItem.getItem().getClass();
            return filterClass == stackClass;
        }

        @Override
        public Collection<ItemStack> getDisplayItems() {
            return List.of(new ItemStack(Items.WOODEN_PICKAXE), new ItemStack(Items.GOLDEN_PICKAXE));
        }
    };

    @Override
    public FilterType getNext() {
        FilterType[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    @Override
    public FilterType getPrev() {
        FilterType[] values = values();
        return values[(ordinal() + values.length - 1) % values.length];
    }

    @Override
    public String getName() {
        return name();
    }
}
