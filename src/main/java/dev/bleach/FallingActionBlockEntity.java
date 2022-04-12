package dev.bleach;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import dev.bleach.ItemBlock.ItemBlockEntityRenderer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.GlassBlock;
import net.minecraft.block.PaneBlock;
import net.minecraft.block.StainedGlassBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class FallingActionBlockEntity extends FallingBlockEntity {

	private static Direction[] HORIZONTAL = new Direction[] { Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST };

	private List<Entity> inEntities = new ArrayList<>();

	protected FallingActionBlockEntity(World world, double prevX, double prevY, double prevZ, BlockState block) {
		super(world, prevX, prevY, prevZ, block);
	}

	public static FallingActionBlockEntity create(World world, ItemStack stack, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
		Pair<BlockState, Supplier<BlockEntity>> state = OneBlock.stateFromStack(world, stack);

		FallingActionBlockEntity entity = new FallingActionBlockEntity(world, x, y, z, state.getLeft());
		entity.addVelocity(velocityX, velocityY, velocityZ);
		entity.timeFalling = 1;

		if (state.getRight() != null) {
			NbtCompound nbt = new NbtCompound();
			state.getRight().get().toNbt(nbt);
			entity.tileEntityData = nbt;
		}

		return entity;
	}

	@Override
	public void tick() {
		if (ticksAlive == 1) {
			for (Entity e: this.world.getEntitiesIn(this, this.getBoundingBox())) {
				inEntities.add(e);
			}
		} else {
			List<Entity> newEntities = new ArrayList<>();
			for (Entity e: this.world.getEntitiesIn(this, this.getBoundingBox())) {
				if (inEntities.contains(e)) {
					newEntities.add(e);
				} else {
					e.damage(DamageSource.IN_WALL, 1f);
					e.addVelocity(velocityX * 0.5, velocityY * 0.5, velocityZ * 0.5);

					if (e instanceof PlayerEntity) {
						e.getArmorStacks()[3] = OneBlock.stackFromBlock(getBlockState(), tileEntityData);
						remove();
						return;
					}
				}
			}

			inEntities = newEntities;
		}

		super.tick();

		if (!this.world.isClient && removed) {
			Block block = getBlockState().getBlock();
			BlockPos pos = world.getBlockState(getBlockPos()).getBlock() == ItemBlock.BLOCK ? getBlockPos() : getBlockPos().down();
			
			if (block == Blocks.CRAFTING_TABLE) {
				for (Direction d: HORIZONTAL) {
					ItemStack stack = RecipeHelper.getBlockRecipe(world, pos, d);
					if (stack != null) {
						replaceBlocks(world, pos, OneBlock.stateFromStack(world, stack));
						return;
					}
				}
			} else if (block == Blocks.FURNACE || block == Blocks.LIT_FURNACE) {
				ItemStack stack = RecipeHelper.getFurnaceBlockRecipe(world, pos);
				if (stack != null) {
					replaceBlock(world, pos, OneBlock.stateFromStack(world, stack));
				}
			} else if ((block instanceof GlassBlock || block instanceof StainedGlassBlock || block instanceof PaneBlock) && ThreadLocalRandom.current().nextBoolean()) {
				world.setBlockState(getBlockPos(), Blocks.AIR.getDefaultState());
				world.playSound(pos.getX(), pos.getY(), pos.getZ(), block.sound.getSound(), 1f, 1f);
			}
		}
	}

	@Override
	public ItemEntity dropItem(ItemStack stack, float yOffset) {
		if (getBlockState().getBlock() == ItemBlock.BLOCK && tileEntityData != null) {
			ItemStack nbtStack = ItemStack.fromNbt(tileEntityData.getCompound("ItemStack"));
			ItemEntity entity = new ItemEntity(this.world, this.x, this.y + (double)yOffset, this.z, nbtStack);
			entity.setToDefaultPickupDelay();
			this.world.spawnEntity(entity);
			return entity;
		}

		return super.dropItem(stack, yOffset);
	}

	private void replaceBlocks(World world, BlockPos pos, Pair<BlockState, Supplier<BlockEntity>> state) {
		for (int y = -1; y <= 1; y++) {
			for (int x = -1; x <= 1; x++) {
				BlockPos newPos = pos.add(x, 0, y);
				if (!world.getBlockState(newPos).getBlock().getMaterial().isReplaceable()) {
					replaceBlock(world, newPos, state);
				}
			}
		}
	}
	
	private void replaceBlock(World world, BlockPos pos, Pair<BlockState, Supplier<BlockEntity>> state) {
		world.setBlockState(pos, Blocks.AIR.getDefaultState());
		world.setBlockState(pos, state.getLeft());
		if (state.getRight() != null)
			world.setBlockEntity(pos, state.getRight().get());
	}

	public static class FallingActionBlockEntityRenderer extends EntityRenderer<FallingActionBlockEntity> {

		public FallingActionBlockEntityRenderer(EntityRenderDispatcher entityRenderDispatcher) {
			super(entityRenderDispatcher);
		}

		@Override
		public void render(FallingActionBlockEntity fallingBlockEntity, double d, double e, double f, float g, float h) {
			if (fallingBlockEntity.getBlockState().getBlock() == ItemBlock.BLOCK && fallingBlockEntity.tileEntityData != null) {
				ItemStack stack = ItemStack.fromNbt(fallingBlockEntity.tileEntityData.getCompound("ItemStack"));
				if (stack != null)
					ItemBlockEntityRenderer.renderItem(stack, d, e, f, (System.currentTimeMillis() / 5L) % 360, h);

				super.render(fallingBlockEntity, d, e, f, g, h);
			} else {
				dispatcher.getRenderer(FallingBlockEntity.class).render(fallingBlockEntity, d, e, f, g, h);
			}
		}

		@Override
		protected Identifier getTexture(FallingActionBlockEntity entity) {
			return SpriteAtlasTexture.BLOCK_ATLAS_TEX;
		}
	}
}
