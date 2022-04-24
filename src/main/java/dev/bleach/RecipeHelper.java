package dev.bleach;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeDispatcher;
import net.minecraft.recipe.SmeltingRecipeRegistry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class RecipeHelper {
	
	private static CraftingInventory TEST_INV = new CraftingInventory(new ScreenHandler() {
		public boolean canUse(PlayerEntity player) { return false; }
	}, 3, 3);
	
	public static ItemStack getBlockRecipe(World world, BlockPos pos, Direction direction) {
		ItemStack[] items = new ItemStack[9];
		for (int y = -1; y <= 1; y++) {
			for (int x = -1; x <= 1; x++) {
				BlockPos newPos = pos.offset(direction, x).offset(direction.rotateYClockwise(), y);
				BlockState state = world.getBlockState(newPos);

				if (!state.getBlock().getMaterial(state).isReplaceable())
					items[(y + 1) * 3 + (x + 1)] = OneBlock.stackFromBlock(world.getBlockState(newPos), world.getBlockEntity(newPos));
			}
		}

		return getRecipe(world, items);
	}
	
	public static ItemStack getRecipe(World world, ItemStack... items) {
		for (int i = 0; i < items.length; i++)
			TEST_INV.setInvStack(i, items[i] == null ? ItemStack.EMPTY : items[i]);

		return RecipeDispatcher.matches(TEST_INV, world);
	}
	
	public static ItemStack getFurnaceBlockRecipe(World world, BlockPos pos) {
		return getFurnaceRecipe(OneBlock.stackFromBlock(world.getBlockState(pos), world.getBlockEntity(pos)));
	}
	
	public static ItemStack getFurnaceRecipe(ItemStack input) {
		return input == null ? null : SmeltingRecipeRegistry.getInstance().getResult(input);
	}

}
