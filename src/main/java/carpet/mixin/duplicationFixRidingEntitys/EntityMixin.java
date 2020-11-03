package carpet.mixin.duplicationFixRidingEntitys;

import carpet.CarpetSettings;
import carpet.patches.EntityPlayerMPFake;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Entity.class)
public class EntityMixin {
    @Shadow @Final private List<Entity> riddenByEntities;

    @Inject(method = "writeToNBTOptional", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NBTTagCompound;setString(Ljava/lang/String;Ljava/lang/String;)V"), cancellable = true)
    private void duplicationFixRidingEntitys(NBTTagCompound compound, CallbackInfoReturnable<Boolean> cir) {
        // Fix for fixing duplication caused by riding entitys into unloaded chunks CARPET-XCOM
        if(CarpetSettings.duplicationFixRidingEntitys && hasPlayerPassenger()) {
            cir.setReturnValue(false);
        }
    }

    // Method for fixing duplication caused by riding entitys into unloaded chunks CARPET-XCOM
    private boolean hasPlayerPassenger() {
        for (Entity passenger : riddenByEntities) {
            if (passenger instanceof EntityPlayer && !(passenger instanceof EntityPlayerMPFake)) {
                return true;
            }
            if (((EntityMixin) (Object) passenger).hasPlayerPassenger()) {
                return true;
            }
        }
        return false;
    }
}
