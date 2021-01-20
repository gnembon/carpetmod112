package carpet.mixin.disableBedrockPlacement;

import carpet.CarpetSettings;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    @Shadow public PlayerAbilities abilities;

    @Inject(method = "canPlaceOn", at = @At("HEAD"), cancellable = true)
    private void disableBedrockPlacement(BlockPos pos, Direction facing, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!CarpetSettings.disableBedrockPlacement) return;
        if (stack.getItem() == Item.fromBlock(Blocks.BEDROCK) && !this.abilities.creativeMode) {
            cir.cancel();
        }
    }
}
