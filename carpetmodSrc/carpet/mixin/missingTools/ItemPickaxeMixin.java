package carpet.mixin.missingTools;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Set;

@Mixin(ItemPickaxe.class)
public class ItemPickaxeMixin extends ItemTool {
    protected ItemPickaxeMixin(float attackDamageIn, float attackSpeedIn, ToolMaterial materialIn, Set<Block> effectiveBlocksIn) {
        super(attackDamageIn, attackSpeedIn, materialIn, effectiveBlocksIn);
    }

    @Inject(method = "getDestroySpeed", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/block/state/IBlockState;getMaterial()Lnet/minecraft/block/material/Material;"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void missingTools(ItemStack stack, IBlockState state, CallbackInfoReturnable<Float> cir, Material material) {
        if (!CarpetSettings.missingTools) return;
        if (material == Material.PISTON || material == Material.GLASS) cir.setReturnValue(efficiency);
    }
}
