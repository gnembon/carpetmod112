package carpet.commands;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import carpet.CarpetSettings;
import carpet.helpers.CapturedDrops;
import carpet.worldedit.WorldEditBridge;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;

public class CommandFeel extends CommandBase
{
    /**
     * Gets the name of the command
     */
    public String getName()
    {
        return "feel";
    }

    /**
     * Return the required permission level for this command.
     */
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    /**
     * Gets the usage string for the command.
     */
    public String getUsage(ICommandSender sender)
    {
        return "commands.fill.usage";
    }

    /**
     * Callback for when the command is executed
     */
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 7)
        {
            throw new WrongUsageException("commands.fill.usage", new Object[0]);
        }
        else
        {
            sender.setCommandStat(CommandResultStats.Type.AFFECTED_BLOCKS, 0);
            BlockPos blockpos = parseBlockPos(sender, args, 0, false);
            BlockPos blockpos1 = parseBlockPos(sender, args, 3, false);
            Block block = CommandBase.getBlockByText(sender, args[6]);
            IBlockState iblockstate;

            if (args.length >= 8)
            {
                iblockstate = convertArgToBlockState(block, args[7]);
            }
            else
            {
                iblockstate = block.getDefaultState();
            }

            BlockPos blockpos2 = new BlockPos(Math.min(blockpos.getX(), blockpos1.getX()), Math.min(blockpos.getY(), blockpos1.getY()), Math.min(blockpos.getZ(), blockpos1.getZ()));
            BlockPos blockpos3 = new BlockPos(Math.max(blockpos.getX(), blockpos1.getX()), Math.max(blockpos.getY(), blockpos1.getY()), Math.max(blockpos.getZ(), blockpos1.getZ()));
            int i = (blockpos3.getX() - blockpos2.getX() + 1) * (blockpos3.getY() - blockpos2.getY() + 1) * (blockpos3.getZ() - blockpos2.getZ() + 1);

            if (blockpos2.getY() >= 0 && blockpos3.getY() < 256)
            {
                World world = sender.getEntityWorld();

                NBTTagCompound nbttagcompound = new NBTTagCompound();
                boolean flag = false;

                if (args.length >= 10 && block.hasTileEntity())
                {
                    String s = buildString(args, 9);

                    try
                    {
                        nbttagcompound = JsonToNBT.getTagFromJson(s);
                        flag = true;
                    }
                    catch (NBTException nbtexception)
                    {
                        throw new CommandException("commands.fill.tagError", new Object[] {nbtexception.getMessage()});
                    }
                }

                FillType mode;
                try {
                    mode = args.length <= 8 ? FillType.REPLACE : FillType.valueOf(args[8].toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException e) {
                    mode = FillType.REPLACE;
                }
                Block toReplace = null;
                Predicate<IBlockState> toReplacePredicate = state -> true;
                if ((mode == FillType.REPLACE || mode == FillType.NOUPDATE) && args.length > 9) {
                    toReplace = CommandBase.getBlockByText(sender, args[9]);
                    if (args.length > 10 && !args[10].equals("-1") && !args[10].equals("*")) {
                        toReplacePredicate = CommandBase.convertArgToBlockStatePredicate(toReplace, args[10]);
                    }
                }

                EntityPlayerMP worldEditPlayer = sender instanceof EntityPlayerMP ? (EntityPlayerMP) sender : null;
                NBTTagCompound worldEditTag = flag ? nbttagcompound : null;

                List<BlockPos> list = Lists.<BlockPos>newArrayList();
                i = 0;

                for (int l = blockpos2.getZ(); l <= blockpos3.getZ(); ++l)
                {
                    for (int i1 = blockpos2.getY(); i1 <= blockpos3.getY(); ++i1)
                    {
                        for (int j1 = blockpos2.getX(); j1 <= blockpos3.getX(); ++j1)
                        {
                            BlockPos blockpos4 = new BlockPos(j1, i1, l);

                            if (args.length >= 9)
                            {
                                if (mode != FillType.OUTLINE && mode != FillType.HOLLOW)
                                {
                                    if (mode == FillType.DESTROY)
                                    {
                                        WorldEditBridge.recordBlockEdit(worldEditPlayer, world, blockpos4, Blocks.AIR.getDefaultState(), worldEditTag);
                                        CapturedDrops.setCapturingDrops(true);
                                        world.destroyBlock(blockpos4, true);
                                        CapturedDrops.setCapturingDrops(false);
                                        for (EntityItem drop : CapturedDrops.getCapturedDrops())
                                            WorldEditBridge.recordEntityCreation(worldEditPlayer, world, drop);
                                        CapturedDrops.clearCapturedDrops();
                                    }
                                    else if (mode == FillType.KEEP)
                                    {
                                        if (!world.isAirBlock(blockpos4))
                                        {
                                            continue;
                                        }
                                    }
                                    else if ((mode == FillType.REPLACE || mode == FillType.NOUPDATE) && !block.hasTileEntity() && args.length > 9)
                                    {
                                        IBlockState state = world.getBlockState(blockpos4);
                                        if (state.getBlock() != toReplace || !toReplacePredicate.test(state))
                                        {
                                            continue;
                                        }
                                    }
                                }
                                else if (j1 != blockpos2.getX() && j1 != blockpos3.getX() && i1 != blockpos2.getY() && i1 != blockpos3.getY() && l != blockpos2.getZ() && l != blockpos3.getZ())
                                {
                                    if (mode == FillType.HOLLOW)
                                    {
                                        WorldEditBridge.recordBlockEdit(worldEditPlayer, world, blockpos4, Blocks.AIR.getDefaultState(), worldEditTag);
                                        world.setBlockState(blockpos4, Blocks.AIR.getDefaultState(), 2);
                                        list.add(blockpos4);
                                    }

                                    continue;
                                }
                            }

                            WorldEditBridge.recordBlockEdit(worldEditPlayer, world, blockpos4, iblockstate, worldEditTag);
                            TileEntity tileentity1 = world.getTileEntity(blockpos4);

                            if (tileentity1 != null && tileentity1 instanceof IInventory)
                            {
                                ((IInventory)tileentity1).clear();
                            }

                            if (world.setBlockState(blockpos4, iblockstate, 2 | (mode != FillType.NOUPDATE?0:128)  )) //CM
                            {
                                list.add(blockpos4);
                                ++i;

                                if (flag)
                                {
                                    TileEntity tileentity = world.getTileEntity(blockpos4);

                                    if (tileentity != null)
                                    {
                                        nbttagcompound.setInteger("x", blockpos4.getX());
                                        nbttagcompound.setInteger("y", blockpos4.getY());
                                        nbttagcompound.setInteger("z", blockpos4.getZ());
                                        tileentity.readFromNBT(nbttagcompound);
                                    }
                                }
                            }
                        }
                    }
                }

                /*carpet mod */
                if (mode != FillType.NOUPDATE)
                {
                    /*carpet mod end EXTRA INDENT*/
                    for (BlockPos blockpos5 : list)
                    {
                        Block block2 = world.getBlockState(blockpos5).getBlock();
                        world.notifyNeighborsRespectDebug(blockpos5, block2, false);
                    }
                } //carpet mod back extra indentation

                if (i <= 0)
                {
                    throw new CommandException("commands.fill.failed", new Object[0]);
                }
                else
                {
                    sender.setCommandStat(CommandResultStats.Type.AFFECTED_BLOCKS, i);
                    notifyCommandListener(sender, this, "commands.fill.success", new Object[] {i});
                }
            }
            else
            {
                throw new CommandException("commands.fill.outOfWorld", new Object[0]);
            }
        }
    }

    /**
     * Get a list of options for when the user presses the TAB key
     */
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        if (args.length > 0 && args.length <= 3)
        {
            return getTabCompletionCoordinate(args, 0, targetPos);
        }
        else if (args.length > 3 && args.length <= 6)
        {
            return getTabCompletionCoordinate(args, 3, targetPos);
        }
        else if (args.length == 7)
        {
            return getListOfStringsMatchingLastWord(args, Block.REGISTRY.getKeys());
        }
        else if (args.length == 9)
        {
            return getListOfStringsMatchingLastWord(args, new String[] {"replace", "destroy", "keep", "hollow", "outline", "noupdate", "replace_noupdate"});
        }
        else
        {
            return args.length == 10 && ("replace".equalsIgnoreCase(args[8]) || "noupdate".equalsIgnoreCase(args[8])) ? getListOfStringsMatchingLastWord(args, Block.REGISTRY.getKeys()) : Collections.emptyList();
        }
    }

    enum FillType {
        REPLACE, DESTROY, KEEP, HOLLOW, OUTLINE, NOUPDATE;
    }
}
