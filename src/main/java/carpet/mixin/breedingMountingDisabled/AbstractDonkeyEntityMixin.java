package carpet.mixin.breedingMountingDisabled;

import carpet.CarpetSettings;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractDonkeyEntity.class)
public class AbstractDonkeyEntityMixin extends HorseBaseEntity {
    public AbstractDonkeyEntityMixin(World worldIn) {
        super(worldIn);
    }

    @Redirect(method = "interactMob", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/AbstractDonkeyEntity;putPlayerOnBack(Lnet/minecraft/entity/player/PlayerEntity;)V"))
    private void mountIfNotBreeding(AbstractDonkeyEntity abstractChestHorse, PlayerEntity player, PlayerEntity playerAgain, Hand hand) {
        if (CarpetSettings.breedingMountingDisabled && this.isValidBreedingItem(playerAgain.getStackInHand(hand))) return;
        this.putPlayerOnBack(player);
    }

    protected boolean isValidBreedingItem(ItemStack stack) {
        return true;
    }
}
