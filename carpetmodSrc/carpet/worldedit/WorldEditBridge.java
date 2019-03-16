package carpet.worldedit;

import carpet.CarpetSettings;
import carpet.network.PluginChannelHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
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
    public static boolean worldEditPresent;
    static
    {
        try
        {
            Class.forName("com.sk89q.worldedit.WorldEdit");
            worldEditPresent = true;
        }
        catch (ClassNotFoundException e)
        {
            worldEditPresent = false;
        }
    }
    
    public static boolean worldEditEnabled()
    {
        return CarpetSettings.worldEdit && worldEditPresent;
    }
    
    public static void onServerLoaded(MinecraftServer server)
    {
        if (worldEditPresent)
            CarpetWorldEdit.inst.onServerLoaded(server);
    }
    
    public static void onStartTick()
    {
        if (worldEditPresent)
            CarpetWorldEdit.inst.onStartTick();
    }
    
    public static void onCommand(ICommand command, ICommandSender sender, String[] args)
    {
        if (worldEditEnabled())
            CarpetWorldEdit.inst.onCommand(command, sender, args);
    }
    
    public static boolean onLeftClickBlock(World world, BlockPos pos, EntityPlayerMP player)
    {
        if (worldEditEnabled())
            return CarpetWorldEdit.inst.onLeftClickBlock(world, pos, player);
        else
            return true;
    }
    
    public static boolean onRightClickBlock(World world, BlockPos pos, EntityPlayerMP player)
    {
        if (worldEditEnabled())
            return CarpetWorldEdit.inst.onRightClickBlock(world, pos, player);
        else
            return true;
    }
    
    public static boolean onRightClickAir(World world, EntityPlayerMP player)
    {
        if (worldEditEnabled())
            return CarpetWorldEdit.inst.onRightClickAir(world, player);
        else
            return true;
            
    }
    
    public static void onCustomPayload(CPacketCustomPayload packet, EntityPlayerMP player)
    {
        if (worldEditEnabled())
            WECUIPacketHandler.onCustomPayload(packet, player);
    }
    
    public static void startEditSession(EntityPlayerMP player)
    {
        if (worldEditEnabled())
            CarpetWorldEdit.inst.startEditSession(player);
    }
    
    public static void finishEditSession(EntityPlayerMP player)
    {
        if (worldEditEnabled())
            CarpetWorldEdit.inst.finishEditSession(player);
    }
    
    public static void recordBlockEdit(EntityPlayerMP player, World world, BlockPos pos, IBlockState newBlock, NBTTagCompound newTileEntity)
    {
        if (worldEditEnabled())
            CarpetWorldEdit.inst.recordBlockEdit(player, world, pos, newBlock, newTileEntity);
    }
    
    public static void recordEntityCreation(EntityPlayerMP player, World world, Entity created)
    {
        if (worldEditEnabled())
            CarpetWorldEdit.inst.recordEntityCreation(player, world, created);
    }
    
    public static void recordEntityRemoval(EntityPlayerMP player, World world, Entity removed)
    {
        if (worldEditEnabled())
            CarpetWorldEdit.inst.recordEntityRemoval(player, world, removed);
    }

    public static PluginChannelHandler createChannelHandler() {
        return new PluginChannelHandler() {
            @Override
            public String[] getChannels() {
                return new String[]{CarpetWorldEdit.CUI_PLUGIN_CHANNEL};
            }

            @Override
            public void onCustomPayload(CPacketCustomPayload packet, EntityPlayerMP player) {
                WorldEditBridge.onCustomPayload(packet, player);
            }
        };
    }
}
