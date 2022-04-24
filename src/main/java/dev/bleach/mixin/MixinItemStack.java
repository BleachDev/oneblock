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
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@Mixin(ItemStack.class)
public class MixinItemStack {

	@Unique private int prevCount;

	@Shadow private int count;

	@Shadow public boolean isDamageable() { return false; }

	@Inject(method = "method_11390", at = @At("HEAD"))
	private void onStartUse_H(World world, PlayerEntity player, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> callback) {
		prevCount = count;
	}

	@Inject(method = "method_11390", at = @At("RETURN"), cancellable = true)
	private void onStartUse_R(World world, PlayerEntity player, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> callback) {
		if (prevCount == count) {
			if (!world.isClient) {
				Vec3d velocity = new Vec3d(0, 0, 1)
						.rotateX((float) -Math.toRadians(player.pitch))
						.rotateY((float) -Math.toRadians(player.yaw));

				ItemStack copy = ((ItemStack) (Object) this).copy();
				copy.setCount(1);

				Entity entity = FallingActionBlockEntity.create(
						world, copy, player.x, player.y + player.getEyeHeight() - 0.2, player.z, velocity.x, velocity.y, velocity.z);

				world.spawnEntity(entity);
			}

			count--;
		}
	}

	// Break when damaged
	@Overwrite
	public boolean method_5464(int i, Random rand, ServerPlayerEntity player) {
		return isDamageable();
	}
}
