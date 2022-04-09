package dev.bleach;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import dev.bleach.ItemBlock.ItemBlockEntityRenderer;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.RecipeDispatcher;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class FallingActionBlockEntity extends FallingBlockEntity {

	private static Direction[] HORIZONTAL = new Direction[] { Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST };
	private static CraftingInventory TEST_INV = new CraftingInventory(new ScreenHandler() {
		public boolean canUse(PlayerEntity player) { return false; }
	}, 3, 3);


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

		if (!this.world.isClient && removed && getBlockState().getBlock() == Blocks.CRAFTING_TABLE) {
			BlockPos pos = world.getBlockState(getBlockPos()).getBlock() == ItemBlock.BLOCK ? getBlockPos() : getBlockPos().down();
			for (Direction d: HORIZONTAL) {
				ItemStack stack = getBlockRecipe(world, pos, d);
				if (stack != null) {
					replaceBlocks(world, pos, OneBlock.stateFromStack(world, stack));
					return;
				}
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

	private ItemStack getBlockRecipe(World world, BlockPos pos, Direction direction) {
		ItemStack[] items = new ItemStack[9];
		for (int y = -1; y <= 1; y++) {
			for (int x = -1; x <= 1; x++) {
				BlockPos newPos = pos.offset(direction, x).offset(direction.rotateYClockwise(), y);
				BlockState state = world.getBlockState(newPos);

				if (!state.getBlock().getMaterial().isReplaceable())
					items[(y + 1) * 3 + (x + 1)] = OneBlock.stackFromBlock(world.getBlockState(newPos), world.getBlockEntity(newPos));
			}
		}

		return getRecipe(world, items);
	}

	private ItemStack getRecipe(World world, ItemStack... items) {
		for (int i = 0; i < items.length; i++)
			TEST_INV.setInvStack(i, items[i]);

		return RecipeDispatcher.getInstance().matches(TEST_INV, world);
	}

	private void replaceBlocks(World world, BlockPos pos, Pair<BlockState, Supplier<BlockEntity>> state) {
		for (int y = -1; y <= 1; y++) {
			for (int x = -1; x <= 1; x++) {
				BlockPos newPos = pos.add(x, 0, y);
				BlockState oldState = world.getBlockState(newPos);
				if (!oldState.getBlock().getMaterial().isReplaceable()) {
					world.setBlockState(newPos, Blocks.AIR.getDefaultState());
					world.setBlockState(newPos, state.getLeft());
					if (state.getRight() != null)
						world.setBlockEntity(newPos, state.getRight().get());
				}
			}
		}
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
