package carpet.mixin.noteBlockImitationOf1_13;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.NoteBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoteBlock.class)
public class NoteBlockMixin {
    private int previousInstrument;

    @Inject(method = "neighborUpdate", at = @At(value = "RETURN"))
    private void onInstrumentChange(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, CallbackInfo ci) {
        int instrument = getInstrumentId(world.getBlockState(pos.method_31898()));
        if (previousInstrument != instrument) {
            previousInstrument = instrument;
            // Instrument change updates only observers
            if (CarpetSettings.noteBlockImitationOf1_13) world.method_26099(pos, block);
        }
    }

    @Inject(method = "onUse", at = @At(value = "RETURN", ordinal = 1))
    private void onPitchChange(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction facing, float hitX, float hitY, float hitZ, CallbackInfoReturnable<Boolean> cir) {
        // Right click sends block updates and updates observers
        if(CarpetSettings.noteBlockImitationOf1_13) world.updateNeighborsAlways(pos, (NoteBlock) (Object) this, true);
    }

    @Inject(method = "neighborUpdate", at = @At(value = "FIELD", target = "Lnet/minecraft/class_5287;field_25152:Z"))
    private void onPowerChange(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, CallbackInfo ci) {
        // Dual-edge redstone power change sends block updates and updates observers
        if(CarpetSettings.noteBlockImitationOf1_13) world.updateNeighborsAlways(pos, (NoteBlock) (Object) this, true);
    }

    private static int getInstrumentId(BlockState state) {
        Material material = state.getMaterial();
        if (material == Material.STONE) return 1;
        if (material == Material.AGGREGATE) return 2;
        if (material == Material.GLASS) return 3;
        if (material == Material.WOOD) return 4;
        Block block = state.getBlock();
        if (block == Blocks.CLAY) return 5;
        if (block == Blocks.GOLD_BLOCK) return 6;
        if (block == Blocks.WOOL) return 7;
        if (block == Blocks.PACKED_ICE) return 8;
        if (block == Blocks.BONE_BLOCK) return 9;
        return 0;
    }
}
