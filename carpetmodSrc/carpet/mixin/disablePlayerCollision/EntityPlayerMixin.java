package carpet.mixin.disablePlayerCollision;

import carpet.CarpetSettings;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityPlayer.class)
public abstract class EntityPlayerMixin extends EntityLivingBase {
    public EntityPlayerMixin(World worldIn) {
        super(worldIn);
    }

    @Override
    public boolean canBePushed() {
        return !CarpetSettings.disablePlayerCollision;
    }
}
