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
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public class ItemBlock extends BlockWithEntity {

	public static ItemBlock BLOCK = new ItemBlock();

	protected ItemBlock() {
		super(Material.CLAY);
		this.disableStats();
		this.transluscent = true;
		this.strength = 1f;
		this.setBoundingBox(0.0F, 0.0F, 0.0F, 1.0F, 0.1F, 1.0F);
	}

	@Override
	public boolean hasTransperancy() {
		return false;
	}

	@Override
	public float getAmbientOcclusionLightLevel() {
		return 1.0F;
	}

	@Override
	public Box getCollisionBox(World world, BlockPos pos, BlockState state) {
		return null;
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

		public void fromNbt(NbtCompound nbt) {
			super.fromNbt(nbt);
			if (nbt.contains("ItemStack", 10)) {
				this.setItem(ItemStack.fromNbt(nbt.getCompound("ItemStack")));
			}

		}

		public void toNbt(NbtCompound nbt) {
			super.toNbt(nbt);
			if (this.getItem() != null) {
				nbt.put("ItemStack", this.getItem().toNbt(new NbtCompound()));
			}
		}

		public Packet<?> getPacket() {
			NbtCompound nbtCompound = new NbtCompound();
			this.toNbt(nbtCompound);
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
		public void render(ItemBlockEntity blockEntity, double x, double y, double z, float tickDelta, int destroyProgress) {
			if (blockEntity.item != null)
				renderItem(blockEntity.item, x, y, z, blockEntity.renderYaw, tickDelta);
		}

		public static void renderItem(ItemStack stack, double x, double y, double z, float rotation, float tickDelta) {
			GlStateManager.pushMatrix();
			GlStateManager.translatef((float)x + 0.5F, (float)y, (float)z + 0.5F);

			float f = 2f;
			GlStateManager.scalef(f, f, f);
			GlStateManager.rotatef(rotation, 0f, 1f, 0f);
			GlStateManager.rotatef(90f, 1f, 0f, 0f);

			MinecraftClient.getInstance().getTextureManager().bindTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEX);
			BakedModel bakedModel = MinecraftClient.getInstance().getItemRenderer().getModels().getModel(stack);
			MinecraftClient.getInstance().getItemRenderer().method_10243(stack, bakedModel);

			GlStateManager.popMatrix();
		}
	}
}
