package dev.bleach.mixin.registry;

import java.util.Map;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;

@Mixin(BlockEntityRenderDispatcher.class)
public interface AccessorBlockEntityRenderDispatcher {
	
	@Accessor public Map<Class<? extends BlockEntity>, BlockEntityRenderer<? extends BlockEntity>> getRenderers();
}
