package carpet.mixin.breedingMountingDisabled;

import carpet.CarpetSettings;
import net.minecraft.entity.passive.AbstractChestHorse;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractChestHorse.class)
public class AbstractChestHorseMixin extends AbstractHorse {
    public AbstractChestHorseMixin(World worldIn) {
        super(worldIn);
    }

    @Redirect(method = "processInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/AbstractChestHorse;mountTo(Lnet/minecraft/entity/player/EntityPlayer;)V"))
    private void mountIfNotBreeding(AbstractChestHorse abstractChestHorse, EntityPlayer player, EntityPlayer playerAgain, EnumHand hand) {
        if (CarpetSettings.breedingMountingDisabled && this.isValidBreedingItem(playerAgain.getHeldItem(hand))) return;
        this.mountTo(player);
    }

    protected boolean isValidBreedingItem(ItemStack stack) {
        return true;
    }
}
