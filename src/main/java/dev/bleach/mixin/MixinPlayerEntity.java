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
			while (r.rider != null) {
				if (r.rider == entity) {
					r = null;
					break;
				}

				r = r.rider;
			}

			entity.startRiding(r);
		}
	}

	// Make riding entities a bit higher so they don't block your entire view
	@Override
	public void updatePassengerPosition() {
		if (this.rider != null) {
			this.rider.updatePosition(this.x, this.y + this.height, this.z);
		}
	}

	@Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;tick()V", shift = Shift.BEFORE))
	public void tick(CallbackInfo callback) {
		if (isAnyRider(e -> e instanceof ChickenEntity) && velocityY < 0) {
			velocityY *= 0.6;
		}
	}

	private boolean isAnyRider(Predicate<Entity> func) {
		for (Entity r = rider; r != null; r = r.rider) {
			if (func.test(r)) {
				return true;
			}
		}

		return false;
	}
}
