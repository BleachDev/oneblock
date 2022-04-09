package dev.bleach.mixin.registry;

import java.util.Map;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;

@Mixin(EntityRenderDispatcher.class)
public interface AccessorEntityRenderDispatcher {
	
	@Accessor public Map<Class<? extends Entity>, EntityRenderer<? extends Entity>> getRenderers();
}
