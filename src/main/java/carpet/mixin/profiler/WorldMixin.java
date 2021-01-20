package carpet.mixin.profiler;

import carpet.helpers.LagSpikeHelper;
import carpet.utils.CarpetProfiler;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
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
    @Shadow @Final public Dimension dimension;

    private boolean inTileEntitySection;

    @Inject(method = "tickBlockEntities", at = @At("HEAD"))
    private void preEntity(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes((World) (Object) this, LagSpikeHelper.TickPhase.ENTITY, LagSpikeHelper.EntitySubPhase.PRE);
    }

    @Inject(method = "tickBlockEntities", at = @At(value = "CONSTANT", args = "stringValue=remove"))
    private void postWeather(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes((World) (Object) this, LagSpikeHelper.TickPhase.ENTITY, LagSpikeHelper.EntitySubPhase.POST_WEATHER);
        CarpetProfiler.start_section(this.dimension.getType().method_27531(), "entities");
    }

    @Inject(method = "tickBlockEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;method_26140()V", shift = At.Shift.AFTER))
    private void postPlayers(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes((World) (Object) this, LagSpikeHelper.TickPhase.ENTITY, LagSpikeHelper.EntitySubPhase.POST_PLAYERS);
    }

    @Redirect(method = "tickBlockEntities", at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;", remap = false, ordinal = 3))
    private Object onStartEntity(List<Entity> list, int index) {
        Entity entity = list.get(index);
        CarpetProfiler.start_entity_section(this.dimension.getType().method_27531(), entity);
        return entity;
    }

    @Redirect(method = "tickBlockEntities", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;next()Ljava/lang/Object;", remap = false))
    private Object onStartTileEntity(Iterator<BlockEntity> iterator) {
        BlockEntity te = iterator.next();
        CarpetProfiler.start_tileentity_section(this.dimension.getType().method_27531(), te);
        inTileEntitySection = true;
        return te;
    }

    @Inject(method = "tickBlockEntities", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;hasNext()Z", remap = false))
    private void onEndTileEntity(CallbackInfo ci) {
        if (inTileEntitySection) {
            CarpetProfiler.end_current_entity_section();
            inTileEntitySection = false;
        }
    }

    @Inject(method = "tickBlockEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;pop()V"), slice = @Slice(
        from = @At(value = "CONSTANT", args = "stringValue=remove", ordinal = 1),
        to = @At(value = "CONSTANT", args = "stringValue=blockEntities")
    ))
    private void endCurrentEntitySection(CallbackInfo ci) {
        CarpetProfiler.end_current_entity_section();
    }

    @Inject(method = "tickBlockEntities", at = @At(value = "CONSTANT", args = "stringValue=blockEntities"))
    private void postEntity(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes((World) (Object) this, LagSpikeHelper.TickPhase.ENTITY, LagSpikeHelper.EntitySubPhase.POST_NORMAL);
        CarpetProfiler.end_current_section();
        CarpetProfiler.start_section(this.dimension.getType().method_27531(), "tileentities");
    }

    @Inject(method = "tickBlockEntities", at = @At(value = "CONSTANT", args = "stringValue=blockEntities", shift = At.Shift.AFTER))
    private void preBlockEntities(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes((World) (Object) this, LagSpikeHelper.TickPhase.TILE_ENTITY, LagSpikeHelper.PrePostSubPhase.PRE);
    }

    @Inject(method = "tickBlockEntities", at = @At("RETURN"))
    private void postBlockEntities(CallbackInfo ci) {
        LagSpikeHelper.processLagSpikes((World) (Object) this, LagSpikeHelper.TickPhase.TILE_ENTITY, LagSpikeHelper.PrePostSubPhase.POST);
        CarpetProfiler.end_current_section();
    }
}
