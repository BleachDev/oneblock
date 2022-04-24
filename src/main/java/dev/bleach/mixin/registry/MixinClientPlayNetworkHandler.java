package dev.bleach.mixin.registry;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.At.Shift;

import dev.bleach.SimpleRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {

	@Shadow private MinecraftClient client;
	@Shadow private ClientWorld clientWorld;

	// Needed to make the client accept non-standard block entities
	@Overwrite
	public void onBlockEntityUpdate(BlockEntityUpdateS2CPacket packet) {
		NetworkThreadUtils.forceMainThread(packet, (ClientPlayNetworkHandler) (Object) this, this.client);
		if (this.client.world.blockExists(packet.getPos())) {
			this.client.world.getBlockEntity(packet.getPos()).fromNbt(packet.getNbt());
		}
	}

	@ModifyVariable(method = "onEntitySpawn", at = @At(value = "CONSTANT", args = "nullValue=true", shift = Shift.BY, ordinal = 0, by = 2))
	public Entity onEntitySpawn(Entity entity, EntitySpawnS2CPacket packet) {
		return SimpleRegistry.deserialize(clientWorld, packet);
	}
}
