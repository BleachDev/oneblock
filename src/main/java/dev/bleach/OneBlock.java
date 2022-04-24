package dev.bleach;

import java.util.function.Supplier;

import org.lwjgl.input.Keyboard;

import dev.bleach.FallingActionBlockEntity.FallingActionBlockEntityRenderer;
import dev.bleach.ItemBlock.ItemBlockEntity;
import dev.bleach.ItemBlock.ItemBlockEntityRenderer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

public class OneBlock implements ModInitializer {

	@Override
	public void onInitialize() {
	}
	
	public static void postInit() {
		System.out.println("Loading OneBlock..");
		SimpleRegistry.registerBlock(555, new Identifier("oneblock", "itemblock"), ItemBlock.BLOCK);
		SimpleRegistry.registerBlockEntity("ItemBlock", ItemBlockEntity.class, new ItemBlockEntityRenderer());
		SimpleRegistry.registerEntity(101, FallingActionBlockEntity.class,
				new FallingActionBlockEntityRenderer(MinecraftClient.getInstance().getEntityRenderManager()),
				e -> {
					ItemStack item = e.tileEntityData == null ? null : ItemStack.fromNbt(e.tileEntityData.getCompound("ItemStack"));
					return Block.getByBlockState(e.getBlockState()) | (item == null ? 0 : (Item.getRawId(item.getItem()) << 16));
				},
				(w, x, y, z, d) -> {
					FallingActionBlockEntity fe = new FallingActionBlockEntity(w, x, y, z, Block.getStateFromRawId(d & 0xffff));
					int bid = (d >> 16) & 0xffff;
					if (bid != 0) {
						NbtCompound nbt = new NbtCompound();
						nbt.put("ItemStack", new ItemStack(Item.byRawId(bid)).toNbt(new NbtCompound()));
						fe.tileEntityData = nbt;
					}
					return fe;
				});
		System.out.println("Fininshed Loading OneBlock!");
	}

	public static boolean cheatCodeEnabled() {
		return FabricLoader.getInstance().isDevelopmentEnvironment() && Keyboard.isKeyDown(Keyboard.KEY_F7);
	}

	public static Pair<BlockState, Supplier<BlockEntity>> stateFromStack(World world, ItemStack stack) {
		if (stack.getItem() instanceof BlockItem) {
			return new Pair<>(((BlockItem) stack.getItem()).getBlock().stateFromData(stack.getMeta()), null);
		} else {
			return new Pair<>(ItemBlock.BLOCK.getDefaultState(), () -> {
				BlockEntity be = ItemBlock.BLOCK.createBlockEntity(world, 0);
				NbtCompound nbt = new NbtCompound();
				nbt.put("ItemStack", stack.toNbt(new NbtCompound()));
				be.fromNbt(nbt);
				return be;
			});
		}
	}

	public static ItemStack stackFromBlock(BlockState state) {
		int i = 0;
		Item item = Item.fromBlock(state.getBlock());
		if (item != null && item.isUnbreakable()) {
			i = state.getBlock().getData(state);
		}

		return item == null ? null : new ItemStack(item, 1, i);
	}

	public static ItemStack stackFromBlock(BlockState state, NbtCompound blockEntityData) {
		if (state.getBlock() == ItemBlock.BLOCK && blockEntityData != null)
			return ItemStack.fromNbt(blockEntityData.getCompound("ItemStack"));

		return stackFromBlock(state);
	}
	
	public static ItemStack stackFromBlock(BlockState state, BlockEntity blockEntity) {
		NbtCompound nbt = new NbtCompound();
		if (blockEntity != null)
			blockEntity.toNbt(nbt);

		return stackFromBlock(state, nbt);
	}
}
