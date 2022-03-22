package redstone.multimeter.util;

import net.minecraft.util.ResourceLocation;

public class IdentifierUtils {
	
	public static boolean isValid(ResourceLocation id) {
		return isValid(id.getNamespace(), id.getPath());
	}
	
	public static boolean isValid(String namespace, String path) {
		return isValidNamespace(namespace) && isValidPath(path);
	}
	
	public static boolean isValidNamespace(String namespace) {
		return namespace.chars().allMatch(chr -> {
			return chr == '-' || chr == '.' || chr == '_' || (chr >= 'a' && chr <= 'z') || (chr >= '0' && chr <= '9');
		});
	}
	
	public static boolean isValidPath(String namespace) {
		return namespace.chars().allMatch(chr -> {
			return chr == '-' || chr == '.' || chr == '/' || chr == '_' || (chr >= 'a' && chr <= 'z') || (chr >= '0' && chr <= '9');
		});
	}
}
