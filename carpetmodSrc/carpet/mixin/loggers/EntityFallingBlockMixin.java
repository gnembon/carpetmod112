package carpet.mixin.loggers;

import carpet.logging.LoggerRegistry;
import carpet.logging.logHelpers.TrajectoryLogHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityFallingBlock.class)
public abstract class EntityFallingBlockMixin extends Entity {
    private TrajectoryLogHelper logHelper;

    public EntityFallingBlockMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/World;DDDLnet/minecraft/block/state/IBlockState;)V", at = @At("RETURN"))
    private void onInit(World worldIn, double x, double y, double z, IBlockState fallingBlockState, CallbackInfo ci) {
        if (LoggerRegistry.__fallingBlocks) {
            logHelper = new TrajectoryLogHelper("fallingBlocks");
        }
    }

    @Inject(method = "onUpdate", at = @At("HEAD"))
    private void onUpdate(CallbackInfo ci) {
        if (LoggerRegistry.__fallingBlocks && logHelper != null) {
            logHelper.onTick(posX, posY, posZ, motionX, motionY, motionZ);
        }
    }

    @Override
    public void setDead() {
        if (LoggerRegistry.__fallingBlocks && logHelper != null)
            logHelper.onFinish();
        super.setDead();
    }
}
