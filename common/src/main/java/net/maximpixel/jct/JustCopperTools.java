package net.maximpixel.jct;

import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.networking.simple.MessageType;
import dev.architectury.networking.simple.SimpleNetworkManager;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.maximpixel.jct.block.CopperHopperBlock;
import net.maximpixel.jct.block.CopperItemStandBlock;
import net.maximpixel.jct.block.entity.CopperHopperBlockEntity;
import net.maximpixel.jct.block.entity.CopperItemStandBlockEntity;
import net.maximpixel.jct.client.renderer.CopperItemStandRenderer;
import net.maximpixel.jct.inventory.CopperHopperMenu;
import net.maximpixel.jct.item.crafting.RepairCopperToolRecipe;
import net.maximpixel.jct.networking.ServerboundCopperHopperPacket;
import net.maximpixel.jct.screens.CopperHopperScreen;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public abstract class JustCopperTools  {

	public static final String MODID = "jct";

	private static final SimpleNetworkManager SIMPLE_NETWORK_MANAGER = SimpleNetworkManager.create(MODID);

	public static final MessageType TEST_MESSAGE = SIMPLE_NETWORK_MANAGER.registerC2S("test", ServerboundCopperHopperPacket::new);

	public static JustCopperTools INSTANCE;

	public static final Tier COPPER_TIER = new Tier() {
		@Override
		public int getUses() {
			return 131;
		}

		@Override
		public float getSpeed() {
			return 12F;
		}

		@Override
		public float getAttackDamageBonus() {
			return 1F;
		}

		@Override
		public int getLevel() {
			return 14;
		}

		@Override
		public int getEnchantmentValue() {
			return 0;
		}

		@Override
		public @NotNull Ingredient getRepairIngredient() {
			return Ingredient.of(Items.COPPER_INGOT);
		}
	};

	private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(MODID, Registries.BLOCK);
	private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(MODID, Registries.ITEM);
	private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(MODID, Registries.RECIPE_SERIALIZER);
	private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(MODID, Registries.BLOCK_ENTITY_TYPE);
	private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(MODID, Registries.MENU);

	public static final RegistrySupplier<Item>
			COPPER_SHOVEL = regI("copper_shovel", () -> new ShovelItem(COPPER_TIER, 1.5F, -3F, new Item.Properties())),
			COPPER_PICKAXE = regI("copper_pickaxe", () -> new PickaxeItem(COPPER_TIER, 1, -2.8F, new Item.Properties())),
			COPPER_AXE = regI("copper_axe", () -> new AxeItem(COPPER_TIER, 6F, -3.1F, new Item.Properties())),
			COPPER_HOE = regI("copper_hoe", () -> new HoeItem(COPPER_TIER, -2, -1F, new Item.Properties())),
			COPPER_SWORD = regI("copper_sword", () -> new SwordItem(COPPER_TIER, 3, -24F, new Item.Properties()));
	public static final RegistrySupplier<Block> COPPER_HOPPER = regB("copper_hopper", () -> new CopperHopperBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(3F, 4.8F).sound(SoundType.METAL).noOcclusion())),
			COPPER_ITEM_STAND = regB("copper_item_stand", () -> new CopperItemStandBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(3F, 4.8F).sound(SoundType.METAL).noOcclusion()));
	public static final RegistrySupplier<RecipeSerializer<RepairCopperToolRecipe>>
			COPPER_TOOL_REPAIR_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register("repair_copper_tool", () -> new SimpleCraftingRecipeSerializer<>(RepairCopperToolRecipe::new));
	public static final RegistrySupplier<BlockEntityType<CopperHopperBlockEntity>> COPPER_HOPPER_BLOCK_ENTITY_TYPE = BLOCK_ENTITY_TYPES.register("copper_hopper", () -> BlockEntityType.Builder.of(CopperHopperBlockEntity::new, COPPER_HOPPER.get()).build(null));
	public static final RegistrySupplier<BlockEntityType<CopperItemStandBlockEntity>> COPPER_ITEM_STAND_BLOCK_ENTITY_TYPE = BLOCK_ENTITY_TYPES.register("copper_item_stand", () -> BlockEntityType.Builder.of(CopperItemStandBlockEntity::new, COPPER_ITEM_STAND.get()).build(null));
	public static final RegistrySupplier<MenuType<CopperHopperMenu>> COPPER_HOPPER_MENU = MENUS.register("copper_copper", () -> new MenuType<>(CopperHopperMenu::new, FeatureFlags.VANILLA_SET));

	public static RegistrySupplier<Item> regI(String id, Supplier<Item> item) {
		return ITEMS.register(id, item);
	}

	public static RegistrySupplier<Block> regB(String id, Supplier<Block> block) {
		RegistrySupplier<Block> registeredBlock = BLOCKS.register(id, block);
		regI(id, () -> new BlockItem(registeredBlock.get(), new Item.Properties()));
		return registeredBlock;
	}

	public void init() {
		INSTANCE = this;

		BLOCKS.register();
		ITEMS.register();
		RECIPE_SERIALIZERS.register();
		BLOCK_ENTITY_TYPES.register();
		MENUS.register();

		LifecycleEvent.SETUP.register(() -> ITEMS.forEach(item -> CreativeTabRegistry.append(CreativeModeTabs.TOOLS_AND_UTILITIES, item)));

		ClientLifecycleEvent.CLIENT_SETUP.register(instance -> {
			MenuRegistry.registerScreenFactory(COPPER_HOPPER_MENU.get(), CopperHopperScreen::new);
			BlockEntityRendererRegistry.register(COPPER_ITEM_STAND_BLOCK_ENTITY_TYPE.get(), CopperItemStandRenderer::new);
		});
	}
}
