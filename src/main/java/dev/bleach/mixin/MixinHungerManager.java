package dev.bleach.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(HungerManager.class)
public class MixinHungerManager {

	@Shadow private int foodLevel = 20;
	@Shadow private int prevFoodLevel = 20;

	// Hacky way to lock the food level but it works
	@Inject(method = "update", at = @At("RETURN"))
	private void update(PlayerEntity player, CallbackInfo callback) {
		foodLevel = 20;
		prevFoodLevel = 20;
	}
}
