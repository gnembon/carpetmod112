package carpet.mixin.blockEventSerializer;

import carpet.CarpetSettings;
import carpet.helpers.ScheduledBlockEventSerializer;
import carpet.utils.extensions.WorldWithBlockEventSerializer;
import net.minecraft.block.Block;
import net.minecraft.class_1268;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World implements WorldWithBlockEventSerializer {
    protected ScheduledBlockEventSerializer blockEventSerializer;

    protected ServerWorldMixin(class_1268 levelProperties, LevelProperties levelProperties2, Dimension dimension, Profiler profiler, boolean isClient) {
        super(levelProperties, levelProperties2, dimension, profiler, isClient);
    }

    @Inject(method = "method_26064", at = @At("RETURN"))
    private void onInit(CallbackInfoReturnable<World> cir) {
        initBlockEventSerializer();
    }

    @Inject(method = "addBlockAction", at = @At("RETURN"))
    private void onAddBlockEvent(BlockPos pos, Block blockIn, int eventID, int eventParam, CallbackInfo ci) {
        if(CarpetSettings.blockEventSerializer) blockEventSerializer.markDirty();
    }

    protected void initBlockEventSerializer() {
        blockEventSerializer = (ScheduledBlockEventSerializer)this.field_23593.method_28353(ScheduledBlockEventSerializer.class, "blockEvents");

        if (blockEventSerializer == null) {
            blockEventSerializer = new ScheduledBlockEventSerializer();
            this.field_23593.method_28355("blockEvents", blockEventSerializer);
        }

        blockEventSerializer.setBlockEvents((ServerWorld) (Object) this);
    }

    @Override
    public ScheduledBlockEventSerializer getBlockEventSerializer() {
        return blockEventSerializer;
    }
}
