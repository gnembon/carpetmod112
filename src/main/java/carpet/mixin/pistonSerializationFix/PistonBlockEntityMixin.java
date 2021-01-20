package carpet.mixin.pistonSerializationFix;

import carpet.CarpetSettings;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PistonBlockEntity.class)
public class PistonBlockEntityMixin {
    @Shadow private float lastProgress;
    @Shadow private float progress;

    @Inject(method = "fromTag", at = @At("RETURN"))
    private void onDeserialize(CompoundTag compound, CallbackInfo ci) {
        if (CarpetSettings.pistonSerializationFix && compound.contains("lastProgress", 5)) {
            this.lastProgress = compound.getFloat("lastProgress");
        }
    }

    @Redirect(method = "toTag", at = @At(value = "FIELD", target = "Lnet/minecraft/block/entity/PistonBlockEntity;lastProgress:F"))
    private float serializeProgress(PistonBlockEntity te, CompoundTag compound) {
        if (!CarpetSettings.pistonSerializationFix) return lastProgress;
        compound.putFloat("lastProgress", lastProgress);
        return progress;
    }
}
