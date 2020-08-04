package carpet.mixin.chorusFruitShootable;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChorusFlower;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockChorusFlower.class)
public class BlockChorusFlowerMixin extends Block {

    protected BlockChorusFlowerMixin(Material materialIn) {
        super(materialIn);
    }

    @Override
    public void onEntityCollision(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
        if(!CarpetSettings.chorusFruitShootable || !(entityIn instanceof net.minecraft.entity.projectile.EntityArrow)) return;
        worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
        spawnAsEntity(worldIn, pos, new ItemStack(Item.getItemFromBlock(this)));
    }
}
