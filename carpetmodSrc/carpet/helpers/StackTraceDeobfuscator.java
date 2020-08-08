package carpet.helpers;

import carpet.launch.MixinServiceCarpetMod;
import carpet.utils.LRUCache;
import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;
import org.spongepowered.asm.service.mojang.MixinServiceLaunchWrapper;
import org.spongepowered.asm.util.Annotations;

import javax.annotation.Nullable;
import java.io.*;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Earthcomputer
 * <p>
 * Example: StackTraceDeobfuscator.create()
 * .withMinecraftVersion("1.12")
 * .withSnapshotMcpNames("20180713-1.12")
 * .withCurrentStackTrace()
 * .printDeobf();
 */
public class StackTraceDeobfuscator {

    private String srgUrl;
    private String namesUrl;
    private StackTraceElement[] stackTrace;

    private final LRUCache<StackTraceElement, StackTraceElement> deobfCache = new LRUCache<>(1024);
    private Map<String, String> classMappings, methodMappings, methodNames;

    private static final Map<String, Map<String, String>> classMappingsCache = new HashMap<>(),
            methodMappingsCache = new HashMap<>(),
            methodNamesCache = new HashMap<>();
    private static final Set<String> srgUrlsLoaded = new HashSet<>(), namesUrlsLoaded = new HashSet<>();
    private static final Object SRG_SYNC_LOCK = new Object();
    private static final Object NAMES_SYNC_LOCK = new Object();
    private static final String CARPET_DIRECTORY = "carpet";
    private static final String JOINED_FILE_NAME = CARPET_DIRECTORY + "/joined.srg";
    private static final String METHODS_FILE_NAME = CARPET_DIRECTORY + "/methods.csv";

    private StackTraceDeobfuscator() {
    }

    // BUILDER

    public static StackTraceDeobfuscator create() {
        return new StackTraceDeobfuscator();
    }

    public StackTraceDeobfuscator withSrgUrl(String srgUrl) {
        this.srgUrl = srgUrl;
        return this;
    }

    public StackTraceDeobfuscator withMinecraftVersion(String minecraftVersion) {
        return withSrgUrl(String.format("https://files.minecraftforge.net/maven/de/oceanlabs/mcp/mcp/%1$s/mcp-%1$s-srg.zip", minecraftVersion));
    }

    public StackTraceDeobfuscator withNamesUrl(String namesUrl) {
        this.namesUrl = namesUrl;
        return this;
    }

    // e.g. 39-1.12
    public StackTraceDeobfuscator withStableMcpNames(String mcpVersion) {
        return withNamesUrl(String.format("https://files.minecraftforge.net/maven/de/oceanlabs/mcp/mcp_stable/%1$s/mcp_stable-%1$s.zip", mcpVersion));
    }

    // e.g. 20180204-1.12
    public StackTraceDeobfuscator withSnapshotMcpNames(String mcpVersion) {
        return withNamesUrl(String.format("https://files.minecraftforge.net/maven/de/oceanlabs/mcp/mcp_snapshot/%1$s/mcp_snapshot-%1$s.zip", mcpVersion));
    }

    public StackTraceDeobfuscator withStackTrace(StackTraceElement[] stackTrace) {
        this.stackTrace = stackTrace;
        return this;
    }

    public StackTraceDeobfuscator withCurrentStackTrace() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        final String thisClass = getClass().getName();
        boolean foundThisClass = false;
        int firstIndex;
        for (firstIndex = 0; firstIndex < stackTrace.length; firstIndex++) {
            if (stackTrace[firstIndex].getClassName().equals(thisClass))
                foundThisClass = true;
            else if (foundThisClass)
                break;
        }
        return withStackTrace(Arrays.copyOfRange(stackTrace, firstIndex, stackTrace.length));
    }

    // IMPLEMENTATION

    private void ensureSrgLoaded() {
        while (true) {
            synchronized (SRG_SYNC_LOCK) {
                if (srgUrlsLoaded.contains(srgUrl))
                    break;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void ensureNamesLoaded() {
        if (namesUrl == null)
            return;

        while (true) {
            synchronized (NAMES_SYNC_LOCK) {
                if (namesUrlsLoaded.contains(namesUrl))
                    break;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadMappings() {
        boolean loadingSrg;
        synchronized (SRG_SYNC_LOCK) {
            loadingSrg = classMappingsCache.containsKey(srgUrl);
            if (!loadingSrg) {
                classMappingsCache.put(srgUrl, new HashMap<>());
                methodMappingsCache.put(srgUrl, new HashMap<>());
            }
            classMappings = classMappingsCache.get(srgUrl);
            methodMappings = methodMappingsCache.get(srgUrl);
        }


        if (!loadingSrg) {
            Thread t = new Thread(() -> {
                URL url;
                InputStream in = null;
                File carpetDirectory = new File(CARPET_DIRECTORY);
                File joinedFile = new File(JOINED_FILE_NAME);
                if (!carpetDirectory.exists() || !joinedFile.exists()) {
                    carpetDirectory.mkdir();
                    try {
                        url = new URL(srgUrl);
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        in = url.openConnection().getInputStream();
                        Files.copy(in, Paths.get(JOINED_FILE_NAME));
                    } catch (Exception e) {
                    }
                }
                try {
                    in = new FileInputStream(joinedFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    ZipInputStream zipIn = new ZipInputStream(in);
                    ZipEntry entry;
                    while ((entry = zipIn.getNextEntry()) != null) {
                        if (entry.getName().equals("joined.srg")) {
                            loadSrg(new BufferedReader(new InputStreamReader(zipIn)));
                            break;
                        } else {
                            zipIn.closeEntry();
                        }
                    }
                    zipIn.close();
                } catch (IOException e) {
                    System.err.println("Unable to load srg mappings");
                }
            });
            t.setDaemon(true);
            t.start();
        }

        if (namesUrl != null) {
            boolean loadingNames;
            synchronized (NAMES_SYNC_LOCK) {
                loadingNames = methodNamesCache.containsKey(namesUrl);
                if (!loadingNames) {
                    methodNamesCache.put(namesUrl, new HashMap<>());
                }
                methodNames = methodNamesCache.get(namesUrl);
            }

            if (!loadingNames) {
                Thread t = new Thread(() -> {
                    URL url;
                    InputStream in = null;
                    File carpetDirectory = new File(CARPET_DIRECTORY);
                    File methodsFile = new File(METHODS_FILE_NAME);
                    if (!carpetDirectory.exists() || !methodsFile.exists()) {
                        carpetDirectory.mkdir();
                        try {
                            url = new URL(namesUrl);
                        } catch (MalformedURLException e) {
                            throw new RuntimeException(e);
                        }
                        try {
                            in = url.openConnection().getInputStream();
                            Files.copy(in, Paths.get(METHODS_FILE_NAME));
                        } catch (Exception e) {
                        }
                    }
                    try {
                        in = new FileInputStream(methodsFile);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    try {
                        ZipInputStream zipIn = new ZipInputStream(in);
                        ZipEntry entry;
                        while ((entry = zipIn.getNextEntry()) != null) {
                            if (entry.getName().equals("methods.csv")) {
                                loadNames(new BufferedReader(new InputStreamReader(zipIn)));
                                break;
                            } else {
                                zipIn.closeEntry();
                            }
                        }
                        zipIn.close();
                    } catch (IOException e) {
                        System.err.println("Unable to load method names");
                    }
                });
                t.setDaemon(true);
                t.start();
            }
        }
    }

    private void loadSrg(BufferedReader in) {
        in.lines().map(line -> line.split(" ")).forEach(tokens -> {
            if (tokens[0].equals("CL:")) {
                classMappings.put(tokens[1], tokens[2]);
            } else if (tokens[0].equals("MD:")) {
                methodMappings.put(tokens[1] + tokens[2], tokens[3].substring(tokens[3].lastIndexOf('/') + 1));
            }
        });
        synchronized (SRG_SYNC_LOCK) {
            srgUrlsLoaded.add(srgUrl);
        }
    }

    private void loadNames(BufferedReader in) {
        in.lines().skip(1).map(line -> line.split(",")).forEach(tokens -> {
            methodNames.put(tokens[0], tokens[1]);
        });
        synchronized (NAMES_SYNC_LOCK) {
            namesUrlsLoaded.add(namesUrl);
        }
    }

    public void printDeobf() {
        printDeobf(System.err);
    }

    public void printDeobf(PrintStream out) {
        out.println(deobfAsString());
    }

    public String deobfAsString() {
        StackTraceElement[] elems = deobfuscate();
        return Arrays.stream(elems).map(StackTraceElement::toString).collect(Collectors.joining("\n"));
    }

    public StackTraceElement[] deobfuscate() {
        if (srgUrl == null) {
            throw new IllegalStateException("No mappings url has been set");
        }
        if (stackTrace == null) {
            throw new IllegalStateException("No stack trace has been set");
        }

        loadMappings();

        StackTraceElement[] deobfStackTrace = new StackTraceElement[stackTrace.length];
        for (int i = 0; i < stackTrace.length; i++) {
            deobfStackTrace[i] = deobfuscate(stackTrace[i]);
        }
        return deobfStackTrace;
    }

    private StackTraceElement deobfuscate(StackTraceElement elem) {
        return deobfCache.computeIfAbsent(elem, e -> {
            StackTraceElement deobf = computeDeobfuscatedElement(e);
            System.out.println(deobf);
            return deobf;
        });
    }

    private StackTraceElement computeDeobfuscatedElement(StackTraceElement elem) {
        String className = elem.getClassName().replace('.', '/');
        ensureSrgLoaded();
        // if (!classMappings.containsKey(className)) return elem;
        String methodName = elem.getMethodName();
        String fileName = elem.getFileName();
        int line = elem.getLineNumber();
        try {
            ClassNode classNode = MixinServiceCarpetMod.getClass(elem.getClassName(), true);
            if (classNode != null) {
                MethodNode method = findMethod(classNode.methods, methodName, line);
                if (method != null) {
                    Pair<ClassNode, MethodNode> mixinMethod = findMatchingMixin(method);
                    if (mixinMethod != null) {
                        // This method was merged from a mixin
                        classNode = mixinMethod.getLeft();
                        className = classNode.name;
                        // reset file name so it gets generated from the class name
                        fileName = null;
                        int origFirstLine = getFirstLine(method);
                        method = mixinMethod.getRight();
                        methodName = method.name;
                        int mixinFirstLine = getFirstLine(method);
                        // Mixin translates the line numbers, but relative to each other they are still correct
                        if (origFirstLine > 0 && mixinFirstLine > 0) {
                            line -= origFirstLine - mixinFirstLine;
                        }
                    } else {
                        // Not a mixin, try to remap method & class
                        String key = className + "/" + methodName + method.desc;
                        ensureNamesLoaded();
                        if (methodMappings.containsKey(key)) {
                            methodName = methodMappings.get(key);
                            if (methodNames != null && methodNames.containsKey(methodName))
                                methodName = methodNames.get(methodName);
                        }
                        className = classMappings.getOrDefault(className, className);
                    }
                    className = className.replace('/', '.');
                    fileName = createFileName(className, fileName);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new StackTraceElement(className, methodName, fileName, line);
    }

    @Nullable
    private static MethodNode findMethod(List<MethodNode> methods, String methodName, int line) {
        Set<MethodNode> methodsWithMatchingName = methods.stream()
                .filter(m -> methodName.equals(m.name))
                .collect(Collectors.toSet());
        if (methodsWithMatchingName.size() == 1) {
            return methodsWithMatchingName.iterator().next();
        }
        for (MethodNode method : methodsWithMatchingName) {
            if (containsLine(method, line)) {
                return method;
            }
        }
        return null;
    }

    private static int getFirstLine(MethodNode method) {
        for (Iterator<AbstractInsnNode> iter = method.instructions.iterator(); iter.hasNext();) {
            AbstractInsnNode insn = iter.next();
            if (insn instanceof LineNumberNode) {
                return ((LineNumberNode) insn).line;
            }
        }
        return -1;
    }

    private Pair<ClassNode, MethodNode> findMatchingMixin(MethodNode method) {
        if (method.visibleAnnotations == null) return null;
        AnnotationNode merged = Annotations.getVisible(method, MixinMerged.class);
        if (merged == null) return null;
        String mixinClassName = Annotations.getValue(merged, "mixin");
        String methodName = method.name.substring(method.name.lastIndexOf('$') + 1);
        try {
            ClassNode mixinClass = MixinServiceCarpetMod.getClass(mixinClassName, false);
            if (mixinClass == null) return null;
            for (MethodNode mixinMethod : mixinClass.methods) {
                if (mixinMethod.name.equals(methodName) && mixinMethod.desc.equals(method.desc)) {
                    return Pair.of(mixinClass, mixinMethod);
                }
            }
        } catch (IOException ignored) {}
        return null;
    }

    private static boolean containsLine(MethodNode method, int line) {
        for (Iterator<AbstractInsnNode> iter = method.instructions.iterator(); iter.hasNext();) {
            AbstractInsnNode insn = iter.next();
            if (insn instanceof LineNumberNode) {
                if (((LineNumberNode) insn).line == line) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void skip(DataInputStream dataIn, long n) throws IOException {
        long actual = 0;
        while (actual < n) {
            actual += dataIn.skip(n - actual);
        }
    }

    private static String createFileName(String className, String oldFileName) {
        if (oldFileName == null || "SourceFile".equals(oldFileName)) {
            if (className.contains("."))
                className = className.substring(className.lastIndexOf('.') + 1);
            if (className.contains("$"))
                className = className.substring(0, className.indexOf('$'));
            return className + ".java";
        } else {
            return oldFileName;
        }
    }

}