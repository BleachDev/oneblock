package dev.bleach.mixin;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.Window;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

@Mixin(InGameHud.class)
public class MixinInGameHud extends DrawableHelper {
	
	@Shadow private MinecraftClient client;
	@Shadow private Random random;
	@Shadow private int ticks;
	@Shadow private long heartJumpEndTick;
	@Shadow private int lastHealthValue;

	// No hotbars here
	@Overwrite
	public void renderHotbar(Window window, float tickDelta) {
	}
	
	@Overwrite
	public void renderExperienceBar(Window window, int i) {
	}

	@Overwrite
	private void renderStatusBars(Window window) {
		if (this.client.getCameraEntity() instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity)this.client.getCameraEntity();
			EntityAttributeInstance attributes = player.initializeAttribute(EntityAttributes.GENERIC_MAX_HEALTH);
			
			int health = MathHelper.ceil(player.getHealth());
			float maxHealth = (float)attributes.getValue();
			float absorption = player.getAbsorption();
			boolean bl = this.heartJumpEndTick > (long)this.ticks && (this.heartJumpEndTick - (long)this.ticks) / 3L % 2L == 1L;
			
			int width = window.getWidth() / 2 - 40;
			int height = window.getHeight() - 12;
			int p = MathHelper.ceil((maxHealth + absorption) / 2.0F / 10.0F);
			int q = Math.max(10 - (p - 2), 3);
			
			for (int u = MathHelper.ceil((maxHealth + absorption) / 2.0F) - 1; u >= 0; --u) {
				int v = 16;
				if (player.hasStatusEffect(StatusEffects.POISON)) {
					v += 36;
				} else if (player.hasStatusEffect(StatusEffects.WITHER)) {
					v += 72;
				}

				int w = 0;
				if (bl) {
					w = 1;
				}

				int x = MathHelper.ceil((float)(u + 1) / 10.0F) - 1;
				int y = width + u % 10 * 8;
				int z = height - x * q;
				if (health <= 4) {
					z += this.random.nextInt(2);
				}

				if (u == -1) {
					z -= 2;
				}

				int aa = 0;
				if (player.world.getLevelProperties().isHardcore()) {
					aa = 5;
				}

				this.drawTexture(y, z, 16 + w * 9, 9 * aa, 9, 9);
				if (bl) {
					if (u * 2 + 1 < lastHealthValue) {
						this.drawTexture(y, z, v + 54, 9 * aa, 9, 9);
					}

					if (u * 2 + 1 == lastHealthValue) {
						this.drawTexture(y, z, v + 63, 9 * aa, 9, 9);
					}
				}

				float h = absorption;
				if (h > 0.0F) {
					if (h == absorption && absorption % 2.0F == 1.0F) {
						this.drawTexture(y, z, v + 153, 9 * aa, 9, 9);
					} else {
						this.drawTexture(y, z, v + 144, 9 * aa, 9, 9);
					}

					h -= 2.0F;
				} else {
					if (u * 2 + 1 < health) {
						this.drawTexture(y, z, v + 36, 9 * aa, 9, 9);
					}

					if (u * 2 + 1 == health) {
						this.drawTexture(y, z, v + 45, 9 * aa, 9, 9);
					}
				}
			}
		}
	}
}
