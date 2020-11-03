package carpet.mixin.rideableGhasts;

import carpet.CarpetSettings;
import carpet.helpers.GhastHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIFindEntityNearestPlayer;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityGhast.class)
public class EntityGhastMixin extends EntityFlying {
    public EntityGhastMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "initEntityAI", at = @At("RETURN"))
    private void addNewTasks(CallbackInfo ci) {
        this.tasks.addTask(3, new GhastHelper.AIFollowClues((EntityGhast) (Object) this));
        this.tasks.addTask(4, new GhastHelper.AIFindOwner((EntityGhast) (Object) this));
    }

    @Redirect(method = "initEntityAI", at = @At(value = "NEW", target = "net/minecraft/entity/ai/EntityAIFindEntityNearestPlayer"))
    private EntityAIFindEntityNearestPlayer replaceTargetTask(EntityLiving ghast) {
        return new GhastHelper.GhastEntityAIFindEntityNearestPlayer(ghast);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (this.rand.nextInt(400) == 0 && CarpetSettings.rideableGhasts && this.deathTime == 0 && this.isBeingRidden()) {
            this.heal(1.0F);
        }
    }

    @Override
    protected boolean processInteract(EntityPlayer player, EnumHand hand) {
        if (!(CarpetSettings.rideableGhasts) || this.isBeingRidden()) {
            return super.processInteract(player, hand);
        }
        if (!GhastHelper.is_yo_bro((EntityGhast) (Object) this, player)) {
            return super.processInteract(player, hand);
        }
        boolean worked = super.processInteract(player, hand);
        if (!worked) {
            player.startRiding(this, true);
            player.getCooldownTracker().setCooldown(Items.FIRE_CHARGE, 1);
        }
        return false;
    }

    @Override
    public Entity getControllingPassenger() {
        return this.getPassengers().isEmpty() ? null : this.getPassengers().get(0);
    }

    @Override
    public double getMountedYOffset() {
        if (CarpetSettings.rideableGhasts) return this.height - 0.2;
        return super.getMountedYOffset();
    }
}
