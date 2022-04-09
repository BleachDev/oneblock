package dev.bleach;
import java.util.HashMap;
import java.util.Map;

import dev.bleach.mixin.registry.AccessorBlockEntity;
import dev.bleach.mixin.registry.AccessorBlockEntityRenderDispatcher;
import dev.bleach.mixin.registry.AccessorEntityRenderDispatcher;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

// doctors hate him because of this one simple trick
public class SimpleRegistry {

	public static final Map<Class<? extends Entity>, Integer> ENTITY_TO_ID = new HashMap<>();
	public static final Map<Class<? extends Entity>, EntitySerializer> ENTITY_TO_SERIALIZER = new HashMap<>();
	public static final Map<Integer, EntityDeserializer> ID_TO_DESERIALIZER = new HashMap<>();

	public static void registerBlock(int id, Identifier identifier, Block block) {
		MinecraftClient.getInstance().getBlockRenderManager().getModels().addBlocks(block);
		Block.REGISTRY.add(id, identifier, block);
		Block.BLOCK_STATES.set(block.getDefaultState(), id);
	}

	public static void registerBlockEntity(String name, Class<? extends BlockEntity> beClass, BlockEntityRenderer<?> renderer) {
		if (renderer != null)
			((AccessorBlockEntityRenderDispatcher) BlockEntityRenderDispatcher.INSTANCE).getRenderers().put(beClass, renderer);
		AccessorBlockEntity.callRegisterBlockEntity(beClass, name);
	}

	public static void registerEntity(int typeId, Class<? extends Entity> entityClass, EntityRenderer<?> renderer, EntitySerializer entitySerializer, EntityDeserializer deserializer) {
		ENTITY_TO_ID.put(entityClass, typeId);
		ENTITY_TO_SERIALIZER.put(entityClass, entitySerializer);
		ID_TO_DESERIALIZER.put(typeId, deserializer);
		if (renderer != null)
			((AccessorEntityRenderDispatcher) MinecraftClient.getInstance().getEntityRenderManager()).getRenderers().put(entityClass, renderer);
	}

	@FunctionalInterface
	public static interface EntitySerializer {
		public int serialize(Entity entity);
	}

	@FunctionalInterface
	public static interface EntityDeserializer {
		public Entity deserialize(ClientWorld world, double x, double y, double z, int data);
	}
}
