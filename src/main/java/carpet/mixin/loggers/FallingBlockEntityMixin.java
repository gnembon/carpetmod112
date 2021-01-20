package carpet.mixin.loggers;

import carpet.logging.LoggerRegistry;
import carpet.logging.logHelpers.TrajectoryLogHelper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityMixin extends Entity {
    private TrajectoryLogHelper logHelper;

    public FallingBlockEntityMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/World;DDDLnet/minecraft/block/BlockState;)V", at = @At("RETURN"))
    private void onInit(World worldIn, double x, double y, double z, BlockState fallingBlockState, CallbackInfo ci) {
        if (LoggerRegistry.__fallingBlocks) {
            logHelper = new TrajectoryLogHelper("fallingBlocks");
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onUpdate(CallbackInfo ci) {
        if (LoggerRegistry.__fallingBlocks && logHelper != null) {
            logHelper.onTick(x, y, z, velocityX, velocityY, velocityZ);
        }
    }

    @Override
    public void remove() {
        if (LoggerRegistry.__fallingBlocks && logHelper != null)
            logHelper.onFinish();
        super.remove();
    }
}
