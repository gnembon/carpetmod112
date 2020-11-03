package carpet.mixin.hopperCounters;

import carpet.patches.BlockWool;
import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.block.material.Material;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Block.class)
public class BlockMixin {
    @Redirect(method = "registerBlocks", at = @At(value = "NEW", target = "net/minecraft/block/BlockColored", ordinal = 0))
    private static BlockColored customWoolBlock(Material material) {
        return new BlockWool();
    }
}
