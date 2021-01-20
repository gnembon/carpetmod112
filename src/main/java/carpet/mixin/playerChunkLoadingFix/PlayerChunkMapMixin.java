package carpet.mixin.playerChunkLoadingFix;

import carpet.CarpetSettings;
import net.minecraft.class_6380;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// Fix the player chunk map truncation in negative coords causing offsets in chunk loading CARPET-XCOM
@Mixin(class_6380.class)
public class PlayerChunkMapMixin {
    @Redirect(method = {
        "method_33582",
        "method_33592"
    }, at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ServerPlayerEntity;field_33071:D", ordinal = 0))
    private double getPosX(ServerPlayerEntity player) {
        return CarpetSettings.playerChunkLoadingFix ? Math.floor(player.field_33071) : player.field_33071;
    }

    @Redirect(method = {
        "method_33582",
        "method_33592"
    }, at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ServerPlayerEntity;field_33073:D", ordinal = 0))
    private double getPosZ(ServerPlayerEntity player) {
        return CarpetSettings.playerChunkLoadingFix ? Math.floor(player.field_33073) : player.field_33073;
    }

    @Redirect(method = "method_33588", at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ServerPlayerEntity;field_31749:D", ordinal = 0))
    private double getManagedPosX(ServerPlayerEntity player) {
        return CarpetSettings.playerChunkLoadingFix ? Math.floor(player.field_31749) : player.field_31749;
    }

    @Redirect(method = "method_33588", at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ServerPlayerEntity;field_31750:D", ordinal = 0))
    private double getManagedPosZ(ServerPlayerEntity player) {
        return CarpetSettings.playerChunkLoadingFix ? Math.floor(player.field_31750) : player.field_31750;
    }

    @Redirect(method = "method_33592", at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ServerPlayerEntity;field_31749:D", ordinal = 1))
    private double getManagedPosX2(ServerPlayerEntity player) {
        return CarpetSettings.playerChunkLoadingFix ? Math.floor(player.field_31749) : player.field_31749;
    }

    @Redirect(method = "method_33592", at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ServerPlayerEntity;field_31750:D", ordinal = 1))
    private double getManagedPosZ2(ServerPlayerEntity player) {
        return CarpetSettings.playerChunkLoadingFix ? Math.floor(player.field_31750) : player.field_31750;
    }
}
