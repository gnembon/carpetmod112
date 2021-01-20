package carpet.mixin.leashFix;

import carpet.CarpetSettings;
import carpet.utils.extensions.EntityWithPostLoad;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin implements EntityWithPostLoad {
    @Shadow protected abstract void deserializeLeashTag();
    @Shadow private Entity holdingEntity;
    @Shadow private CompoundTag leashTag;

    @Override
    public void postLoad() {
        if (CarpetSettings.leashFix == CarpetSettings.LeashFix.cool) deserializeLeashTag();
    }

    @Inject(method = "writeCustomDataToTag", at = @At(value = "CONSTANT", args = "stringValue=LeftHanded"))
    private void saveNBT(CompoundTag compound, CallbackInfo ci) {
        if (holdingEntity == null && CarpetSettings.leashFix == CarpetSettings.LeashFix.casual && leashTag != null) {
            compound.put("Leash", leashTag);
        }
    }
}
