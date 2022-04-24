package dev.bleach.mixin;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.bleach.FallingActionBlockEntity;
import dev.bleach.OneBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(ServerPlayerInteractionManager.class)
public class MixinServerPlayerInteractionManager {

	@Shadow private World world;
	@Shadow private ServerPlayerEntity player;

	@Shadow private boolean tryBreakBlock(BlockPos pos) { return false; }
	@Shadow private boolean isCreative() { return false; }

	@Inject(method = "method_10766",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;syncWorldEvent(Lnet/minecraft/entity/player/PlayerEntity;ILnet/minecraft/util/math/BlockPos;I)V", shift = Shift.AFTER),
			cancellable = true)
	private void method_10766(BlockPos pos, CallbackInfoReturnable<Boolean> callback) {
		BlockState state = this.world.getBlockState(pos);

		ItemStack item = OneBlock.stackFromBlock(state, world.getBlockEntity(pos));

		boolean bl = this.tryBreakBlock(pos);
		this.player.networkHandler.sendPacket(new BlockUpdateS2CPacket(this.world, pos));
		if (item != null) {
			if (player.getMainHandStack() != null)
				player.getMainHandStack().onBlockBroken(this.world, state.getBlock(), pos, this.player);

			if (player.getMainHandStack() != null && player.getMainHandStack().count != 0) {
				Random random = new Random();
				Entity e = FallingActionBlockEntity.create(
						world, player.getMainHandStack(), player.x, player.y + player.getEyeHeight(), player.z,
						(random.nextDouble() - 0.5) * 0.5, 0.1, (random.nextDouble() - 0.5) * 0.5);

				world.spawnEntity(e);
			}

			this.player.inventory.setInvStack(player.inventory.selectedSlot, item);
		}

		callback.setReturnValue(bl);
	}
}
