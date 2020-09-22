package carpet.mixin.autoCraftingTable;

import carpet.CarpetSettings;
import carpet.helpers.TileEntityCraftingTable;
import carpet.mixin.accessors.InventoryCraftingAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockWorkbench;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import javax.annotation.Nullable;

@Mixin(BlockWorkbench.class)
public class BlockWorkbenchMixin extends Block implements ITileEntityProvider {
    protected BlockWorkbenchMixin(Material materialIn) {
        super(materialIn);
    }

    @Redirect(method = "onBlockActivated", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;displayGui(Lnet/minecraft/world/IInteractionObject;)V"))
    private void displayGui(EntityPlayer player, IInteractionObject gui, World world, BlockPos pos) {
        TileEntityCraftingTable te = getTileEntity(world, pos);
        player.displayGui(te != null ? te : gui);
    }

    @Override
    public boolean hasTileEntity() {
        return CarpetSettings.autocrafter;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityCraftingTable();
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {
        TileEntityCraftingTable te = getTileEntity(world, pos);
        if (te == null) return 0;
        int count = 0;
        for (ItemStack stack : ((InventoryCraftingAccessor) te.inventory).getStackList()) {
            if (!stack.isEmpty()) count++;
        }
        return (count * 15) / 9;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if (!hasTileEntity()) return;
        // Maybe also check for some carpet rule
        TileEntityCraftingTable tileEntity = getTileEntity(world, pos);
        if (tileEntity != null) {
            tileEntity.dropContent(world, pos);
            world.updateComparatorOutputLevel(pos, this);
        }
        world.removeTileEntity(pos);
        super.breakBlock(world, pos, state);
    }

    /**
     * Tries to get the crafting table's block entity when auto crafting is enabled, otherwise null
     * @param world The world
     * @param pos The position of the crafting table
     * @return the block entity or null if auto crafting is disabled or none was found
     */
    @Nullable
    private TileEntityCraftingTable getTileEntity(World world, BlockPos pos) {
        if (!hasTileEntity()) return null;
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityCraftingTable) return (TileEntityCraftingTable) te;
        return null;
    }
}
