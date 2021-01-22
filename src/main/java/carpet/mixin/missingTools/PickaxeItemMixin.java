package carpet.mixin.missingTools;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.PickaxeItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Set;

@Mixin(PickaxeItem.class)
public class PickaxeItemMixin extends MiningToolItem {
    protected PickaxeItemMixin(float attackDamageIn, float attackSpeedIn, ToolMaterial materialIn, Set<Block> effectiveBlocksIn) {
        super(attackDamageIn, attackSpeedIn, materialIn, effectiveBlocksIn);
    }

    @Inject(method = "getMiningSpeedMultiplier", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/block/BlockState;getMaterial()Lnet/minecraft/block/Material;"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void missingTools(ItemStack stack, BlockState state, CallbackInfoReturnable<Float> cir, Material material) {
        if (!CarpetSettings.missingTools) return;
        if (material == Material.PISTON || material == Material.GLASS) cir.setReturnValue(field_22942);
    }
}
