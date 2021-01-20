package carpet.mixin.blockEventSerializer;

import net.minecraft.class_1268;
import net.minecraft.server.world.SecondaryServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SecondaryServerWorld.class)
public abstract class SecondaryServerWorldMixin extends ServerWorldMixin {
    protected SecondaryServerWorldMixin(class_1268 levelProperties, LevelProperties levelProperties2, Dimension dimension, Profiler profiler, boolean isClient) {
        super(levelProperties, levelProperties2, dimension, profiler, isClient);
    }

    @Inject(method = "method_26064", at = @At("RETURN"))
    private void onInit(CallbackInfoReturnable<World> cir) {
        initBlockEventSerializer();
    }
}
