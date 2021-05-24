package carpet.commands;

import carpet.helpers.LazyChunkBehaviorHelper;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Collections;
import java.util.List;


public class CommandLazyChunkBehavior extends CommandCarpetBase{


    /**
     * Gets the name of the command
     */

    public String getUsage(ICommandSender sender)
    {
        return "lazychunkbehavior <add, removeAll, remove, list> <X> <Z>";
    }

    public String getName()
    {
        return "lazychunkbehavior";
    }

    private void list(ICommandSender sender, String[] args) throws CommandException{
        if (args.length != 1)
        {
            throw new WrongUsageException(getUsage(sender));
        }
        LazyChunkBehaviorHelper.listLazyChunks(sender);
    }
    private void removeAll(ICommandSender sender, String[] args) throws CommandException{
        if (args.length != 1)
        {
            throw new WrongUsageException(getUsage(sender));
        }

        LazyChunkBehaviorHelper.removeAll();
        TextComponentString text = new TextComponentString("All chunks have been removed." );
        text.getStyle().setColor(TextFormatting.RED);
        sender.sendMessage(text);
    }

    private void add(ICommandSender sender, String[] args) throws CommandException{

        if (args.length != 3)
        {
            throw new WrongUsageException(getUsage(sender));
        }
        int chunkX = parseInt(args[1]);
        int chunkZ = parseInt(args[2]);
        World world = sender.getEntityWorld();
        Chunk chunk = world.getChunk(chunkX, chunkZ);
        LazyChunkBehaviorHelper.addLazyChunk(chunk);
        TextComponentString text = new TextComponentString("Chunk " + chunkX + ", " + chunkZ + " in world " + chunk.getWorld().provider.getDimensionType() + " has been added." );
        text.getStyle().setColor(TextFormatting.RED);
        sender.sendMessage(text);
    }

    private void remove(ICommandSender sender, String[] args) throws CommandException{

        if (args.length != 3)
        {
            throw new WrongUsageException(getUsage(sender));
        }

        int chunkX = parseInt(args[1]);
        int chunkZ = parseInt(args[2]);
        World world = sender.getEntityWorld();
        Chunk chunk = world.getChunk(chunkX, chunkZ);

        LazyChunkBehaviorHelper.removeLazyChunk(chunk);
        TextComponentString text = new TextComponentString("Chunk " + chunkX + ", " + chunkZ + " in world " + chunk.getWorld().provider.getDimensionType() + " has been removed." );
        text.getStyle().setColor(TextFormatting.RED);
        sender.sendMessage(text);
    }



    /**
     * Callback for when the command is executed
     */
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (!command_enabled("CommandLazyChunkBehavior", sender)) return;

        if(args.length < 1 || args.length > 3){
            throw new WrongUsageException(getUsage(sender));

        }
        switch (args[0]){
            case "list":
                list(sender,args);
                return;
            case "add":
                add(sender,args);
                return;
            case "remove":
                remove(sender,args);
                return;
            case "removeAll":
                removeAll(sender,args);
        }
    }
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {

        int chunkX = sender.getPosition().getX() >> 4;
        int chunkZ = sender.getPosition().getZ() >> 4;

        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "list", "add", "remove","removeAll");
        }
        else if(args.length == 2 && !args[0].equals("list") && !args[0].equals("removeAll")){
            return getListOfStringsMatchingLastWord(args, Integer.toString(chunkX));
        }
        else if (args.length == 3 && !args[0].equals("list") && !args[0].equals("removeAll")) {
            return getListOfStringsMatchingLastWord(args, Integer.toString(chunkZ));
        } else {
            return Collections.emptyList();
        }
    }
}
