package carpet.mixin.spectatorsDontLoadChunks;

import carpet.CarpetSettings;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    public ServerPlayerEntityMixin(World worldIn, GameProfile gameProfileIn) {
        super(worldIn, gameProfileIn);
    }

    @Inject(method = "setGameMode", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;method_25049()V"))
    private void onChangeToSpectator(GameMode gameType, CallbackInfo ci) {
        if (CarpetSettings.spectatorsDontLoadChunks) {
            ((ServerWorld) world).getRaidManager().method_33588((ServerPlayerEntity) (Object) this);
        }
    }

    @Inject(method = "setGameMode", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;setCameraEntity(Lnet/minecraft/entity/Entity;)V"))
    private void onChangeFromSpectator(GameMode gameType, CallbackInfo ci) {
        if (CarpetSettings.spectatorsDontLoadChunks) {
            ((ServerWorld) world).getRaidManager().method_33582((ServerPlayerEntity) (Object) this);
        }
    }
}
