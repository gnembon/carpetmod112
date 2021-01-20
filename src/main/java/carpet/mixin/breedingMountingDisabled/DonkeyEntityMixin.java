package carpet.mixin.breedingMountingDisabled;

import net.minecraft.entity.passive.DonkeyEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DonkeyEntity.class)
public class DonkeyEntityMixin extends AbstractDonkeyEntityMixin {
    public DonkeyEntityMixin(World worldIn) {
        super(worldIn);
    }

    @Override
    protected boolean isValidBreedingItem(ItemStack itemstack) {
        Item item = itemstack.getItem();
        return item != Items.GOLDEN_CARROT && item != Items.GOLDEN_APPLE;
    }
}
