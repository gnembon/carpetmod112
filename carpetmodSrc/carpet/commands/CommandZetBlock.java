package carpet.commands;

import carpet.CarpetSettings;
import carpet.helpers.CapturedDrops;
import carpet.worldedit.WorldEditBridge;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.*;
import net.minecraft.command.server.CommandSetBlock;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandZetBlock extends CommandSetBlock {
    /**
     * Gets the name of the command
     */
    public String getName()
    {
        return "zetblock";
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
        return "commands.setblock.usage";
    }

    /**
     * Callback for when the command is executed
     */
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 4)
        {
            throw new WrongUsageException("commands.setblock.usage", new Object[0]);
        }
        else
        {
            sender.setCommandStat(CommandResultStats.Type.AFFECTED_BLOCKS, 0);
            BlockPos blockpos = parseBlockPos(sender, args, 0, false);
            Block block = CommandBase.getBlockByText(sender, args[3]);
            IBlockState iblockstate;

            if (args.length >= 5)
            {
                iblockstate = convertArgToBlockState(block, args[4]);
            }
            else
            {
                iblockstate = block.getDefaultState();
            }

            World world = sender.getEntityWorld();

			NBTTagCompound nbttagcompound = new NBTTagCompound();
			boolean flag = false;

			if (args.length >= 7 && block.hasTileEntity())
			{
				String s = buildString(args, 6);

				try
				{
					nbttagcompound = JsonToNBT.getTagFromJson(s);
					flag = true;
				}
				catch (NBTException nbtexception)
				{
					throw new CommandException("commands.setblock.tagError", new Object[] {nbtexception.getMessage()});
				}
			}

			EntityPlayerMP worldEditPlayer = sender instanceof EntityPlayerMP ? (EntityPlayerMP) sender : null;
			NBTTagCompound worldEditTag = flag ? nbttagcompound : null;

			boolean updates = true;

			if (args.length >= 6)
			{
				if ("destroy".equals(args[5]))
				{
					WorldEditBridge.recordBlockEdit(worldEditPlayer, world, blockpos, Blocks.AIR.getDefaultState(), worldEditTag);
					CapturedDrops.setCapturingDrops(true);
					world.destroyBlock(blockpos, true);
					CapturedDrops.setCapturingDrops(false);
					for (EntityItem drop : CapturedDrops.getCapturedDrops())
						WorldEditBridge.recordEntityCreation(worldEditPlayer, world, drop);
					CapturedDrops.clearCapturedDrops();

					if (block == Blocks.AIR)
					{
						notifyCommandListener(sender, this, "commands.setblock.success", new Object[0]);
						return;
					}
				}
				else if ("keep".equals(args[5]) && !world.isAirBlock(blockpos))
				{
					throw new CommandException("commands.setblock.noChange", new Object[0]);
				}
				else if ("noupdate".equals(args[5]))
				{
					updates = false;
				}
			}

			WorldEditBridge.recordBlockEdit(worldEditPlayer, world, blockpos, iblockstate, worldEditTag);

			TileEntity tileentity1 = world.getTileEntity(blockpos);

			if (tileentity1 != null && tileentity1 instanceof IInventory)
			{
				((IInventory)tileentity1).clear();
			}

			if (!world.setBlockState(blockpos, iblockstate, 2 | (updates ? 0 : 128)))
			{
				throw new CommandException("commands.setblock.noChange", new Object[0]);
			}
			else
			{
				if (flag)
				{
					TileEntity tileentity = world.getTileEntity(blockpos);

					if (tileentity != null)
					{
						nbttagcompound.setInteger("x", blockpos.getX());
						nbttagcompound.setInteger("y", blockpos.getY());
						nbttagcompound.setInteger("z", blockpos.getZ());
						tileentity.readFromNBT(nbttagcompound);
					}
				}

				if (updates)
				{
					world.notifyNeighborsRespectDebug(blockpos, iblockstate.getBlock(), false);
				}
				sender.setCommandStat(CommandResultStats.Type.AFFECTED_BLOCKS, 1);
				notifyCommandListener(sender, this, "commands.setblock.success", new Object[0]);
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
        else if (args.length == 4)
        {
            return getListOfStringsMatchingLastWord(args, Block.REGISTRY.getKeys());
        }
        else
        {
            return args.length == 6 ? getListOfStringsMatchingLastWord(args, new String[] {"replace", "destroy", "keep", "noupdate"}) : Collections.emptyList();
        }
    }
}
