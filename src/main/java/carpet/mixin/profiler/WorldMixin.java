package carpet.mixin.profiler;

import carpet.helpers.LagSpikeHelper;
import carpet.utils.CarpetProfiler;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.List;

@Mixin(World.class)
public class WorldMixin {
    @Shadow @Final public WorldProvider provider;

    private boolean inTileEntitySection;

    @Inject(method = "updateEntities", at = @At("HEAD"))
    private void preEntity(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes((World) (Object) this, LagSpikeHelper.TickPhase.ENTITY, LagSpikeHelper.EntitySubPhase.PRE);
    }

    @Inject(method = "updateEntities", at = @At(value = "CONSTANT", args = "stringValue=remove"))
    private void postWeather(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes((World) (Object) this, LagSpikeHelper.TickPhase.ENTITY, LagSpikeHelper.EntitySubPhase.POST_WEATHER);
        CarpetProfiler.start_section(this.provider.getDimensionType().getName(), "entities");
    }

    @Inject(method = "updateEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;tickPlayers()V", shift = At.Shift.AFTER))
    private void postPlayers(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes((World) (Object) this, LagSpikeHelper.TickPhase.ENTITY, LagSpikeHelper.EntitySubPhase.POST_PLAYERS);
    }

    @Redirect(method = "updateEntities", at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;", remap = false, ordinal = 3))
    private Object onStartEntity(List<Entity> list, int index) {
        Entity entity = list.get(index);
        CarpetProfiler.start_entity_section(this.provider.getDimensionType().getName(), entity);
        return entity;
    }

    @Redirect(method = "updateEntities", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;next()Ljava/lang/Object;", remap = false))
    private Object onStartTileEntity(Iterator<TileEntity> iterator) {
        TileEntity te = iterator.next();
        CarpetProfiler.start_tileentity_section(this.provider.getDimensionType().getName(), te);
        inTileEntitySection = true;
        return te;
    }

    @Inject(method = "updateEntities", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;hasNext()Z", remap = false))
    private void onEndTileEntity(CallbackInfo ci) {
        if (inTileEntitySection) {
            CarpetProfiler.end_current_entity_section();
            inTileEntitySection = false;
        }
    }

    @Inject(method = "updateEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;endSection()V"), slice = @Slice(
        from = @At(value = "CONSTANT", args = "stringValue=remove", ordinal = 1),
        to = @At(value = "CONSTANT", args = "stringValue=blockEntities")
    ))
    private void endCurrentEntitySection(CallbackInfo ci) {
        CarpetProfiler.end_current_entity_section();
    }

    @Inject(method = "updateEntities", at = @At(value = "CONSTANT", args = "stringValue=blockEntities"))
    private void postEntity(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes((World) (Object) this, LagSpikeHelper.TickPhase.ENTITY, LagSpikeHelper.EntitySubPhase.POST_NORMAL);
        CarpetProfiler.end_current_section();
        CarpetProfiler.start_section(this.provider.getDimensionType().getName(), "tileentities");
    }

    @Inject(method = "updateEntities", at = @At(value = "CONSTANT", args = "stringValue=blockEntities", shift = At.Shift.AFTER))
    private void preBlockEntities(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes((World) (Object) this, LagSpikeHelper.TickPhase.TILE_ENTITY, LagSpikeHelper.PrePostSubPhase.PRE);
    }

    @Inject(method = "updateEntities", at = @At("RETURN"))
    private void postBlockEntities(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes((World) (Object) this, LagSpikeHelper.TickPhase.TILE_ENTITY, LagSpikeHelper.PrePostSubPhase.POST);
        CarpetProfiler.end_current_section();
    }
}
