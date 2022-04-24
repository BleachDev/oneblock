package dev.bleach.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.block.Block;

@Mixin(Block.class)
public class MixinBlock {

	@ModifyVariable(method = "setStrength", argsOnly = true, at = @At("HEAD"))
	public float setStrength(float strength) {
		return strength >= 0f ? strength : 10f;
	}

	@ModifyVariable(method = "setResistance", argsOnly = true, at = @At("HEAD"))
	public float setResistance(float resistance) {
		return resistance < 10f ? resistance : 10f;
	}
}
