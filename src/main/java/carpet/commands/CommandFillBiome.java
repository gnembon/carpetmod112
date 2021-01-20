package carpet.commands;

import java.util.Collections;
import java.util.List;

import carpet.mixin.accessors.PlayerChunkMapEntryAccessor;
import carpet.mixin.accessors.ServerWorldAccessor;
import net.minecraft.class_4615;
import net.minecraft.class_6175;
import net.minecraft.class_6182;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.WorldChunk;

public class CommandFillBiome extends CommandCarpetBase
{
    @Override
    public String method_29277()
    {
        return "fillbiome";
    }

    @Override
    public String method_29275(CommandSource sender)
    {
        return "/fillbiome <from: x z> <to: x z> <biome>";
    }

    @Override
    public void method_29272(MinecraftServer server, CommandSource sender, String[] args) throws class_6175
    {
        if (!command_enabled("commandFillBiome", sender))
            return;
        
        if (args.length < 5)
            throw new class_6182(method_29275(sender));
        
        int x1 = (int) Math.round(method_28702(sender.getBlockPos().getX(), args[0], false).method_28750());
        int z1 = (int) Math.round(method_28702(sender.getBlockPos().getZ(), args[1], false).method_28750());
        int x2 = (int) Math.round(method_28702(sender.getBlockPos().getX(), args[2], false).method_28750());
        int z2 = (int) Math.round(method_28702(sender.getBlockPos().getZ(), args[3], false).method_28750());
        
        int minX = Math.min(x1, x2);
        int maxX = Math.max(x1, x2);
        int minZ = Math.min(z1, z2);
        int maxZ = Math.max(z1, z2);
        
        Biome biome;
        try
        {
            biome = Biome.method_26230(Integer.parseInt(args[4]));
        }
        catch (NumberFormatException e)
        {
            biome = Biome.field_23677.get(new Identifier(args[4]));
        }
        if (biome == null)
        {
            throw new class_6175("Unknown biome " + args[4]);
        }
        byte biomeId = (byte) (Biome.method_26235(biome) & 255);
        
        ServerWorld world = (ServerWorld) sender.getEntityWorld();
        if (!world.setBlockState(new BlockPos(minX, 0, minZ), new BlockPos(maxX, 0, maxZ)))
        {
            throw new class_6175("commands.fill.outOfWorld");
        }
        
        BlockPos.Mutable pos = new BlockPos.Mutable();
        
        for (int x = minX; x <= maxX; x++)
        {
            for (int z = minZ; z <= maxZ; z++)
            {
                WorldChunk chunk = world.getWorldChunk(pos.set(x, 0, z));
                chunk.method_27418()[(x & 15) | (z & 15) << 4] = biomeId;
                chunk.markDirty();
            }
        }
        
        int minChunkX = Math.floorDiv(minX, 16);
        int maxChunkX = Math.floorDiv(maxX, 16);
        int minChunkZ = Math.floorDiv(minZ, 16);
        int maxChunkZ = Math.floorDiv(maxZ, 16);
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++)
        {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++)
            {
                class_4615 entry = ((ServerWorldAccessor) world).getPlayerChunkMap().method_33587(chunkX, chunkZ);
                if (entry != null)
                {
                    WorldChunk chunk = entry.method_33575();
                    if (chunk != null)
                    {
                        ChunkDataS2CPacket packet = new ChunkDataS2CPacket(chunk, 65535);
                        for (ServerPlayerEntity player : ((PlayerChunkMapEntryAccessor) entry).getPlayers())
                            player.networkHandler.sendPacket(packet);
                    }
                }
            }
        }
        
        method_28710(sender, this, ((maxX - minX + 1) * (maxZ - minZ + 1)) + " biome blocks changed");
    }

    @Override
    public List<String> method_29273(MinecraftServer server, CommandSource sender, String[] args, BlockPos targetPos)
    {
        if (args.length == 0)
        {
            return Collections.emptyList();
        }
        else if (args.length == 1 || args.length == 3)
        {
            if (targetPos == null)
                return method_28732(args, "~");
            else
                return method_28732(args, String.valueOf(targetPos.getX()));
        }
        else if (args.length == 2 || args.length == 4)
        {
            if (targetPos == null)
                return method_28732(args, "~");
            else
                return method_28732(args, String.valueOf(targetPos.getZ()));
        }
        else if (args.length == 5)
        {
            return method_28731(args, Biome.field_23677.getIds());
        }
        else
        {
            return Collections.emptyList();
        }
    }
}
