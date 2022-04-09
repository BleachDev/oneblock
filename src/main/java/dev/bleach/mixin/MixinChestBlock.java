package dev.bleach.mixin;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import dev.bleach.FallingActionBlockEntity;
import dev.bleach.OneBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.LockableScreenHandlerFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

@Mixin(ChestBlock.class)
public class MixinChestBlock {

	@Shadow public LockableScreenHandlerFactory method_8702(World world, BlockPos blockPos) { return null; }

	@Overwrite
	public boolean onUse(World world, BlockPos pos, BlockState state, PlayerEntity player, Direction direction, float f, float g, float h) {
		if (world.isClient)
			return true;

		LockableScreenHandlerFactory inventory = method_8702(world, pos);
		if (OneBlock.cheatCodeEnabled()) {
			player.openInventory(inventory);
		} else {
			Random random = new Random();
			for (int slot = 0; slot < inventory.getInvSize(); slot++) {
				ItemStack stack = inventory.getInvStack(slot);
				if (stack == null || stack.count < 1)
					continue;

				Entity entity = FallingActionBlockEntity.create(
						world, stack, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
						(random.nextDouble() - 0.5) * 0.75,
						random.nextDouble() * 0.75,
						(random.nextDouble() - 0.5) * 0.75);

				world.spawnEntity(entity);
			}

			inventory.clear();
			world.removeBlock(pos, false);
		}

		return true;
	}
}
