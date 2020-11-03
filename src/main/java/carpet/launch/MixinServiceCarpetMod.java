package carpet.launch;

import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import org.spongepowered.asm.mixin.transformer.ext.Extensions;
import org.spongepowered.asm.mixin.transformer.ext.IExtension;
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext;
import org.spongepowered.asm.service.mojang.MixinServiceLaunchWrapper;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MixinServiceCarpetMod extends MixinServiceLaunchWrapper {
    private static MixinServiceCarpetMod INSTANCE;
    private static final Map<String, byte[]> generatedClasses = new HashMap<>();

    public MixinServiceCarpetMod() {
        if (INSTANCE != null) throw new IllegalStateException("Tried to instantiate MixinService twice");
        INSTANCE = this;
    }

    public static void registerExtensions() {
        IMixinTransformer transformer = (IMixinTransformer) MixinEnvironment.getCurrentEnvironment().getActiveTransformer();
        Extensions extensions = (Extensions) transformer.getExtensions();
        extensions.add(new IExtension() {
            @Override
            public boolean checkActive(MixinEnvironment environment) {
                return true;
            }

            @Override
            public void preApply(ITargetClassContext context) {}

            @Override
            public void postApply(ITargetClassContext context) {}

            @Override
            public void export(MixinEnvironment env, String name, boolean force, ClassNode classNode) {
                ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
                classNode.accept(writer);
                generatedClasses.put(name.replace('.', '/'), writer.toByteArray());
            }
        });
    }

    @Override
    public Collection<String> getPlatformAgents() {
        return Collections.singleton("carpet.launch.MixinPlatformAgent");
    }

    public static MixinServiceCarpetMod getInstance() {
        if (INSTANCE == null) throw new IllegalStateException("Not initialized");
        return INSTANCE;
    }

    public static ClassNode getClass(String name, boolean transformed) throws IOException {
        String className = name.replace('.', '/');
        byte[] bytes = getClassBinary(className, transformed);
        if (bytes != null) {
            ClassReader reader = new ClassReader(bytes);
            ClassNode node = new ClassNode(Opcodes.ASM5);
            reader.accept(node, ClassReader.EXPAND_FRAMES);
            return node;
        }
        try {
            ClassNode transformedClass = MixinServiceCarpetMod.getInstance().getClassNode(className, transformed);
            if (transformedClass != null) return transformedClass;
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static byte[] getClassBinary(String name, boolean transformed) throws IOException {
        if (!transformed) return Launch.classLoader.getClassBytes(name.replace('/', '.'));
        return generatedClasses.get(name);
    }
}
