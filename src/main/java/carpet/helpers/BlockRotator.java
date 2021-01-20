package carpet.helpers;

import carpet.CarpetSettings;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class BlockRotator
{
	public static boolean flipBlockWithCactus(World worldIn, BlockPos pos, BlockState state, PlayerEntity playerIn, Hand hand, Direction facing, float hitX, float hitY, float hitZ)
    {
        if (!playerIn.abilities.allowModifyWorld || !CarpetSettings.flippinCactus || !player_holds_cactus_mainhand(playerIn))
        {
            return false;
        }
        return flip_block(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }
    public static BlockState placeBlockWithCactus(Block block, World worldIn, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer)
    {
        if (block instanceof ObserverBlock)
        {
            return block.getDefaultState()
                .with(FacingBlock.FACING, Direction.byId((int)hitX - 2))
                .with(ObserverBlock.field_24607, CarpetSettings.observersDoNonUpdate);
        }
        return null;
    }

    public static BlockState alternativeBlockPlacement(Block block, World worldIn, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer)
    {
        //actual alternative block placement code
        //
        if (block instanceof GlazedTerracottaBlock)
        {
            facing = Direction.byId((int)hitX - 2);
            if(facing == Direction.UP || facing == Direction.DOWN)
            {
                facing = placer.getHorizontalFacing().getOpposite();
            }
            return block.getDefaultState().with(HorizontalFacingBlock.field_24496, facing);
        }
        else if (block instanceof ObserverBlock)
        {
            return block.getDefaultState()
                    .with(FacingBlock.FACING, Direction.byId((int)hitX - 2))
                    .with(ObserverBlock.field_24607, CarpetSettings.observersDoNonUpdate);
        }
        else if (block instanceof RepeaterBlock)
        {
            facing = Direction.byId((((int)hitX) % 10) - 2);
            if(facing == Direction.UP || facing == Direction.DOWN)
            {
                facing = placer.getHorizontalFacing().getOpposite();
            }
            return block.getDefaultState()
                    .with(HorizontalFacingBlock.field_24496, facing)
                    .with(RepeaterBlock.field_24733, MathHelper.clamp((((int) hitX) / 10) + 1, 1, 4))
                    .with(RepeaterBlock.field_24732, Boolean.FALSE);
        }
        else if (block instanceof TrapdoorBlock)
        {
            return block.getDefaultState()
                    .with(TrapdoorBlock.FACING, Direction.byId((((int)hitX) % 10) - 2))
                    .with(TrapdoorBlock.OPEN, Boolean.FALSE)
                    .with(TrapdoorBlock.field_24932, (hitX > 10) ? TrapdoorBlock.class_5272.field_24938 : TrapdoorBlock.class_5272.field_24939)
                    .with(TrapdoorBlock.OPEN, worldIn.isReceivingRedstonePower(pos));
        }
        else if (block instanceof ComparatorBlock)
        {
            facing = Direction.byId((((int)hitX) % 10) - 2);
            if((facing == Direction.UP) || (facing == Direction.DOWN))
            {
                facing = placer.getHorizontalFacing().getOpposite();
            }
            ComparatorBlock.ComparatorMode m = (hitX > 10)?ComparatorBlock.ComparatorMode.SUBTRACT: ComparatorBlock.ComparatorMode.COMPARE;
            return block.getDefaultState()
                    .with(HorizontalFacingBlock.field_24496, facing)
                    .with(ComparatorBlock.field_24287, Boolean.FALSE)
                    .with(ComparatorBlock.field_24288, m);
        }
        else if (block instanceof DispenserBlock)
        {
            return block.getDefaultState()
                    .with(DispenserBlock.FACING, Direction.byId((int)hitX - 2))
                    .with(DispenserBlock.field_24324, Boolean.FALSE);
        }
        else if (block instanceof PistonBlock)
        {
            return block.getDefaultState()
                    .with(FacingBlock.FACING,Direction.byId((int)hitX - 2) )
                    .with(PistonBlock.field_25225, Boolean.FALSE);
        }
        else if (block instanceof StairsBlock)
        {
            return block.getPlacementState(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer)
                    .with(StairsBlock.FACING, Direction.byId((((int)hitX) % 10) - 2))
                    .with(StairsBlock.field_24830, ( hitX > 10)?StairsBlock.BlockHalf.TOP : StairsBlock.BlockHalf.BOTTOM);
        }
        else if (block instanceof FenceGateBlock)
        {
            return block.getPlacementState(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer)
                    .with(FenceGateBlock.field_24496, Direction.byId((((int)hitX) % 10) - 2))
                    .with(FenceGateBlock.field_24402, hitX > 10);
        }
        else if (block instanceof PumpkinBlock)
        {
            return block.getPlacementState(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer)
                    .with(FenceGateBlock.field_24496, Direction.byId((((int)hitX) % 10) - 2));
        }
        else if (block instanceof ChestBlock)
        {
            return block.getPlacementState(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer)
                    .with(FenceGateBlock.field_24496, Direction.byId((((int)hitX) % 10) - 2));
        }
        else if (block instanceof EnderChestBlock)
        {
            return block.getPlacementState(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer)
                    .with(FenceGateBlock.field_24496, Direction.byId((((int)hitX) % 10) - 2));
        }
        else if (block instanceof DoorBlock)
        {
            return block.getPlacementState(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer)
                    .with(DoorBlock.FACING, Direction.byId((((int)hitX) % 10) - 2))
                    .with(DoorBlock.field_24338, hitX % 100 < 10 ? DoorBlock.DoorHinge.LEFT : DoorBlock.DoorHinge.RIGHT)
                    .with(DoorBlock.field_24337, hitX > 100);
        }
        return null;
    }




    public static boolean flip_block(World worldIn, BlockPos pos, BlockState state, PlayerEntity playerIn, Hand hand, Direction facing, float hitX, float hitY, float hitZ)
    {
        Block block = state.getBlock();
        if ( (block instanceof GlazedTerracottaBlock) || (block instanceof AbstractRedstoneGateBlock) || (block instanceof AbstractRailBlock) ||
             (block instanceof TrapdoorBlock)         || (block instanceof LeverBlock)         || (block instanceof FenceGateBlock))
        {
            worldIn.setBlockState(pos, block.rotate(state, BlockRotation.CLOCKWISE_90), 130);
        }
        else if ((block instanceof ObserverBlock) || (block instanceof EndRodBlock))
        {
            worldIn.setBlockState(pos, state.with(FacingBlock.FACING, state.get(FacingBlock.FACING).getOpposite()), 130);
        }
        else if (block instanceof DispenserBlock)
        {
            worldIn.setBlockState(pos, state.with(DispenserBlock.FACING, state.get(DispenserBlock.FACING).getOpposite()), 130);
        }
        else if (block instanceof PistonBlock)
        {
            if (!state.get(PistonBlock.field_25225))
                worldIn.setBlockState(pos, state.with(FacingBlock.FACING, state.get(FacingBlock.FACING).getOpposite()), 130);
        }
        else if (block instanceof SlabBlock)
        {
            if (!((SlabBlock) block).method_26650())
            {
                if (state.get(SlabBlock.field_24481) == SlabBlock.class_5189.field_24484)
                {
                    worldIn.setBlockState(pos, state.with(SlabBlock.field_24481, SlabBlock.class_5189.field_24485), 130);
                }
                else
                {
                    worldIn.setBlockState(pos, state.with(SlabBlock.field_24481, SlabBlock.class_5189.field_24484), 130);
                }
            }
        }
        else if (block instanceof HopperBlock)
        {
            if (state.get(HopperBlock.field_24489) != Direction.DOWN)
            {
                worldIn.setBlockState(pos, state.with(HopperBlock.field_24489, state.get(HopperBlock.field_24489).rotateYClockwise()), 130);
            }
        }
        else if (block instanceof StairsBlock)
        {
            //LOG.error(String.format("hit with facing: %s, at side %.1fX, X %.1fY, Y %.1fZ",facing, hitX, hitY, hitZ));
            if ((facing == Direction.UP && hitY == 1.0f) || (facing == Direction.DOWN && hitY == 0.0f))
            {
                if (state.get(StairsBlock.field_24830) == StairsBlock.BlockHalf.TOP)
                {
                    worldIn.setBlockState(pos, state.with(StairsBlock.field_24830, StairsBlock.BlockHalf.BOTTOM), 130);
                }
                else
                {
                    worldIn.setBlockState(pos, state.with(StairsBlock.field_24830, StairsBlock.BlockHalf.TOP), 130);
                }
            }
            else
            {
                boolean turn_right = true;
                if (facing == Direction.NORTH)
                {
                    turn_right = (hitX <= 0.5);
                }
                else if (facing == Direction.SOUTH)
                {
                    turn_right = !(hitX <= 0.5);
                }
                else if (facing == Direction.EAST)
                {
                    turn_right = (hitZ <= 0.5);
                }
                else if (facing == Direction.WEST)
                {
                    turn_right = !(hitZ <= 0.5);
                }
                else
                {
                    return false;
                }
                if (turn_right)
                {
                    worldIn.setBlockState(pos, block.rotate(state, BlockRotation.COUNTERCLOCKWISE_90), 130);
                }
                else
                {
                    worldIn.setBlockState(pos, block.rotate(state, BlockRotation.CLOCKWISE_90), 130);
                }
            }
        }
        else
        {
            return false;
        }
        worldIn.method_26081(pos, pos);
        return true;
    }
    private static boolean player_holds_cactus_mainhand(PlayerEntity playerIn)
    {
        return (!playerIn.getMainHandStack().isEmpty()
                && playerIn.getMainHandStack().getItem() instanceof BlockItem &&
                ((BlockItem) (playerIn.getMainHandStack().getItem())).getBlock() == Blocks.CACTUS);
    }
    public static boolean flippinEligibility(Entity entity)
    {
        if (CarpetSettings.flippinCactus
                && (entity instanceof PlayerEntity))
        {
            PlayerEntity player = (PlayerEntity)entity;
            return (!player.getOffHandStack().isEmpty()
                    && player.getOffHandStack().getItem() instanceof BlockItem &&
                    ((BlockItem) (player.getOffHandStack().getItem())).getBlock() == Blocks.CACTUS);
        }
        return false;
    }
}
