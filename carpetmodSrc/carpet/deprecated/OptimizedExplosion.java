package carpet.deprecated;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentProtection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import carpet.CarpetSettings;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;

public class OptimizedExplosion {

	private static List<Entity> entitylist;
	private static Vec3d vec3dmem;
	private static long tickmem;

	public static void doExplosionA(Explosion e) {
		Set<BlockPos> set = rayCalcs(e);
		if (!CarpetSettings.getBool("explosionNoBlockDamage")) {
			e.affectedBlockPositions.addAll(set);
		}
		float f3 = e.explosionSize * 2.0F;
		int k1 = MathHelper.floor(e.explosionX - (double) f3 - 1.0D);
		int l1 = MathHelper.floor(e.explosionX + (double) f3 + 1.0D);
		int i2 = MathHelper.floor(e.explosionY - (double) f3 - 1.0D);
		int i1 = MathHelper.floor(e.explosionY + (double) f3 + 1.0D);
		int j2 = MathHelper.floor(e.explosionZ - (double) f3 - 1.0D);
		int j1 = MathHelper.floor(e.explosionZ + (double) f3 + 1.0D);
		Vec3d vec3d = new Vec3d(e.explosionX, e.explosionY, e.explosionZ);

		if (vec3dmem == null || !vec3dmem.equals(vec3d) || tickmem != e.worldObj.getTotalWorldTime()) {
			vec3dmem = vec3d;
			tickmem = e.worldObj.getTotalWorldTime();
			entitylist = e.worldObj.getEntitiesWithinAABBExcludingEntity(null,
					new AxisAlignedBB((double) k1, (double) i2, (double) j2, (double) l1, (double) i1, (double) j1));
			e.explosionSound = 0;
		}
		e.explosionSound++;

		for (int k2 = 0; k2 < entitylist.size(); ++k2) {
			Entity entity = entitylist.get(k2);

			if (entity.equals(e.exploder)) {
				// entitylist.remove(k2);
				RemoveFast(entitylist, k2);
				k2--;
				continue;
			}

			if (entity instanceof EntityTNTPrimed && entity.posX == e.exploder.posX && entity.posY == e.exploder.posY
					&& entity.posZ == e.exploder.posZ) {
				continue;
			}

			if (!entity.isImmuneToExplosions()) {
				double d12 = entity.getDistance(e.explosionX, e.explosionY, e.explosionZ) / (double) f3;

				if (d12 <= 1.0D) {
					double d5 = entity.posX - e.explosionX;
					double d7 = entity.posY + (double) entity.getEyeHeight() - e.explosionY;
					double d9 = entity.posZ - e.explosionZ;
					double d13 = (double) MathHelper.sqrt(d5 * d5 + d7 * d7 + d9 * d9);

					if (d13 != 0.0D) {
						d5 = d5 / d13;
						d7 = d7 / d13;
						d9 = d9 / d13;
						double d14 = (double) e.worldObj.getBlockDensity(vec3d, entity.getEntityBoundingBox());
						double d10 = (1.0D - d12) * d14;
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
	}

	private static void RemoveFast(List<Entity> lst, int index) {
		if (index < lst.size() - 1)
			lst.set(index, lst.get(lst.size() - 1));
		lst.remove(lst.size() - 1);
	}

	private static Set<BlockPos> rayCalcs(Explosion e) {
		Set<BlockPos> set = Sets.<BlockPos>newHashSet();
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
						float rand = e.worldObj.rand.nextFloat();
						if (CarpetSettings.tntRandomRange >= 0) {
							rand = CarpetSettings.tntRandomRange;
						}
						float f = e.explosionSize * (0.7F + rand * 0.6F);
						double d4 = e.explosionX;
						double d6 = e.explosionY;
						double d8 = e.explosionZ;

						for (float f1 = 0.3F; f > 0.0F; f -= 0.22500001F) {
							BlockPos blockpos = new BlockPos(d4, d6, d8);
							IBlockState iblockstate = e.worldObj.getBlockState(blockpos);

							if (iblockstate.getMaterial() != Material.AIR) {
								float f2 = e.exploder != null
										? e.exploder.getExplosionResistance(e, e.worldObj, blockpos, iblockstate)
										: iblockstate.getBlock().getExplosionResistance((Entity) null);
								f -= (f2 + 0.3F) * 0.3F;
							}

							if (f > 0.0F && (e.exploder == null
									|| e.exploder.verifyExplosion(e, e.worldObj, blockpos, iblockstate, f))) {
								set.add(blockpos);
							} else if (first) {
								return set;
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

		return set;
	}
}
