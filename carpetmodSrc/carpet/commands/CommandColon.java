package carpet.commands;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandClone;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import carpet.CarpetSettings;
import carpet.worldedit.WorldEditBridge;
import net.minecraft.entity.player.EntityPlayerMP;

public class CommandColon extends CommandBase
{
    /**
     * Gets the name of the command
     */
    public String getName()
    {
        return "colon";
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
        return "commands.clone.usage";
    }

    /**
     * Callback for when the command is executed
     */
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 9)
        {
            throw new WrongUsageException("commands.clone.usage", new Object[0]);
        }
        else
        {
            sender.setCommandStat(CommandResultStats.Type.AFFECTED_BLOCKS, 0);
            BlockPos blockpos = parseBlockPos(sender, args, 0, false);
            BlockPos blockpos1 = parseBlockPos(sender, args, 3, false);
            BlockPos blockpos2 = parseBlockPos(sender, args, 6, false);
            StructureBoundingBox structureboundingbox = new StructureBoundingBox(blockpos, blockpos1);
            StructureBoundingBox structureboundingbox1 = new StructureBoundingBox(blockpos2, blockpos2.add(structureboundingbox.getLength()));

            boolean flag = false;
            Block block = null;
            Predicate<IBlockState> predicate = null;

            if ((args.length < 11 || !"force".equals(args[10]) && !"move".equals(args[10]) && !"force_noupdate".equals(args[10]) && !"move_noupdate".equals(args[10])) && structureboundingbox.intersectsWith(structureboundingbox1))
            {
                throw new CommandException("commands.clone.noOverlap", new Object[0]);
            }
            else
            {
                if (args.length >= 11 && ("move".equals(args[10]) || "move_noupdate".equals(args[10])))
                {
                    flag = true;
                }

                boolean update = true;
                if (args.length >= 11 && ("noupdate".equals(args[10]) || "force_noupdate".equals(args[10]) || "move_noupdate".equals(args[10])))
                {
                    update = false;
                }

                if (structureboundingbox.minY >= 0 && structureboundingbox.maxY < 256 && structureboundingbox1.minY >= 0 && structureboundingbox1.maxY < 256)
                {
                    World world = sender.getEntityWorld();

                    boolean flag1 = false;

                    if (args.length >= 10)
                    {
                        if ("masked".equals(args[9]))
                        {
                            flag1 = true;
                        }
                        else if ("filtered".equals(args[9]))
                        {
                            if (args.length < 12)
                            {
                                throw new WrongUsageException("commands.clone.usage", new Object[0]);
                            }

                            block = getBlockByText(sender, args[11]);

                            if (args.length >= 13)
                            {
                                predicate = convertArgToBlockStatePredicate(block, args[12]);
                            }
                        }
                    }

                    List<StaticCloneData> list = Lists.newArrayList();
                    List<StaticCloneData> list1 = Lists.newArrayList();
                    List<StaticCloneData> list2 = Lists.newArrayList();
                    Deque<BlockPos> deque = Lists.<BlockPos>newLinkedList();
                    BlockPos blockpos3 = new BlockPos(structureboundingbox1.minX - structureboundingbox.minX, structureboundingbox1.minY - structureboundingbox.minY, structureboundingbox1.minZ - structureboundingbox.minZ);

                    for (int j = structureboundingbox.minZ; j <= structureboundingbox.maxZ; ++j)
                    {
                        for (int k = structureboundingbox.minY; k <= structureboundingbox.maxY; ++k)
                        {
                            for (int l = structureboundingbox.minX; l <= structureboundingbox.maxX; ++l)
                            {
                                BlockPos blockpos4 = new BlockPos(l, k, j);
                                BlockPos blockpos5 = blockpos4.add(blockpos3);
                                IBlockState iblockstate = world.getBlockState(blockpos4);

                                if ((!flag1 || iblockstate.getBlock() != Blocks.AIR) && (block == null || iblockstate.getBlock() == block && (predicate == null || predicate.apply(iblockstate))))
                                {
                                    TileEntity tileentity = world.getTileEntity(blockpos4);

                                    if (tileentity != null)
                                    {
                                        NBTTagCompound nbttagcompound = tileentity.writeToNBT(new NBTTagCompound());
                                        list1.add(new StaticCloneData(blockpos5, iblockstate, nbttagcompound));
                                        deque.addLast(blockpos4);
                                    }
                                    else if (!iblockstate.isFullBlock() && !iblockstate.isFullCube())
                                    {
                                        list2.add(new StaticCloneData(blockpos5, iblockstate, (NBTTagCompound)null));
                                        deque.addFirst(blockpos4);
                                    }
                                    else
                                    {
                                        list.add(new StaticCloneData(blockpos5, iblockstate, (NBTTagCompound)null));
                                        deque.addLast(blockpos4);
                                    }
                                }
                            }
                        }
                    }

                    EntityPlayerMP worldEditPlayer = sender instanceof EntityPlayerMP ? (EntityPlayerMP) sender : null;

                    if (flag)
                    {
                        for (BlockPos blockpos6 : deque)
                        {
                            WorldEditBridge.recordBlockEdit(worldEditPlayer, world, blockpos6, Blocks.AIR.getDefaultState(), null);
                            TileEntity tileentity1 = world.getTileEntity(blockpos6);

                            if (tileentity1 instanceof IInventory)
                            {
                                ((IInventory)tileentity1).clear();
                            }

                            world.setBlockState(blockpos6, Blocks.BARRIER.getDefaultState(), 2 | (update?0:128)); //carpet
                        }

                        for (BlockPos blockpos7 : deque)
                        {
                            world.setBlockState(blockpos7, Blocks.AIR.getDefaultState(), (update?3:131)); //carpet
                        }
                    }

                    List<StaticCloneData> list3 = Lists.newArrayList();
                    list3.addAll(list);
                    list3.addAll(list1);
                    list3.addAll(list2);
                    List<StaticCloneData> list4 = Lists.reverse(list3);

                    for (StaticCloneData commandclone$staticclonedata : list4)
                    {
                        WorldEditBridge.recordBlockEdit(worldEditPlayer, world, commandclone$staticclonedata.pos, commandclone$staticclonedata.blockState, commandclone$staticclonedata.nbt);
                        TileEntity tileentity2 = world.getTileEntity(commandclone$staticclonedata.pos);

                        if (tileentity2 instanceof IInventory)
                        {
                            ((IInventory)tileentity2).clear();
                        }

                        world.setBlockState(commandclone$staticclonedata.pos, Blocks.BARRIER.getDefaultState(), 2 | (update?0:128)); //carpet
                    }

                    int i = 0;

                    for (StaticCloneData commandclone$staticclonedata1 : list3)
                    {
                        if (world.setBlockState(commandclone$staticclonedata1.pos, commandclone$staticclonedata1.blockState, 2 | (update?0:128))) //carpet
                        {
                            ++i;
                        }
                    }
                    for (StaticCloneData commandclone$staticclonedata2 : list1)
                    {
                        TileEntity tileentity3 = world.getTileEntity(commandclone$staticclonedata2.pos);

                        if (commandclone$staticclonedata2.nbt != null && tileentity3 != null)
                        {
                            commandclone$staticclonedata2.nbt.setInteger("x", commandclone$staticclonedata2.pos.getX());
                            commandclone$staticclonedata2.nbt.setInteger("y", commandclone$staticclonedata2.pos.getY());
                            commandclone$staticclonedata2.nbt.setInteger("z", commandclone$staticclonedata2.pos.getZ());
                            tileentity3.readFromNBT(commandclone$staticclonedata2.nbt);
                            tileentity3.markDirty();
                        }

                        world.setBlockState(commandclone$staticclonedata2.pos, commandclone$staticclonedata2.blockState, 2);
                    }

                    /*carpet mod */
                    if (update)
                    {
                        /*carpet mod end EXTRA INDENTATION START*/
                        for (StaticCloneData commandclone$staticclonedata3 : list4)
                        {
                            world.notifyNeighborsRespectDebug(commandclone$staticclonedata3.pos, commandclone$staticclonedata3.blockState.getBlock(), false);
                        }

                        List<NextTickListEntry> list5 = world.getPendingBlockUpdates(structureboundingbox, false);

                        if (list5 != null)
                        {
                            for (NextTickListEntry nextticklistentry : list5)
                            {
                                if (structureboundingbox.isVecInside(nextticklistentry.position))
                                {
                                    BlockPos blockpos8 = nextticklistentry.position.add(blockpos3);
                                    world.scheduleBlockUpdate(blockpos8, nextticklistentry.getBlock(), (int)(nextticklistentry.scheduledTime - world.getWorldInfo().getWorldTotalTime()), nextticklistentry.priority);
                                }
                            }
                        }
                    } //carpet mod back extra indentation

                    if (i <= 0)
                    {
                        throw new CommandException("commands.clone.failed", new Object[0]);
                    }
                    else
                    {
                        sender.setCommandStat(CommandResultStats.Type.AFFECTED_BLOCKS, i);
                        notifyCommandListener(sender, this, "commands.clone.success", new Object[] {i});
                    }
                }
                else
                {
                    throw new CommandException("commands.clone.outOfWorld", new Object[0]);
                }
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
        else if (args.length > 6 && args.length <= 9)
        {
            return getTabCompletionCoordinate(args, 6, targetPos);
        }
        else if (args.length == 10)
        {
            return getListOfStringsMatchingLastWord(args, new String[] {"replace", "masked", "filtered"});
        }
        else if (args.length == 11)
        {
            return getListOfStringsMatchingLastWord(args, new String[] {"normal", "force", "move", "noupdate", "force_noupdate", "move_noupdate"});
        }
        else
        {
            return args.length == 12 && "filtered".equals(args[9]) ? getListOfStringsMatchingLastWord(args, Block.REGISTRY.getKeys()) : Collections.emptyList();
        }
    }

    static class StaticCloneData
    {
        public final BlockPos pos;
        public final IBlockState blockState;
        public final NBTTagCompound nbt;

        public StaticCloneData(BlockPos posIn, IBlockState stateIn, NBTTagCompound compoundIn)
        {
            this.pos = posIn;
            this.blockState = stateIn;
            this.nbt = compoundIn;
        }
    }
}
