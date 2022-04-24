package dev.bleach.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.mojang.blaze3d.platform.GlStateManager;

import org.spongepowered.asm.mixin.injection.At.Shift;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.options.HandOption;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

@Mixin(HeldItemRenderer.class)
public class MixinHeldItemRenderer {

	// conc
	@Shadow private ItemStack field_13531;
	@Shadow private float field_13533;
	@Shadow private float lastEquipProgress;

	@Shadow private void method_12325(float f, float g, HandOption handOption) {}
	@Shadow private void renderItem(LivingEntity entity, ItemStack stack, ModelTransformation.Mode renderMode) {}

	// Custom arm rendering logic
	@Inject(method = "renderArmHoldingItem",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;method_9870(F)V", shift = Shift.AFTER),
			locals = LocalCapture.CAPTURE_FAILSOFT,
			cancellable = true)
	private void renderArmHoldingItem(float tickDelta, CallbackInfo callback, AbstractClientPlayerEntity player, float swingProgress, Hand hand, float pitch) {
		callback.cancel();
		MinecraftClient.getInstance().getTextureManager().bindTexture(MinecraftClient.getInstance().player.getSkinTexture());
		
		GlStateManager.pushMatrix();
		float equipProgress = 1.0F - (this.lastEquipProgress + (this.field_13533 - this.lastEquipProgress) * tickDelta);
		method_12325(equipProgress, swingProgress, HandOption.RIGHT);
		GlStateManager.popMatrix();
		
		GlStateManager.pushMatrix();
		GlStateManager.scalef(-1f, 1f, 1f);
		method_12325(equipProgress, 0f, HandOption.RIGHT);
		GlStateManager.popMatrix();
		
		GlStateManager.pushMatrix();
		GlStateManager.translatef(0, -0.45F, -0.9F);
		GlStateManager.translatef(0.0F, equipProgress * -0.6F, 0.0F);
		
		if (field_13531 != null && field_13531.getItem() instanceof BlockItem) {
			//GlStateManager.scalef(0.6f, 0.6f, 0.6f);
			GlStateManager.rotatef(45f, 0f, 1f, 0f);
		}

		MinecraftClient.getInstance().getTextureManager().bindTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEX);
		this.renderItem(player, this.field_13531, ModelTransformation.Mode.GUI);
		GlStateManager.popMatrix();

		GlStateManager.disableRescaleNormal();
		DiffuseLighting.disable();
	}
}
