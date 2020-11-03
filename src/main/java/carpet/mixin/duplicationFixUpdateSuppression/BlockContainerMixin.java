package carpet.mixin.duplicationFixUpdateSuppression;

import carpet.CarpetSettings;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BlockContainer.class)
public class BlockContainerMixin {
    @Inject(method = "harvestBlock", at = @At(value = "NEW", target = "net/minecraft/item/ItemStack"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void fixShulkerBox(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack stack, CallbackInfo ci, int fortune, Item item) {
        // Remove ability to drop shulker boxes given the set block to air already does it. This causes duplication with duplicationFixUpdateSuppression.
        // In vanilla this behavior never triggers CARPET-XCOM
        if(CarpetSettings.duplicationFixUpdateSuppression && item instanceof ItemShulkerBox) ci.cancel();
    }
}
