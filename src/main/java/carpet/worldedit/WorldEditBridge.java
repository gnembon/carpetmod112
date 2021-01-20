package carpet.worldedit;

import carpet.CarpetSettings;
import carpet.network.PluginChannelHandler;
import net.minecraft.block.BlockState;
import net.minecraft.class_2010;
import net.minecraft.class_5607;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
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
    
    public static void onCommand(class_5607 command, class_2010 sender, String[] args)
    {
        if (worldEditEnabled())
            CarpetWorldEdit.inst.onCommand(command, sender, args);
    }
    
    public static boolean onLeftClickBlock(World world, BlockPos pos, ServerPlayerEntity player)
    {
        if (worldEditEnabled())
            return CarpetWorldEdit.inst.onLeftClickBlock(world, pos, player);
        else
            return true;
    }
    
    public static boolean onRightClickBlock(World world, BlockPos pos, ServerPlayerEntity player)
    {
        if (worldEditEnabled())
            return CarpetWorldEdit.inst.onRightClickBlock(world, pos, player);
        else
            return true;
    }
    
    public static boolean onRightClickAir(World world, ServerPlayerEntity player)
    {
        if (worldEditEnabled())
            return CarpetWorldEdit.inst.onRightClickAir(world, player);
        else
            return true;
            
    }
    
    public static void onCustomPayload(CustomPayloadC2SPacket packet, ServerPlayerEntity player)
    {
        if (worldEditEnabled())
            WECUIPacketHandler.onCustomPayload(packet, player);
    }
    
    public static void startEditSession(ServerPlayerEntity player)
    {
        if (worldEditEnabled())
            CarpetWorldEdit.inst.startEditSession(player);
    }
    
    public static void finishEditSession(ServerPlayerEntity player)
    {
        if (worldEditEnabled())
            CarpetWorldEdit.inst.finishEditSession(player);
    }
    
    public static void recordBlockEdit(ServerPlayerEntity player, World world, BlockPos pos, BlockState newBlock, CompoundTag newTileEntity)
    {
        if (worldEditEnabled())
            CarpetWorldEdit.inst.recordBlockEdit(player, world, pos, newBlock, newTileEntity);
    }
    
    public static void recordEntityCreation(ServerPlayerEntity player, World world, Entity created)
    {
        if (worldEditEnabled())
            CarpetWorldEdit.inst.recordEntityCreation(player, world, created);
    }
    
    public static void recordEntityRemoval(ServerPlayerEntity player, World world, Entity removed)
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
            public void onCustomPayload(CustomPayloadC2SPacket packet, ServerPlayerEntity player) {
                WorldEditBridge.onCustomPayload(packet, player);
            }
        };
    }
}
