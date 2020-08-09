package carpet.mixin.pistonSerializationFix;

import carpet.CarpetSettings;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityPiston;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntityPiston.class)
public class TileEntityPistonMixin {
    @Shadow public float lastProgress;

    @Shadow private float progress;

    @Inject(method = "readFromNBT", at = @At("RETURN"))
    private void onDeserialize(NBTTagCompound compound, CallbackInfo ci) {
        if (CarpetSettings.pistonSerializationFix && compound.hasKey("lastProgress", 5)) {
            this.lastProgress = compound.getFloat("lastProgress");
        }
    }

    @Redirect(method = "writeToNBT", at = @At(value = "FIELD", target = "Lnet/minecraft/tileentity/TileEntityPiston;lastProgress:F"))
    private float serializeProgress(TileEntityPiston te, NBTTagCompound compound) {
        if (!CarpetSettings.pistonSerializationFix) return lastProgress;
        compound.setFloat("lastProgress", lastProgress);
        return progress;
    }
}
