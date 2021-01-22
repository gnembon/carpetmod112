package carpet.helpers;
//Author: xcom & masa

import carpet.CarpetSettings;
import carpet.mixin.accessors.ExplosionAccessor;
import carpet.utils.Messenger;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.explosion.Explosion;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class OptimizedTNT
{
    private static List<Entity> entitylist;
    private static Vec3d vec3dmem;
    private static long tickmem;
    // For disabling the explosion particles and sound
    public static int explosionSound = 0;

    private static Object2DoubleOpenHashMap<Pair<Vec3d, Box>> densityCache = new Object2DoubleOpenHashMap<>();
    private static MutablePair<Vec3d, Box> pairMutable = new MutablePair<>();
    private static Object2ObjectOpenHashMap<BlockPos, BlockState> stateCache = new Object2ObjectOpenHashMap<>();
    private static BlockPos.Mutable posMutable = new BlockPos.Mutable(0, 0, 0);
    private static ObjectOpenHashSet<BlockPos> affectedBlockPositionsSet = new ObjectOpenHashSet<>();
    private static boolean firstRay;
    private static boolean rayCalcDone;
    private static ArrayList<Float> chances = new ArrayList<>();
    private static BlockPos blastChanceLocation;
    private static boolean minecartTNT;

    public static void doExplosionA(ExplosionAccessor e) {
        blastCalc(e);

        if (!CarpetSettings.explosionNoBlockDamage) {
            rayCalcDone = false;
            firstRay = true;
            minecartTNT = e.getEntity() instanceof TntMinecartEntity;
			getAffectedPositionsOnPlaneY(e,  0,  0, 15,  0, 15); // bottom
			getAffectedPositionsOnPlaneY(e, 15,  0, 15,  0, 15); // top
			getAffectedPositionsOnPlaneX(e,  0,  1, 14,  0, 15); // west
			getAffectedPositionsOnPlaneX(e, 15,  1, 14,  0, 15); // east
			getAffectedPositionsOnPlaneZ(e,  0,  1, 14,  1, 14); // north
			getAffectedPositionsOnPlaneZ(e, 15,  1, 14,  1, 14); // south
			stateCache.clear();

            e.getAffectedBlocks().addAll(affectedBlockPositionsSet);
            affectedBlockPositionsSet.clear();
        }

        float f3 = e.getPower() * 2.0F;
        int k1 = MathHelper.floor(e.getX() - (double) f3 - 1.0D);
        int l1 = MathHelper.floor(e.getX() + (double) f3 + 1.0D);
        int i2 = MathHelper.floor(e.getY() - (double) f3 - 1.0D);
        int i1 = MathHelper.floor(e.getY() + (double) f3 + 1.0D);
        int j2 = MathHelper.floor(e.getZ() - (double) f3 - 1.0D);
        int j1 = MathHelper.floor(e.getZ() + (double) f3 + 1.0D);
        Vec3d vec3d = new Vec3d(e.getX(), e.getY(), e.getZ());

        if (vec3dmem == null || !vec3dmem.equals(vec3d) || tickmem != e.getWorld().getTime()) {
            vec3dmem = vec3d;
            tickmem = e.getWorld().getTime();
            entitylist = e.getWorld().getEntitiesIn(null, new Box(k1, i2, j2, l1, i1, j1));
            explosionSound = 0;
        }

        explosionSound++;

        for (int k2 = 0; k2 < entitylist.size(); ++k2) {
            Entity entity = entitylist.get(k2);

            if (entity == e.getEntity()) {
                // entitylist.remove(k2);
                removeFast(entitylist, k2);
                k2--;
                continue;
            }

            if (entity instanceof TntEntity &&
                entity.x == e.getEntity().x &&
                entity.y == e.getEntity().y &&
                entity.z == e.getEntity().z) {
                continue;
            }

            if (!entity.isImmuneToExplosion()) {
                double d12 = entity.distanceTo(e.getX(), e.getY(), e.getZ()) / (double) f3;

                if (d12 <= 1.0D) {
                    double d5 = entity.x - e.getX();
                    double d7 = entity.y + (double) entity.getStandingEyeHeight() - e.getY();
                    double d9 = entity.z - e.getZ();
                    double d13 = (double) MathHelper.sqrt(d5 * d5 + d7 * d7 + d9 * d9);

                    if (d13 != 0.0D) {
                        d5 = d5 / d13;
                        d7 = d7 / d13;
                        d9 = d9 / d13;
                        double density;

						pairMutable.setLeft(vec3d);
						pairMutable.setRight(entity.getBoundingBox());
						density = densityCache.getOrDefault(pairMutable, Double.MAX_VALUE);

						if (density == Double.MAX_VALUE)
						{
							Pair<Vec3d, Box> pair = Pair.of(vec3d, entity.getBoundingBox());
							density = e.getWorld().method_26003(vec3d, entity.getBoundingBox());
							densityCache.put(pair, density);
						}

                        double d10 = (1.0D - d12) * density;
                        entity.damage(DamageSource.explosion((Explosion) e),
                                (float) ((int) ((d10 * d10 + d10) / 2.0D * 7.0D * (double) f3 + 1.0D)));
                        double d11 = d10;

                        if (entity instanceof LivingEntity) {
                            d11 = ProtectionEnchantment.transformExplosionKnockback((LivingEntity) entity, d10);
                        }

                        entity.velocityX += d5 * d11;
                        entity.velocityY += d7 * d11;
                        entity.velocityZ += d9 * d11;

                        if (entity instanceof PlayerEntity) {
                            PlayerEntity entityplayer = (PlayerEntity) entity;

                            if (!entityplayer.isSpectator()
                                    && (!entityplayer.isCreative() || !entityplayer.abilities.flying)) {
                                e.getAffectedPlayers().put(entityplayer, new Vec3d(d5 * d10, d7 * d10, d9 * d10));
                            }
                        }
                    }
                }
            }
        }

        densityCache.clear();
    }

    public static void doExplosionB(ExplosionAccessor e, boolean spawnParticles)
    {
        World world = e.getWorld();
        double posX = e.getX();
        double posY = e.getY();
        double posZ = e.getZ();

        // explosionSound incremented till disabling the explosion particles and sound
        if (explosionSound < 100 || explosionSound % 100 == 0)
        {
            world.playSound(null, posX, posY, posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F,
                    (1.0F + (world.random.nextFloat() - world.random.nextFloat()) * 0.2F) * 0.7F);

            if (e.getPower() >= 2.0F && e.getDamagesTerrain())
            {
                world.addParticle(ParticleTypes.EXPLOSION_HUGE, posX, posY, posZ, 1.0D, 0.0D, 0.0D);
            }
            else
            {
                world.addParticle(ParticleTypes.EXPLOSION_LARGE, posX, posY, posZ, 1.0D, 0.0D, 0.0D);
            }
        }

        if (e.getDamagesTerrain())
        {
            for (BlockPos blockpos : e.getAffectedBlocks())
            {
                BlockState iblockstate = world.getBlockState(blockpos);
                Block block = iblockstate.getBlock();

                if (spawnParticles)
                {
                    double d0 = (double)((float)blockpos.getX() + world.random.nextFloat());
                    double d1 = (double)((float)blockpos.getY() + world.random.nextFloat());
                    double d2 = (double)((float)blockpos.getZ() + world.random.nextFloat());
                    double d3 = d0 - posX;
                    double d4 = d1 - posY;
                    double d5 = d2 - posZ;
                    double d6 = (double)MathHelper.sqrt(d3 * d3 + d4 * d4 + d5 * d5);
                    d3 = d3 / d6;
                    d4 = d4 / d6;
                    d5 = d5 / d6;
                    double d7 = 0.5D / (d6 / (double) e.getPower() + 0.1D);
                    d7 = d7 * (double)(world.random.nextFloat() * world.random.nextFloat() + 0.3F);
                    d3 = d3 * d7;
                    d4 = d4 * d7;
                    d5 = d5 * d7;
                    world.addParticle(ParticleTypes.EXPLOSION_NORMAL,
                            (d0 + posX) / 2.0D, (d1 + posY) / 2.0D, (d2 + posZ) / 2.0D, d3, d4, d5);
                    world.addParticle(ParticleTypes.SMOKE_NORMAL, d0, d1, d2, d3, d4, d5);
                }

                if (iblockstate.getMaterial() != Material.AIR)
                {
                    if (block.shouldDropItemsOnExplosion((Explosion) e))
                    {
                        // CARPET-MASA: use the state from above instead of getting it again from the world
                        block.onStacksDropped(world, blockpos, iblockstate, 1.0F / e.getPower(), 0);
                    }

                    world.setBlockState(blockpos, Blocks.AIR.getDefaultState(), 3);
                    block.onDestroyedByExplosion(world, blockpos, (Explosion) e);
                }
            }
        }

        if (e.getCreateFire())
        {
            for (BlockPos blockpos1 : e.getAffectedBlocks())
            {
                // Use the same Chunk reference because the positions are in the same xz-column
                Chunk chunk = world.method_25975(blockpos1.getX() >> 4, blockpos1.getZ() >> 4);

                if (chunk.getBlockState(blockpos1).getMaterial() == Material.AIR &&
                    chunk.getBlockState(blockpos1.down()).isOpaque() &&
                    e.getRandom().nextInt(3) == 0)
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

    private static void rayCalcs(ExplosionAccessor e) {
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
                        float rand = e.getWorld().random.nextFloat();
                        if (CarpetSettings.tntRandomRange >= 0) {
                            rand = (float) CarpetSettings.tntRandomRange;
                        }
                        float f = e.getPower() * (0.7F + rand * 0.6F);
                        double d4 = e.getX();
                        double d6 = e.getY();
                        double d8 = e.getZ();

                        for (float f1 = 0.3F; f > 0.0F; f -= 0.22500001F) {
                            BlockPos blockpos = new BlockPos(d4, d6, d8);
                            BlockState iblockstate = e.getWorld().getBlockState(blockpos);

                            if (iblockstate.getMaterial() != Material.AIR) {
                                float f2 = e.getEntity() != null
                                        ? e.getEntity().getEffectiveExplosionResistance((Explosion) e, e.getWorld(), blockpos, iblockstate)
                                        : iblockstate.getBlock().getBlastResistance(null);
                                f -= (f2 + 0.3F) * 0.3F;
                            }

                            if (f > 0.0F && (e.getEntity() == null ||
                                e.getEntity().canExplosionDestroyBlock((Explosion) e, e.getWorld(), blockpos, iblockstate, f)))
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

    private static void getAffectedPositionsOnPlaneX(ExplosionAccessor e, int x, int yStart, int yEnd, int zStart, int zEnd)
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

    private static void getAffectedPositionsOnPlaneY(ExplosionAccessor e, int y, int xStart, int xEnd, int zStart, int zEnd)
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

    private static void getAffectedPositionsOnPlaneZ(ExplosionAccessor e, int z, int xStart, int xEnd, int yStart, int yEnd)
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

    private static boolean checkAffectedPosition(ExplosionAccessor e, double xRel, double yRel, double zRel)
    {
        double len = Math.sqrt(xRel * xRel + yRel * yRel + zRel * zRel);
        double xInc = (xRel / len) * 0.3;
        double yInc = (yRel / len) * 0.3;
        double zInc = (zRel / len) * 0.3;
        float rand = e.getWorld().random.nextFloat();
        float sizeRand = (CarpetSettings.tntRandomRange >= 0 ? (float) CarpetSettings.tntRandomRange : rand);
        float size = e.getPower() * (0.7F + sizeRand * 0.6F);
        double posX = e.getX();
        double posY = e.getY();
        double posZ = e.getZ();

        for (float f1 = 0.3F; size > 0.0F; size -= 0.22500001F)
        {
            posMutable.set(posX, posY, posZ);

            // Don't query already cached positions again from the world
            BlockState state = stateCache.get(posMutable);
            BlockPos posImmutable = null;

            if (state == null)
            {
                posImmutable = posMutable.toImmutable();
                state = e.getWorld().getBlockState(posImmutable);
                stateCache.put(posImmutable, state);
            }

            if (state.getMaterial() != Material.AIR)
            {
                float resistance;

                if (e.getEntity() != null)
                {
                    resistance = e.getEntity().getEffectiveExplosionResistance((Explosion) e, e.getWorld(), posMutable, state);
                }
                else
                {
                    resistance = state.getBlock().getBlastResistance(null);
                }

                size -= (resistance + 0.3F) * 0.3F;
            }

            if (size > 0.0F && (e.getEntity() == null || e.getEntity().canExplosionDestroyBlock((Explosion) e, e.getWorld(), posMutable, state, size)))
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

    private static void blastCalc(ExplosionAccessor e){
        if(blastChanceLocation == null || blastChanceLocation.getSquaredDistance(e.getX(), e.getY(), e.getZ()) > 200) return;
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
                        float f = e.getPower() * (0.7F + 0.6F);
                        double d4 = e.getX();
                        double d6 = e.getY();
                        double d8 = e.getZ();
                        boolean found = false;

                        for (float f1 = 0.3F; f > 0.0F; f -= 0.22500001F) {
                            BlockPos blockpos = new BlockPos(d4, d6, d8);
                            BlockState iblockstate = e.getWorld().getBlockState(blockpos);

                            if (iblockstate.getMaterial() != Material.AIR) {
                                float f2 = e.getEntity() != null
                                        ? e.getEntity().getEffectiveExplosionResistance((Explosion) e, e.getWorld(), blockpos, iblockstate)
                                        : iblockstate.getBlock().getBlastResistance((Entity) null);
                                f -= (f2 + 0.3F) * 0.3F;
                            }

                            if (f > 0.0F && (e.getEntity() == null ||
                                    e.getEntity().canExplosionDestroyBlock((Explosion) e, e.getWorld(), blockpos, iblockstate, f))) {
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

    private static void showTNTblastChance(ExplosionAccessor e){
        double randMax = 0.6F * e.getPower();
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
        for(PlayerEntity player : e.getWorld().field_23576){
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
                    String.format("c %.1f ", e.getPower()),
                    "^w TNT blast size",
                    "?" + e.getPower(),
                    "w @: ",
                    String.format("c [%.1f %.1f %.1f] ", e.getX(), e.getY(), e.getZ()),
                    "^w TNT blast location X:" + e.getX() + " Y:" + e.getY() + " Z:" + e.getZ(),
                    "?" + e.getX() + " " + e.getY() + " " + e.getZ()
            );
        }
    }


}