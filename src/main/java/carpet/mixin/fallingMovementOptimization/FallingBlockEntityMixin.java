package carpet.mixin.fallingMovementOptimization;

import carpet.CarpetSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityMixin extends Entity {
    // Optimization methods CARPET-XCOM
    private static double[] cache = new double[12];
    private static boolean[] cacheBool = new boolean[2];
    private static long cacheTime = 0;

    public FallingBlockEntityMixin(World worldIn) {
        super(worldIn);
    }

    @Unique private boolean cacheMatching() {
        return cache[0] == field_33071 && cache[1] == field_33072 && cache[2] == field_33073 && cache[3] == field_33074 && cache[4] == field_33075 && cache[5] == field_33076 && cacheTime == method_29602().getTicks();
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/FallingBlockEntity;method_34411(Lnet/minecraft/entity/MovementType;DDD)V"))
    private void movementOptimization(FallingBlockEntity fallingBlock, MovementType type, double x, double y, double z) {
        if (!CarpetSettings.fallingMovementOptimization) {
            fallingBlock.method_34411(type, x, y, z);
            return;
        }
        if (!cacheMatching()) {
            cache[0] = field_33071;
            cache[1] = field_33072;
            cache[2] = field_33073;
            cache[3] = field_33074;
            cache[4] = field_33075;
            cache[5] = field_33076;
            cacheTime = method_29602().getTicks();
            this.method_34411(MovementType.SELF, this.field_33074, this.field_33075, this.field_33076);
            if (!removed) {
                cache[6] = field_33071;
                cache[7] = field_33072;
                cache[8] = field_33073;
                cache[9] = field_33074;
                cache[10] = field_33075;
                cache[11] = field_33076;
                cacheBool[0] = field_32999;
                cacheBool[1] = onGround;
            } else {
                cache[0] = Integer.MAX_VALUE;
            }
        } else {
            this.updatePosition(cache[6], cache[7], cache[8]);
            field_33074 = cache[9];
            field_33075 = cache[10];
            field_33076 = cache[11];
            field_32999 = cacheBool[0];
            onGround = cacheBool[1];
        }
    }
}
