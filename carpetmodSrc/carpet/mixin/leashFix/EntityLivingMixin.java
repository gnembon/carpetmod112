package carpet.mixin.leashFix;

import carpet.CarpetSettings;
import carpet.utils.extensions.EntityWithPostLoad;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityLiving.class)
public abstract class EntityLivingMixin implements EntityWithPostLoad {
    @Shadow protected abstract void recreateLeash();
    @Shadow private Entity leashHolder;
    @Shadow private NBTTagCompound leashNBTTag;

    @Override
    public void postLoad() {
        if (CarpetSettings.leashFix == CarpetSettings.LeashFix.cool) recreateLeash();
    }

    @Inject(method = "writeEntityToNBT", at = @At(value = "CONSTANT", args = "stringValue=LeftHanded"))
    private void saveNBT(NBTTagCompound compound, CallbackInfo ci) {
        if (leashHolder == null && CarpetSettings.leashFix == CarpetSettings.LeashFix.casual && leashNBTTag != null) {
            compound.setTag("Leash", leashNBTTag);
        }
    }
}
