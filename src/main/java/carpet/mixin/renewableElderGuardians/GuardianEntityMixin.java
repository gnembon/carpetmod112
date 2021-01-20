package carpet.mixin.renewableElderGuardians;

import carpet.CarpetSettings;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.mob.ElderGuardianEntity;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GuardianEntity.class)
public abstract class GuardianEntityMixin extends HostileEntity {
    public GuardianEntityMixin(World worldIn) {
        super(worldIn);
    }

    @Override
    public void onStruckByLightning(LightningEntity lightningBolt) {
        if (!this.world.isClient && !this.removed && CarpetSettings.renewableElderGuardians) {
            ElderGuardianEntity elderGuardian = new ElderGuardianEntity(this.world);
            elderGuardian.refreshPositionAndAngles(this.field_33071, this.field_33072, this.field_33073, this.yaw, this.pitch);
            elderGuardian.initialize(this.world.getLocalDifficulty(new BlockPos(elderGuardian)), null);
            elderGuardian.setAiDisabled(this.isAiDisabled());

            if (this.method_34200()) {
                elderGuardian.method_34525(this.method_34510());
                elderGuardian.setCustomNameVisible(this.isCustomNameVisible());
            }

            this.world.method_26040(elderGuardian);
            this.remove();
        } else {
            super.onStruckByLightning(lightningBolt);
        }
    }
}
