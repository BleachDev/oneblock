package dev.bleach.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
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
}
