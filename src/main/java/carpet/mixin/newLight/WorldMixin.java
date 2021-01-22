package carpet.mixin.newLight;

import carpet.CarpetSettings;
import carpet.helpers.LightingEngine;
import carpet.utils.extensions.NewLightWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.WorldSaveHandler;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public class WorldMixin implements NewLightWorld {
    @Shadow @Final public Profiler profiler;
    protected LightingEngine lightingEngine;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onCtor(WorldSaveHandler levelProperties, LevelProperties levelProperties2, Dimension dimension, Profiler profiler, boolean isClient, CallbackInfo ci) {
        this.lightingEngine = new LightingEngine((World) (Object) this);
    }

    @Inject(method = "method_26095", at = @At("HEAD"), cancellable = true)
    private void checkLightForNewLight(LightType lightType, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!CarpetSettings.newLight) return;
        this.lightingEngine.scheduleLightUpdate(lightType, pos);
        cir.setReturnValue(true);
    }

    @Override
    public LightingEngine getLightingEngine() {
        return lightingEngine;
    }
}
