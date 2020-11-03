package carpet.mixin.chunkLogger;

import carpet.carpetclient.CarpetClientChunkLogger;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityLivingBase.class)
public abstract class EntityLivingBaseMixin extends Entity {
    public EntityLivingBaseMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "travel", at = @At("HEAD"))
    private void onTravelStart(float strafe, float vertical, float forward, CallbackInfo ci) {
        CarpetClientChunkLogger.setReason(() -> "Entity walking around: " + getName());
    }

    @Inject(method = "travel", at = @At("RETURN"))
    private void onTravelEnd(float strafe, float vertical, float forward, CallbackInfo ci) {
        CarpetClientChunkLogger.resetReason();
    }
}
