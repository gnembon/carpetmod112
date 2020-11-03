package carpet.mixin.renewableDragonEggs;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDragonEgg;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.LazyLoadBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;

@Mixin(BlockDragonEgg.class)
public abstract class BlockDragonEggMixin extends Block {
    @Shadow protected abstract void teleport(World worldIn, BlockPos pos);

    private static final Set<Item> FOOD_ITEMS = new HashSet<>();

    private static void initFoodItems() {
        FOOD_ITEMS.add(Items.ROTTEN_FLESH);
        FOOD_ITEMS.add(Items.BEEF);
        FOOD_ITEMS.add(Items.COOKED_BEEF);
        FOOD_ITEMS.add(Items.CHICKEN);
        FOOD_ITEMS.add(Items.COOKED_CHICKEN);
        FOOD_ITEMS.add(Items.FISH);
        FOOD_ITEMS.add(Items.COOKED_FISH);
        FOOD_ITEMS.add(Items.PORKCHOP);
        FOOD_ITEMS.add(Items.COOKED_PORKCHOP);
        FOOD_ITEMS.add(Items.RABBIT);
        FOOD_ITEMS.add(Items.COOKED_RABBIT);
        FOOD_ITEMS.add(Items.MUTTON);
        FOOD_ITEMS.add(Items.COOKED_MUTTON);
        FOOD_ITEMS.add(Items.SPIDER_EYE);
    }

    protected BlockDragonEggMixin(Material materialIn) {
        super(materialIn);
    }

    @Inject(method = "onBlockActivated", at = @At("HEAD"), cancellable = true)
    private void tryFeed(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ, CallbackInfoReturnable<Boolean> cir) {
        if (CarpetSettings.renewableDragonEggs) {
            ItemStack itemstack = playerIn.getHeldItem(hand);
            if (isMeat(itemstack.getItem())) {
                int saturation = (int) (((ItemFood) itemstack.getItem()).getSaturationModifier(itemstack) * 10);
                if (!playerIn.capabilities.isCreativeMode) {
                    itemstack.shrink(1);
                }
                for (int i = 0; i < saturation; i++) {
                    this.teleport(worldIn, pos);
                    worldIn.setBlockState(pos, this.getDefaultState(), 2);
                }
                cir.setReturnValue(true);
            }
        }
    }

    private boolean isMeat(Item food) {
        if (FOOD_ITEMS.isEmpty()) initFoodItems();
        return FOOD_ITEMS.contains(food);
    }
}
