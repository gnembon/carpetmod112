package carpet.helpers;

import carpet.CarpetSettings;
import carpet.utils.LRUCache;
import carpetmod.Build;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.mapping.reader.v2.MappingGetter;
import net.fabricmc.mapping.reader.v2.TinyMetadata;
import net.fabricmc.mapping.reader.v2.TinyV2Factory;
import net.fabricmc.mapping.reader.v2.TinyVisitor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
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
    private static final Logger LOGGER = LogManager.getLogger("Carpet|Deobfuscator");
    private static final Path CACHE_DIRECTORY = Paths.get(".fabric", "carpetCache");
    private final LRUCache<StackTraceElement, StackTraceElement> deobfCache = new LRUCache<>(1024);
    private final Map<String, String> mappings;

    private StackTraceDeobfuscator(Map<String, String> mappings) {
        this.mappings = mappings;
    }

    public static class Builder {
        private CompletableFuture<URI> url;
        private Map<String, String> mappings;
        private String path = "mappings/mappings.tiny";
        private String namespaceFrom = FabricLoader.getInstance().getMappingResolver().getCurrentRuntimeNamespace();
        private String namespaceTo = "named";

        public Builder withNamesUrl(CompletableFuture<URI> url) {
            this.url = url;
            return this;
        }

        public Builder withNamesUrl(URI url) {
            return withNamesUrl(CompletableFuture.completedFuture(url));
        }

        public Builder withPath(@Nullable String path) {
            this.path = path;
            return this;
        }

        public Builder withNamespaces(String namespaceFrom, String namespaceTo) {
            this.namespaceFrom = namespaceFrom;
            this.namespaceTo = namespaceTo;
            return this;
        }

        public Builder withLegacyYarnNames(String version) {
            try {
                return withNamesUrl(new URI("https://dl.bintray.com/legacy-fabric/Legacy-Fabric-Maven/net/fabricmc/yarn/" + version + "/yarn-" + version + "-v2.jar"));
            } catch (URISyntaxException e) {
                throw new IllegalStateException(e);
            }
        }

        public Builder withMappings(Map<String, String> mappings) {
            this.mappings = mappings;
            return this;
        }

        public CompletableFuture<StackTraceDeobfuscator> build() {
            if (mappings != null) return CompletableFuture.completedFuture(new StackTraceDeobfuscator(mappings));
            if (url == null) throw new IllegalStateException("No URL set");
            return url.thenComposeAsync(StackTraceDeobfuscator::downloadFile).thenApply(in -> {
                if (path == null) {
                    try {
                        return new StackTraceDeobfuscator(readMappings(in, namespaceFrom, namespaceTo));
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    } finally {
                        try {
                            in.close();
                        } catch (IOException ignored) {}
                    }
                } else {
                    try (ZipInputStream zip = new ZipInputStream(in)) {
                        ZipEntry entry;
                        while ((entry = zip.getNextEntry()) != null) {
                            if (entry.getName().equals(path)) {
                                return new StackTraceDeobfuscator(readMappings(zip, namespaceFrom, namespaceTo));
                            } else {
                                zip.closeEntry();
                            }
                        }
                        throw new IllegalStateException("Entry '" + path + "' not found in mappings jar");
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
            });
        }
    }

    public static CompletableFuture<StackTraceDeobfuscator> loadDefault() {
        Builder builder = new Builder();
        if (FabricLoader.getInstance().isDevelopmentEnvironment() && false) {
            builder.withMappings(new HashMap<>());
        } else {
            builder.withLegacyYarnNames(Build.YARN_MAPPINGS);
        }
        return builder.build();
    }

    private static CompletableFuture<InputStream> downloadFile(URI url) {
        return CompletableFuture.supplyAsync(() -> {
            Path cachePath = getCacheFile(url);
            if (Files.exists(cachePath)) {
                try {
                    return Files.newInputStream(cachePath);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
            try {
                LOGGER.info("Downloading {}", url);
                Files.createDirectories(cachePath.getParent());
                try (InputStream in = url.toURL().openStream()) {
                    Files.copy(in, cachePath);
                }
                return Files.newInputStream(cachePath);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    private static Path getCacheFile(URI url) {
        String path = url.getPath();
        path = path.substring(path.lastIndexOf('/') + 1);
        return CACHE_DIRECTORY.resolve(path);
    }

    private static Map<String, String> readMappings(InputStream in, String namespaceFrom, String namespaceTo) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        Map<String, String> mappings = new HashMap<>();
        TinyV2Factory.visit(reader, new TinyVisitor() {
            private int columnFrom;
            private int columnTo;

            private void add(MappingGetter mapping) {
                mappings.put(mapping.get(columnFrom), mapping.get(columnTo));
            }

            @Override
            public void start(TinyMetadata metadata) {
                columnFrom = metadata.index(namespaceFrom);
                columnTo = metadata.index(namespaceTo);
            }

            @Override
            public void pushClass(MappingGetter name) {
                add(name);
            }

            @Override
            public void pushMethod(MappingGetter name, String descriptor) {
                add(name);
            }

            @Override
            public void pushField(MappingGetter name, String descriptor) {
                add(name);
            }
        });
        return mappings;
    }

    public <T extends Throwable> T deobfuscate(T throwable) {
        StackTraceElement[] stack = throwable.getStackTrace();
        StackTraceElement[] deobf = deobfuscate(stack);
        throwable.setStackTrace(deobf);
        return throwable;
    }

    public StackTraceElement[] deobfuscate(StackTraceElement[] stackTrace) {
        StackTraceElement[] deobfStackTrace = new StackTraceElement[stackTrace.length];
        for (int i = 0; i < stackTrace.length; i++) {
            deobfStackTrace[i] = deobfuscate(stackTrace[i]);
        }
        return deobfStackTrace;
    }

    private StackTraceElement deobfuscate(StackTraceElement elem) {
        return deobfCache.computeIfAbsent(elem, this::computeDeobfuscatedElement);
    }


    private StackTraceElement computeDeobfuscatedElement(StackTraceElement elem) {
        String className = elem.getClassName().replace('.', '/');
        String methodName = elem.getMethodName();
        String fileName = elem.getFileName();
        className = mappings.getOrDefault(className, className);
        methodName = mappings.getOrDefault(methodName, methodName);
        fileName = createFileName(className, fileName);
        return new StackTraceElement(className.replace('/', '.'), methodName, fileName, elem.getLineNumber());
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