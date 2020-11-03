package carpet.worldedit;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.entity.metadata.EntityType;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.INpc;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityEnderEye;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;

class CarpetEntityType implements EntityType {

    private final Entity entity;

    public CarpetEntityType(Entity entity) {
        checkNotNull(entity);
        this.entity = entity;
    }

    @Override
    public boolean isPlayerDerived() {
        return entity instanceof EntityPlayer;
    }

    @Override
    public boolean isProjectile() {
        return entity instanceof EntityEnderEye || entity instanceof IProjectile;
    }

    @Override
    public boolean isItem() {
        return entity instanceof EntityItem;
    }

    @Override
    public boolean isFallingBlock() {
        return entity instanceof EntityFallingBlock;
    }

    @Override
    public boolean isPainting() {
        return entity instanceof EntityPainting;
    }

    @Override
    public boolean isItemFrame() {
        return entity instanceof EntityItemFrame;
    }

    @Override
    public boolean isBoat() {
        return entity instanceof EntityBoat;
    }

    @Override
    public boolean isMinecart() {
        return entity instanceof EntityMinecart;
    }

    @Override
    public boolean isTNT() {
        return entity instanceof EntityTNTPrimed;
    }

    @Override
    public boolean isExperienceOrb() {
        return entity instanceof EntityXPOrb;
    }

    @Override
    public boolean isLiving() {
        return entity instanceof EntityLiving;
    }

    @Override
    public boolean isAnimal() {
        return entity instanceof IAnimals;
    }

    @Override
    public boolean isAmbient() {
        return entity instanceof EntityAmbientCreature;
    }

    @Override
    public boolean isNPC() {
        return entity instanceof INpc || entity instanceof IMerchant;
    }

    @Override
    public boolean isGolem() {
        return entity instanceof EntityGolem;
    }

    @Override
    public boolean isTamed() {
        return entity instanceof EntityTameable && ((EntityTameable) entity).isTamed();
    }

    @Override
    public boolean isTagged() {
        return entity instanceof EntityLiving && ((EntityLiving) entity).hasCustomName();
    }

    @Override
    public boolean isArmorStand() {
        return entity instanceof EntityArmorStand;
    }
}
