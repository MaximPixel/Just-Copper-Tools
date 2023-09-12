package net.maximpixel.jct.item.crafting;

import net.maximpixel.jct.JustCopperTools;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class RepairCopperToolRecipe extends CustomRecipe {

    public RepairCopperToolRecipe(ResourceLocation id, CraftingBookCategory craftingBookCategory) {
        super(id, craftingBookCategory);
    }

    protected MatchResult matchResult(CraftingContainer inventory) {
        ItemStack toolStack = ItemStack.EMPTY;
        ItemStack materialStack = ItemStack.EMPTY;

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);

            if (!stack.isEmpty()) {
                Item item = stack.getItem();

                if (item instanceof TieredItem && ((TieredItem) item).getTier() == JustCopperTools.COPPER_TIER) {
                    if (!toolStack.isEmpty() || !stack.isDamaged()) {
                        return MatchResult.EMPTY;
                    }

                    toolStack = stack;
                    continue;
                } else if (item == Items.COPPER_INGOT || item == Items.RAW_COPPER) {
                    if (!materialStack.isEmpty()) {
                        return MatchResult.EMPTY;
                    }

                    materialStack = stack;
                    continue;
                }

                return MatchResult.EMPTY;
            }
        }

        return new MatchResult(toolStack, materialStack);
    }

    @Override
    public boolean matches(@NotNull CraftingContainer inventory, @NotNull Level level) {
        return matchResult(inventory).matches();
    }

    @Override
    @NotNull
    public ItemStack assemble(@NotNull CraftingContainer inventory, RegistryAccess registryAccess) {
        MatchResult matchResult = matchResult(inventory);

        if (matchResult.matches()) {
            ItemStack toolStack = matchResult.getToolStack();
            ItemStack materialStack = matchResult.getMaterialStack();

            Item materialItem = materialStack.getItem();

            int damageRepair = 10;

            if (materialItem == Items.COPPER_INGOT) {
                damageRepair = 20;
            }

            ItemStack craftStack = toolStack.copy();

            int damage = Math.max(craftStack.getDamageValue() - damageRepair, 0);

            craftStack.setDamageValue(damage);

            return craftStack;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    @NotNull
    public RecipeSerializer<?> getSerializer() {
        return JustCopperTools.COPPER_TOOL_REPAIR_RECIPE_SERIALIZER.get();
    }

    private static class MatchResult {
        public static final MatchResult EMPTY = new MatchResult(ItemStack.EMPTY, ItemStack.EMPTY);

        private final ItemStack toolStack, materialStack;

        private MatchResult(ItemStack toolStack, ItemStack materialStack) {
            this.toolStack = toolStack;
            this.materialStack = materialStack;
        }

        public ItemStack getToolStack() {
            return toolStack;
        }

        public ItemStack getMaterialStack() {
            return materialStack;
        }

        public boolean matches() {
            return this != EMPTY && !toolStack.isEmpty() && !materialStack.isEmpty();
        }
    }
}
