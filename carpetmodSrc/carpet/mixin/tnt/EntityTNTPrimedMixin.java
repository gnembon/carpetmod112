package carpet.mixin.tnt;

import carpet.CarpetSettings;
import carpet.commands.CommandTNT;
import carpet.logging.LoggerRegistry;
import carpet.logging.logHelpers.TNTLogHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(EntityTNTPrimed.class)
public abstract class EntityTNTPrimedMixin extends Entity {
    @Shadow private int fuse;
    private TNTLogHelper logHelper = null;
    private int mergedTNT = 1;
    private boolean mergeBool;
    private static final double[] cache = new double[12];
    private static final boolean[] cacheBool = new boolean[2];
    private static long cacheTime = 0;
    public EntityTNTPrimedMixin(World worldIn) {
        super(worldIn);
    }

    @Redirect(method = "<init>(Lnet/minecraft/world/World;DDDLnet/minecraft/entity/EntityLivingBase;)V", at = @At(value = "INVOKE", target = "Ljava/lang/Math;random()D", remap = false))
    private double getRandomAngle() {
        if (CarpetSettings.hardcodeTNTangle >= 0) return CarpetSettings.hardcodeTNTangle / (Math.PI * 2);
        if (CarpetSettings.TNTAdjustableRandomAngle) return CommandTNT.rand.nextDouble();
        if (CarpetSettings.tntPrimerMomentumRemoved) return 0;
        return Math.random();
    }

    @Inject(method = "<init>(Lnet/minecraft/world/World;DDDLnet/minecraft/entity/EntityLivingBase;)V", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onInit(World worldIn, double x, double y, double z, EntityLivingBase igniter, CallbackInfo ci, float angle) {
        if (CarpetSettings.tntPrimerMomentumRemoved) {
            this.motionX = 0;
            this.motionY = 0;
            this.motionZ = 0;
        }
        if (LoggerRegistry.__tnt) {
            logHelper = new TNTLogHelper();
            logHelper.onPrimed(x, y, z, angle);
        }
    }

    @Unique private boolean cacheMatching() {
        return cache[0] == posX && cache[1] == posY && cache[2] == posZ && cache[3] == motionX && cache[4] == motionY && cache[5] == motionZ && cacheTime == getServer().getTickCounter();
    }

    @Redirect(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/EntityTNTPrimed;move(Lnet/minecraft/entity/MoverType;DDD)V"))
    private void movementOptimization(EntityTNTPrimed tnt, MoverType type, double x, double y, double z) {
        if (!CarpetSettings.TNTmovementOptimization) {
            tnt.move(type, x, y, z);
            return;
        }
        // Optimized TNT movement skipping the move code given its expensive if identical tnt movement is done. CARPET-XCOM
        if(!cacheMatching()) {
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

    @Inject(method = "onUpdate", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/item/EntityTNTPrimed;motionX:D", ordinal = 0), slice = @Slice(
        from = @At(value = "FIELD", target = "Lnet/minecraft/entity/item/EntityTNTPrimed;onGround:Z")
    ))
    private void mergeTNT(CallbackInfo ci) {
        // Merge code for combining tnt into a single entity if they happen to exist in the same spot, same fuse, no motion CARPET-XCOM
        if(CarpetSettings.mergeTNT){
            if(!world.isRemote && mergeBool && this.motionX == 0 && this.motionY == 0 && this.motionZ == 0){
                mergeBool = false;
                for(Entity entity : world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox())){
                    if(entity instanceof EntityTNTPrimedMixin && !entity.isDead){
                        EntityTNTPrimedMixin entityTNTPrimed = (EntityTNTPrimedMixin) entity;
                        if(entityTNTPrimed.motionX == 0 && entityTNTPrimed.motionY == 0 && entityTNTPrimed.motionZ == 0
                                && this.posX == entityTNTPrimed.posX && this.posZ == entityTNTPrimed.posZ && this.posY == entityTNTPrimed.posY
                                && this.fuse == entityTNTPrimed.fuse){
                            mergedTNT += entityTNTPrimed.mergedTNT;
                            entityTNTPrimed.setDead();
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "onUpdate", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/item/EntityTNTPrimed;fuse:I", ordinal = 0))
    private void checkMergeAllowed(CallbackInfo ci) {
        // Merge only tnt that have had a chance to move CARPET-XCOM
        if(!world.isRemote && (this.motionY != 0 || this.motionX != 0 || this.motionZ != 0)){
            mergeBool = true;
        }
    }

    @Inject(method = "explode", at = @At("HEAD"))
    private void onExplode(CallbackInfo ci) {
        if (logHelper != null) logHelper.onExploded(posX, posY, posZ);
    }

    @Redirect(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;createExplosion(Lnet/minecraft/entity/Entity;DDDFZ)Lnet/minecraft/world/Explosion;"))
    private Explosion explodeMerged(World world, Entity entity, double x, double y, double z, float strength, boolean damagesTerrain) {
        // Multi explode the amount of merged TNT CARPET-XCOM
        for (int i = 0; i < mergedTNT; i++) {
            world.createExplosion(entity, x, y, z, strength, damagesTerrain);
        }
        return null;
    }
}
