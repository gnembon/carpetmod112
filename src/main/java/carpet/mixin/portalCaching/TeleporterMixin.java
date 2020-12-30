package carpet.mixin.portalCaching;

import carpet.CarpetSettings;
import carpet.logging.LoggerRegistry;
import carpet.logging.logHelpers.PortalCaching;
import carpet.mixin.accessors.NetHandlerPlayServerAccessor;
import carpet.utils.extensions.ExtendedPortalPosition;
import carpet.utils.extensions.ExtendedTeleporter;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;
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

@Mixin(Teleporter.class)
public class TeleporterMixin implements ExtendedTeleporter {
    @Shadow @Final private Long2ObjectMap<Teleporter.PortalPosition> destinationCoordinateCache;
    @Shadow @Final private WorldServer world;
    private final Long2ObjectMap<Teleporter.PortalPosition> destinationHistoryCache = new Long2ObjectOpenHashMap<>(4096);

    @Overwrite
    public boolean placeInExistingPortal(Entity entity, float rotationYaw) {
        int range = 128;
        double distance = -1.0D;
        int x = MathHelper.floor(entity.posX);
        int z = MathHelper.floor(entity.posZ);
        boolean flag = true;
        boolean flag_cm = true;
        BlockPos outPos = BlockPos.ORIGIN;
        long posKey = ChunkPos.asLong(x, z);

        if (this.destinationCoordinateCache.containsKey(posKey)) {
            Teleporter.PortalPosition pos = this.destinationCoordinateCache.get(posKey);
            distance = 0.0D;
            outPos = pos;
            pos.lastUpdateTime = this.world.getTotalWorldTime();
            flag = false;
        } else if (CarpetSettings.portalCaching && this.destinationHistoryCache.containsKey(posKey)) {
            // potential best candidate for linkage.
            Teleporter.PortalPosition pos = this.destinationHistoryCache.get(posKey);
            //just to verify nobody is cheating the system with update suppression
            if (this.world.getBlockState(pos).getBlock() == Blocks.PORTAL) {
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
                    for (BlockPos currentPos = entityBlockPos.add(offX, this.world.getActualHeight() - 1 - entityBlockPos.getY(), offZ); currentPos.getY() >= 0; currentPos = blockpos2) {
                        blockpos2 = currentPos.down();

                        if (this.world.getBlockState(currentPos).getBlock() == Blocks.PORTAL) {
                            for (blockpos2 = currentPos.down(); this.world.getBlockState(blockpos2).getBlock() == Blocks.PORTAL; blockpos2 = blockpos2.down()) {
                                currentPos = blockpos2;
                            }

                            double currentDistance = currentPos.distanceSq(entityBlockPos);

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
            this.destinationCoordinateCache.put(posKey, createPortalPosition(outPos, this.world.getTotalWorldTime(), new Vec3d(entity.posX, entity.posY, entity.posZ)));
        }

        if (CarpetSettings.portalCaching && (flag || flag_cm)) {
            //its timeless
            this.destinationHistoryCache.put(posKey, createPortalPosition(outPos, 0L, new Vec3d(entity.posX, entity.posY, entity.posZ)));
        }

        double outX = outPos.getX() + 0.5;
        double outZ = outPos.getZ() + 0.5;
        BlockPattern.PatternHelper pattern = Blocks.PORTAL.createPatternHelper(this.world, outPos);
        boolean axisNegative = pattern.getForwards().rotateY().getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE;
        double horizontal = pattern.getForwards().getAxis() == EnumFacing.Axis.X ? pattern.getFrontTopLeft().getZ() : pattern.getFrontTopLeft().getX();
        double outY = pattern.getFrontTopLeft().getY() + 1 - entity.getLastPortalVec().y * pattern.getHeight();

        if (axisNegative) {
            ++horizontal;
        }

        //CM portalSuffocationFix
        //removed offset calculation outside of the if statement
        double offset = (1.0D - entity.getLastPortalVec().x) * pattern.getWidth() * pattern.getForwards().rotateY().getAxisDirection().getOffset();
        if (CarpetSettings.portalSuffocationFix) {
            double correctedRadius = 1.02 * entity.width / 2;
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

        if (pattern.getForwards().getAxis() == EnumFacing.Axis.X) {
            outZ = horizontal + offset;
        } else {
            outX = horizontal + offset;
        }

        float x2x = 0.0F;
        float z2z = 0.0F;
        float x2z = 0.0F;
        float z2x = 0.0F;

        EnumFacing backwards = pattern.getForwards().getOpposite();
        EnumFacing teleportDir = entity.getTeleportDirection();
        if (backwards == teleportDir) {
            x2x = 1;
            z2z = 1;
        } else if (backwards == teleportDir.getOpposite()) {
            x2x = -1;
            z2z = -1;
        } else if (backwards == teleportDir.rotateY()) {
            x2z = 1;
            z2x = -1;
        } else {
            x2z = -1;
            z2x = 1;
        }

        double motionX = entity.motionX;
        double motionZ = entity.motionZ;
        entity.motionX = motionX * (double) x2x + motionZ * (double) z2x;
        entity.motionZ = motionX * (double) x2z + motionZ * (double) z2z;
        entity.rotationYaw = rotationYaw - (float) (teleportDir.getOpposite().getHorizontalIndex() * 90) + (float) (pattern.getForwards().getHorizontalIndex() * 90);

        if (entity instanceof EntityPlayerMP) {
            ((EntityPlayerMP) entity).connection.setPlayerLocation(outX, outY, outZ, entity.rotationYaw, entity.rotationPitch);
            // Resets the players position after move to fix a bug created in the teleportation. CARPET-XCOM
            if (CarpetSettings.portalTeleportationFix) {
                ((NetHandlerPlayServerAccessor) ((EntityPlayerMP) entity).connection).invokeCaptureCurrentPosition();
            }
        } else {
            entity.setLocationAndAngles(outX, outY, outZ, entity.rotationYaw, entity.rotationPitch);
        }

        return true;
    }

    @Overwrite
    public void removeStalePortalLocations(long worldTime)
    {
        if (worldTime % 100 != 0) return;
        long uncachingTime = worldTime - 300L;
        ObjectIterator<Teleporter.PortalPosition> it = this.destinationCoordinateCache.values().iterator();
        ArrayList<Vec3d> uncachings = new ArrayList<>();
        while (it.hasNext()) {
            Teleporter.PortalPosition pos = it.next();

            if (pos == null || pos.lastUpdateTime < uncachingTime) {
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
            PortalCaching.portalCachingCleared(world, destinationCoordinateCache.size(), uncachings);
        }
    }

    @Inject(method = "makePortal", at = @At("RETURN"))
    private void onMakePortal(Entity entityIn, CallbackInfoReturnable<Boolean> cir) {
        if (CarpetSettings.portalCaching) clearHistoryCache();
    }

    @Override
    public void clearHistoryCache() {
        MinecraftServer server = this.world.getMinecraftServer();
        for (WorldServer world : server.worlds) {
            ((TeleporterMixin) (Object) world.getDefaultTeleporter()).removeAllCachedEntries();
        }
    }

    public void removeAllCachedEntries() {
        this.destinationHistoryCache.clear();
    }

    private static MethodHandle portalPositionConstructor;

    private Teleporter.PortalPosition createPortalPosition(BlockPos pos, long lastUpdate, Vec3d cachingCoords) {
        if (portalPositionConstructor == null) {
            try {
                MethodHandles.Lookup lookup = MethodHandles.lookup();
                Constructor<Teleporter.PortalPosition> constructor = Teleporter.PortalPosition.class.getDeclaredConstructor(Teleporter.class, BlockPos.class, long.class);
                portalPositionConstructor = lookup.unreflectConstructor(constructor);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            Teleporter.PortalPosition portalPos = (Teleporter.PortalPosition) portalPositionConstructor.invokeExact((Teleporter) (Object) this, pos, lastUpdate);
            ((ExtendedPortalPosition) portalPos).setCachingCoords(cachingCoords);
            return portalPos;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
