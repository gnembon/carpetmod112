package carpet.mixin.portalCaching;

import carpet.CarpetSettings;
import carpet.logging.LoggerRegistry;
import carpet.logging.logHelpers.PortalCaching;
import carpet.mixin.accessors.ServerPlayNetworkHandlerAccessor;
import carpet.utils.extensions.ExtendedPortalPosition;
import carpet.utils.extensions.ExtendedPortalForcer;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.block.Blocks;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.PortalForcer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.util.ArrayList;

@Mixin(PortalForcer.class)
public class PortalForcerMixin implements ExtendedPortalForcer {
    @Shadow @Final private Long2ObjectMap<PortalForcer.class_5096> field_23640;
    @Shadow @Final private ServerWorld world;
    private final Long2ObjectMap<PortalForcer.class_5096> destinationHistoryCache = new Long2ObjectOpenHashMap<>(4096);

    @Overwrite
    public boolean method_26217(Entity entity, float rotationYaw) {
        int range = 128;
        double distance = -1.0D;
        int x = MathHelper.floor(entity.field_33071);
        int z = MathHelper.floor(entity.field_33073);
        boolean flag = true;
        boolean flag_cm = true;
        BlockPos outPos = BlockPos.ORIGIN;
        long posKey = ColumnPos.method_25891(x, z);

        if (this.field_23640.containsKey(posKey)) {
            PortalForcer.class_5096 pos = this.field_23640.get(posKey);
            distance = 0.0D;
            outPos = pos;
            pos.field_23641 = this.world.getTime();
            flag = false;
        } else if (CarpetSettings.portalCaching && this.destinationHistoryCache.containsKey(posKey)) {
            // potential best candidate for linkage.
            PortalForcer.class_5096 pos = this.destinationHistoryCache.get(posKey);
            //just to verify nobody is cheating the system with update suppression
            if (this.world.getBlockState(pos).getBlock() == Blocks.NETHER_PORTAL) {
                distance = 0.0D;
                outPos = pos;
                flag_cm = false;
            }
        }

        if (distance < 0.0D) {
            BlockPos entityBlockPos = new BlockPos(entity);

            for (int offX = -range; offX <= range; ++offX) {
                BlockPos blockpos2;

                for (int offZ = -range; offZ <= range; ++offZ) {
                    for (BlockPos currentPos = entityBlockPos.add(offX, this.world.getEffectiveHeight() - 1 - entityBlockPos.getY(), offZ); currentPos.getY() >= 0; currentPos = blockpos2) {
                        blockpos2 = currentPos.method_31898();

                        if (this.world.getBlockState(currentPos).getBlock() == Blocks.NETHER_PORTAL) {
                            for (blockpos2 = currentPos.method_31898(); this.world.getBlockState(blockpos2).getBlock() == Blocks.NETHER_PORTAL; blockpos2 = blockpos2.method_31898()) {
                                currentPos = blockpos2;
                            }

                            double currentDistance = currentPos.getSquaredDistance(entityBlockPos);

                            if (distance < 0.0D || currentDistance < distance) {
                                distance = currentDistance;
                                outPos = currentPos;
                            }
                        }
                    }
                }
            }
        }

        if (!(distance >= 0.0D)) {
            return false;
        }

        if (flag) {
            this.field_23640.put(posKey, createPortalPosition(outPos, this.world.getTime(), new Vec3d(entity.field_33071, entity.field_33072, entity.field_33073)));
        }

        if (CarpetSettings.portalCaching && (flag || flag_cm)) {
            //its timeless
            this.destinationHistoryCache.put(posKey, createPortalPosition(outPos, 0L, new Vec3d(entity.field_33071, entity.field_33072, entity.field_33073)));
        }

        double outX = outPos.getX() + 0.5;
        double outZ = outPos.getZ() + 0.5;
        BlockPattern.Result pattern = Blocks.NETHER_PORTAL.method_26722(this.world, outPos);
        boolean axisNegative = pattern.getForwards().rotateYClockwise().getDirection() == Direction.AxisDirection.NEGATIVE;
        double horizontal = pattern.getForwards().getAxis() == Direction.Axis.X ? pattern.getFrontTopLeft().getZ() : pattern.getFrontTopLeft().getX();
        double outY = pattern.getFrontTopLeft().getY() + 1 - entity.getLastNetherPortalDirectionVector().y * pattern.getHeight();

        if (axisNegative) {
            ++horizontal;
        }

        //CM portalSuffocationFix
        //removed offset calculation outside of the if statement
        double offset = (1.0D - entity.getLastNetherPortalDirectionVector().x) * pattern.getWidth() * pattern.getForwards().rotateYClockwise().getDirection().offset();
        if (CarpetSettings.portalSuffocationFix) {
            double correctedRadius = 1.02 * entity.field_33001 / 2;
            if (correctedRadius >= pattern.getWidth() - correctedRadius) {
                //entity is wider than portal, so will suffocate anyways, so place it directly in the middle
                correctedRadius = (double) pattern.getWidth() / 2 - 0.001;
            }

            if (offset >= 0) {
                offset = MathHelper.clamp(offset, correctedRadius, pattern.getWidth() - correctedRadius);
            } else {
                offset = MathHelper.clamp(offset, -pattern.getWidth() + correctedRadius, -correctedRadius);
            }
        }

        if (pattern.getForwards().getAxis() == Direction.Axis.X) {
            outZ = horizontal + offset;
        } else {
            outX = horizontal + offset;
        }

        float x2x = 0.0F;
        float z2z = 0.0F;
        float x2z = 0.0F;
        float z2x = 0.0F;

        Direction backwards = pattern.getForwards().getOpposite();
        Direction teleportDir = entity.getLastNetherPortalDirection();
        if (backwards == teleportDir) {
            x2x = 1;
            z2z = 1;
        } else if (backwards == teleportDir.getOpposite()) {
            x2x = -1;
            z2z = -1;
        } else if (backwards == teleportDir.rotateYClockwise()) {
            x2z = 1;
            z2x = -1;
        } else {
            x2z = -1;
            z2x = 1;
        }

        double motionX = entity.field_33074;
        double motionZ = entity.field_33076;
        entity.field_33074 = motionX * (double) x2x + motionZ * (double) z2x;
        entity.field_33076 = motionX * (double) x2z + motionZ * (double) z2z;
        entity.yaw = rotationYaw - (float) (teleportDir.getOpposite().getHorizontal() * 90) + (float) (pattern.getForwards().getHorizontal() * 90);

        if (entity instanceof ServerPlayerEntity) {
            ((ServerPlayerEntity) entity).networkHandler.requestTeleport(outX, outY, outZ, entity.yaw, entity.pitch);
            // Resets the players position after move to fix a bug created in the teleportation. CARPET-XCOM
            if (CarpetSettings.portalTeleportationFix) {
                ((ServerPlayNetworkHandlerAccessor) ((ServerPlayerEntity) entity).networkHandler).invokeCaptureCurrentPosition();
            }
        } else {
            entity.refreshPositionAndAngles(outX, outY, outZ, entity.yaw, entity.pitch);
        }

        return true;
    }

    @Overwrite
    public void method_26214(long worldTime)
    {
        if (worldTime % 100 != 0) return;
        long uncachingTime = worldTime - 300L;
        ObjectIterator<PortalForcer.class_5096> it = this.field_23640.values().iterator();
        ArrayList<Vec3d> uncachings = new ArrayList<>();
        while (it.hasNext()) {
            PortalForcer.class_5096 pos = it.next();

            if (pos == null || pos.field_23641 < uncachingTime) {
                uncachings.add(((ExtendedPortalPosition) pos).getCachingCoords());
                it.remove();
            }
        }

        // Carpet Mod
        //failsafe - arbitrary, but will never happen in normal circumstances,
        //but who knows these freekin players.
        if (CarpetSettings.portalCaching && this.destinationHistoryCache.size() > 65000) {
            removeAllCachedEntries();
        }

        // Log portal uncaching CARPET-XCOM
        if(LoggerRegistry.__portalCaching) {
            PortalCaching.portalCachingCleared(world, field_23640.size(), uncachings);
        }
    }

    @Inject(method = "method_26215", at = @At("RETURN"))
    private void onMakePortal(Entity entityIn, CallbackInfoReturnable<Boolean> cir) {
        if (CarpetSettings.portalCaching) clearHistoryCache();
    }

    @Override
    public void clearHistoryCache() {
        MinecraftServer server = this.world.getServer();
        for (ServerWorld world : server.worlds) {
            ((PortalForcerMixin) (Object) world.getPortalForcer()).removeAllCachedEntries();
        }
    }

    public void removeAllCachedEntries() {
        this.destinationHistoryCache.clear();
    }

    private static MethodHandle portalPositionConstructor;

    private PortalForcer.class_5096 createPortalPosition(BlockPos pos, long lastUpdate, Vec3d cachingCoords) {
        if (portalPositionConstructor == null) {
            try {
                MethodHandles.Lookup lookup = MethodHandles.lookup();
                Constructor<PortalForcer.class_5096> constructor = PortalForcer.class_5096.class.getDeclaredConstructor(PortalForcer.class, BlockPos.class, long.class);
                portalPositionConstructor = lookup.unreflectConstructor(constructor);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            PortalForcer.class_5096 portalPos = (PortalForcer.class_5096) portalPositionConstructor.invokeExact((PortalForcer) (Object) this, pos, lastUpdate);
            ((ExtendedPortalPosition) portalPos).setCachingCoords(cachingCoords);
            return portalPos;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
