package carpet.helpers;

import carpet.CarpetSettings;
import carpet.mixin.accessors.LivingEntityAccessor;
import net.minecraft.class_3092;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class GhastHelper
{
    /*target selector to make sure no player with names is chosen
     */
    public static class GhastEntityAIFindEntityNearestPlayer extends class_3092
    {
        private MobEntity entity;
        public GhastEntityAIFindEntityNearestPlayer(MobEntity entityLivingIn)
        {
            super(entityLivingIn);
            this.entity = entityLivingIn;
        }

        @Override
        public boolean canStart()
        {
            if (CarpetSettings.rideableGhasts && entity.hasCustomName())
            {
                return false;
            }
            return super.canStart();
        }

        @Override
        public boolean shouldContinue()
        {
            if (CarpetSettings.rideableGhasts && entity.hasCustomName())
            {
                return false;
            }
            return super.shouldContinue();
        }

    }
    public static boolean is_yo_bro(GhastEntity ghast, PlayerEntity player)
    {
        return (ghast.hasCustomName() && player.getGameProfile().getName().equals(ghast.getCustomName()));
    }
    public static boolean holds_yo_tear(PlayerEntity player)
    {
        return (
                (!player.getMainHandStack().isEmpty() &&
                        player.getMainHandStack().getItem() == Items.GHAST_TEAR)
                        ||
                (!player.getOffHandStack().isEmpty() &&
                                player.getOffHandStack().getItem() == Items.GHAST_TEAR)
        );
    }
    /*sets off fireball on demand
     */
    public static void set_off_fball(GhastEntity ghast, World world, PlayerEntity player)
    {
        world.syncWorldEvent(null, 1015, new BlockPos(ghast), 0);
        Vec3d vec3d = player.getRotationVec(1.0F);
        world.syncWorldEvent(null, 1016, new BlockPos(ghast), 0);
        FireballEntity entitylargefireball = new FireballEntity(world, player, 30.0*vec3d.x, 30.0*vec3d.y, 30.0*vec3d.z);
        entitylargefireball.explosionPower = ghast.getFireballStrength();
        entitylargefireball.x = ghast.x + vec3d.x * 4.0D;
        entitylargefireball.y = ghast.y + (double)(ghast.height / 2.0F) +vec3d.y * 4.0D+ 0.5D;
        entitylargefireball.z = ghast.z + vec3d.z * 4.0D;
        world.spawnEntity(entitylargefireball);
    }

    /*rided ghast follows rider's tear clues
    */
    public static class AIFollowClues extends Goal
    {
        private final GhastEntity parentEntity;
        private PlayerEntity rider = null;
        public AIFollowClues(GhastEntity ghast)
        {
            this.parentEntity = ghast;
            this.method_34938(1);
        }
        public boolean canStart()
        {
            if (!CarpetSettings.rideableGhasts)
            {
                return false;
            }
            if (this.parentEntity.hasPassengers())
            {
                Entity p = this.parentEntity.getPrimaryPassenger();
                if (p instanceof PlayerEntity)
                {
                    if (holds_yo_tear((PlayerEntity)p))
                    {
                        return true;
                    }
                }
            }
            return false;
            //return (this.parentEntity.isBeingRidden() && this.parentEntity.getPassengers().get(0) instanceof EntityPlayer);
        }
        public void start()
        {
            rider = (PlayerEntity)this.parentEntity.getPrimaryPassenger();
        }
        public void stop()
        {
            this.rider = null;
        }
        //private Vec3d look_left(Vec3d v)
        //{
            //return new Vec3d(v.zCoord+v.yCoord, -v.zCoord+v.xCoord, -v.yCoord-v.xCoord);
            //return new Vec3d(v.zCoord+v.yCoord, 0.0, -v.yCoord-v.xCoord);
        //}
        public void tick()
        {
            float strafe = rider.sidewaysSpeed;
            float forward = rider.forwardSpeed;
            if (forward <= 0.0F)
            {
                forward *= 0.5F;
            }
            Vec3d vec3d = Vec3d.ZERO;
            if (forward != 0.0f)
            {
                vec3d = rider.getRotationVec(1.0F);
                if (forward < 0.0f)
                {
                    vec3d = vec3d.reverseSubtract(Vec3d.ZERO);
                }
            }
            if (strafe != 0.0f)
            {
                //Vec3d strafe_vec = rider.getLook(1.0F).rotateYaw((float)Math.PI / 2F).rotatePitch(-rider.rotationPitch).scale(strafe);
                //Vec3d strafe_vec = this.look_left(rider.getLook(1.0F)).scale(strafe);

                //strafe_vec = new Vec3d(strafe_vec.xCoord, 0.0f, strafe_vec.zCoord);
                //vec3d = vec3d.add(strafe_vec);
                float c = MathHelper.cos(rider.yaw* 0.017453292F);
                float s = MathHelper.sin(rider.yaw* 0.017453292F);
                vec3d = new Vec3d(vec3d.x+c*strafe,vec3d.y,vec3d.z+s*strafe);
            }
            if (((LivingEntityAccessor) rider).isJumping())
            {
                vec3d = new Vec3d(vec3d.x,vec3d.y+1.0D,vec3d.z);
            }
            if (!(vec3d.equals(Vec3d.ZERO)))
            {
                this.parentEntity.getMoveControl().moveTo(this.parentEntity.x + vec3d.x, this.parentEntity.y + vec3d.y,this.parentEntity.z + vec3d.z, 1.0D );
            }
            else
            {
                this.parentEntity.getMoveControl().state = MoveControl.State.WAIT;
            }
        }
    }

    /* homing abilities to find the player
    */
    public static class AIFindOwner extends Goal
    {
        private final GhastEntity parentEntity;
        private PlayerEntity owner = null;
        public AIFindOwner(GhastEntity ghast)
        {
            this.parentEntity = ghast;
            this.method_34938(1);
        }

        private PlayerEntity findOwner()
        {
            if (!this.parentEntity.hasPassengers() && this.parentEntity.hasCustomName())
            {
                PlayerEntity player = this.parentEntity.getEntityWorld().getServer().getPlayerManager().getPlayer(this.parentEntity.getCustomName());
                if (player != null && player.dimensionId == this.parentEntity.dimensionId && this.parentEntity.squaredDistanceTo(player) < 300.0D*300.0D)
                {
                    if (!(player.hasVehicle() && player.getVehicle() instanceof GhastEntity))
                    {
                        if (this.parentEntity.squaredDistanceTo(player) > 10.0D*10.0D && holds_yo_tear(player))
                        {
                            return player;
                        }
                    }
                }
            }
            return null;
        }

        public boolean canStart()
        {
            if (!CarpetSettings.rideableGhasts)
            {
                return false;
            }
            if (owner != null)
            {
                owner = null;
                return false;
            }
            if (this.parentEntity.getRandom().nextInt(5) != 0)
            {
                return false;
            }

            owner = findOwner();
            if (owner == null)
            {
                return false;
            }
            return true;
        }
        public void start()
        {
            continueExecuting();
        }
        public void stop()
        {
            this.owner = null;
        }
        public boolean continueExecuting()
        {
            if (owner != null && owner.dimensionId == this.parentEntity.dimensionId)
                {
                    if (this.parentEntity.squaredDistanceTo(owner) > 50D && holds_yo_tear(owner))
                    {
                        Vec3d target = new Vec3d(this.owner.x - this.parentEntity.x, this.owner.y - this.parentEntity.y,this.owner.z - this.parentEntity.z).normalize();
                        this.parentEntity.getMoveControl().moveTo(this.parentEntity.x + target.x, this.parentEntity.y + target.y, this.parentEntity.z + target.z, 1.0D);
                        return true;
                    }
                }
            return false;
        }
    }
}
