package dev.bleach.mixin;

import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity extends LivingEntity {

	public MixinPlayerEntity(World world) {
		super(world);
	}

	// Entity stacking :sunglasses:
	@Overwrite
	public void method_3216(Entity entity) {
		if (entity.isAttackable()) {
			Entity r = this;
			while (!r.getPassengerList().isEmpty()) {
				r = r.getPassengerList().get(0);
				
				if (r == entity) {
					r.stopRiding();
					return;
				}
			}

			entity.startRiding(r, true);
		}
	}

	// Make riding entities a bit higher so they don't block your entire view
	@Override
	public double getMountedHeightOffset() {
		return height;
	}

	@Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;tick()V", shift = Shift.BEFORE))
	public void tick(CallbackInfo callback) {
		if (isAnyRider(e -> e instanceof ChickenEntity) && velocityY < 0) {
			velocityY *= 0.6;
		}
	}

	private boolean isAnyRider(Predicate<Entity> func) {
		Entity r = this;
		while (!r.getPassengerList().isEmpty()) {
			r = r.getPassengerList().get(0);
			if (func.test(r)) {
				return true;
			}
		}

		return false;
	}
}
