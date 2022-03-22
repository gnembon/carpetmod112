package redstone.multimeter.registry;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.util.ResourceLocation;

import redstone.multimeter.RedstoneMultimeter;

public class SupplierClazzRegistry<T> {
	
	private final ResourceLocation id;
	private final Map<Class<? extends T>, ResourceLocation> clazzToId;
	private final Map<ResourceLocation, Supplier<? extends T>> idToSupplier;
	
	public SupplierClazzRegistry(String name) {
		this.id = new ResourceLocation(RedstoneMultimeter.NAMESPACE, name);
		this.clazzToId = new HashMap<>();
		this.idToSupplier = new HashMap<>();
	}
	
	public ResourceLocation getId() {
		return id;
	}
	
	@SuppressWarnings("unchecked")
	public <P extends T> P get(ResourceLocation id) {
		Supplier<? extends T> objSupplier = idToSupplier.get(id);
		return objSupplier == null ? null : (P)objSupplier.get();
	}
	
	public <P extends T> ResourceLocation getId(P obj) {
		return clazzToId.get(obj.getClass());
	}
	
	public <P extends T> void register(String name, Class<P> clazz, Supplier<P> supplier) {
		String namespace = id.getNamespace();
		String path = String.format("%s/%s", id.getPath(), name);
		ResourceLocation id = new ResourceLocation(namespace, path);
		
		if (clazzToId.containsKey(clazz)) {
			throw new IllegalStateException("Registry " + this.id + " already registered an entry with clazz " + clazz);
		}
		if (idToSupplier.containsKey(id)) {
			throw new IllegalStateException("Registry " + this.id + " already registered an entry with id " + id);
		}
		
		clazzToId.put(clazz, id);
		idToSupplier.put(id, supplier);
	}
}
