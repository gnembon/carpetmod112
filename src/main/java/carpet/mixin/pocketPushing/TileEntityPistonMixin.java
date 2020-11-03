package carpet.mixin.pocketPushing;

import carpet.CarpetSettings;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(TileEntityPiston.class)
public abstract class TileEntityPistonMixin extends TileEntity {
    @Shadow public abstract AxisAlignedBB getAABB(IBlockAccess p_184321_1_, BlockPos p_184321_2_);

    @Shadow private boolean extending;

    @Shadow private EnumFacing pistonFacing;

    @Shadow private IBlockState pistonState;

    @Inject(method = "moveCollidedEntities", at = @At("HEAD"), cancellable = true)
    private void pocketPushing(float nextProgress, CallbackInfo ci) {
        if (CarpetSettings.pocketPushing) {
            translocateCollidedEntities();
            ci.cancel();
        }
    }

    private void translocateCollidedEntities() {
        AxisAlignedBB axisalignedbb = this.getAABB(this.world, this.pos).offset(this.pos);
        List<Entity> entities = this.world.getEntitiesWithinAABBExcludingEntity(null, axisalignedbb);
        if (!entities.isEmpty()) {
            EnumFacing facing = this.extending ? this.pistonFacing : this.pistonFacing.getOpposite();
            for (Entity entity : entities) {
                if (entity.getPushReaction() != EnumPushReaction.IGNORE) {
                    double dx = 0;
                    double dy = 0;
                    double dz = 0;
                    AxisAlignedBB box = entity.getEntityBoundingBox();
                    if (this.pistonState.getBlock() == Blocks.SLIME_BLOCK) {
                        switch (facing.getAxis()) {
                            case X:
                                entity.motionX = facing.getXOffset();
                                break;
                            case Y:
                                entity.motionY = facing.getYOffset();
                                break;
                            case Z:
                                entity.motionZ = facing.getZOffset();
                                break;
                        }
                    }
                    switch (facing.getAxis()) {
                        case X:
                            if (facing.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE) {
                                dx = axisalignedbb.maxX - box.minX;
                            } else {
                                dx = box.maxX - axisalignedbb.minX;
                            }
                            dx = dx + 0.01D;
                            break;
                        case Y:
                            if (facing.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE) {
                                dy = axisalignedbb.maxY - box.minY;
                            } else {
                                dy = box.maxY - axisalignedbb.minY;
                            }
                            dy = dy + 0.01D;
                            break;
                        case Z:
                            if (facing.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE) {
                                dz = axisalignedbb.maxZ - box.minZ;
                            } else {
                                dz = box.maxZ - axisalignedbb.minZ;
                            }
                            dz = dz + 0.01D;
                            break;
                    }
                    entity.move(MoverType.SELF, dx * facing.getXOffset(), dy * facing.getYOffset(), dz * facing.getZOffset());
                }
            }
        }
    }
}
