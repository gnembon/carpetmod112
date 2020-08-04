package carpet.mixin.renewableElderGuardians;

import carpet.CarpetSettings;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntityElderGuardian;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityGuardian.class)
public abstract class EntityGuardianMixin extends EntityMob {
    public EntityGuardianMixin(World worldIn) {
        super(worldIn);
    }

    @Override
    public void onStruckByLightning(EntityLightningBolt lightningBolt) {
        if (!this.world.isRemote && !this.isDead && CarpetSettings.renewableElderGuardians) {
            EntityElderGuardian elderGuardian = new EntityElderGuardian(this.world);
            elderGuardian.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
            elderGuardian.onInitialSpawn(this.world.getDifficultyForLocation(new BlockPos(elderGuardian)), null);
            elderGuardian.setNoAI(this.isAIDisabled());

            if (this.hasCustomName()) {
                elderGuardian.setCustomNameTag(this.getCustomNameTag());
                elderGuardian.setAlwaysRenderNameTag(this.getAlwaysRenderNameTag());
            }

            this.world.spawnEntity(elderGuardian);
            this.setDead();
        } else {
            super.onStruckByLightning(lightningBolt);
        }
    }
}
