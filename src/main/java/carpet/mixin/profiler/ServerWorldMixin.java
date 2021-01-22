package carpet.mixin.profiler;

import carpet.helpers.LagSpikeHelper;
import carpet.utils.CarpetProfiler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.WorldSaveHandler;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static carpet.helpers.LagSpikeHelper.PrePostSubPhase.*;
import static carpet.helpers.LagSpikeHelper.TickPhase.*;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World {
    protected ServerWorldMixin(WorldSaveHandler levelProperties, LevelProperties levelProperties2, Dimension dimension, Profiler profiler, boolean isClient) {
        super(levelProperties, levelProperties2, dimension, profiler, isClient);
    }

    @Inject(method = "tickTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/SpawnHelper;method_26212(Lnet/minecraft/server/world/ServerWorld;ZZZ)I"))
    private void preSpawning(CallbackInfo ci) {
        CarpetProfiler.start_section(this.dimension.getType().getSaveDir(), "spawning");
        LagSpikeHelper.processLagSpikes(this, MOB_SPAWNING, PRE);
    }

    @Inject(method = "tickTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/SpawnHelper;method_26212(Lnet/minecraft/server/world/ServerWorld;ZZZ)I", shift = At.Shift.AFTER))
    private void postSpawning(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes(this, MOB_SPAWNING, POST);
        CarpetProfiler.end_current_section();
    }

    @Inject(method = "tickTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/ChunkCache;tick()Z"))
    private void preChunkUnloading(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes(this, CHUNK_UNLOADING, PRE);
    }

    @Inject(method = "tickTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/ChunkCache;tick()Z", shift = At.Shift.AFTER))
    private void postChunkUnloading(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes(this, CHUNK_UNLOADING, POST);
    }

    @Inject(method = "tickTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;method_26051(Z)Z"))
    private void preTileTick(CallbackInfo ci) {
        CarpetProfiler.start_section(this.dimension.getType().getSaveDir(), "blocks");
        LagSpikeHelper.processLagSpikes(this, TILE_TICK, PRE);
    }

    @Inject(method = "tickTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;method_26051(Z)Z", shift = At.Shift.AFTER))
    private void postTileTick(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes(this, TILE_TICK, PRE);
        CarpetProfiler.end_current_section();
    }

    @Inject(method = "tickTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;tickChunk()V"))
    private void preRandomTick(CallbackInfo ci) {
        CarpetProfiler.start_section(this.dimension.getType().getSaveDir(), "blocks");
        LagSpikeHelper.processLagSpikes(this, RANDOM_TICK, PRE);
    }

    @Inject(method = "tickTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;tickChunk()V", shift = At.Shift.AFTER))
    private void postRandomTick(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes(this, RANDOM_TICK, POST);
        CarpetProfiler.end_current_section();
    }

    @Inject(method = "tickTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_6380;method_33590()V"))
    private void preChunkMap(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes(this, PLAYER_CHUNK_MAP, PRE);
    }

    @Inject(method = "tickTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_6380;method_33590()V", shift = At.Shift.AFTER))
    private void postChunkMap(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes(this, PLAYER_CHUNK_MAP, POST);
    }

    @Inject(method = "tickTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/VillageState;method_35113()V"))
    private void preVillage(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes(this, VILLAGE, PRE);
    }

    @Inject(method = "tickTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/village/ZombieSiegeManager;method_35109()V", shift = At.Shift.AFTER))
    private void postVillage(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes(this, VILLAGE, POST);
    }

    @Inject(method = "tickTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;sendBlockActions()V"))
    private void preBlockEvent(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes(this, BLOCK_EVENT, PRE);
    }

    @Inject(method = "tickTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;sendBlockActions()V", shift = At.Shift.AFTER))
    private void postBlockEvent(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes(this, BLOCK_EVENT, POST);
    }
}
