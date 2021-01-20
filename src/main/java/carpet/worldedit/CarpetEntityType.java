package carpet.worldedit;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.entity.metadata.EntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.EyeOfEnderEntity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.Npc;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.Animal;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.Projectile;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.village.Trader;

class CarpetEntityType implements EntityType {

    private final Entity entity;

    public CarpetEntityType(Entity entity) {
        checkNotNull(entity);
        this.entity = entity;
    }

    @Override
    public boolean isPlayerDerived() {
        return entity instanceof PlayerEntity;
    }

    @Override
    public boolean isProjectile() {
        return entity instanceof EyeOfEnderEntity || entity instanceof Projectile;
    }

    @Override
    public boolean isItem() {
        return entity instanceof ItemEntity;
    }

    @Override
    public boolean isFallingBlock() {
        return entity instanceof FallingBlockEntity;
    }

    @Override
    public boolean isPainting() {
        return entity instanceof PaintingEntity;
    }

    @Override
    public boolean isItemFrame() {
        return entity instanceof ItemFrameEntity;
    }

    @Override
    public boolean isBoat() {
        return entity instanceof BoatEntity;
    }

    @Override
    public boolean isMinecart() {
        return entity instanceof AbstractMinecartEntity;
    }

    @Override
    public boolean isTNT() {
        return entity instanceof TntEntity;
    }

    @Override
    public boolean isExperienceOrb() {
        return entity instanceof ExperienceOrbEntity;
    }

    @Override
    public boolean isLiving() {
        return entity instanceof MobEntity;
    }

    @Override
    public boolean isAnimal() {
        return entity instanceof Animal;
    }

    @Override
    public boolean isAmbient() {
        return entity instanceof AmbientEntity;
    }

    @Override
    public boolean isNPC() {
        return entity instanceof Npc || entity instanceof Trader;
    }

    @Override
    public boolean isGolem() {
        return entity instanceof GolemEntity;
    }

    @Override
    public boolean isTamed() {
        return entity instanceof TameableEntity && ((TameableEntity) entity).isTamed();
    }

    @Override
    public boolean isTagged() {
        return entity instanceof MobEntity && entity.hasCustomName();
    }

    @Override
    public boolean isArmorStand() {
        return entity instanceof ArmorStandEntity;
    }
}
