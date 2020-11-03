package carpet.mixin.boundingBoxFix;

import carpet.CarpetSettings;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.gen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StructureStart.class)
public abstract class StructureStartMixin {
    @Shadow protected abstract void updateBoundingBox();

    @Inject(method = "writeStructureComponentsToNBT", at = @At(value = "CONSTANT", args = "stringValue=Children"))
    private void fixBoundingBox(int chunkX, int chunkZ, CallbackInfoReturnable<NBTTagCompound> cir) {
        //FIXME: why is this not @At("HEAD")?
        if(CarpetSettings.boundingBoxFix) {
            updateBoundingBox();
        }
    }
}
