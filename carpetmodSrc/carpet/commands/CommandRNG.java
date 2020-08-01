package carpet.commands;

import java.util.*;

import javax.annotation.Nullable;

import carpet.utils.Messenger;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import carpet.CarpetSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.ChunkGeneratorEnd;
import net.minecraft.world.gen.ChunkGeneratorOverworld;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;

public class CommandRNG extends CommandCarpetBase {
    /**
     * Gets the name of the command
     */
    public String getName() {
        return "rng";
    }

    /**
     * Gets the usage string for the command.
     *
     * @param sender The ICommandSender who is requesting usage details
     */
    public String getUsage(ICommandSender sender) {
        return "rng <rule> <value>";
    }

    /**
     * Callback for when the command is executed
     *
     * @param server The server instance
     * @param sender The sender who executed the command
     * @param args   The arguments that were passed
     */
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!command_enabled("commandRNG", sender)) return;
        if (args.length <=0) throw new WrongUsageException(getUsage(sender));
        if ("seed".equalsIgnoreCase(args[0])) {
            try {
                World world = sender.getEntityWorld();

                IChunkGenerator gen = ((ChunkProviderServer) world.getChunkProvider()).chunkGenerator;

                if (gen instanceof ChunkGeneratorOverworld) {
                    int x;
                    int z;
                    if (args.length < 3) {
                        x = sender.getPosition().getX() / 16;
                        z = sender.getPosition().getZ() / 16;
                        if (x < 0) {
                            x--;
                        }
                        if (z < 0) {
                            z--;
                        }
                    } else {
                        try {
                            x = Integer.parseInt(args[1]);
                            z = Integer.parseInt(args[2]);
                        } catch (NumberFormatException e) {
                            x = sender.getPosition().getX() / 16;
                            z = sender.getPosition().getZ() / 16;
                            if (x < 0) {
                                x--;
                            }
                            if (z < 0) {
                                z--;
                            }
                        }
                    }

                    ((ChunkGeneratorOverworld) gen).woodlandMansionGenerator.generate(world, x, z, (ChunkPrimer) null);
                    notifyCommandListener(sender, this,
                            String.format("Seed at chunk coords: %d %d seed: %d", x, z, world.getRandSeed()));
                }
            } catch (Exception e) {
                System.out.println("some error at seed");
            }
            return;
        } else if ("setSeed".equalsIgnoreCase(args[0])) {
            try {
                CarpetSettings.setSeed = Long.parseLong(args[1]);
                notifyCommandListener(sender, this, "RNG seed set to " + args[1]);
            } catch (Exception e) {
                notifyCommandListener(sender, this, "rng setSeed <seed>, default seed to 0 for turning off RNG.");
            }
        } else if ("getMobspawningChunk".equalsIgnoreCase(args[0])) {
            long seed;
            int chunkNum;
            int playerSize;
            try {
                chunkNum = Integer.parseInt(args[2]);
            } catch (Exception e) {
                chunkNum = 1;
            }
            try {
                playerSize = Integer.parseInt(args[3]);
            } catch (Exception e) {
                playerSize = 1;
            }
            try {
                seed = Long.parseLong(args[1]);
            } catch (NumberFormatException e) {
                notifyCommandListener(sender, this, "rng getMobspawningChunk <seed> <chunkNum> <playersHashSize>");
                return;
            }

            World world = sender.getEntityWorld();
            if (world instanceof WorldServer && sender instanceof EntityPlayer) {
                displayMobSpawningChunkInfo((WorldServer) world, sender, seed, chunkNum, playerSize);
            }
        } else if ("randomtickedChunksCount".equalsIgnoreCase(args[0])) {
            World world = sender.getEntityWorld();
            int iters = Integer.MAX_VALUE;
            int chunkCount = 0;
            int x = 0;
            int z = 0;

            if (args.length == 2) {
                try {
                    iters = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                }
            }

            if (world instanceof WorldServer) {
                int count = 0;
                for (Iterator<Chunk> iterator = ((WorldServer) world).playerChunkMap.getChunkIterator(); iterator
                        .hasNext() && chunkCount < iters; ((WorldServer) world).profiler.endSection()) {
                    Chunk chunk = iterator.next();
                    if (iters != Integer.MAX_VALUE) {
                        chunkCount++;
                        x = chunk.x;
                        z = chunk.z;
                    }
                    count++;
                }
                if (iters != Integer.MAX_VALUE) {
                    notifyCommandListener(sender, this,
                            String.format(
                                    "Number of chunks till chunk index from player position: %d at chunk coord: (%d,%d)",
                                    count, x, z));
                } else {
                    notifyCommandListener(sender, this,
                            String.format("Number of chunks around the player random ticking: %d", count));
                }
            }
        } else if ("randomtickedBlocksInRange".equalsIgnoreCase(args[0])) {
            World world = sender.getEntityWorld();

            int x = 0;
            int z = 0;
            int iters = Integer.MAX_VALUE;
            int chunkCount = 0;
            boolean check = false;

            if (args.length == 2) {
                try {
                    iters = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                }
            } else if (args.length == 3) {
                try {
                    check = true;
                    x = Integer.parseInt(args[1]);
                    z = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                }
            }

            if (world instanceof WorldServer) {
                int count = 0;
                for (Iterator<Chunk> iterator = ((WorldServer) world).playerChunkMap.getChunkIterator(); iterator
                        .hasNext() && chunkCount < iters; ((WorldServer) world).profiler.endSection()) {
                    Chunk chunk = iterator.next();
                    if (iters != Integer.MAX_VALUE) {
                        chunkCount++;
                        x = chunk.x;
                        z = chunk.z;
                    }
                    if (!check || (x == chunk.x && z == chunk.z)) {
                        for (ExtendedBlockStorage extendedblockstorage : chunk.getBlockStorageArray()) {
                            if (extendedblockstorage != Chunk.NULL_BLOCK_STORAGE) {
                                for (int i = 0; i < 16; ++i) {
                                    for (int j = 0; j < 16; ++j) {
                                        for (int k = 0; k < 16; ++k) {
                                            Block block = extendedblockstorage.get(i, j, k).getBlock();

                                            if (block != Blocks.AIR) {
                                                if (rndInfluencingBlock(block)) {
                                                    count++;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (check) {
                            notifyCommandListener(sender, this,
                                    String.format("Number of rand influencing blocks: %d", count));
                            return;
                        }
                    }
                }
                if (!check) {
                    if (iters != Integer.MAX_VALUE) {
                        notifyCommandListener(sender, this, String.format(
                                "Number of rand influencing blocks: %d  until hitting chunk: (%d,%d)", count, x, z));
                    } else {
                        notifyCommandListener(sender, this,
                                String.format("Number of rand influencing blocks: %d", count));
                    }
                } else {
                    notifyCommandListener(sender, this, "Chosen location is not in loaded random ticked area.");
                }
            }
        } else if ("getLCG".equalsIgnoreCase(args[0])) {
            ArrayList<Object> strings = new ArrayList<>();
            for (World world : server.worlds) {
                Messenger.m(sender, "w " + world.provider.getDimensionType().toString() + ": ", "c " + world.updateLCG, "^w Dimention LCG at beginning of game loop : " + world.updateLCG, "?/rng setLCG " + world.provider.getDimensionType().toString() + " " + world.updateLCG);
            }
        } else if ("setLCG".equalsIgnoreCase(args[0])) {
            if (args.length == 3) {
                for (World world : server.worlds) {
                    if (world.provider.getDimensionType().toString().equals(args[1])) {
                        try {
                            world.updateLCG = Integer.parseInt(args[2]);
                            notifyCommandListener(sender, this, world.provider.getDimensionType() + " LCG changed to " + args[2]);
                        } catch (Exception e) {
                        }
                    }
                }
            }
        } else if ("getEndChunkSeed".equalsIgnoreCase(args[0])) {
            ChunkProviderServer chunkProvider = server.getWorld(1).getChunkProvider();
            ChunkGeneratorEnd chunkGeneratorEnd = ((ChunkGeneratorEnd) chunkProvider.chunkGenerator);
            long seed = chunkGeneratorEnd.lastRandomSeed;
            if (chunkGeneratorEnd.randomSeedUsed) {
                sender.sendMessage(new TextComponentString("The ChunkGeneratorEnd seed " + seed + " was already used for population and is currently random!"));
            } else {
                sender.sendMessage(new TextComponentString("Current ChunkGeneratorEnd seed: " + seed));
            }
        } else if ("setEndChunkSeed".equalsIgnoreCase(args[0])) {
            if (args.length == 2 || (args.length == 3 && "once".equalsIgnoreCase(args[2]))) {
                ChunkProviderServer chunkProvider = server.getWorld(1).getChunkProvider();
                ChunkGeneratorEnd chunkGeneratorEnd = ((ChunkGeneratorEnd) chunkProvider.chunkGenerator);
                long seed = parseLong(args[1]);
                chunkGeneratorEnd.randomSeedUsed = false;
                chunkGeneratorEnd.lastRandomSeed = seed;
                if (args.length == 2) {
                    CarpetSettings.endChunkSeed = seed;
                    sender.sendMessage(new TextComponentString("Set the ChunkGeneratorEnd seed to: " + CarpetSettings.endChunkSeed));
                } else if (args.length == 3) {
                    chunkGeneratorEnd.setEndChunkSeed(seed);
                    sender.sendMessage(new TextComponentString("Set the ChunkGeneratorEnd seed once to: " + chunkGeneratorEnd.lastRandomSeed));
                }
            } else {
                throw new WrongUsageException("/rng setEndChunkSeed <seed> [once]");
            }
        }
    }

    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
                                          @Nullable BlockPos targetPos) {
        if (!CarpetSettings.commandRNG) {
            return Collections.<String>emptyList();
        }
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "seed", "setSeed", "getMobspawningChunk",
                    "randomtickedChunksCount", "randomtickedBlocksInRange", "logWeather", "getLCG", "setLCG", "getEndChunkSeed", "setEndChunkSeed");
        }
        if (args.length >= 2) {
            if ("seed".equalsIgnoreCase(args[0])) {
                BlockPos pos = sender.getPosition();
                return getTabComplet(args, 1, pos);
            }
            if ("setSeed".equalsIgnoreCase(args[0])) {
                return getListOfStringsMatchingLastWord(args, "0");
            }
            if ("getMobspawningChunk".equalsIgnoreCase(args[0])) {
                return getListOfStringsMatchingLastWord(args, "1");
            }
            if ("randomtickedBlocksInRange".equalsIgnoreCase(args[0])) {
                BlockPos pos = sender.getPosition();
                return getTabComplet(args, 1, pos);
            }
            if ("logWeather".equalsIgnoreCase(args[0])) {
                return getListOfStringsMatchingLastWord(args, "true", "false");
            }
            if ("setLCG".equalsIgnoreCase(args[0])) {
                return getListOfStringsMatchingLastWord(args, "OVERWORLD", "NETHER", "THE_END");
            }
            if ("setEndChunkSeed".equalsIgnoreCase(args[0]) && args.length == 3) {
                return getListOfStringsMatchingLastWord(args, "once");
            }
        }

        return Collections.<String>emptyList();
    }

    public static List<String> getTabComplet(String[] inputArgs, int index, @Nullable BlockPos lookedPos) {
        if (lookedPos == null) {
            return Lists.newArrayList("~");
        } else {
            int i = inputArgs.length - 1;
            String s;

            if (i == index) {
                s = Integer.toString(lookedPos.getX() / 16);
            } else {
                if (i != index + 1) {
                    return Collections.<String>emptyList();
                }

                s = Integer.toString(lookedPos.getZ() / 16);
            }

            return Lists.newArrayList(s);
        }
    }

    private boolean rndInfluencingBlock(Block block) {
        return Blocks.LAVA == block || Blocks.SAPLING == block || Blocks.GLASS == block ||
                Blocks.VINE == block || Blocks.CARROTS == block || Blocks.WHEAT == block ||
                Blocks.BEETROOTS == block || Blocks.FIRE == block || Blocks.COCOA == block ||
                Blocks.POTATOES == block;

    }

    public void displayMobSpawningChunkInfo(WorldServer worldServerIn, ICommandSender sender, long seed, int chunkNum,
                                            int playerSize) {
        EntityPlayer entityplayer = (EntityPlayer) sender;
        Set<ChunkPos> eligibleChunksForSpawning = Sets.<ChunkPos>newHashSet();

        for (int i = 0; i < (playerSize * 225); i++) {
            ChunkPos chunkpos = new ChunkPos(0, i);
            eligibleChunksForSpawning.add(chunkpos);
        }
        eligibleChunksForSpawning.clear();

        Random rand = new Random();
        rand.setSeed(seed ^ 0x5DEECE66DL);

        int j = MathHelper.floor(entityplayer.posX / 16.0D);
        int k = MathHelper.floor(entityplayer.posZ / 16.0D);
        int l = 8;

        for (int i1 = -8; i1 <= 8; ++i1) {
            for (int j1 = -8; j1 <= 8; ++j1) {
                boolean flag = i1 == -8 || i1 == 8 || j1 == -8 || j1 == 8;
                ChunkPos chunkpos = new ChunkPos(i1 + j, j1 + k);

                if (!eligibleChunksForSpawning.contains(chunkpos)) {
                    if (!flag && worldServerIn.getWorldBorder().contains(chunkpos)) {
                        PlayerChunkMapEntry playerchunkmapentry = worldServerIn.getPlayerChunkMap()
                                .getEntry(chunkpos.x, chunkpos.z);

                        if (playerchunkmapentry != null && playerchunkmapentry.isSentToPlayers()) {
                            eligibleChunksForSpawning.add(chunkpos);
                        }
                    }
                }
            }
        }

        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        BlockPos blockpos1 = worldServerIn.getSpawnPoint();
        EnumCreatureType enumcreaturetype = EnumCreatureType.MONSTER;
        int chunkCount = 0;
        StringBuffer sb = new StringBuffer();

        for (ChunkPos chunkpos1 : eligibleChunksForSpawning) {
            BlockPos blockpos = getRandomChunkPosition(worldServerIn, rand, chunkpos1.x, chunkpos1.z);
            int k1 = blockpos.getX();
            int l1 = blockpos.getY();
            int i2 = blockpos.getZ();
            IBlockState iblockstate = worldServerIn.getBlockState(blockpos);
            chunkCount++;

            if (chunkNum == chunkCount) {
                sb.append("Spawning chunk " + chunkNum + " coords: " + chunkpos1.x + "," + chunkpos1.z + "\n");
                sb.append("Block spawning point: " + blockpos + "\n");
                int l2 = k1;
                int i3 = l1;
                int j3 = i2;
                int k3 = 6;
                Biome.SpawnListEntry biome$spawnlistentry = null;
                IEntityLivingData ientitylivingdata = null;

                for (int i4 = 0; i4 < 4; ++i4) {
                    l2 += rand.nextInt(6) - rand.nextInt(6);
                    i3 += rand.nextInt(1) - rand.nextInt(1);
                    j3 += rand.nextInt(6) - rand.nextInt(6);
                    blockpos$mutableblockpos.setPos(l2, i3, j3);
                    float f = (float) l2 + 0.5F;
                    float f1 = (float) j3 + 0.5F;

                    if (!worldServerIn.isAnyPlayerWithinRangeAt((double) f, (double) i3, (double) f1, 24.0D)
                            && blockpos1.distanceSq((double) f, (double) i3, (double) f1) >= 576.0D) {
                        if (biome$spawnlistentry == null) {
                            biome$spawnlistentry = getSpawnListEntryForTypeAt(worldServerIn, rand, enumcreaturetype,
                                    blockpos$mutableblockpos);

                            if (biome$spawnlistentry == null) {
                                break;
                            } else {
                                try {
                                    sb.append("MobType: " + EntityList.getEntityString((EntityLiving) biome$spawnlistentry.entityClass.getConstructor(World.class).newInstance(worldServerIn)) + "\n");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        rand.nextFloat();

                        sb.append("Spawning position" + (i4 + 1) + ": " + blockpos$mutableblockpos + "\n");

                        EntityLiving entityliving;

                        try {
                            entityliving = biome$spawnlistentry.entityClass.getConstructor(World.class)
                                    .newInstance(worldServerIn);
                        } catch (Exception exception) {
                            return;
                        }

                        if (entityliving instanceof EntityZombieVillager) {
                            int profession = rand.nextInt(6);

                            sb.append("Zomble profession: " + profession(profession) + "\n");
                        }
                    }
                }
                notifyCommandListener(sender, this, sb.toString());
                return;
            }
        }
    }

    private String profession(int type) {
        switch (type) {
            case 0:
                return "Farmer";

            case 1:
                return "Librarian";

            case 2:
                return "Priest";

            case 3:
                return "Smith";

            case 4:
                return "Butcher";

            case 5:
            default:
                return "Nitwit";
        }
    }

    public Biome.SpawnListEntry getSpawnListEntryForTypeAt(WorldServer worldServerIn, Random rand,
                                                           EnumCreatureType creatureType, BlockPos pos) {
        List<Biome.SpawnListEntry> list = worldServerIn.getChunkProvider().getPossibleCreatures(creatureType, pos);
        return list != null && !list.isEmpty() ? (Biome.SpawnListEntry) WeightedRandom.getRandomItem(rand, list) : null;
    }

    public static boolean canCreatureTypeSpawnAtLocation(EntityLiving.SpawnPlacementType spawnPlacementTypeIn,
                                                         World worldIn, BlockPos pos) {
        if (!worldIn.getWorldBorder().contains(pos)) {
            return false;
        }
        IBlockState iblockstate = worldIn.getBlockState(pos);

        if (spawnPlacementTypeIn == EntityLiving.SpawnPlacementType.IN_WATER) {
            return iblockstate.getMaterial() == Material.WATER
                    && worldIn.getBlockState(pos.down()).getMaterial() == Material.WATER
                    && !worldIn.getBlockState(pos.up()).isNormalCube();
        } else {
            BlockPos blockpos = pos.down();

            if (!worldIn.getBlockState(blockpos).isOpaqueCube()) {
                return false;
            } else {
                Block block = worldIn.getBlockState(blockpos).getBlock();
                boolean flag = block != Blocks.BEDROCK && block != Blocks.BARRIER;
                return flag && isValidEmptySpawnBlock(iblockstate)
                        && isValidEmptySpawnBlock(worldIn.getBlockState(pos.up()));
            }
        }
    }

    public static boolean isValidEmptySpawnBlock(IBlockState state) {
        return !state.isBlockNormalCube() && !state.canProvidePower() && !state.getMaterial().isLiquid() && !BlockRailBase.isRailBlock(state);
    }

    private static BlockPos getRandomChunkPosition(World worldIn, Random rand, int x, int z) {
        Chunk chunk = worldIn.getChunk(x, z);
        int i = x * 16 + rand.nextInt(16);
        int j = z * 16 + rand.nextInt(16);
        int k = MathHelper.roundUp(chunk.getHeight(new BlockPos(i, 0, j)) + 1, 16);
        int l = rand.nextInt(k > 0 ? k : chunk.getTopFilledSegment() + 16 - 1);
        return new BlockPos(i, l, j);
    }
}
