package carpet.patches;

import carpet.CarpetSettings;
import carpet.carpetclient.CarpetClientChunkLogger;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ColoredBlock;
import net.minecraft.block.Material;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class WoolBlock extends ColoredBlock {

    private Map<DyeColor, Set<Pair<Integer, BlockPos>>> woolBlocks = new EnumMap<>(DyeColor.class);
    private EnumSet<DyeColor> alreadyCheckedColors = EnumSet.noneOf(DyeColor.class);
    private boolean updatingWool;
    private Set<Pair<Integer, BlockPos>> updatedBlocks = new HashSet<>();

    public WoolBlock() {
        super(Material.WOOL);
        for (DyeColor color : DyeColor.values())
            woolBlocks.put(color, new HashSet<>());
    }

    public void clearWirelessLocations() {
        woolBlocks.forEach((k, v) -> v.clear());
    }

    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return CarpetSettings.wirelessRedstone;
    }

    private List<Pair<Integer, BlockPos>> getAllWoolOfType(MinecraftServer server, DyeColor type) {
        List<Pair<Integer, BlockPos>> woolList = new ArrayList<>();

        Iterator<Pair<Integer, BlockPos>> locationItr = woolBlocks.get(type).iterator();
        while (locationItr.hasNext()) {
            Pair<Integer, BlockPos> location = locationItr.next();
            World world = server.getWorldById(location.getLeft());
            CarpetClientChunkLogger.setReason("Carpet wireless redstone");
            BlockState state = world.getBlockState(location.getRight());
            if (state.getBlock() != this || state.get(field_24283) != type) {
                locationItr.remove();
            } else {
                woolList.add(location);
            }
            CarpetClientChunkLogger.resetReason();
        }

        return woolList;
    }

    @Override
    public void neighborUpdate(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!CarpetSettings.wirelessRedstone) return;

        // Adds this location if absent
        woolBlocks.get(state.get(field_24283)).add(Pair.of(worldIn.dimension.getType().getRawId(), pos));

        boolean updateRoot = !updatingWool;
        updatingWool = true;

        if (!updatedBlocks.add(Pair.of(worldIn.dimension.getType().getRawId(), pos)))
            return;

        for (Pair<Integer, BlockPos> wool : getAllWoolOfType(worldIn.getServer(), state.get(field_24283))) {
            World world = worldIn.getServer().getWorldById(wool.getLeft());
            CarpetClientChunkLogger.setReason("Carpet wireless redstone");
            world.updateNeighborsAlways(wool.getRight(), this, false);
            CarpetClientChunkLogger.resetReason();
        }

        if (updateRoot) {
            updatingWool = false;
            updatedBlocks.clear();
        }
    }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockEntityProvider blockAccess, BlockPos pos, Direction side) {
        if (!CarpetSettings.wirelessRedstone)
            return 0;

        World worldIn = (World) blockAccess;
        // Adds this location if absent
        woolBlocks.get(state.get(field_24283)).add(Pair.of(worldIn.dimension.getType().getRawId(), pos));

        if (!alreadyCheckedColors.add(state.get(field_24283)))
            return 0;

        int power = 0;
        for (Pair<Integer, BlockPos> location : getAllWoolOfType(worldIn.getServer(), state.get(field_24283))) {
            World world = worldIn.getServer().getWorldById(location.getLeft());
            CarpetClientChunkLogger.setReason("Carpet wireless redstone");
            for (Direction facing : Direction.values()) {
                BlockPos testPos = location.getRight().offset(facing);
                if (world.getBlockState(testPos) != state)
                    power = Math.max(power, world.getEmittedRedstonePower(testPos, facing));
            }
            CarpetClientChunkLogger.resetReason();
        }

        alreadyCheckedColors.clear();

        return power;
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, BlockState state) {
        if (CarpetSettings.wirelessRedstone) {
            woolBlocks.get(state.get(field_24283)).add(Pair.of(worldIn.dimension.getType().getRawId(), pos));
        }
    }

    @Override
    public void onBlockRemoved(World worldIn, BlockPos pos, BlockState state) {
        if (CarpetSettings.wirelessRedstone) {
            woolBlocks.get(state.get(field_24283)).remove(Pair.of(worldIn.dimension.getType().getRawId(), pos));
        }
    }
}
