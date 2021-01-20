package carpet.mixin.spectatorsDontLoadChunks;

import carpet.CarpetSettings;
import carpet.utils.ChunkLoading;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.class_4615;
import net.minecraft.class_6380;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.List;

@Mixin(class_6380.class)
public abstract class PlayerChunkMapMixin {
    @Shadow private static long method_33594(int chunkX, int chunkZ) { throw new AbstractMethodError(); }
    @Shadow @Final private Long2ObjectMap<class_4615> field_31805;
    @Shadow @Final private List<class_4615> field_31808;
    @Shadow @Final private List<class_4615> field_31807;
    @Shadow @Final private List<class_4615> field_31809;

    @Redirect(method = {
        "method_33582",
        "method_33592"
    }, at = @At(value = "INVOKE", target = "Lnet/minecraft/class_6380;method_33591(II)Lnet/minecraft/class_4615;"))
    private class_4615 getOrCreateHooks(class_6380 map, int chunkX, int chunkZ, ServerPlayerEntity player) {
        return getOrCreateEntry(chunkX, chunkZ, player);
    }

    @Inject(method = "method_33578", at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ServerPlayerEntity;field_33071:D"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void capturePlayer(int radius, CallbackInfo ci, int i, List<ServerPlayerEntity> list, Iterator<ServerPlayerEntity> iterator, ServerPlayerEntity player) {
        ChunkLoading.INITIAL_PLAYER_FOR_CHUNK_MAP_ENTRY.set(player);
    }

    @Redirect(method = "method_33578", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_6380;method_33591(II)Lnet/minecraft/class_4615;"))
    private class_4615 getOrCreateOnSetRadius(class_6380 map, int chunkX, int chunkZ) {
        return getOrCreateEntry(chunkX, chunkZ, null);
    }

    @Unique private class_4615 getOrCreateEntry(int chunkX, int chunkZ, ServerPlayerEntity player) {
        long i = method_33594(chunkX, chunkZ);
        class_4615 entry = this.field_31805.get(i);

        if (entry == null) {
            if (player != null) ChunkLoading.INITIAL_PLAYER_FOR_CHUNK_MAP_ENTRY.set(player);
            entry = new class_4615((class_6380) (Object) this, chunkX, chunkZ);
            ChunkLoading.INITIAL_PLAYER_FOR_CHUNK_MAP_ENTRY.set(null);
            if (!CarpetSettings.spectatorsDontLoadChunks || !player.isSpectator())
                this.field_31805.put(i, entry);
            this.field_31809.add(entry);

            if (entry.method_33575() == null) {
                if (!CarpetSettings.spectatorsDontLoadChunks || !player.isSpectator())
                    this.field_31808.add(entry);
            }

            if (!entry.method_33568()) {
                this.field_31807.add(entry);
            }
        }
        return entry;
    }
}
