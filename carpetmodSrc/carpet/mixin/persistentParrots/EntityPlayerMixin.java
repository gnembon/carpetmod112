package carpet.mixin.persistentParrots;

import carpet.CarpetSettings;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayer.class)
public abstract class EntityPlayerMixin extends EntityLivingBase {
    @Shadow public PlayerCapabilities capabilities;
    @Shadow protected abstract void spawnShoulderEntities();
    @Shadow protected abstract void spawnShoulderEntity(NBTTagCompound tag);
    @Shadow protected abstract void setLeftShoulderEntity(NBTTagCompound tag);
    @Shadow public abstract NBTTagCompound getLeftShoulderEntity();
    @Shadow public abstract NBTTagCompound getRightShoulderEntity();
    @Shadow protected abstract void setRightShoulderEntity(NBTTagCompound tag);

    public EntityPlayerMixin(World worldIn) {
        super(worldIn);
    }

    @Redirect(method = "onLivingUpdate", at = @At(value = "FIELD", target = "Lnet/minecraft/world/World;isRemote:Z", ordinal = 1))
    private boolean isRemoteCheck(World world) {
        return false;
    }

    @Inject(method = "onLivingUpdate", at = @At("TAIL"))
    private void onLivingUpdateEnd(CallbackInfo ci) {
        boolean parrots_will_drop = !CarpetSettings.persistentParrots || this.capabilities.disableDamage;
        if (!this.world.isRemote && ((parrots_will_drop && this.fallDistance > 0.5F) || this.isInWater() || (parrots_will_drop && this.isRiding())) || this.capabilities.isFlying) {
            this.spawnShoulderEntities();
        }
    }

    @Redirect(method = "attackEntityFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;spawnShoulderEntities()V"))
    private void dropParrotsOnAttack(EntityPlayer entityPlayer, DamageSource source, float amount) {
        if (CarpetSettings.persistentParrots && !this.isSneaking()) {
            if (this.rand.nextFloat() < amount / 15.0) {
                this.spawnShoulderEntity(this.getLeftShoulderEntity());
                this.setLeftShoulderEntity(new NBTTagCompound());
            }
            if (this.rand.nextFloat() < amount / 15.0) {
                this.spawnShoulderEntity(this.getRightShoulderEntity());
                this.setRightShoulderEntity(new NBTTagCompound());
            }
        }
    }
}
