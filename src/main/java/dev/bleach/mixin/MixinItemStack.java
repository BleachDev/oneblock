package dev.bleach.mixin;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.bleach.FallingActionBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@Mixin(ItemStack.class)
public class MixinItemStack {

	@Unique private int prevCount;

	@Shadow private int count;

	@Shadow public boolean isDamageable() { return false; }

	@Inject(method = "onStartUse", at = @At("HEAD"))
	private void onStartUse_H(World world, PlayerEntity player, CallbackInfoReturnable<ItemStack> callback) {
		prevCount = count;
	}

	@Inject(method = "onStartUse", at = @At("RETURN"), cancellable = true)
	private void onStartUse_R(World world, PlayerEntity player, CallbackInfoReturnable<ItemStack> callback) {
		if (prevCount == count) {
			if (!world.isClient) {
				Vec3d velocity = new Vec3d(0, 0, 1)
						.rotateX((float) -Math.toRadians(player.pitch))
						.rotateY((float) -Math.toRadians(player.yaw));

				ItemStack copy = ((ItemStack) (Object) this).copy();
				copy.count = 1;

				Entity entity = FallingActionBlockEntity.create(
						world, copy, player.x, player.y + player.getEyeHeight() - 0.2, player.z, velocity.x, velocity.y, velocity.z);

				world.spawnEntity(entity);
			}

			count--;
			callback.setReturnValue((ItemStack) (Object) this);
		}
	}
	
	// Break when damaged
	@Overwrite
	public boolean damage(int amount, Random random) {
		return isDamageable();
	}
}
