package carpet.mixin.blockEventSerializer;

import carpet.CarpetSettings;
import carpet.helpers.ScheduledBlockEventSerializer;
import net.minecraft.block.Block;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldServer.class)
public abstract class WorldServerMixin extends World {
    protected ScheduledBlockEventSerializer blockEventSerializer;

    protected WorldServerMixin(ISaveHandler saveHandlerIn, WorldInfo info, WorldProvider providerIn, Profiler profilerIn, boolean client) {
        super(saveHandlerIn, info, providerIn, profilerIn, client);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfoReturnable<World> cir) {
        initBlockEventSerializer();
    }

    @Inject(method = "addBlockEvent", at = @At("RETURN"))
    private void onAddBlockEvent(BlockPos pos, Block blockIn, int eventID, int eventParam, CallbackInfo ci) {
        if(CarpetSettings.blockEventSerializer) blockEventSerializer.markDirty();
    }

    protected void initBlockEventSerializer() {
        blockEventSerializer = (ScheduledBlockEventSerializer)this.mapStorage.getOrLoadData(ScheduledBlockEventSerializer.class, "blockEvents");

        if (blockEventSerializer == null)
        {
            blockEventSerializer = new ScheduledBlockEventSerializer();
            this.mapStorage.setData("blockEvents", blockEventSerializer);
        }

        blockEventSerializer.setBlockEvents((WorldServer) (Object) this);
    }
}
