package carpet.worldedit;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.entity.metadata.EntityType;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.NullWorld;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;

import static com.google.common.base.Preconditions.checkNotNull;

class CarpetEntity implements Entity {

    private final WeakReference<net.minecraft.entity.Entity> entityRef;

    CarpetEntity(net.minecraft.entity.Entity entity) {
        checkNotNull(entity);
        this.entityRef = new WeakReference<net.minecraft.entity.Entity>(entity);
    }

    @Override
    public BaseEntity getState() {
        net.minecraft.entity.Entity entity = entityRef.get();
        if (entity != null) {
            String id = EntityList.getEntityString(entity);
            if (id != null) {
                NBTTagCompound tag = new NBTTagCompound();
                entity.writeToNBT(tag);
                return new BaseEntity(id, NBTConverter.fromNative(tag));
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public Location getLocation() {
        net.minecraft.entity.Entity entity = entityRef.get();
        if (entity != null) {
            Vector position = new Vector(entity.posX, entity.posY, entity.posZ);
            float yaw = entity.rotationYaw;
            float pitch = entity.rotationPitch;

            return new Location(CarpetAdapter.adapt(entity.world), position, yaw, pitch);
        } else {
            return new Location(NullWorld.getInstance());
        }
    }

    @Override
    public Extent getExtent() {
        net.minecraft.entity.Entity entity = entityRef.get();
        if (entity != null) {
            return CarpetAdapter.adapt(entity.world);
        } else {
            return NullWorld.getInstance();
        }
    }

    @Override
    public boolean remove() {
        net.minecraft.entity.Entity entity = entityRef.get();
        if (entity != null) {
            entity.setDead();
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getFacet(Class<? extends T> cls) {
        net.minecraft.entity.Entity entity = entityRef.get();
        if (entity != null) {
            if (EntityType.class.isAssignableFrom(cls)) {
                return (T) new CarpetEntityType(entity);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
