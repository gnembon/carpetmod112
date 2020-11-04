package carpet.mixin.disableBedrockPlacement;

import carpet.CarpetSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityPlayer.class)
public class EntityPlayerMixin {
    @Shadow public PlayerCapabilities capabilities;

    @Inject(method = "canPlayerEdit", at = @At("HEAD"), cancellable = true)
    private void disableBedrockPlacement(BlockPos pos, EnumFacing facing, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!CarpetSettings.disableBedrockPlacement) return;
        if (stack.getItem() == Item.getItemFromBlock(Blocks.BEDROCK) && !this.capabilities.isCreativeMode) {
            cir.cancel();
        }
    }
}
