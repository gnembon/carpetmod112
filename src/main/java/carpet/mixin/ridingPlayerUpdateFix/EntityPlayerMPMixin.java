package carpet.mixin.ridingPlayerUpdateFix;

import carpet.CarpetSettings;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.passive.EntityLlama;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerMP.class)
public abstract class EntityPlayerMPMixin extends EntityPlayer {
    @Shadow @Final public MinecraftServer server;

    public EntityPlayerMPMixin(World worldIn, GameProfile gameProfileIn) {
        super(worldIn, gameProfileIn);
    }

    @Inject(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/critereon/TickTrigger;trigger(Lnet/minecraft/entity/player/EntityPlayerMP;)V"))
    private void ridingPlayerUpdateFix(CallbackInfo ci) {
        if (CarpetSettings.ridingPlayerUpdateFix) {
            Entity riding = getLowestRidingEntity();
            if (riding instanceof EntityMinecart || riding instanceof EntityLlama){
                this.server.getPlayerList().serverUpdateMovingPlayer((EntityPlayerMP) (Object) this);
            }
        }
    }
}
