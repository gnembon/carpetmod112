package carpet.worldedit;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * WorldEdit bridge class, adds an extra layer of abstraction so classes from
 * WorldEdit itself are not attempted to be loaded when WorldEdit is not present.
 */
public class WorldEditBridge
{
    private static boolean worldEdit;
    static
    {
        try
        {
            Class.forName("com.sk89q.worldedit.WorldEdit");
            worldEdit = true;
        }
        catch (ClassNotFoundException e)
        {
            worldEdit = false;
        }
    }
    
    public static void onServerLoaded(MinecraftServer server)
    {
        if (worldEdit)
            CarpetWorldEdit.inst.onServerLoaded(server);
    }
    
    public static void onStartTick()
    {
        if (worldEdit)
            CarpetWorldEdit.inst.onStartTick();
    }
    
    public static void onCommand(ICommand command, ICommandSender sender, String[] args)
    {
        if (worldEdit)
            CarpetWorldEdit.inst.onCommand(command, sender, args);
    }
    
    public static boolean onLeftClickBlock(World world, BlockPos pos, EntityPlayerMP player)
    {
        if (worldEdit)
            return CarpetWorldEdit.inst.onLeftClickBlock(world, pos, player);
        else
            return true;
    }
    
    public static boolean onRightClickBlock(World world, BlockPos pos, EntityPlayerMP player)
    {
        if (worldEdit)
            return CarpetWorldEdit.inst.onRightClickBlock(world, pos, player);
        else
            return true;
    }
    
    public static boolean onRightClickAir(World world, EntityPlayerMP player)
    {
        if (worldEdit)
            return CarpetWorldEdit.inst.onRightClickAir(world, player);
        else
            return true;
            
    }
    
    public static void onCustomPayload(CPacketCustomPayload packet, EntityPlayerMP player)
    {
        if (worldEdit)
            WECUIPacketHandler.onCustomPayload(packet, player);
    }
}
