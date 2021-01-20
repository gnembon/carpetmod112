package carpet.mixin.renewableDragonEggs;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DragonEggBlock;
import net.minecraft.block.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;

@Mixin(DragonEggBlock.class)
public abstract class DragonEggBlockMixin extends Block {
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

    protected DragonEggBlockMixin(Material materialIn) {
        super(materialIn);
    }

    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    private void tryFeed(World worldIn, BlockPos pos, BlockState state, PlayerEntity playerIn, Hand hand, Direction facing, float hitX, float hitY, float hitZ, CallbackInfoReturnable<Boolean> cir) {
        if (CarpetSettings.renewableDragonEggs) {
            ItemStack itemstack = playerIn.getStackInHand(hand);
            if (isMeat(itemstack.getItem())) {
                int saturation = (int) (((FoodItem) itemstack.getItem()).getSaturationModifier(itemstack) * 10);
                if (!playerIn.abilities.creativeMode) {
                    itemstack.decrement(1);
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
