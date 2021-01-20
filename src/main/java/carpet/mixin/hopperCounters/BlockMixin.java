package carpet.mixin.hopperCounters;

import carpet.patches.WoolBlock;
import net.minecraft.block.Block;
import net.minecraft.block.ColoredBlock;
import net.minecraft.block.Material;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Block.class)
public class BlockMixin {
    @Redirect(method = "register()V", at = @At(value = "NEW", target = "net/minecraft/block/ColoredBlock", ordinal = 0))
    private static ColoredBlock customWoolBlock(Material material) {
        return new WoolBlock();
    }
}
