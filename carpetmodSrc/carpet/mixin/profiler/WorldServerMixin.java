package carpet.mixin.profiler;

import carpet.helpers.LagSpikeHelper;
import carpet.utils.CarpetProfiler;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static carpet.helpers.LagSpikeHelper.PrePostSubPhase.*;
import static carpet.helpers.LagSpikeHelper.TickPhase.*;

@Mixin(WorldServer.class)
public abstract class WorldServerMixin extends World {
    protected WorldServerMixin(ISaveHandler saveHandlerIn, WorldInfo info, WorldProvider providerIn, Profiler profilerIn, boolean client) {
        super(saveHandlerIn, info, providerIn, profilerIn, client);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldEntitySpawner;findChunksForSpawning(Lnet/minecraft/world/WorldServer;ZZZ)I"))
    private void preSpawning(CallbackInfo ci) {
        CarpetProfiler.start_section(this.provider.getDimensionType().getName(), "spawning");
        LagSpikeHelper.processLagSpikes(this, MOB_SPAWNING, PRE);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldEntitySpawner;findChunksForSpawning(Lnet/minecraft/world/WorldServer;ZZZ)I", shift = At.Shift.AFTER))
    private void postSpawning(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes(this, MOB_SPAWNING, POST);
        CarpetProfiler.end_current_section();
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/IChunkProvider;tick()Z"))
    private void preChunkUnloading(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes(this, CHUNK_UNLOADING, PRE);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/IChunkProvider;tick()Z", shift = At.Shift.AFTER))
    private void postChunkUnloading(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes(this, CHUNK_UNLOADING, POST);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;tickUpdates(Z)Z"))
    private void preTileTick(CallbackInfo ci) {
        CarpetProfiler.start_section(this.provider.getDimensionType().getName(), "blocks");
        LagSpikeHelper.processLagSpikes(this, TILE_TICK, PRE);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;tickUpdates(Z)Z", shift = At.Shift.AFTER))
    private void postTileTick(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes(this, TILE_TICK, PRE);
        CarpetProfiler.end_current_section();
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;updateBlocks()V"))
    private void preRandomTick(CallbackInfo ci) {
        CarpetProfiler.start_section(this.provider.getDimensionType().getName(), "blocks");
        LagSpikeHelper.processLagSpikes(this, RANDOM_TICK, PRE);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;updateBlocks()V", shift = At.Shift.AFTER))
    private void postRandomTick(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes(this, RANDOM_TICK, POST);
        CarpetProfiler.end_current_section();
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerChunkMap;tick()V"))
    private void preChunkMap(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes(this, PLAYER_CHUNK_MAP, PRE);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerChunkMap;tick()V", shift = At.Shift.AFTER))
    private void postChunkMap(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes(this, PLAYER_CHUNK_MAP, POST);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/village/VillageCollection;tick()V"))
    private void preVillage(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes(this, VILLAGE, PRE);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/village/VillageSiege;tick()V", shift = At.Shift.AFTER))
    private void postVillage(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes(this, VILLAGE, POST);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;sendQueuedBlockEvents()V"))
    private void preBlockEvent(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes(this, BLOCK_EVENT, PRE);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;sendQueuedBlockEvents()V", shift = At.Shift.AFTER))
    private void postBlockEvent(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes(this, BLOCK_EVENT, POST);
    }
}
