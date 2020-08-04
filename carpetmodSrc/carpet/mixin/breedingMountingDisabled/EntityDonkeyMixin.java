package carpet.mixin.breedingMountingDisabled;

import net.minecraft.entity.passive.EntityDonkey;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityDonkey.class)
public class EntityDonkeyMixin extends AbstractChestHorseMixin {
    public EntityDonkeyMixin(World worldIn) {
        super(worldIn);
    }

    @Override
    protected boolean isValidBreedingItem(ItemStack itemstack) {
        Item item = itemstack.getItem();
        return item != Items.GOLDEN_CARROT && item != Items.GOLDEN_APPLE;
    }
}
