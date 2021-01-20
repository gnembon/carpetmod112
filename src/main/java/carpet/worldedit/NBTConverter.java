package carpet.worldedit;

import java.util.*;
import java.util.Map.Entry;

/**
 * Converts between JNBT and Minecraft NBT classes.
 */
final class NBTConverter {

    private NBTConverter() {
    }

    public static net.minecraft.nbt.Tag toNative(com.sk89q.jnbt.Tag tag) {
        if (tag instanceof com.sk89q.jnbt.IntArrayTag) {
            return toNative((com.sk89q.jnbt.IntArrayTag) tag);

        } else if (tag instanceof com.sk89q.jnbt.ListTag) {
            return toNative((com.sk89q.jnbt.ListTag) tag);

        } else if (tag instanceof com.sk89q.jnbt.LongTag) {
            return toNative((com.sk89q.jnbt.LongTag) tag);

        } else if (tag instanceof com.sk89q.jnbt.StringTag) {
            return toNative((com.sk89q.jnbt.StringTag) tag);

        } else if (tag instanceof com.sk89q.jnbt.IntTag) {
            return toNative((com.sk89q.jnbt.IntTag) tag);

        } else if (tag instanceof com.sk89q.jnbt.ByteTag) {
            return toNative((com.sk89q.jnbt.ByteTag) tag);

        } else if (tag instanceof com.sk89q.jnbt.ByteArrayTag) {
            return toNative((com.sk89q.jnbt.ByteArrayTag) tag);

        } else if (tag instanceof com.sk89q.jnbt.CompoundTag) {
            return toNative((com.sk89q.jnbt.CompoundTag) tag);

        } else if (tag instanceof com.sk89q.jnbt.FloatTag) {
            return toNative((com.sk89q.jnbt.FloatTag) tag);

        } else if (tag instanceof com.sk89q.jnbt.ShortTag) {
            return toNative((com.sk89q.jnbt.ShortTag) tag);

        } else if (tag instanceof com.sk89q.jnbt.DoubleTag) {
            return toNative((com.sk89q.jnbt.DoubleTag) tag);
        } else {
            throw new IllegalArgumentException("Can't convert tag of type " + tag.getClass().getCanonicalName());
        }
    }

    public static net.minecraft.nbt.IntArrayTag toNative(com.sk89q.jnbt.IntArrayTag tag) {
        int[] value = tag.getValue();
        return new net.minecraft.nbt.IntArrayTag(Arrays.copyOf(value, value.length));
    }

    public static net.minecraft.nbt.ListTag toNative(com.sk89q.jnbt.ListTag tag) {
        net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();
        for (com.sk89q.jnbt.Tag child : tag.getValue()) {
            if (child instanceof com.sk89q.jnbt.EndTag) {
                continue;
            }
            list.add(toNative(child));
        }
        return list;
    }

    public static net.minecraft.nbt.LongTag toNative(com.sk89q.jnbt.LongTag tag) {
        return new net.minecraft.nbt.LongTag(tag.getValue());
    }

    public static net.minecraft.nbt.StringTag toNative(com.sk89q.jnbt.StringTag tag) {
        return new net.minecraft.nbt.StringTag(tag.getValue());
    }

    public static net.minecraft.nbt.IntTag toNative(com.sk89q.jnbt.IntTag tag) {
        return new net.minecraft.nbt.IntTag(tag.getValue());
    }

    public static net.minecraft.nbt.ByteTag toNative(com.sk89q.jnbt.ByteTag tag) {
        return new net.minecraft.nbt.ByteTag(tag.getValue());
    }

    public static net.minecraft.nbt.ByteArrayTag toNative(com.sk89q.jnbt.ByteArrayTag tag) {
        byte[] value = tag.getValue();
        return new net.minecraft.nbt.ByteArrayTag(Arrays.copyOf(value, value.length));
    }

    public static net.minecraft.nbt.CompoundTag toNative(com.sk89q.jnbt.CompoundTag tag) {
        net.minecraft.nbt.CompoundTag compound = new net.minecraft.nbt.CompoundTag();
        for (Entry<String, com.sk89q.jnbt.Tag> child : tag.getValue().entrySet()) {
            compound.put(child.getKey(), toNative(child.getValue()));
        }
        return compound;
    }

    public static net.minecraft.nbt.FloatTag toNative(com.sk89q.jnbt.FloatTag tag) {
        return new net.minecraft.nbt.FloatTag(tag.getValue());
    }

    public static net.minecraft.nbt.ShortTag toNative(com.sk89q.jnbt.ShortTag tag) {
        return new net.minecraft.nbt.ShortTag(tag.getValue());
    }

    public static net.minecraft.nbt.DoubleTag toNative(com.sk89q.jnbt.DoubleTag tag) {
        return new net.minecraft.nbt.DoubleTag(tag.getValue());
    }

    public static com.sk89q.jnbt.Tag fromNative(net.minecraft.nbt.Tag other) {
        if (other instanceof net.minecraft.nbt.IntArrayTag) {
            return fromNative((net.minecraft.nbt.IntArrayTag) other);

        } else if (other instanceof net.minecraft.nbt.ListTag) {
            return fromNative((net.minecraft.nbt.ListTag) other);

        } else if (other instanceof net.minecraft.nbt.EndTag) {
            return fromNative((net.minecraft.nbt.EndTag) other);

        } else if (other instanceof net.minecraft.nbt.LongTag) {
            return fromNative((net.minecraft.nbt.LongTag) other);

        } else if (other instanceof net.minecraft.nbt.StringTag) {
            return fromNative((net.minecraft.nbt.StringTag) other);

        } else if (other instanceof net.minecraft.nbt.IntTag) {
            return fromNative((net.minecraft.nbt.IntTag) other);

        } else if (other instanceof net.minecraft.nbt.ByteTag) {
            return fromNative((net.minecraft.nbt.ByteTag) other);

        } else if (other instanceof net.minecraft.nbt.ByteArrayTag) {
            return fromNative((net.minecraft.nbt.ByteArrayTag) other);

        } else if (other instanceof net.minecraft.nbt.CompoundTag) {
            return fromNative((net.minecraft.nbt.CompoundTag) other);

        } else if (other instanceof net.minecraft.nbt.FloatTag) {
            return fromNative((net.minecraft.nbt.FloatTag) other);

        } else if (other instanceof net.minecraft.nbt.ShortTag) {
            return fromNative((net.minecraft.nbt.ShortTag) other);

        } else if (other instanceof net.minecraft.nbt.DoubleTag) {
            return fromNative((net.minecraft.nbt.DoubleTag) other);
        } else {
            throw new IllegalArgumentException("Can't convert other of type " + other.getClass().getCanonicalName());
        }
    }

    public static com.sk89q.jnbt.IntArrayTag fromNative(net.minecraft.nbt.IntArrayTag other) {
        int[] value = other.getIntArray();
        return new com.sk89q.jnbt.IntArrayTag(Arrays.copyOf(value, value.length));
    }

    public static com.sk89q.jnbt.ListTag fromNative(net.minecraft.nbt.ListTag other) {
        other = other.copy();
        List<com.sk89q.jnbt.Tag> list = new ArrayList<>();
        Class<? extends com.sk89q.jnbt.Tag> listClass = com.sk89q.jnbt.StringTag.class;
        int tags = other.size();
        for (int i = 0; i < tags; i++) {
            com.sk89q.jnbt.Tag child = fromNative(other.method_32101(0));
            list.add(child);
            listClass = child.getClass();
        }
        return new com.sk89q.jnbt.ListTag(listClass, list);
    }

    public static com.sk89q.jnbt.EndTag fromNative(net.minecraft.nbt.EndTag other) {
        return new com.sk89q.jnbt.EndTag();
    }

    public static com.sk89q.jnbt.LongTag fromNative(net.minecraft.nbt.LongTag other) {
        return new com.sk89q.jnbt.LongTag(other.getLong());
    }

    public static com.sk89q.jnbt.StringTag fromNative(net.minecraft.nbt.StringTag other) {
        return new com.sk89q.jnbt.StringTag(other.asString());
    }

    public static com.sk89q.jnbt.IntTag fromNative(net.minecraft.nbt.IntTag other) {
        return new com.sk89q.jnbt.IntTag(other.getInt());
    }

    public static com.sk89q.jnbt.ByteTag fromNative(net.minecraft.nbt.ByteTag other) {
        return new com.sk89q.jnbt.ByteTag(other.getByte());
    }

    public static com.sk89q.jnbt.ByteArrayTag fromNative(net.minecraft.nbt.ByteArrayTag other) {
        byte[] value = other.getByteArray();
        return new com.sk89q.jnbt.ByteArrayTag(Arrays.copyOf(value, value.length));
    }

    public static com.sk89q.jnbt.CompoundTag fromNative(net.minecraft.nbt.CompoundTag other) {
        Collection<String> tags = other.getKeys();
        Map<String, com.sk89q.jnbt.Tag> map = new HashMap<String, com.sk89q.jnbt.Tag>();
        for (String tagName : tags) {
            map.put(tagName, fromNative(other.get(tagName)));
        }
        return new com.sk89q.jnbt.CompoundTag(map);
    }

    public static com.sk89q.jnbt.FloatTag fromNative(net.minecraft.nbt.FloatTag other) {
        return new com.sk89q.jnbt.FloatTag(other.getFloat());
    }

    public static com.sk89q.jnbt.ShortTag fromNative(net.minecraft.nbt.ShortTag other) {
        return new com.sk89q.jnbt.ShortTag(other.getShort());
    }

    public static com.sk89q.jnbt.DoubleTag fromNative(net.minecraft.nbt.DoubleTag other) {
        return new com.sk89q.jnbt.DoubleTag(other.getDouble());
    }

}
