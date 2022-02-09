package carpet.commands;

import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BitArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.chunk.*;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import javax.annotation.Nullable;
import java.util.*;

public class CommandPalette extends CommandCarpetBase {
    /**
     * Gets the name of the command
     */

    public String getUsage(ICommandSender sender) {
        return "Usage: palette <bits | fill | size | posInfo> <X> <Y> <Z> <full | normal> <4-8 | 13>";
    }

    public String getName() {
        return "palette";
    }

    /**
     * Callback for when the command is executed
     */
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!command_enabled("commandChunk", sender)) return;

        try {
            BlockPos pos = new BlockPos(sender.getPosition().getX(), sender.getPosition().getY(), sender.getPosition().getZ());
            if (args.length < 4 && args[0].equals("posInfo")) {
                throw new WrongUsageException(getUsage(sender));
            } else if (args.length >= 4) {
                pos = parseBlockPos(sender, args, 1, false);
            }
            World world = sender.getEntityWorld();
            Chunk chunk = world.getChunk(pos);
            ExtendedBlockStorage[] list = chunk.getBlockStorageArray();
            int h = pos.getY() >> 4;
            if (h < 0) h = 0;
            if (h > 15) h = 15;
            ExtendedBlockStorage ebs = list[h];
            BlockStateContainer bsc = ebs.getBlockStateContainer();
            int bits = bsc.getBits();

            switch (args[0]) {
                case "bits":
                    sender.sendMessage(new TextComponentString("Palette bit size: " + bits));
                    return;
                case "size":
                    getSize(sender, bsc);
                    return;
                case "posInfo":
                    boolean isFull = false;
                    if (args.length >= 5) isFull = args[4].equals("full");
                    Block block = null;
                    if (args.length >= 6) block = CommandBase.getBlockByText(sender, args[5]);
                    IBlockState iblockstate = null;
                    if (args.length >= 7 && block != null) {
                        iblockstate = convertArgToBlockState(block, args[6]);
                    } else if (block != null) {
                        iblockstate = block.getDefaultState();
                    }
                    infoPalette(sender, bsc, pos, isFull, iblockstate);
                    return;
                case "fill":
                    int bitSize = -1;
                    int type = 1;
                    if (args.length >= 5) type = args[4].equals("full") ? 2 : 1;
                    if (args.length >= 6) bitSize = parseInt(args[5]);
                    fill(sender, bsc, pos, type, bitSize);
                    return;
                default:
                    throw new WrongUsageException(getUsage(sender));

            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new WrongUsageException(getUsage(sender));
        }
    }

    private static IBlockState[] backup = null;
    private static HashMap<BlockPos, TileEntity> tileEntityList = new HashMap<>();

    private static void fill(ICommandSender sender, BlockStateContainer bsc, BlockPos pos, int type, int bitSize) {
        if (type != 3 && backup != null) type = 3;

        if (bitSize < 1 || bitSize > 64) bitSize = bsc.getStorage().getBitsPerEntry();

        BlockPos basePos = new BlockPos(pos.getX() >>> 4 << 4, pos.getY() >>> 4 << 4, pos.getZ() >>> 4 << 4);
        int color = -1;
        int storeJ = -1;
        if (type != 3) {
            backup = new IBlockState[4096];
        }
        HashSet<BlockPos> backupSet = new HashSet<>();
        for (int i = 0; i < 4096; i++) {
            BlockPos set = getBlockIndex(i, basePos);
            if (type == 1) {
                int j = i * bitSize / 64;
                int k = ((i + 1) * bitSize - 1) / 64;

                if (j != k) {
                    backup[i] = sender.getEntityWorld().getBlockState(set);
                    TileEntity te = sender.getEntityWorld().getTileEntity(set);
                    if (te != null) {
                        tileEntityList.put(set, te);
                        sender.getEntityWorld().removeTileEntity(set);
                    }
                    sender.getEntityWorld().setBlockState(set, Blocks.GLASS.getDefaultState(), 128);
                }
            } else if (type == 2) {
                backup[i] = sender.getEntityWorld().getBlockState(set);
                TileEntity te = sender.getEntityWorld().getTileEntity(set);
                if (te != null) {
                    tileEntityList.put(set, te);
                    sender.getEntityWorld().removeTileEntity(set);
                }
                set = getBlockIndex(i, basePos);
                int j = i * bitSize / 64;
                int k = ((i + 1) * bitSize - 1) / 64;

                if (j != storeJ) {
                    storeJ = j;
                    color = (color + 1) & 15;
                }

                if (j != k) {
                    sender.getEntityWorld().setBlockState(set, Blocks.GLASS.getDefaultState(), 128);
                } else {
                    sender.getEntityWorld().setBlockState(set, Blocks.STAINED_GLASS.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.byMetadata(color)), 128);
                }
            } else if (type == 3) {
                if (backup[i] != null && !backupSet.contains(set)) {
                    backupSet.add(set);
                    sender.getEntityWorld().setBlockState(set, backup[i], 128);
                    TileEntity te = tileEntityList.get(set);
                    if (te != null) {
                        sender.getEntityWorld().removeTileEntity(set);
                        te.validate();
                        sender.getEntityWorld().setTileEntity(set, te);
                    }
                }
            }
        }
        if (type == 3) {
            backup = null;
            tileEntityList.clear();
        }
    }

    private void infoPalette(ICommandSender sender, BlockStateContainer bsc, BlockPos pos, boolean full, IBlockState blockState) {
        BitArray bArray = bsc.getStorage();
        int bits = bArray.getBitsPerEntry();
        int index = getIndex(pos);
        int i = index * bits;
        int j = i / 64;
        int k = ((index + 1) * bits - 1) / 64;
        int l = i % 64;
        long[] longArray = bArray.getBackingLongArray();

        if (j == k) {
            displayJKBits(sender, longArray[j], l, l + bits - 1, "");
            if (full) {
                for (BlockPos bp : getArrayFromJK(j, k, bits, pos)) {
                    sender.sendMessage(new TextComponentString(bp.toString()));
                }
            }
        } else {
            displayJKBits(sender, longArray[j], l, 64, "1");
            displayJKBits(sender, longArray[k], 0, (l + bits - 1) % 64, "2");
            if (full) {
                for (BlockPos bp : getArrayFromJK(j, k, bits, pos)) {
                    sender.sendMessage(new TextComponentString(bp.toString()));
                }
            }
        }
        if (blockState != null && bsc.getPalette() instanceof BlockStatePaletteRegistry && j != k) {
            int blockStateBits = Block.BLOCK_STATE_IDS.get(blockState);
            int leftBits = 64 - l;
            int rightBits = bits - leftBits;
            int leftMask = (1 << leftBits) - 1;
            int rightMask = ((1 << rightBits) - 1) << leftBits;
            int blockStateMaskL = blockStateBits & leftMask;
            int blockStateMaskR = blockStateBits & rightMask;
            sender.sendMessage(new TextComponentString("Left bit match:"));
            for (int itr = 0; itr < Block.BLOCK_STATE_IDS.size(); itr++) {
                IBlockState ibs = Block.BLOCK_STATE_IDS.getByValue(itr);
                if (ibs != null) {
                    int left = itr & leftMask;
                    if (left == blockStateMaskL) {
                        String s = String.format("%" + bits + "s", Integer.toBinaryString(itr)).replace(' ', '0') + " " + ibs.toString().replace("minecraft:", "");
                        sender.sendMessage(new TextComponentString(s));
                    }
                }
            }
            sender.sendMessage(new TextComponentString("Right bit match:"));
            for (int itr = 0; itr < Block.BLOCK_STATE_IDS.size(); itr++) {
                IBlockState ibs = Block.BLOCK_STATE_IDS.getByValue(itr);
                if (ibs != null) {
                    int right = itr & rightMask;
                    if (right == blockStateMaskR) {
                        String s = String.format("%" + bits + "s", Integer.toBinaryString(itr)).replace(' ', '0') + " " + ibs.toString().replace("minecraft:", "");
                        sender.sendMessage(new TextComponentString(s));
                    }
                }
            }
        } else if (blockState != null && j != k) {
            sender.sendMessage(new TextComponentString("This location doesn't share two bit arrays."));
        } else if (blockState != null && bsc.getPalette() instanceof BlockStatePaletteRegistry) {
            sender.sendMessage(new TextComponentString("This subchunk doesn't have enough palettes, add more palettes."));
        }
    }

    private static void displayJKBits(ICommandSender sender, long longString, long l1, long l2, String append) {
        StringBuilder sb = new StringBuilder();

        String add = "§f";
        for (int bitNum = 0; bitNum < 64; bitNum++) {
            char s = (longString & 1) == 1 ? '1' : '0';
            longString = longString >> 1;
            if (bitNum == l1) add = "§c";
            sb.append(add + s);
            if (bitNum == l2) add = "§f";
        }
        sender.sendMessage(new TextComponentString("§8L" + append + ":" + sb));
    }

    private static BlockPos[] getArrayFromJK(int j, int k, int bits, BlockPos pos) {
        BlockPos basePos = new BlockPos(pos.getX() >>> 4 << 4, pos.getY() >>> 4 << 4, pos.getZ() >>> 4 << 4);
        ArrayList<BlockPos> list = new ArrayList<>();
        for (int index = 0; index < 4096; index++) {
            int i = index * bits;
            int jj = i / 64;
            int kk = ((index + 1) * bits - 1) / 64;
            if (jj == j || kk == k || jj == k || kk == j) {
                list.add(getBlockIndex(index, basePos));
            }
        }
        return list.toArray(new BlockPos[0]);
    }

    private static int getIndex(BlockPos pos) {
        int x = pos.getX() & 15;
        int y = pos.getY() & 15;
        int z = pos.getZ() & 15;

        return y << 8 | z << 4 | x;
    }

    private static BlockPos getBlockIndex(int index, BlockPos pos) {
        int x = (pos.getX() & ~0xF) | (index & 0xF);
        int y = (pos.getY() & ~0xF) | ((index >>> 8) & 0xF);
        int z = (pos.getZ() & ~0xF) | ((index >>> 4) & 0xF);

        return new BlockPos(x, y, z);
    }

    private void getSize(ICommandSender sender, BlockStateContainer bsc) {
        IBlockStatePalette ibsp = bsc.getPalette();
        if (ibsp instanceof BlockStatePaletteLinear) {
            sender.sendMessage(new TextComponentString("Palette size: " + ((BlockStatePaletteLinear) ibsp).paletteSize()));
        } else if (ibsp instanceof BlockStatePaletteHashMap) {
            sender.sendMessage(new TextComponentString("Palette size: " + ((BlockStatePaletteHashMap) ibsp).paletteSize()));
        } else if (ibsp instanceof BlockStatePaletteRegistry) {
            sender.sendMessage(new TextComponentString("Palette size MAX aka " + Block.BLOCK_STATE_IDS.size()));
        }
    }

    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "bits", "size", "posInfo", "fill");
        } else if (args.length >= 2 && args.length <= 4) {
            return getTabCompletionCoordinate(args, 1, targetPos);
        } else if (args.length == 5 && (args[0].equals("posInfo") || args[0].equals("fill"))) {
            return getListOfStringsMatchingLastWord(args, "full", "normal");
        } else if (args.length == 6 && args[0].equals("fill")) {
            return getListOfStringsMatchingLastWord(args, "4", "5", "6", "7", "8", "13");
        } else if (args.length == 6 && args[0].equals("posInfo")) {
            return getListOfStringsMatchingLastWord(args, Block.REGISTRY.getKeys());
        } else {
            return Collections.emptyList();
        }
    }
}