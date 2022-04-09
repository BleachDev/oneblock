package dev.bleach.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.mojang.blaze3d.platform.GlStateManager;

import org.spongepowered.asm.mixin.injection.At.Shift;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;

@Mixin(HeldItemRenderer.class)
public class MixinHeldItemRenderer {

	// conc
	@Shadow private ItemStack mainHand;

	@Shadow private void renderArm(AbstractClientPlayerEntity player, float f, float g) {}
	@Shadow private void renderItem(LivingEntity entity, ItemStack stack, ModelTransformation.Mode renderMode) {}

	// Custom arm rendering logic
	@Inject(method = "renderArmHoldingItem",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;applyPlayerRotation(Lnet/minecraft/entity/player/ClientPlayerEntity;F)V", shift = Shift.AFTER),
			locals = LocalCapture.CAPTURE_FAILSOFT,
			cancellable = true)
	private void renderArmHoldingItem(float tickDelta, CallbackInfo callback, float equipProgress, AbstractClientPlayerEntity player, float swingProgress, float pitch) {
		callback.cancel();
		
		GlStateManager.pushMatrix();
		renderArm(player, equipProgress, swingProgress);
		GlStateManager.popMatrix();
		
		GlStateManager.pushMatrix();
		GlStateManager.scalef(-1f, 1f, 1f);
		renderArm(player, equipProgress, 0f);
		GlStateManager.popMatrix();
		
		GlStateManager.pushMatrix();
		GlStateManager.translatef(0, -0.45F, -0.9F);
		GlStateManager.translatef(0.0F, equipProgress * -0.6F, 0.0F);
		
		if (mainHand != null && mainHand.getItem() instanceof BlockItem) {
			GlStateManager.scalef(0.6f, 0.6f, 0.6f);
			GlStateManager.rotatef(45f, 0f, 1f, 0f);
		}

		this.renderItem(player, this.mainHand, ModelTransformation.Mode.GUI);
		GlStateManager.popMatrix();

		GlStateManager.disableRescaleNormal();
		DiffuseLighting.disable();
	}
}
