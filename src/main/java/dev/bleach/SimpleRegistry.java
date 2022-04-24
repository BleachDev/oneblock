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
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.Identifier;

// doctors hate him because of this one simple trick
@SuppressWarnings("rawtypes")
public class SimpleRegistry {

	private static final Map<Class<? extends Entity>, Integer> ENTITY_TO_ID = new HashMap<>();
	private static final Map<Class<? extends Entity>, EntitySerializer> ENTITY_TO_SERIALIZER = new HashMap<>();
	private static final Map<Integer, EntityDeserializer> ID_TO_DESERIALIZER = new HashMap<>();
	
	// Registering

	public static void registerBlock(int id, Identifier identifier, Block block) {
		MinecraftClient.getInstance().getBlockRenderManager().getModels().addBlocks(block);
		Block.REGISTRY.add(id, identifier, block);
		Block.BLOCK_STATES.set(block.getDefaultState(), id);
	}

	public static <T extends BlockEntity> void registerBlockEntity(String name, Class<T> beClass, BlockEntityRenderer<T> renderer) {
		if (renderer != null)
			((AccessorBlockEntityRenderDispatcher) BlockEntityRenderDispatcher.INSTANCE).getRenderers().put(beClass, renderer);

		AccessorBlockEntity.callRegisterBlockEntity(beClass, name);
	}

	public static <T extends Entity> void registerEntity(int typeId, Class<T> entityClass, EntityRenderer<T> renderer, EntitySerializer<T> entitySerializer, EntityDeserializer<T> deserializer) {
		ENTITY_TO_ID.put(entityClass, typeId);
		ENTITY_TO_SERIALIZER.put(entityClass, entitySerializer);
		ID_TO_DESERIALIZER.put(typeId, deserializer);
		if (renderer != null)
			((AccessorEntityRenderDispatcher) MinecraftClient.getInstance().getEntityRenderManager()).getRenderers().put(entityClass, renderer);
	}
	
	// Serializing

	@SuppressWarnings("unchecked")
	public static EntitySpawnS2CPacket serialize(Entity entity) {
		Integer i = ENTITY_TO_ID.get(entity.getClass());
		EntitySerializer sr = ENTITY_TO_SERIALIZER.getOrDefault(entity.getClass(), e -> 0);
		return i == null ? null : new EntitySpawnS2CPacket(entity, i, sr.serialize(entity));
	}

	public static Entity deserialize(ClientWorld world, EntitySpawnS2CPacket packet) {
		EntityDeserializer dsr = ID_TO_DESERIALIZER.get(packet.getEntityData());
		return dsr == null ? null : dsr.deserialize(world, packet.getX() / 32d, packet.getY() / 32d, packet.getZ() / 32d, packet.getDataId());
	}
	
	// Functions

	@FunctionalInterface
	public static interface EntitySerializer<T extends Entity> {
		public int serialize(T entity);
	}

	@FunctionalInterface
	public static interface EntityDeserializer<T extends Entity> {
		public T deserialize(ClientWorld world, double x, double y, double z, int data);
	}
}
