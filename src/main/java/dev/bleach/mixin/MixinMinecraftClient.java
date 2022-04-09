package dev.bleach.mixin;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.bleach.OneBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {

	@Inject(method = "initializeGame", at = @At("RETURN"))
	private void initializeGame(CallbackInfo callback) {
		OneBlock.postInit();
	}

	// No slot selecting for you
	@Redirect(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerInventory;selectedSlot:I", opcode = Opcodes.PUTFIELD))
	public void method_8422(PlayerInventory inventory, int v) {
	}

	@Inject(method = "openScreen", at = @At("HEAD"), cancellable = true)
	public void openScreen(Screen screen, CallbackInfo callback) {
		if (screen instanceof HandledScreen && !OneBlock.cheatCodeEnabled())
			callback.cancel();
	}
}
