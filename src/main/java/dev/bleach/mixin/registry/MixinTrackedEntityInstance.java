package dev.bleach.mixin.registry;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.bleach.SimpleRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TrackedEntityInstance;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;

@Mixin(TrackedEntityInstance.class)
public class MixinTrackedEntityInstance {
	
	@Shadow public Entity trackedEntity;

	@Inject(method = "method_2182", at = @At("HEAD"), cancellable = true)
	private void method_2182(CallbackInfoReturnable<Packet<?>> callback) {
		Integer i = SimpleRegistry.ENTITY_TO_ID.get(trackedEntity.getClass());
		if (!trackedEntity.removed && i != null)
			callback.setReturnValue(new EntitySpawnS2CPacket(
					this.trackedEntity, i, SimpleRegistry.ENTITY_TO_SERIALIZER.get(trackedEntity.getClass()).serialize(trackedEntity)));
	}
}
