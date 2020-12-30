package carpet.mixin.core;

import carpet.CarpetSettings;
import carpet.utils.SpawnReporter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.Set;

@Mixin(WorldEntitySpawner.class)
public class WorldEntitySpawnerMixin {
    @Shadow @Final private static int MOB_COUNT_DIV;
    private WorldServer world;
    private int localSpawns;
    private int did;
    private String suffix;
    private EnumCreatureType currentCategory;
    private int chunksCount;
    private int mobcapTotal;

    @Inject(method = "findChunksForSpawning", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;getSpawnPoint()Lnet/minecraft/util/math/BlockPos;"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void optimizedEarlyExit(WorldServer worldServer, boolean spawnHostileMobs, boolean spawnPeacefulMobs, boolean spawnOnSetTickRate, CallbackInfoReturnable<Integer> cir, int count) {
        if (count == 0 && CarpetSettings.optimizedDespawnRange) {
            cir.setReturnValue(0);
            return;
        }
        if (world == null) {
            world = worldServer;
            chunksCount = count;
            did = world.provider.getDimensionType().getId();
            suffix = (did == 0 ? "" : (did < 0 ? " (N)" : " (E)"));
        }
    }

    @Inject(method = "findChunksForSpawning", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EnumCreatureType;getPeacefulCreature()Z", ordinal = 0, shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void spawnTrackingAll(WorldServer worldServer, boolean spawnHostileMobs, boolean spawnPeacefulMobs, boolean spawnOnSetTickRate, CallbackInfoReturnable<Integer> cir, int chunks, int j4, BlockPos spawnPos, EnumCreatureType[] types, int var9, int var10, EnumCreatureType category) {
        currentCategory = category;
        if (SpawnReporter.track_spawns <= 0) return;
        String group_code = category + suffix;
        SpawnReporter.overall_spawn_ticks.put(group_code, SpawnReporter.overall_spawn_ticks.get(group_code) + SpawnReporter.spawn_tries.getOrDefault(category, 1));
    }

    @Redirect(method = "findChunksForSpawning", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EnumCreatureType;getMaxNumberOfCreature()I"))
    private int getMaxNumberOfCreature(EnumCreatureType category) {
        int max = (int) Math.pow(2.0, (SpawnReporter.mobcap_exponent / 4)) * category.getMaxNumberOfCreature();
        mobcapTotal = max * chunksCount / MOB_COUNT_DIV;
        return max;
    }

    // This one is tricky to target, we want the number of existing mobs right where it's compared to the mobcap.
    // The comparison looks like ILOAD 12, ILOAD 13, IF_ICMPGT -> index = 12
    // If @At would actually respect the slice here we could use that with ordinal=0
    // but apparently 12 is also used in the loop that counts chunks, so ordinal needs to be 4.
    // The resulting code looks like:
    //   int l4 = this.redirect$zej000$getMaxNumberOfCreature(enumcreaturetype) * i / MOB_COUNT_DIV;
    //   k4 = this.localvar$zej000$modifyExistingCount(k4);
    //   if (k4 <= l4) {
    @ModifyVariable(method = "findChunksForSpawning", at = @At(value = "LOAD", ordinal = 4), index = 12)
    private int modifyExistingCount(int existingCount) {
        String group_code = currentCategory + suffix;
        SpawnReporter.mobcaps.get(did).put(currentCategory, new Tuple<>(existingCount, mobcapTotal));
        if (SpawnReporter.track_spawns > 0L) {
            int tries = SpawnReporter.spawn_tries.getOrDefault(currentCategory, 1);
            if (existingCount > mobcapTotal) {
                SpawnReporter.spawn_ticks_full.put(group_code, SpawnReporter.spawn_ticks_full.get(group_code) + tries);
            }
            SpawnReporter.spawn_attempts.put(group_code, SpawnReporter.spawn_attempts.get(group_code) + tries);
            SpawnReporter.spawn_cap_count.put(group_code, SpawnReporter.spawn_cap_count.get(group_code) + existingCount);
        }
        if (SpawnReporter.mock_spawns) {
            return 0;
        }
        return existingCount;
    }

    @Redirect(method = "findChunksForSpawning", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;ceil(D)I"))
    private int ceil(double value) {
        return CarpetSettings._1_8Spawning ? 4 : MathHelper.ceil(value);
    }

    // This creates a special iterator that respects the number of tries (SpawnReporter.spawn_tries)
    // which acts like a for (int i = 0; i < tries; i++) loop around the spawning code
    // the iterator executes the code below when it is finished
    @Redirect(method = "findChunksForSpawning", at = @At(value = "INVOKE", target = "Ljava/util/Set;iterator()Ljava/util/Iterator;", remap = false))
    private Iterator<ChunkPos> getChunkIterator(Set<ChunkPos> set) {
        return SpawnReporter.createChunkIterator(set, currentCategory, () -> {
            if (SpawnReporter.track_spawns <= 0L) return;
            String group_code = currentCategory + suffix;
            if (localSpawns > 0) {
                SpawnReporter.spawn_ticks_succ.put(group_code, SpawnReporter.spawn_ticks_succ.get(group_code) + 1L);
                SpawnReporter.spawn_ticks_spawns.put(group_code, SpawnReporter.spawn_ticks_spawns.get(group_code) + localSpawns);
            } else {
                SpawnReporter.spawn_ticks_fail.put(group_code, SpawnReporter.spawn_ticks_fail.get(group_code) + 1L);
            }
        });
    }

    @Redirect(method = "findChunksForSpawning", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
    private boolean spawnEntity(WorldServer worldServer, Entity entity) {
        EntityLiving living = (EntityLiving) entity;
        if (CarpetSettings.optimizedDespawnRange && SpawnReporter.willImmediatelyDespawn(living)) {
            entity.setDead();
            return false;
        }
        localSpawns++;
        if (SpawnReporter.track_spawns > 0) {
            SpawnReporter.registerSpawn(living, currentCategory);
        }
        if (SpawnReporter.mock_spawns) {
            entity.setDead();
            return false;
        }
        return worldServer.spawnEntity(entity);
    }
}
