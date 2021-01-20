package carpet.commands;

import java.util.*;

import javax.annotation.Nullable;

import carpet.mixin.accessors.OverworldChunkGeneratorAccessor;
import carpet.mixin.accessors.ServerChunkManagerAccessor;
import carpet.mixin.accessors.WorldAccessor;
import carpet.mixin.accessors.ServerWorldAccessor;
import carpet.utils.Messenger;
import carpet.utils.extensions.ExtendedFloatingIslandsChunkGenerator;
import carpet.utils.extensions.ExtendedWorld;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import carpet.CarpetSettings;
import net.minecraft.*;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.WeightedPicker;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.chunk.OverworldChunkGenerator;

public class CommandRNG extends CommandCarpetBase {
    /**
     * Gets the name of the command
     */
    @Override
    public String method_29277() {
        return "rng";
    }

    /**
     * Gets the usage string for the command.
     *
     * @param sender The ICommandSender who is requesting usage details
     */
    @Override
    public String method_29275(class_2010 sender) {
        return "rng <rule> <value>";
    }

    /**
     * Callback for when the command is executed
     *
     * @param server The server instance
     * @param sender The sender who executed the command
     * @param args   The arguments that were passed
     */
    @Override
    public void method_29272(MinecraftServer server, class_2010 sender, String[] args) throws class_6175 {
        if (!command_enabled("commandRNG", sender)) return;
        if (args.length <=0) throw new class_6182(method_29275(sender));
        if ("seed".equalsIgnoreCase(args[0])) {
            try {
                World world = sender.method_29608();

                ChunkManager gen = ((ServerChunkManagerAccessor) world.getChunkManager()).getChunkGenerator();

                if (gen instanceof OverworldChunkGenerator) {
                    int x;
                    int z;
                    if (args.length < 3) {
                        x = sender.method_29606().getX() / 16;
                        z = sender.method_29606().getZ() / 16;
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
                            x = sender.method_29606().getX() / 16;
                            z = sender.method_29606().getZ() / 16;
                            if (x < 0) {
                                x--;
                            }
                            if (z < 0) {
                                z--;
                            }
                        }
                    }

                    ((OverworldChunkGeneratorAccessor) gen).getWoodlandMansionGenerator().method_27580(world, x, z, null);
                    method_28710(sender, this,
                            String.format("Seed at chunk coords: %d %d seed: %d", x, z, ((ExtendedWorld) world).getRandSeed()));
                }
            } catch (Exception e) {
                System.out.println("some error at seed");
            }
            return;
        } else if ("setSeed".equalsIgnoreCase(args[0])) {
            try {
                CarpetSettings.setSeed = Long.parseLong(args[1]);
                method_28710(sender, this, "RNG seed set to " + args[1]);
            } catch (Exception e) {
                method_28710(sender, this, "rng setSeed <seed>, default seed to 0 for turning off RNG.");
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
                method_28710(sender, this, "rng getMobspawningChunk <seed> <chunkNum> <playersHashSize>");
                return;
            }

            World world = sender.method_29608();
            if (world instanceof ServerWorld && sender instanceof PlayerEntity) {
                displayMobSpawningChunkInfo((ServerWorld) world, sender, seed, chunkNum, playerSize);
            }
        } else if ("randomtickedChunksCount".equalsIgnoreCase(args[0])) {
            World world = sender.method_29608();
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

            if (world instanceof ServerWorld) {
                int count = 0;
                for (Iterator<WorldChunk> iterator = ((ServerWorldAccessor) world).getPlayerChunkMap().method_33585(); iterator
                        .hasNext() && chunkCount < iters; world.profiler.pop()) {
                    WorldChunk chunk = iterator.next();
                    if (iters != Integer.MAX_VALUE) {
                        chunkCount++;
                        x = chunk.field_25365;
                        z = chunk.field_25366;
                    }
                    count++;
                }
                if (iters != Integer.MAX_VALUE) {
                    method_28710(sender, this,
                            String.format(
                                    "Number of chunks till chunk index from player position: %d at chunk coord: (%d,%d)",
                                    count, x, z));
                } else {
                    method_28710(sender, this,
                            String.format("Number of chunks around the player random ticking: %d", count));
                }
            }
        } else if ("randomtickedBlocksInRange".equalsIgnoreCase(args[0])) {
            World world = sender.method_29608();

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

            if (world instanceof ServerWorld) {
                int count = 0;
                for (Iterator<WorldChunk> iterator = ((ServerWorldAccessor) world).getPlayerChunkMap().method_33585(); iterator
                        .hasNext() && chunkCount < iters; world.profiler.pop()) {
                    WorldChunk chunk = iterator.next();
                    if (iters != Integer.MAX_VALUE) {
                        chunkCount++;
                        x = chunk.field_25365;
                        z = chunk.field_25366;
                    }
                    if (!check || (x == chunk.field_25365 && z == chunk.field_25366)) {
                        for (ChunkSection section : chunk.method_27413()) {
                            if (section != WorldChunk.EMPTY_SECTION) {
                                for (int i = 0; i < 16; ++i) {
                                    for (int j = 0; j < 16; ++j) {
                                        for (int k = 0; k < 16; ++k) {
                                            Block block = section.method_27435(i, j, k).getBlock();

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
                            method_28710(sender, this,
                                    String.format("Number of rand influencing blocks: %d", count));
                            return;
                        }
                    }
                }
                if (!check) {
                    if (iters != Integer.MAX_VALUE) {
                        method_28710(sender, this, String.format(
                                "Number of rand influencing blocks: %d  until hitting chunk: (%d,%d)", count, x, z));
                    } else {
                        method_28710(sender, this,
                                String.format("Number of rand influencing blocks: %d", count));
                    }
                } else {
                    method_28710(sender, this, "Chosen location is not in loaded random ticked area.");
                }
            }
        } else if ("getLCG".equalsIgnoreCase(args[0])) {
            ArrayList<Object> strings = new ArrayList<>();
            for (World world : server.worlds) {
                int seed = ((WorldAccessor) world).getUpdateLCG();
                Messenger.m(sender, "w " + world.dimension.getType().toString() + ": ", "c " + seed, "^w Dimension LCG at beginning of game loop : " + seed, "?/rng setLCG " + world.dimension.getType().toString() + " " + seed);
            }
        } else if ("setLCG".equalsIgnoreCase(args[0])) {
            if (args.length == 3) {
                for (World world : server.worlds) {
                    if (world.dimension.getType().toString().equals(args[1])) {
                        try {
                            ((WorldAccessor) world).setUpdateLCG(Integer.parseInt(args[2]));
                            method_28710(sender, this, world.dimension.getType() + " LCG changed to " + args[2]);
                        } catch (Exception e) {
                        }
                    }
                }
            }
        } else if ("getEndChunkSeed".equalsIgnoreCase(args[0])) {
            ServerChunkManager chunkProvider = server.getWorldById(1).getChunkManager();
            ExtendedFloatingIslandsChunkGenerator chunkGeneratorEnd = ((ExtendedFloatingIslandsChunkGenerator) ((ServerChunkManagerAccessor) chunkProvider).getChunkGenerator());
            long seed = chunkGeneratorEnd.getLastRandomSeed();
            if (chunkGeneratorEnd.wasRandomSeedUsed()) {
                sender.sendMessage(new LiteralText("The ChunkGeneratorEnd seed " + seed + " was already used for population and is currently random!"));
            } else {
                sender.sendMessage(new LiteralText("Current ChunkGeneratorEnd seed: " + seed));
            }
        } else if ("setEndChunkSeed".equalsIgnoreCase(args[0])) {
            if (args.length == 2 || (args.length == 3 && "once".equalsIgnoreCase(args[2]))) {
                ServerChunkManager chunkProvider = server.getWorldById(1).getChunkManager();
                ExtendedFloatingIslandsChunkGenerator chunkGeneratorEnd = ((ExtendedFloatingIslandsChunkGenerator) ((ServerChunkManagerAccessor) chunkProvider).getChunkGenerator());
                long seed = method_28738(args[1]);
                chunkGeneratorEnd.setRandomSeedUsed(false);
                chunkGeneratorEnd.setEndChunkSeed(seed);
                if (args.length == 2) {
                    CarpetSettings.endChunkSeed = seed;
                    sender.sendMessage(new LiteralText("Set the ChunkGeneratorEnd seed to: " + CarpetSettings.endChunkSeed));
                } else if (args.length == 3) {
                    chunkGeneratorEnd.setEndChunkSeed(seed);
                    sender.sendMessage(new LiteralText("Set the ChunkGeneratorEnd seed once to: " + chunkGeneratorEnd.getLastRandomSeed()));
                }
            } else {
                throw new class_6182("/rng setEndChunkSeed <seed> [once]");
            }
        }
    }

    @Override
    public List<String> method_29273(MinecraftServer server, class_2010 sender, String[] args,
                                          @Nullable BlockPos targetPos) {
        if (!CarpetSettings.commandRNG) {
            return Collections.<String>emptyList();
        }
        if (args.length == 1) {
            return method_28732(args, "seed", "setSeed", "getMobspawningChunk",
                    "randomtickedChunksCount", "randomtickedBlocksInRange", "logWeather", "getLCG", "setLCG", "getEndChunkSeed", "setEndChunkSeed");
        }
        if (args.length >= 2) {
            if ("seed".equalsIgnoreCase(args[0])) {
                BlockPos pos = sender.method_29606();
                return getTabComplet(args, 1, pos);
            }
            if ("setSeed".equalsIgnoreCase(args[0])) {
                return method_28732(args, "0");
            }
            if ("getMobspawningChunk".equalsIgnoreCase(args[0])) {
                return method_28732(args, "1");
            }
            if ("randomtickedBlocksInRange".equalsIgnoreCase(args[0])) {
                BlockPos pos = sender.method_29606();
                return getTabComplet(args, 1, pos);
            }
            if ("logWeather".equalsIgnoreCase(args[0])) {
                return method_28732(args, "true", "false");
            }
            if ("setLCG".equalsIgnoreCase(args[0])) {
                return method_28732(args, "OVERWORLD", "NETHER", "THE_END");
            }
            if ("setEndChunkSeed".equalsIgnoreCase(args[0]) && args.length == 3) {
                return method_28732(args, "once");
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

    public void displayMobSpawningChunkInfo(ServerWorld worldServerIn, class_2010 sender, long seed, int chunkNum,
                                            int playerSize) {
        PlayerEntity entityplayer = (PlayerEntity) sender;
        Set<ColumnPos> eligibleChunksForSpawning = Sets.newHashSet();

        for (int i = 0; i < (playerSize * 225); i++) {
            ColumnPos chunkpos = new ColumnPos(0, i);
            eligibleChunksForSpawning.add(chunkpos);
        }
        eligibleChunksForSpawning.clear();

        Random rand = new Random();
        rand.setSeed(seed ^ 0x5DEECE66DL);

        int j = MathHelper.floor(entityplayer.field_33071 / 16.0D);
        int k = MathHelper.floor(entityplayer.field_33073 / 16.0D);
        int l = 8;

        for (int i1 = -8; i1 <= 8; ++i1) {
            for (int j1 = -8; j1 <= 8; ++j1) {
                boolean flag = i1 == -8 || i1 == 8 || j1 == -8 || j1 == 8;
                ColumnPos chunkpos = new ColumnPos(i1 + j, j1 + k);

                if (!eligibleChunksForSpawning.contains(chunkpos)) {
                    if (!flag && worldServerIn.getWorldBorder().method_27301(chunkpos)) {
                        class_4615 playerchunkmapentry = worldServerIn.getRaidManager()
                                .method_33587(chunkpos.x, chunkpos.z);

                        if (playerchunkmapentry != null && playerchunkmapentry.method_33574()) {
                            eligibleChunksForSpawning.add(chunkpos);
                        }
                    }
                }
            }
        }

        BlockPos.Mutable blockpos$mutableblockpos = new BlockPos.Mutable();
        BlockPos blockpos1 = worldServerIn.getSpawnPos();
        EntityCategory enumcreaturetype = EntityCategory.MONSTER;
        int chunkCount = 0;
        StringBuffer sb = new StringBuffer();

        for (ColumnPos chunkpos1 : eligibleChunksForSpawning) {
            BlockPos blockpos = getRandomChunkPosition(worldServerIn, rand, chunkpos1.x, chunkpos1.z);
            int k1 = blockpos.getX();
            int l1 = blockpos.getY();
            int i2 = blockpos.getZ();
            BlockState iblockstate = worldServerIn.getBlockState(blockpos);
            chunkCount++;

            if (chunkNum == chunkCount) {
                sb.append("Spawning chunk " + chunkNum + " coords: " + chunkpos1.x + "," + chunkpos1.z + "\n");
                sb.append("Block spawning point: " + blockpos + "\n");
                int l2 = k1;
                int i3 = l1;
                int j3 = i2;
                int k3 = 6;
                Biome.SpawnEntry biome$spawnlistentry = null;
                EntityData ientitylivingdata = null;

                for (int i4 = 0; i4 < 4; ++i4) {
                    l2 += rand.nextInt(6) - rand.nextInt(6);
                    i3 += rand.nextInt(1) - rand.nextInt(1);
                    j3 += rand.nextInt(6) - rand.nextInt(6);
                    blockpos$mutableblockpos.set(l2, i3, j3);
                    float f = (float) l2 + 0.5F;
                    float f1 = (float) j3 + 0.5F;

                    if (!worldServerIn.method_25966(f, i3, f1, 24.0D)
                            && blockpos1.getSquaredDistance(f, i3, f1) >= 576.0D) {
                        if (biome$spawnlistentry == null) {
                            biome$spawnlistentry = getSpawnListEntryForTypeAt(worldServerIn, rand, enumcreaturetype,
                                    blockpos$mutableblockpos);

                            if (biome$spawnlistentry == null) {
                                break;
                            } else {
                                try {
                                    sb.append("MobType: " + class_2245.method_34602(biome$spawnlistentry.field_23703.getConstructor(World.class).newInstance(worldServerIn)) + "\n");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        rand.nextFloat();

                        sb.append("Spawning position" + (i4 + 1) + ": " + blockpos$mutableblockpos + "\n");

                        MobEntity entityliving;

                        try {
                            entityliving = biome$spawnlistentry.field_23703.getConstructor(World.class)
                                    .newInstance(worldServerIn);
                        } catch (Exception exception) {
                            return;
                        }

                        if (entityliving instanceof ZombieVillagerEntity) {
                            int profession = rand.nextInt(6);

                            sb.append("Zomble profession: " + profession(profession) + "\n");
                        }
                    }
                }
                method_28710(sender, this, sb.toString());
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

    public Biome.SpawnEntry getSpawnListEntryForTypeAt(ServerWorld worldServerIn, Random rand,
                                                           EntityCategory creatureType, BlockPos pos) {
        List<Biome.SpawnEntry> list = worldServerIn.getChunkManager().method_33449(creatureType, pos);
        return list != null && !list.isEmpty() ? WeightedPicker.getRandom(rand, list) : null;
    }

    public static boolean canCreatureTypeSpawnAtLocation(MobEntity.class_6451 spawnPlacementTypeIn,
                                                         World worldIn, BlockPos pos) {
        if (!worldIn.getWorldBorder().contains(pos)) {
            return false;
        }
        BlockState iblockstate = worldIn.getBlockState(pos);

        if (spawnPlacementTypeIn == MobEntity.class_6451.field_33241) {
            return iblockstate.getMaterial() == Material.WATER
                    && worldIn.getBlockState(pos.method_31898()).getMaterial() == Material.WATER
                    && !worldIn.getBlockState(pos.up()).method_27207();
        } else {
            BlockPos blockpos = pos.method_31898();

            if (!worldIn.getBlockState(blockpos).method_27211()) {
                return false;
            } else {
                Block block = worldIn.getBlockState(blockpos).getBlock();
                boolean flag = block != Blocks.BEDROCK && block != Blocks.BARRIER;
                return flag && isValidEmptySpawnBlock(iblockstate)
                        && isValidEmptySpawnBlock(worldIn.getBlockState(pos.up()));
            }
        }
    }

    public static boolean isValidEmptySpawnBlock(BlockState state) {
        return !state.method_27206() && !state.method_27208() && !state.getMaterial().isLiquid() && !AbstractRailBlock.method_26336(state);
    }

    private static BlockPos getRandomChunkPosition(World worldIn, Random rand, int x, int z) {
        WorldChunk chunk = worldIn.method_25975(x, z);
        int i = x * 16 + rand.nextInt(16);
        int j = z * 16 + rand.nextInt(16);
        int k = MathHelper.roundUp(chunk.method_27405(new BlockPos(i, 0, j)) + 1, 16);
        int l = rand.nextInt(k > 0 ? k : chunk.method_27410() + 16 - 1);
        return new BlockPos(i, l, j);
    }
}
