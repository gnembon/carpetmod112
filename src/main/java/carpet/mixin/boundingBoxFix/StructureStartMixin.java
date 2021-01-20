package carpet.mixin.boundingBoxFix;

import carpet.CarpetSettings;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StructureStart.class)
public abstract class StructureStartMixin {
    @Shadow protected abstract void setBoundingBoxFromChildren();

    @Inject(method = "method_27890", at = @At(value = "CONSTANT", args = "stringValue=Children"))
    private void fixBoundingBox(int chunkX, int chunkZ, CallbackInfoReturnable<CompoundTag> cir) {
        //FIXME: why is this not @At("HEAD")?
        if(CarpetSettings.boundingBoxFix) {
            setBoundingBoxFromChildren();
        }
    }
}
