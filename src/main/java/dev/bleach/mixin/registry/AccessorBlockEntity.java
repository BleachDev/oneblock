package dev.bleach.mixin.registry;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.block.entity.BlockEntity;

@Mixin(BlockEntity.class)
public interface AccessorBlockEntity {
	
	@Invoker public static void callRegisterBlockEntity(Class<? extends BlockEntity> clazz, String stringId) {}
}
