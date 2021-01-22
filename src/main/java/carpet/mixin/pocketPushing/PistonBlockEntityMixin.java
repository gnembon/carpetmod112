package carpet.mixin.pocketPushing;

import carpet.CarpetSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PistonBlockEntity.class)
public abstract class PistonBlockEntityMixin extends BlockEntity {
    @Shadow public abstract Box method_27144(BlockView p_184321_1_, BlockPos p_184321_2_);
    @Shadow private boolean extending;
    @Shadow private Direction facing;
    @Shadow private BlockState pushedBlock;

    @Inject(method = "method_27160", at = @At("HEAD"), cancellable = true)
    private void pocketPushing(float nextProgress, CallbackInfo ci) {
        if (CarpetSettings.pocketPushing) {
            translocateCollidedEntities();
            ci.cancel();
        }
    }

    private void translocateCollidedEntities() {
        Box axisalignedbb = this.method_27144(this.world, this.pos).offset(this.pos);
        List<Entity> entities = this.world.getEntitiesIn(null, axisalignedbb);
        if (!entities.isEmpty()) {
            Direction facing = this.extending ? this.facing : this.facing.getOpposite();
            for (Entity entity : entities) {
                if (entity.getPistonBehavior() != PistonBehavior.IGNORE) {
                    double dx = 0;
                    double dy = 0;
                    double dz = 0;
                    Box box = entity.getBoundingBox();
                    if (this.pushedBlock.getBlock() == Blocks.SLIME_BLOCK) {
                        switch (facing.getAxis()) {
                            case X:
                                entity.velocityX = facing.getOffsetX();
                                break;
                            case Y:
                                entity.velocityY = facing.getOffsetY();
                                break;
                            case Z:
                                entity.velocityZ = facing.getOffsetZ();
                                break;
                        }
                    }
                    switch (facing.getAxis()) {
                        case X:
                            if (facing.getDirection() == Direction.AxisDirection.POSITIVE) {
                                dx = axisalignedbb.x2 - box.x1;
                            } else {
                                dx = box.x2 - axisalignedbb.x1;
                            }
                            dx = dx + 0.01D;
                            break;
                        case Y:
                            if (facing.getDirection() == Direction.AxisDirection.POSITIVE) {
                                dy = axisalignedbb.y2 - box.y1;
                            } else {
                                dy = box.y2 - axisalignedbb.y1;
                            }
                            dy = dy + 0.01D;
                            break;
                        case Z:
                            if (facing.getDirection() == Direction.AxisDirection.POSITIVE) {
                                dz = axisalignedbb.z2 - box.z1;
                            } else {
                                dz = box.z2 - axisalignedbb.z1;
                            }
                            dz = dz + 0.01D;
                            break;
                    }
                    entity.move(MovementType.SELF, dx * facing.getOffsetX(), dy * facing.getOffsetY(), dz * facing.getOffsetZ());
                }
            }
        }
    }
}
