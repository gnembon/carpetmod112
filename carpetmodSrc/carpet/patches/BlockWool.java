package carpet.patches;

import carpet.CarpetSettings;
import carpet.carpetclient.CarpetClientChunkLogger;
import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class BlockWool extends BlockColored {

    private Map<EnumDyeColor, Set<Pair<Integer, BlockPos>>> woolBlocks = new EnumMap<>(EnumDyeColor.class);
    private EnumSet<EnumDyeColor> alreadyCheckedColors = EnumSet.noneOf(EnumDyeColor.class);
    private boolean updatingWool;
    private Set<Pair<Integer, BlockPos>> updatedBlocks = new HashSet<>();

    public BlockWool() {
        super(Material.CLOTH);
        for (EnumDyeColor color : EnumDyeColor.values())
            woolBlocks.put(color, new HashSet<>());
    }

    public void clearWirelessLocations() {
        woolBlocks.forEach((k, v) -> v.clear());
    }

    @Override
    public boolean canProvidePower(IBlockState state) {
        return CarpetSettings.wirelessRedstone;
    }

    private List<Pair<Integer, BlockPos>> getAllWoolOfType(MinecraftServer server, EnumDyeColor type) {
        List<Pair<Integer, BlockPos>> woolList = new ArrayList<>();

        Iterator<Pair<Integer, BlockPos>> locationItr = woolBlocks.get(type).iterator();
        while (locationItr.hasNext()) {
            Pair<Integer, BlockPos> location = locationItr.next();
            World world = server.getWorld(location.getLeft());
            CarpetClientChunkLogger.setReason("Carpet wireless redstone");
            IBlockState state = world.getBlockState(location.getRight());
            if (state.getBlock() != this || state.getValue(COLOR) != type) {
                locationItr.remove();
            } else {
                woolList.add(location);
            }
            CarpetClientChunkLogger.resetReason();
        }

        return woolList;
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!CarpetSettings.wirelessRedstone) return;

        // Adds this location if absent
        woolBlocks.get(state.getValue(COLOR)).add(Pair.of(worldIn.provider.getDimensionType().getId(), pos));

        boolean updateRoot = !updatingWool;
        updatingWool = true;

        if (!updatedBlocks.add(Pair.of(worldIn.provider.getDimensionType().getId(), pos)))
            return;

        for (Pair<Integer, BlockPos> wool : getAllWoolOfType(worldIn.getMinecraftServer(), state.getValue(COLOR))) {
            World world = worldIn.getMinecraftServer().getWorld(wool.getLeft());
            CarpetClientChunkLogger.setReason("Carpet wireless redstone");
            world.notifyNeighborsOfStateChange(wool.getRight(), this, false);
            CarpetClientChunkLogger.resetReason();
        }

        if (updateRoot) {
            updatingWool = false;
            updatedBlocks.clear();
        }
    }

    @Override
    public int getWeakPower(IBlockState state, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        if (!CarpetSettings.wirelessRedstone)
            return 0;

        World worldIn = (World) blockAccess;
        // Adds this location if absent
        woolBlocks.get(state.getValue(COLOR)).add(Pair.of(worldIn.provider.getDimensionType().getId(), pos));

        if (!alreadyCheckedColors.add(state.getValue(COLOR)))
            return 0;

        int power = 0;
        for (Pair<Integer, BlockPos> location : getAllWoolOfType(worldIn.getMinecraftServer(), state.getValue(COLOR))) {
            World world = worldIn.getMinecraftServer().getWorld(location.getLeft());
            CarpetClientChunkLogger.setReason("Carpet wireless redstone");
            for (EnumFacing facing : EnumFacing.values()) {
                BlockPos testPos = location.getRight().offset(facing);
                if (world.getBlockState(testPos) != state)
                    power = Math.max(power, world.getRedstonePower(testPos, facing));
            }
            CarpetClientChunkLogger.resetReason();
        }

        alreadyCheckedColors.clear();

        return power;
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        if (CarpetSettings.wirelessRedstone) {
            woolBlocks.get(state.getValue(COLOR)).add(Pair.of(worldIn.provider.getDimensionType().getId(), pos));
        }
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        if (CarpetSettings.wirelessRedstone) {
            woolBlocks.get(state.getValue(COLOR)).remove(Pair.of(worldIn.provider.getDimensionType().getId(), pos));
        }
    }
}
