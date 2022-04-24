package dev.bleach;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class ItemBlock extends BlockWithEntity {

	public static ItemBlock BLOCK = new ItemBlock();

	protected ItemBlock() {
		super(Material.CLAY);
		this.disableStats();
		this.transluscent = true;
		this.strength = 1f;
	}

	@Override
	public boolean method_11560(BlockState blockState) {
		return false;
	}

	@Override
	public float method_11565(BlockState blockState) {
		return 1.0F;
	}

	@Override
	public Box getBoundingBox(BlockState state, WorldView view, BlockPos pos) {
		return new Box(0, 0, 0, 1, 0.1, 1);
	}

	@Override
	public void randomDropAsItem(World world, BlockPos pos, BlockState state, float chance, int id) {
		ItemStack item = ((ItemBlockEntity) world.getBlockEntity(pos)).getItem();
		if (item != null) {
			onBlockBreak(world, pos, new ItemStack(item.getItem(), 1, this.getMeta(state)));
		}
	}

	@Override
	public BlockEntity createBlockEntity(World world, int id) {
		return new ItemBlockEntity();
	}

	public static class ItemBlockEntity extends BlockEntity {

		private ItemStack item;
		public float renderYaw = (System.currentTimeMillis() / 5L) % 360;

		@Override
		public void fromNbt(NbtCompound nbt) {
			super.fromNbt(nbt);
			if (nbt.contains("ItemStack", 10)) {
				this.setItem(new ItemStack(nbt.getCompound("ItemStack")));
			}

		}

		@Override
		public NbtCompound method_541(NbtCompound nbt) {
			if (this.getItem() != null) {
				nbt.put("ItemStack", this.getItem().toNbt(new NbtCompound()));
			}
			return super.method_541(nbt);
		}

		@Override
		public BlockEntityUpdateS2CPacket getUpdatePacket() {
			NbtCompound nbtCompound = new NbtCompound();
			this.method_541(nbtCompound);
			return new BlockEntityUpdateS2CPacket(this.pos, 0, nbtCompound);
		}

		public ItemStack getItem() {
			return this.item;
		}

		public void setItem(ItemStack itemStack) {
			this.item = itemStack;
			this.markDirty();
		}
	}

	public static class ItemBlockEntityRenderer extends BlockEntityRenderer<ItemBlockEntity> {

		@Override
		public void method_1631(ItemBlockEntity blockEntity, double x, double y, double z, float tickDelta, int destroyProgress, float k) {
			if (blockEntity.item != null)
				renderItem(blockEntity.item, x, y, z, blockEntity.renderYaw, tickDelta);
		}

		public static void renderItem(ItemStack stack, double x, double y, double z, float rotation, float tickDelta) {
			GlStateManager.pushMatrix();
			GlStateManager.translatef((float)x + 0.5F, (float)y, (float)z + 0.5F);

			GlStateManager.rotatef(rotation, 0f, 1f, 0f);
			GlStateManager.rotatef(90f, 1f, 0f, 0f);

			MinecraftClient.getInstance().getTextureManager().bindTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEX);
			BakedModel bakedModel = MinecraftClient.getInstance().getItemRenderer().getModels().getModel(stack);
			MinecraftClient.getInstance().getItemRenderer().method_10243(stack, bakedModel);

			GlStateManager.popMatrix();
		}
	}
}
