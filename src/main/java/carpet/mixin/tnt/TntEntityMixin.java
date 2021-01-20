package carpet.mixin.tnt;

import carpet.CarpetSettings;
import carpet.commands.CommandTNT;
import carpet.logging.LoggerRegistry;
import carpet.logging.logHelpers.TNTLogHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.TntEntity;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(TntEntity.class)
public abstract class TntEntityMixin extends Entity {
    @Shadow private int fuseTimer;
    private TNTLogHelper logHelper = null;
    private int mergedTNT = 1;
    private boolean mergeBool;
    private static final double[] cache = new double[12];
    private static final boolean[] cacheBool = new boolean[2];
    private static long cacheTime = 0;
    public TntEntityMixin(World worldIn) {
        super(worldIn);
    }

    @Redirect(method = "<init>(Lnet/minecraft/world/World;DDDLnet/minecraft/entity/LivingEntity;)V", at = @At(value = "INVOKE", target = "Ljava/lang/Math;random()D", remap = false))
    private double getRandomAngle() {
        if (CarpetSettings.hardcodeTNTangle >= 0) return CarpetSettings.hardcodeTNTangle / (Math.PI * 2);
        if (CarpetSettings.TNTAdjustableRandomAngle) return CommandTNT.rand.nextDouble();
        if (CarpetSettings.tntPrimerMomentumRemoved) return 0;
        return Math.random();
    }

    @Inject(method = "<init>(Lnet/minecraft/world/World;DDDLnet/minecraft/entity/LivingEntity;)V", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onInit(World worldIn, double x, double y, double z, LivingEntity igniter, CallbackInfo ci, float angle) {
        if (CarpetSettings.tntPrimerMomentumRemoved) {
            this.field_33074 = 0;
            this.field_33075 = 0;
            this.field_33076 = 0;
        }
        if (LoggerRegistry.__tnt) {
            logHelper = new TNTLogHelper();
            logHelper.onPrimed(x, y, z, angle);
        }
    }

    @Unique private boolean cacheMatching() {
        return cache[0] == field_33071 && cache[1] == field_33072 && cache[2] == field_33073 && cache[3] == field_33074 && cache[4] == field_33075 && cache[5] == field_33076 && cacheTime == method_29602().getTicks();
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/TntEntity;method_34411(Lnet/minecraft/entity/MovementType;DDD)V"))
    private void movementOptimization(TntEntity tnt, MovementType type, double x, double y, double z) {
        if (!CarpetSettings.TNTmovementOptimization) {
            tnt.method_34411(type, x, y, z);
            return;
        }
        // Optimized TNT movement skipping the move code given its expensive if identical tnt movement is done. CARPET-XCOM
        if(!cacheMatching()) {
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

    @Inject(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/TntEntity;field_33074:D", ordinal = 0), slice = @Slice(
        from = @At(value = "FIELD", target = "Lnet/minecraft/entity/TntEntity;onGround:Z")
    ))
    private void mergeTNT(CallbackInfo ci) {
        // Merge code for combining tnt into a single entity if they happen to exist in the same spot, same fuse, no motion CARPET-XCOM
        if(CarpetSettings.mergeTNT){
            if(!world.isClient && mergeBool && this.field_33074 == 0 && this.field_33075 == 0 && this.field_33076 == 0){
                mergeBool = false;
                for(Entity entity : world.method_26090(this, this.getBoundingBox())){
                    if(entity instanceof TntEntityMixin && !entity.removed){
                        TntEntityMixin entityTNTPrimed = (TntEntityMixin) entity;
                        if(entityTNTPrimed.field_33074 == 0 && entityTNTPrimed.field_33075 == 0 && entityTNTPrimed.field_33076 == 0
                                && this.field_33071 == entityTNTPrimed.field_33071 && this.field_33072 == entityTNTPrimed.field_33072 && this.field_33073 == entityTNTPrimed.field_33073
                                && this.fuseTimer == entityTNTPrimed.fuseTimer){
                            mergedTNT += entityTNTPrimed.mergedTNT;
                            entityTNTPrimed.remove();
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/TntEntity;fuseTimer:I", ordinal = 0))
    private void checkMergeAllowed(CallbackInfo ci) {
        // Merge only tnt that have had a chance to move CARPET-XCOM
        if(!world.isClient && (this.field_33074 != 0 || this.field_33075 != 0 || this.field_33076 != 0)){
            mergeBool = true;
        }
    }

    @Inject(method = "method_24703", at = @At("HEAD"))
    private void onExplode(CallbackInfo ci) {
        if (logHelper != null) logHelper.onExploded(field_33071, field_33072, field_33073);
    }

    @Redirect(method = "method_24703", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;createExplosion(Lnet/minecraft/entity/Entity;DDDFZ)Lnet/minecraft/world/explosion/Explosion;"))
    private Explosion explodeMerged(World world, Entity entity, double x, double y, double z, float strength, boolean damagesTerrain) {
        // Multi explode the amount of merged TNT CARPET-XCOM
        for (int i = 0; i < mergedTNT; i++) {
            world.createExplosion(entity, x, y, z, strength, damagesTerrain);
        }
        return null;
    }
}
