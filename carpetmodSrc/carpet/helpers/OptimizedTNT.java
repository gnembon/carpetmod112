package carpet.helpers;
//Author: xcom & masa

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import carpet.logging.LoggerRegistry;
import carpet.utils.Messenger;
import net.minecraft.entity.item.EntityMinecartTNT;
import net.minecraft.util.text.ITextComponent;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentProtection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import carpet.CarpetSettings;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class OptimizedTNT
{
    private static List<Entity> entitylist;
    private static Vec3d vec3dmem;
    private static long tickmem;
    // For disabling the explosion particles and sound
    public static int explosionSound = 0;

    private static Object2DoubleOpenHashMap<Pair<Vec3d, AxisAlignedBB>> densityCache = new Object2DoubleOpenHashMap<>();
    private static MutablePair<Vec3d, AxisAlignedBB> pairMutable = new MutablePair<>();
    private static Object2ObjectOpenHashMap<BlockPos, IBlockState> stateCache = new Object2ObjectOpenHashMap<>();
    private static BlockPos.MutableBlockPos posMutable = new BlockPos.MutableBlockPos(0, 0, 0);
    private static ObjectOpenHashSet<BlockPos> affectedBlockPositionsSet = new ObjectOpenHashSet<>();
    private static boolean firstRay;
    private static boolean rayCalcDone;
    private static ArrayList<Float> chances = new ArrayList<>();
    private static BlockPos blastChanceLocation;
    private static boolean minecartTNT;

    public static void doExplosionA(Explosion e) {
        blastCalc(e);

        if (!CarpetSettings.explosionNoBlockDamage) {
            rayCalcDone = false;
            firstRay = true;
            minecartTNT = e.exploder instanceof EntityMinecartTNT;
			getAffectedPositionsOnPlaneY(e,  0,  0, 15,  0, 15); // bottom
			getAffectedPositionsOnPlaneY(e, 15,  0, 15,  0, 15); // top
			getAffectedPositionsOnPlaneX(e,  0,  1, 14,  0, 15); // west
			getAffectedPositionsOnPlaneX(e, 15,  1, 14,  0, 15); // east
			getAffectedPositionsOnPlaneZ(e,  0,  1, 14,  1, 14); // north
			getAffectedPositionsOnPlaneZ(e, 15,  1, 14,  1, 14); // south
			stateCache.clear();

            e.affectedBlockPositions.addAll(affectedBlockPositionsSet);
            affectedBlockPositionsSet.clear();
        }

        float f3 = e.size * 2.0F;
        int k1 = MathHelper.floor(e.x - (double) f3 - 1.0D);
        int l1 = MathHelper.floor(e.x + (double) f3 + 1.0D);
        int i2 = MathHelper.floor(e.y - (double) f3 - 1.0D);
        int i1 = MathHelper.floor(e.y + (double) f3 + 1.0D);
        int j2 = MathHelper.floor(e.z - (double) f3 - 1.0D);
        int j1 = MathHelper.floor(e.z + (double) f3 + 1.0D);
        Vec3d vec3d = new Vec3d(e.x, e.y, e.z);

        if (vec3dmem == null || !vec3dmem.equals(vec3d) || tickmem != e.world.getTotalWorldTime()) {
            vec3dmem = vec3d;
            tickmem = e.world.getTotalWorldTime();
            entitylist = e.world.getEntitiesWithinAABBExcludingEntity(null,
                    new AxisAlignedBB((double) k1, (double) i2, (double) j2, (double) l1, (double) i1, (double) j1));
            explosionSound = 0;
        }

        explosionSound++;

        for (int k2 = 0; k2 < entitylist.size(); ++k2) {
            Entity entity = entitylist.get(k2);

            if (entity == e.exploder) {
                // entitylist.remove(k2);
                removeFast(entitylist, k2);
                k2--;
                continue;
            }

            if (entity instanceof EntityTNTPrimed &&
                entity.posX == e.exploder.posX &&
                entity.posY == e.exploder.posY &&
                entity.posZ == e.exploder.posZ) {
                continue;
            }

            if (!entity.isImmuneToExplosions()) {
                double d12 = entity.getDistance(e.x, e.y, e.z) / (double) f3;

                if (d12 <= 1.0D) {
                    double d5 = entity.posX - e.x;
                    double d7 = entity.posY + (double) entity.getEyeHeight() - e.y;
                    double d9 = entity.posZ - e.z;
                    double d13 = (double) MathHelper.sqrt(d5 * d5 + d7 * d7 + d9 * d9);

                    if (d13 != 0.0D) {
                        d5 = d5 / d13;
                        d7 = d7 / d13;
                        d9 = d9 / d13;
                        double density;

						pairMutable.setLeft(vec3d);
						pairMutable.setRight(entity.getEntityBoundingBox());
						density = densityCache.getOrDefault(pairMutable, Double.MAX_VALUE);

						if (density == Double.MAX_VALUE)
						{
							Pair<Vec3d, AxisAlignedBB> pair = Pair.of(vec3d, entity.getEntityBoundingBox());
							density = e.world.getBlockDensity(vec3d, entity.getEntityBoundingBox());
							densityCache.put(pair, density);
						}

                        double d10 = (1.0D - d12) * density;
                        entity.attackEntityFrom(DamageSource.causeExplosionDamage(e),
                                (float) ((int) ((d10 * d10 + d10) / 2.0D * 7.0D * (double) f3 + 1.0D)));
                        double d11 = d10;

                        if (entity instanceof EntityLivingBase) {
                            d11 = EnchantmentProtection.getBlastDamageReduction((EntityLivingBase) entity, d10);
                        }

                        entity.motionX += d5 * d11;
                        entity.motionY += d7 * d11;
                        entity.motionZ += d9 * d11;

                        if (entity instanceof EntityPlayer) {
                            EntityPlayer entityplayer = (EntityPlayer) entity;

                            if (!entityplayer.isSpectator()
                                    && (!entityplayer.isCreative() || !entityplayer.capabilities.isFlying)) {
                                e.playerKnockbackMap.put(entityplayer, new Vec3d(d5 * d10, d7 * d10, d9 * d10));
                            }
                        }
                    }
                }
            }
        }

        densityCache.clear();
    }

    public static void doExplosionB(Explosion e, boolean spawnParticles)
    {
        World world = e.world;
        double posX = e.x;
        double posY = e.y;
        double posZ = e.z;

        // explosionSound incremented till disabling the explosion particles and sound
        if (explosionSound < 100 || explosionSound % 100 == 0)
        {
            world.playSound(null, posX, posY, posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F,
                    (1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F) * 0.7F);

            if (e.size >= 2.0F && e.damagesTerrain)
            {
                world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, posX, posY, posZ, 1.0D, 0.0D, 0.0D);
            }
            else
            {
                world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, posX, posY, posZ, 1.0D, 0.0D, 0.0D);
            }
        }

        if (e.damagesTerrain)
        {
            for (BlockPos blockpos : e.affectedBlockPositions)
            {
                IBlockState iblockstate = world.getBlockState(blockpos);
                Block block = iblockstate.getBlock();

                if (spawnParticles)
                {
                    double d0 = (double)((float)blockpos.getX() + world.rand.nextFloat());
                    double d1 = (double)((float)blockpos.getY() + world.rand.nextFloat());
                    double d2 = (double)((float)blockpos.getZ() + world.rand.nextFloat());
                    double d3 = d0 - posX;
                    double d4 = d1 - posY;
                    double d5 = d2 - posZ;
                    double d6 = (double)MathHelper.sqrt(d3 * d3 + d4 * d4 + d5 * d5);
                    d3 = d3 / d6;
                    d4 = d4 / d6;
                    d5 = d5 / d6;
                    double d7 = 0.5D / (d6 / (double) e.size + 0.1D);
                    d7 = d7 * (double)(world.rand.nextFloat() * world.rand.nextFloat() + 0.3F);
                    d3 = d3 * d7;
                    d4 = d4 * d7;
                    d5 = d5 * d7;
                    world.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL,
                            (d0 + posX) / 2.0D, (d1 + posY) / 2.0D, (d2 + posZ) / 2.0D, d3, d4, d5);
                    world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0, d1, d2, d3, d4, d5);
                }

                if (iblockstate.getMaterial() != Material.AIR)
                {
                    if (block.canDropFromExplosion(e))
                    {
                        // CARPET-MASA: use the state from above instead of getting it again from the world
                        block.dropBlockAsItemWithChance(world, blockpos, iblockstate, 1.0F / e.size, 0);
                    }

                    world.setBlockState(blockpos, Blocks.AIR.getDefaultState(), 3);
                    block.onExplosionDestroy(world, blockpos, e);
                }
            }
        }

        if (e.causesFire)
        {
            for (BlockPos blockpos1 : e.affectedBlockPositions)
            {
                // Use the same Chunk reference because the positions are in the same xz-column
                Chunk chunk = world.getChunk(blockpos1.getX() >> 4, blockpos1.getZ() >> 4);

                if (chunk.getBlockState(blockpos1).getMaterial() == Material.AIR &&
                    chunk.getBlockState(blockpos1.down()).isFullBlock() &&
                    e.random.nextInt(3) == 0)
                {
                    world.setBlockState(blockpos1, Blocks.FIRE.getDefaultState());
                }
            }
        }
    }

    private static void removeFast(List<Entity> lst, int index) {
        if (index < lst.size() - 1)
            lst.set(index, lst.get(lst.size() - 1));
        lst.remove(lst.size() - 1);
    }

    private static void rayCalcs(Explosion e) {
        boolean first = true;

        for (int j = 0; j < 16; ++j) {
            for (int k = 0; k < 16; ++k) {
                for (int l = 0; l < 16; ++l) {
                    if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
                        double d0 = (double) ((float) j / 15.0F * 2.0F - 1.0F);
                        double d1 = (double) ((float) k / 15.0F * 2.0F - 1.0F);
                        double d2 = (double) ((float) l / 15.0F * 2.0F - 1.0F);
                        double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                        d0 = d0 / d3;
                        d1 = d1 / d3;
                        d2 = d2 / d3;
                        float rand = e.world.rand.nextFloat();
                        if (CarpetSettings.tntRandomRange >= 0) {
                            rand = (float) CarpetSettings.tntRandomRange;
                        }
                        float f = e.size * (0.7F + rand * 0.6F);
                        double d4 = e.x;
                        double d6 = e.y;
                        double d8 = e.z;

                        for (float f1 = 0.3F; f > 0.0F; f -= 0.22500001F) {
                            BlockPos blockpos = new BlockPos(d4, d6, d8);
                            IBlockState iblockstate = e.world.getBlockState(blockpos);

                            if (iblockstate.getMaterial() != Material.AIR) {
                                float f2 = e.exploder != null
                                        ? e.exploder.getExplosionResistance(e, e.world, blockpos, iblockstate)
                                        : iblockstate.getBlock().getExplosionResistance((Entity) null);
                                f -= (f2 + 0.3F) * 0.3F;
                            }

                            if (f > 0.0F && (e.exploder == null ||
                                e.exploder.canExplosionDestroyBlock(e, e.world, blockpos, iblockstate, f)))
                            {
                                affectedBlockPositionsSet.add(blockpos);
                            }
                            else if (first) {
                                return;
                            }

                            first = false;

                            d4 += d0 * 0.30000001192092896D;
                            d6 += d1 * 0.30000001192092896D;
                            d8 += d2 * 0.30000001192092896D;
                        }
                    }
                }
            }
        }
    }

    private static void getAffectedPositionsOnPlaneX(Explosion e, int x, int yStart, int yEnd, int zStart, int zEnd)
    {
        if (rayCalcDone == false)
        {
            final double xRel = (double) x / 15.0D * 2.0D - 1.0D;

            for (int z = zStart; z <= zEnd; ++z)
            {
                double zRel = (double) z / 15.0D * 2.0D - 1.0D;

                for (int y = yStart; y <= yEnd; ++y)
                {
                    double yRel = (double) y / 15.0D * 2.0D - 1.0D;

                    if (checkAffectedPosition(e, xRel, yRel, zRel))
                    {
                        return;
                    }
                }
            }
        }
    }

    private static void getAffectedPositionsOnPlaneY(Explosion e, int y, int xStart, int xEnd, int zStart, int zEnd)
    {
        if (rayCalcDone == false)
        {
            final double yRel = (double) y / 15.0D * 2.0D - 1.0D;

            for (int z = zStart; z <= zEnd; ++z)
            {
                double zRel = (double) z / 15.0D * 2.0D - 1.0D;

                for (int x = xStart; x <= xEnd; ++x)
                {
                    double xRel = (double) x / 15.0D * 2.0D - 1.0D;

                    if (checkAffectedPosition(e, xRel, yRel, zRel))
                    {
                        return;
                    }
                }
            }
        }
    }

    private static void getAffectedPositionsOnPlaneZ(Explosion e, int z, int xStart, int xEnd, int yStart, int yEnd)
    {
        if (rayCalcDone == false)
        {
            final double zRel = (double) z / 15.0D * 2.0D - 1.0D;

            for (int x = xStart; x <= xEnd; ++x)
            {
                double xRel = (double) x / 15.0D * 2.0D - 1.0D;

                for (int y = yStart; y <= yEnd; ++y)
                {
                    double yRel = (double) y / 15.0D * 2.0D - 1.0D;

                    if (checkAffectedPosition(e, xRel, yRel, zRel))
                    {
                        return;
                    }
                }
            }
        }
    }

    private static boolean checkAffectedPosition(Explosion e, double xRel, double yRel, double zRel)
    {
        double len = Math.sqrt(xRel * xRel + yRel * yRel + zRel * zRel);
        double xInc = (xRel / len) * 0.3;
        double yInc = (yRel / len) * 0.3;
        double zInc = (zRel / len) * 0.3;
        float rand = e.world.rand.nextFloat();
        float sizeRand = (CarpetSettings.tntRandomRange >= 0 ? (float) CarpetSettings.tntRandomRange : rand);
        float size = e.size * (0.7F + sizeRand * 0.6F);
        double posX = e.x;
        double posY = e.y;
        double posZ = e.z;

        for (float f1 = 0.3F; size > 0.0F; size -= 0.22500001F)
        {
            posMutable.setPos(posX, posY, posZ);

            // Don't query already cached positions again from the world
            IBlockState state = stateCache.get(posMutable);
            BlockPos posImmutable = null;

            if (state == null)
            {
                posImmutable = posMutable.toImmutable();
                state = e.world.getBlockState(posImmutable);
                stateCache.put(posImmutable, state);
            }

            if (state.getMaterial() != Material.AIR)
            {
                float resistance;

                if (e.exploder != null)
                {
                    resistance = e.exploder.getExplosionResistance(e, e.world, posMutable, state);
                }
                else
                {
                    resistance = state.getBlock().getExplosionResistance(null);
                }

                size -= (resistance + 0.3F) * 0.3F;
            }

            if (size > 0.0F && (e.exploder == null || e.exploder.canExplosionDestroyBlock(e, e.world, posMutable, state, size)))
            {
                affectedBlockPositionsSet.add(posImmutable != null ? posImmutable : posMutable.toImmutable());
            }
            else if (firstRay && !minecartTNT)
            {
                rayCalcDone = true;
                return true;
            }

            firstRay = false;

            posX += xInc;
            posY += yInc;
            posZ += zInc;
        }

        return false;
    }

    public static void setBlastChanceLocation(BlockPos p){
        blastChanceLocation = p;
    }

    private static void blastCalc(Explosion e){
        if(blastChanceLocation == null || blastChanceLocation.distanceSq(e.x, e.y, e.z) > 200) return;
        chances.clear();
        for (int j = 0; j < 16; ++j) {
            for (int k = 0; k < 16; ++k) {
                for (int l = 0; l < 16; ++l) {
                    if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
                        double d0 = (double) ((float) j / 15.0F * 2.0F - 1.0F);
                        double d1 = (double) ((float) k / 15.0F * 2.0F - 1.0F);
                        double d2 = (double) ((float) l / 15.0F * 2.0F - 1.0F);
                        double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                        d0 = d0 / d3;
                        d1 = d1 / d3;
                        d2 = d2 / d3;
                        float f = e.size * (0.7F + 0.6F);
                        double d4 = e.x;
                        double d6 = e.y;
                        double d8 = e.z;
                        boolean found = false;

                        for (float f1 = 0.3F; f > 0.0F; f -= 0.22500001F) {
                            BlockPos blockpos = new BlockPos(d4, d6, d8);
                            IBlockState iblockstate = e.world.getBlockState(blockpos);

                            if (iblockstate.getMaterial() != Material.AIR) {
                                float f2 = e.exploder != null
                                        ? e.exploder.getExplosionResistance(e, e.world, blockpos, iblockstate)
                                        : iblockstate.getBlock().getExplosionResistance((Entity) null);
                                f -= (f2 + 0.3F) * 0.3F;
                            }

                            if (f > 0.0F && (e.exploder == null ||
                                    e.exploder.canExplosionDestroyBlock(e, e.world, blockpos, iblockstate, f))) {
                                if(!found && blockpos.equals(blastChanceLocation)){
                                    chances.add(f);
                                    found = true;
                                }
                            }

                            d4 += d0 * 0.30000001192092896D;
                            d6 += d1 * 0.30000001192092896D;
                            d8 += d2 * 0.30000001192092896D;
                        }
                    }
                }
            }
        }

        showTNTblastChance(e);
    }

    private static void showTNTblastChance(Explosion e){
        double randMax = 0.6F * e.size;
        double total = 0;
        boolean fullyBlownUp = false;
        boolean first = true;
        int rays = 0;
        for(float f3 : chances){
            rays++;
            double calc = f3 - randMax;
                if(calc > 0) fullyBlownUp = true;
            double chancePerRay = (Math.abs(calc) / randMax);
            if(!fullyBlownUp){
                if(first){
                    first = false;
                    total = chancePerRay;
                }else {
                    total = total * chancePerRay;
                }
            }
        }
        if(fullyBlownUp) total = 0;
        double chance = 1 - total;
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setRoundingMode (RoundingMode.DOWN);
        nf.setMaximumFractionDigits(2);
        for(EntityPlayer player : e.world.playerEntities){
            Messenger.m(player,"w Pop: ",
                    "c " + nf.format(chance) + " ",
                    "^w Chance for the block to be destroyed by the blast: " + chance,
                    "?" + chance,
                    "w Remain: ",
                    String.format("c %.2f ", total),
                    "^w Chance the block survives the blast: " + total,
                    "?" + total,
                    "w Rays: ",
                    String.format("c %d ", rays),
                    "^w TNT blast rays going through the block",
                    "?" + rays,
                    "w Size: ",
                    String.format("c %.1f ", e.size),
                    "^w TNT blast size",
                    "?" + e.size,
                    "w @: ",
                    String.format("c [%.1f %.1f %.1f] ", e.x, e.y, e.z),
                    "^w TNT blast location X:" + e.x + " Y:" + e.y + " Z:" + e.z,
                    "?" + e.x + " " + e.y + " " + e.z
            );
        }
    }


}