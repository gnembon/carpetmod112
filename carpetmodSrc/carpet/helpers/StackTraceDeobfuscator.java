package carpet.helpers;

import java.io.*;
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
    private ClassLoader classLoader = StackTraceDeobfuscator.class.getClassLoader();

    private Map<String, String> classMappings, methodMappings, methodDescCache, methodNames;

    private static final Map<String, Map<String, String>> classMappingsCache = new HashMap<>(),
            methodMappingsCache = new HashMap<>(),
            methodDescCaches = new HashMap<>(),
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

    public StackTraceDeobfuscator withClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
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
                methodDescCaches.put(srgUrl, new HashMap<>());
            }
            classMappings = classMappingsCache.get(srgUrl);
            methodMappings = methodMappingsCache.get(srgUrl);
            methodDescCache = methodDescCaches.get(srgUrl);
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
        String className = elem.getClassName().replace('.', '/');

        ensureSrgLoaded();
        if (!classMappings.containsKey(className))
            return elem;

        String methodName = elem.getMethodName();
        String methodDesc;
        try {
            methodDesc = getMethodDesc(elem);
        } catch (IOException e) {
            methodDesc = null;
        }
        if (methodDesc == null) {
            System.err.println("Failed to get method desc for " + className + "/" + methodName + "@" + elem.getLineNumber());
            return elem;
        }

        String key = className + "/" + methodName + methodDesc;
        ensureNamesLoaded();
        if (methodMappings.containsKey(key)) {
            methodName = methodMappings.get(key);
            if (methodNames != null && methodNames.containsKey(methodName))
                methodName = methodNames.get(methodName);
        }
        className = classMappings.get(className);
        className = className.replace('/', '.');

        return new StackTraceElement(className, methodName, createFileName(className, elem.getFileName()), elem.getLineNumber());
    }

    private String getMethodDesc(StackTraceElement elem) throws IOException {
        String className = elem.getClassName().replace('.', '/');
        String methodName = elem.getMethodName();
        int lineNumber = elem.getLineNumber();
        if (methodDescCache.containsKey(className + "/" + methodName + "@" + lineNumber)) {
            return methodDescCache.get(className + "/" + methodName + "@" + lineNumber);
        }

        // Java Class File Format:
        // https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html

        InputStream is = classLoader.getResourceAsStream(className + ".class");
        if (is == null)
            return null;
        DataInputStream dataIn = new DataInputStream(is);
        skip(dataIn, 8); // header

        // constant pool
        Map<Integer, String> stringConstants = new HashMap<>();
        int cpCount = dataIn.readUnsignedShort();
        int[] constSizes = {-1, -1, -1, 4, 4, 8, 8, 2, 2, 4, 4, 4, 4, -1, -1, 3, 2, -1, 4};
        for (int cpIndex = 1; cpIndex < cpCount; cpIndex++) {
            int tag = dataIn.readUnsignedByte();
            if (tag == 1) { // CONSTANT_Utf8
                stringConstants.put(cpIndex, dataIn.readUTF());
                //System.out.println(cpIndex + " -> " + stringConstants.get(cpIndex));
            } else {
                if (tag == 5 || tag == 6) { // CONSTANT_Long or CONSTANT_Double
                    cpIndex++;
                }
                skip(dataIn, constSizes[tag]);
            }
        }

        skip(dataIn, 6); // more boring information

        // Need to know interface count to know how much to skip over
        int interfaceCount = dataIn.readUnsignedShort();
        skip(dataIn, interfaceCount * 2);

        // Skip over the fields
        int fieldCount = dataIn.readUnsignedShort();
        for (int i = 0; i < fieldCount; i++) {
            skip(dataIn, 6);
            int attrCount = dataIn.readUnsignedShort();
            for (int j = 0; j < attrCount; j++) {
                skip(dataIn, 2);
                long length = Integer.toUnsignedLong(dataIn.readInt());
                skip(dataIn, length);
            }
        }

        // Methods, now we're talking
        int methodCount = dataIn.readUnsignedShort();
        for (int i = 0; i < methodCount; i++) {
            skip(dataIn, 2); // access
            String name = stringConstants.get(dataIn.readUnsignedShort());
            String desc = stringConstants.get(dataIn.readUnsignedShort());
            int attrCount = dataIn.readUnsignedShort();
            for (int j = 0; j < attrCount; j++) {
                String attrName = stringConstants.get(dataIn.readUnsignedShort());
                long length = Integer.toUnsignedLong(dataIn.readInt());
                if (name.equals(methodName) && attrName.equals("Code")) {
                    skip(dataIn, 4); // max stack + locals
                    long codeLength = Integer.toUnsignedLong(dataIn.readInt());
                    skip(dataIn, codeLength);
                    int exceptionTableLength = dataIn.readUnsignedShort();
                    skip(dataIn, exceptionTableLength * 8);
                    int codeAttrCount = dataIn.readUnsignedShort();
                    for (int k = 0; k < codeAttrCount; k++) {
                        String codeAttrName = stringConstants.get(dataIn.readUnsignedShort());
                        long codeAttrLength = Integer.toUnsignedLong(dataIn.readInt());
                        if (codeAttrName.equals("LineNumberTable")) {
                            int lineNumberTableLength = dataIn.readUnsignedShort();
                            for (int l = 0; l < lineNumberTableLength; l++) {
                                skip(dataIn, 2); // start_pc
                                int lineNo = dataIn.readUnsignedShort();
                                if (lineNo == lineNumber) {
                                    methodDescCache.put(className + "/" + methodName + "@" + lineNumber, desc);
                                    return desc;
                                }
                            }
                        } else {
                            skip(dataIn, codeAttrLength);
                        }
                    }
                } else {
                    skip(dataIn, length);
                }
            }
        }

        return null;
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