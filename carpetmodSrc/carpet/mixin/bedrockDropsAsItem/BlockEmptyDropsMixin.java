package carpet.mixin.bedrockDropsAsItem;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEmptyDrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(BlockEmptyDrops.class)
public class BlockEmptyDropsMixin {
    @Inject(method = "getItemDropped", at = @At("HEAD"), cancellable = true)
    private void dropAsItem(IBlockState state, Random rand, int fortune, CallbackInfoReturnable<Item> cir) {
        if (CarpetSettings.bedrockDropsAsItem) cir.setReturnValue(Item.getItemFromBlock((Block) (Object) this));
    }
}
