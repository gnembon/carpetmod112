package carpet.mixin.fallingMovementOptimization;

import carpet.CarpetSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityFallingBlock.class)
public abstract class EntityFallingBlockMixin extends Entity {
    // Optimization methods CARPET-XCOM
    private static double[] cache = new double[12];
    private static boolean[] cacheBool = new boolean[2];
    private static long cacheTime = 0;

    public EntityFallingBlockMixin(World worldIn) {
        super(worldIn);
    }

    @Unique private boolean cacheMatching() {
        return cache[0] == posX && cache[1] == posY && cache[2] == posZ && cache[3] == motionX && cache[4] == motionY && cache[5] == motionZ && cacheTime == getServer().getTickCounter();
    }

    @Redirect(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/EntityFallingBlock;move(Lnet/minecraft/entity/MoverType;DDD)V"))
    private void movementOptimization(EntityFallingBlock fallingBlock, MoverType type, double x, double y, double z) {
        if (!CarpetSettings.fallingMovementOptimization) {
            fallingBlock.move(type, x, y, z);
            return;
        }
        if (!cacheMatching()) {
            cache[0] = posX;
            cache[1] = posY;
            cache[2] = posZ;
            cache[3] = motionX;
            cache[4] = motionY;
            cache[5] = motionZ;
            cacheTime = getServer().getTickCounter();
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            if (!isDead) {
                cache[6] = posX;
                cache[7] = posY;
                cache[8] = posZ;
                cache[9] = motionX;
                cache[10] = motionY;
                cache[11] = motionZ;
                cacheBool[0] = isInWeb;
                cacheBool[1] = onGround;
            } else {
                cache[0] = Integer.MAX_VALUE;
            }
        } else {
            this.setPosition(cache[6], cache[7], cache[8]);
            motionX = cache[9];
            motionY = cache[10];
            motionZ = cache[11];
            isInWeb = cacheBool[0];
            onGround = cacheBool[1];
        }
    }
}
