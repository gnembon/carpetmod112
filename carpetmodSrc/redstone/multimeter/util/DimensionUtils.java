package redstone.multimeter.util;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DimensionType;

public class DimensionUtils {
	
	private static final Map<ResourceLocation, DimensionType> ID_TO_TYPE;
	private static final Map<DimensionType, ResourceLocation> TYPE_TO_ID;
	
	private static void register(ResourceLocation id, DimensionType type) {
		ID_TO_TYPE.put(id, type);
		TYPE_TO_ID.put(type, id);
	}
	
	private static void register(String name, DimensionType type) {
		register(new ResourceLocation(name), type);
	}
	
	public static DimensionType getType(ResourceLocation id) {
		return ID_TO_TYPE.get(id);
	}
	
	public static ResourceLocation getId(DimensionType type) {
		return TYPE_TO_ID.get(type);
	}
	
	static {
		
		ID_TO_TYPE = new HashMap<>();
		TYPE_TO_ID = new HashMap<>();
		
		for (DimensionType type : DimensionType.values()) {
			register(type.getName(), type);
		}
	}
}
