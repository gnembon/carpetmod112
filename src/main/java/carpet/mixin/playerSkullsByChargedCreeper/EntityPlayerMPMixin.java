package carpet.mixin.playerSkullsByChargedCreeper;

import carpet.CarpetSettings;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerMP.class)
public abstract class EntityPlayerMPMixin extends EntityPlayer {
    public EntityPlayerMPMixin(World worldIn, GameProfile gameProfileIn) {
        super(worldIn, gameProfileIn);
    }

    // Allows players to drop their skulls when blown up by charged creeper CARPET-XCOM
    @Inject(method = "onDeath", at = @At("RETURN"))
    private void dropSkullByChargedCreeper(DamageSource cause, CallbackInfo ci) {
        if (!CarpetSettings.playerSkullsByChargedCreeper) return;
        Entity entity = cause.getTrueSource();
        if (!(entity instanceof EntityCreeper)) return;
        EntityCreeper creeper = (EntityCreeper) entity;
        if (!creeper.getPowered() || !creeper.ableToCauseSkullDrop()) return;
        creeper.incrementDroppedSkulls();
        try {
            ItemStack skull = new ItemStack(Items.SKULL, 1, 3);
            skull.setTagCompound(JsonToNBT.getTagFromJson(String.format("{SkullOwner:\"%s\"}", getName().toLowerCase())));
            this.entityDropItem(skull, 0.0F);
        } catch (NBTException e) {
            e.printStackTrace();
        }
    }
}
