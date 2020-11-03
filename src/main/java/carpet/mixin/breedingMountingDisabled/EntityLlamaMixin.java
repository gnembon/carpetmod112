package carpet.mixin.breedingMountingDisabled;

import net.minecraft.entity.passive.EntityLlama;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityLlama.class)
public class EntityLlamaMixin extends AbstractChestHorseMixin {
    public EntityLlamaMixin(World worldIn) {
        super(worldIn);
    }

    @Override
    protected boolean isValidBreedingItem(ItemStack itemstack) {
        Item item = itemstack.getItem();
        return item != Items.WHEAT && item != Item.getItemFromBlock(Blocks.HAY_BLOCK);
    }
}
