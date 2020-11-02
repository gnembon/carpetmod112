package carpet.mixin.spectatorsDontLoadChunks;

import carpet.CarpetSettings;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerMP.class)
public abstract class EntityPlayerMPMixin extends EntityPlayer {
    public EntityPlayerMPMixin(World worldIn, GameProfile gameProfileIn) {
        super(worldIn, gameProfileIn);
    }

    @Inject(method = "setGameType", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayerMP;spawnShoulderEntities()V"))
    private void onChangeToSpectator(GameType gameType, CallbackInfo ci) {
        if (CarpetSettings.spectatorsDontLoadChunks) {
            ((WorldServer) world).getPlayerChunkMap().removePlayer((EntityPlayerMP) (Object) this);
        }
    }

    @Inject(method = "setGameType", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayerMP;setSpectatingEntity(Lnet/minecraft/entity/Entity;)V"))
    private void onChangeFromSpectator(GameType gameType, CallbackInfo ci) {
        if (CarpetSettings.spectatorsDontLoadChunks) {
            ((WorldServer) world).getPlayerChunkMap().addPlayer((EntityPlayerMP) (Object) this);
        }
    }
}
