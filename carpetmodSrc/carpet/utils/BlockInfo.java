package carpet.utils;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BlockInfo
{
    public static String getSoundName(SoundType stype)
    {
        if (stype == SoundType.WOOD   ) { return "WOOD"  ;   }
        if (stype == SoundType.GROUND ) { return "GRAVEL";   }
        if (stype == SoundType.PLANT  ) { return "GRASS" ;   }
        if (stype == SoundType.STONE  ) { return "STONE" ;   }
        if (stype == SoundType.METAL  ) { return "METAL" ;   }
        if (stype == SoundType.GLASS  ) { return "GLASS" ;   }
        if (stype == SoundType.CLOTH  ) { return "WOOL"  ;   }
        if (stype == SoundType.SAND   ) { return "SAND"  ;   }
        if (stype == SoundType.SNOW   ) { return "SNOW"  ;   }
        if (stype == SoundType.LADDER ) { return "LADDER";   }
        if (stype == SoundType.ANVIL  ) { return "ANVIL" ;   }
        if (stype == SoundType.SLIME  ) { return "SLIME" ;   }
        return "Something new";
    }

    private static String getMapColourName(MapColor colour)
    {
        if (colour == MapColor.AIR        ) { return "AIR"        ; }
        if (colour == MapColor.GRASS      ) { return "GRASS"      ; }
        if (colour == MapColor.SAND       ) { return "SAND"       ; }
        if (colour == MapColor.CLOTH      ) { return "WOOL"       ; }
        if (colour == MapColor.TNT        ) { return "TNT"        ; }
        if (colour == MapColor.ICE        ) { return "ICE"        ; }
        if (colour == MapColor.IRON       ) { return "IRON"       ; }
        if (colour == MapColor.FOLIAGE    ) { return "FOLIAGE"    ; }
        if (colour == MapColor.SNOW       ) { return "SNOW"       ; }
        if (colour == MapColor.CLAY       ) { return "CLAY"       ; }
        if (colour == MapColor.DIRT       ) { return "DIRT"       ; }
        if (colour == MapColor.STONE      ) { return "STONE"      ; }
        if (colour == MapColor.WATER      ) { return "WATER"      ; }
        if (colour == MapColor.WOOD       ) { return "WOOD"       ; }
        if (colour == MapColor.QUARTZ     ) { return "QUARTZ"     ; }
        if (colour == MapColor.ADOBE      ) { return "ADOBE"      ; }
        if (colour == MapColor.MAGENTA    ) { return "MAGENTA"    ; }
        if (colour == MapColor.LIGHT_BLUE ) { return "LIGHT_BLUE" ; }
        if (colour == MapColor.YELLOW     ) { return "YELLOW"     ; }
        if (colour == MapColor.LIME       ) { return "LIME"       ; }
        if (colour == MapColor.PINK       ) { return "PINK"       ; }
        if (colour == MapColor.GRAY       ) { return "GRAY"       ; }
        if (colour == MapColor.SILVER     ) { return "SILVER"     ; }
        if (colour == MapColor.CYAN       ) { return "CYAN"       ; }
        if (colour == MapColor.PURPLE     ) { return "PURPLE"     ; }
        if (colour == MapColor.BLUE       ) { return "BLUE"       ; }
        if (colour == MapColor.BROWN      ) { return "BROWN"      ; }
        if (colour == MapColor.GREEN      ) { return "GREEN"      ; }
        if (colour == MapColor.RED        ) { return "RED"        ; }
        if (colour == MapColor.BLACK      ) { return "BLACK"      ; }
        if (colour == MapColor.GOLD       ) { return "GOLD"       ; }
        if (colour == MapColor.DIAMOND    ) { return "DIAMOND"    ; }
        if (colour == MapColor.LAPIS      ) { return "LAPIS"      ; }
        if (colour == MapColor.EMERALD    ) { return "EMERALD"    ; }
        if (colour == MapColor.OBSIDIAN   ) { return "OBSIDIAN"   ; }
        if (colour == MapColor.NETHERRACK ) { return "NETHERRACK" ; }
        return "Something new";
    }

    private static String getMaterialName(Material material)
    {
        if (material == Material.AIR             ) { return "AIR"            ; }
        if (material == Material.GRASS           ) { return "GRASS"          ; }
        if (material == Material.GROUND          ) { return "DIRT"           ; }
        if (material == Material.WOOD            ) { return "WOOD"           ; }
        if (material == Material.ROCK            ) { return "STONE"          ; }
        if (material == Material.IRON            ) { return "IRON"           ; }
        if (material == Material.ANVIL           ) { return "ANVIL"          ; }
        if (material == Material.WATER           ) { return "WATER"          ; }
        if (material == Material.LAVA            ) { return "LAVA"           ; }
        if (material == Material.LEAVES          ) { return "LEAVES"         ; }
        if (material == Material.PLANTS          ) { return "PLANTS"         ; }
        if (material == Material.VINE            ) { return "VINE"           ; }
        if (material == Material.SPONGE          ) { return "SPONGE"         ; }
        if (material == Material.CLOTH           ) { return "WOOL"           ; }
        if (material == Material.FIRE            ) { return "FIRE"           ; }
        if (material == Material.SAND            ) { return "SAND"           ; }
        if (material == Material.CIRCUITS        ) { return "REDSTONE_COMPONENT"; }
        if (material == Material.CARPET          ) { return "CARPET"         ; }
        if (material == Material.GLASS           ) { return "GLASS"          ; }
        if (material == Material.REDSTONE_LIGHT  ) { return "REDSTONE_LAMP"  ; }
        if (material == Material.TNT             ) { return "TNT"            ; }
        if (material == Material.CORAL           ) { return "CORAL"          ; }
        if (material == Material.ICE             ) { return "ICE"            ; }
        if (material == Material.PACKED_ICE      ) { return "PACKED_ICE"     ; }
        if (material == Material.SNOW            ) { return "SNOW_LAYER"     ; }
        if (material == Material.CRAFTED_SNOW    ) { return "SNOW"           ; }
        if (material == Material.CACTUS          ) { return "CACTUS"         ; }
        if (material == Material.CLAY            ) { return "CLAY"           ; }
        if (material == Material.GOURD           ) { return "GOURD"          ; }
        if (material == Material.DRAGON_EGG      ) { return "DRAGON_EGG"     ; }
        if (material == Material.PORTAL          ) { return "PORTAL"         ; }
        if (material == Material.CAKE            ) { return "CAKE"           ; }
        if (material == Material.WEB             ) { return "COBWEB"         ; }
        if (material == Material.PISTON          ) { return "PISTON"         ; }
        if (material == Material.BARRIER         ) { return "BARRIER"        ; }
        if (material == Material.STRUCTURE_VOID  ) { return "STRUCTURE"      ; }
        return "Something new";
    }

    public static <T extends Comparable<T>> ITextComponent formatBlockProperty(IProperty<T> prop, T value) {
        ITextComponent name = new TextComponentString( prop.getName()+ "=");
        ITextComponent valueText = new TextComponentString(prop.getName(value));
        if (prop instanceof PropertyDirection) valueText.getStyle().setColor(TextFormatting.GOLD);
        else if (prop instanceof PropertyBool) valueText.getStyle().setColor((Boolean) value ? TextFormatting.GREEN : TextFormatting.RED);
        else if (prop instanceof PropertyInteger) valueText.getStyle().setColor(TextFormatting.GREEN);
        return name.appendSibling(valueText);
    }

    public static ITextComponent formatBoolean(boolean value) {
        ITextComponent component = new TextComponentString(Boolean.toString(value));
        component.getStyle().setColor(value ? TextFormatting.GREEN : TextFormatting.RED);
        return component;
    }

    public static List<ITextComponent> blockInfo(BlockPos pos, World world)
    {
        IBlockState state = world.getBlockState(pos);
        Material material = state.getMaterial();
        Block block = state.getBlock();
        String metastring = "";
        if (block.getMetaFromState(state) != 0)
        {
            metastring = ":"+block.getMetaFromState(state);
        }
        ITextComponent stateInfo = new TextComponentString("");
        boolean first = true;
        for (Map.Entry<IProperty<?>, Comparable<?>> entry : state.getProperties().entrySet()) {
            if (!first) {
                stateInfo.appendText(", ");
            }
            first = false;
            stateInfo.appendSibling(formatBlockProperty((IProperty) entry.getKey(), (Comparable) entry.getValue()));
        }
        List<ITextComponent> lst = new ArrayList<>();
        lst.add(Messenger.s(null, ""));
        lst.add(Messenger.s(null, "====================================="));
        lst.add(Messenger.s(null, String.format("Block info for %s%s (id %d%s):",Block.REGISTRY.getNameForObject(block),metastring, Block.getIdFromBlock(block), metastring )));
        lst.add(Messenger.m(null, "w  - State: ", stateInfo));
        lst.add(Messenger.s(null, String.format(" - Material: %s", getMaterialName(material))));
        lst.add(Messenger.s(null, String.format(" - Map colour: %s", getMapColourName(state.getMapColor(world, pos)))));
        lst.add(Messenger.s(null, String.format(" - Sound type: %s", getSoundName(block.getSoundType()))));
        lst.add(Messenger.s(null, ""));
        lst.add(Messenger.m(null, "w  - Full block: ", formatBoolean(state.isFullBlock())));
        lst.add(Messenger.m(null, "w  - Full cube: ", formatBoolean(state.isFullCube())));
        lst.add(Messenger.m(null, "w  - Normal cube: ", formatBoolean(state.isNormalCube())));
        lst.add(Messenger.m(null, "w  - Block normal cube: ", formatBoolean(state.isBlockNormalCube())));
        lst.add(Messenger.m(null, "w  - Is liquid: ", formatBoolean(material.isLiquid())));
        lst.add(Messenger.m(null, "w  - Is solid: ", formatBoolean(material.isSolid())));
        lst.add(Messenger.s(null, ""));
        lst.add(Messenger.s(null, String.format(" - Light in: %d, above: %d", world.getLight(pos), world.getLight(pos.up()))));
        lst.add(Messenger.s(null, String.format(" - Brightness in: %.2f, above: %.2f", world.getLightBrightness(pos), world.getLightBrightness(pos.up()))));
        lst.add(Messenger.m(null, "w  - Is opaque: ", formatBoolean(material.isOpaque())));
        lst.add(Messenger.s(null, String.format(" - Light opacity: %d", state.getLightOpacity())));
        lst.add(Messenger.m(null, "w  - Blocks light: ", formatBoolean(material.blocksLight())));
        lst.add(Messenger.s(null, String.format(" - Emitted light: %d", state.getLightValue())));
        lst.add(Messenger.m(null, "w  - Picks neighbour light value: ", formatBoolean(state.useNeighborBrightness())));
        lst.add(Messenger.s(null, ""));
        lst.add(Messenger.m(null, "w  - Causes suffocation: ", formatBoolean(state.causesSuffocation())));
        lst.add(Messenger.m(null, "w  - Blocks movement: ", formatBoolean(!block.isPassable(world, pos))));
        lst.add(Messenger.m(null, "w  - Can burn: ", formatBoolean(material.getCanBurn())));
        lst.add(Messenger.m(null, "w  - Requires a tool: ", formatBoolean(!material.isToolNotRequired())));
        lst.add(Messenger.s(null, String.format(" - Hardness: %.2f", state.getBlockHardness(world, pos))));
        lst.add(Messenger.s(null, String.format(" - Blast resistance: %.2f", block.getExplosionResistance(null))));
        lst.add(Messenger.m(null, "w  - Ticks randomly: ", formatBoolean(block.getTickRandomly())));
        lst.add(Messenger.s(null, ""));
        lst.add(Messenger.m(null, "w  - Can provide power: ", formatBoolean(state.canProvidePower())));
        lst.add(Messenger.s(null, String.format(" - Strong power level: %d", world.getStrongPower(pos))));
        lst.add(Messenger.s(null, String.format(" - Redstone power level: %d", world.getRedstonePowerFromNeighbors(pos))));
        lst.add(Messenger.s(null, ""));
        lst.add(wander_chances(pos.up(), world));

        return lst;
    }

    private static ITextComponent wander_chances(BlockPos pos, World worldIn)
    {
        EntityCreature creature = new EntityPigZombie(worldIn);
        creature.onInitialSpawn(worldIn.getDifficultyForLocation(pos), null);
        creature.setLocationAndAngles(pos.getX()+0.5F, pos.getY(), pos.getZ()+0.5F, 0.0F, 0.0F);
        EntityAIWander wander = new EntityAIWander(creature, 0.8D);
        int success = 0;
        for (int i=0; i<1000; i++)
        {

            Vec3d vec = RandomPositionGenerator.findRandomTarget(creature, 10, 7);
            if (vec == null)
            {
                continue;
            }
            success++;
        }
        long total_ticks = 0;
        for (int trie=0; trie<1000; trie++)
        {
            int i;
            for (i=1;i<30*20*60; i++) //*60 used to be 5 hours, limited to 30 mins
            {
                if (wander.shouldExecute())
                {
                    break;
                }
            }
            total_ticks += 3*i;
        }
        creature.setDead();
        long total_time = (total_ticks)/1000/20;
        return Messenger.s(null, String.format(" - Wander chance above: %.1f%%\n - Average standby above: %s",
                (100.0F*success)/1000,
                ((total_time>5000)?"INFINITY":(total_time + " s"))
        ));
    }
}
