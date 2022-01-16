package carpet.network;

import carpet.CarpetServer;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.util.HttpUtil;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileAccess implements PluginChannelHandler {
    private static final String CHANNEL = "Carpet|FileAccess";
    private static final String[] CHANNELS = {CHANNEL};
    private static final int REQUIRED_PERMISSION_LEVEL = 2;
    private static final int MAX_RECEIVE_LENGTH = 1024 * 1024 * 8; // 8 megabytes
    private static final int MAX_DOWNLOAD_SIZE = 1024 * 1024 * 32; // 32 megabytes

    private static final Pattern PATH_DOT_PATTERN = Pattern.compile("(?:^|(/))(?:\\.(?:/|$))+");

    private static final int COMMAND_LIST_PERMISSIONS = 0;
    private static final int COMMAND_LIST_DIR = 1;
    private static final int COMMAND_DOWNLOAD_FILE = 2;
    private static final int COMMAND_UPLOAD_FILE = 3;
    private static final int COMMAND_UPLOAD_DIR = 4;
    private static final int COMMAND_DELETE_FILE = 5;
    private static final int COMMAND_CREATE_DIR = 6;
    private static final int COMMAND_MOVE_FILE = 7;
    private static final int COMMAND_COPY_FILE = 8;
    private static final int COMMAND_FILE_METADATA = 9;
    private static final int COMMAND_DENY = 255;

    private static final int METADATA_NORMAL_FILE = 0;
    private static final int METADATA_DIRECTORY = 1;
    private static final int METADATA_LINK = 2;
    private static final int METADATA_WRITABLE = 4;

    private static final int MAX_PATH_LENGTH;
    static {
        String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (osName.contains("win") && !osName.contains("darwin")) {
            MAX_PATH_LENGTH = 260;
        } else {
            MAX_PATH_LENGTH = 4096;
        }
    }
    private static final Path SERVER_DIR = CarpetServer.minecraft_server.getDataDirectory().toPath().toAbsolutePath();

    private final List<FilePermission> permissions;

    public FileAccess() {
        if (Files.notExists(resolve("file_permissions.txt"))) {
            try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(resolve("file_permissions.txt")))) {
                writer.println("# This file is used to control access to files on the server via the carpet file transfer protocol.");
                writer.println("# The file is read from top to bottom, and the first matching rule is used.");
                writer.println("# Possible rules are: deny, read, write.");
                writer.println("# * matches any string not containing a /");
                writer.println("# ** matches any string, including those containing a /");
                writer.println("# / is used as the file separator, even on Windows.");
                writer.println();
                writer.println("write " + CarpetServer.minecraft_server.getFolderName() + "/data/functions/**.mcfunction");
                writer.println("write " + CarpetServer.minecraft_server.getFolderName() + "/structures/**.nbt");
                writer.println("read **");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        List<FilePermission> permissions;
        try (Stream<String> lines = Files.lines(resolve("file_permissions.txt"), StandardCharsets.UTF_8)) {
            permissions = lines.map(String::trim)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .map(FilePermission::parse)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            permissions = new ArrayList<>();
        }

        this.permissions = permissions;
    }

    private int getAccessLevel(String path) {
        for (FilePermission permission : permissions) {
            if (permission.path.matcher(path).matches()) {
                return permission.accessLevel;
            }
        }
        return FilePermission.ACCESS_DENY;
    }

    private int getMetadata(Path path) {
        int fileType;
        if (Files.isDirectory(path)) {
            fileType = METADATA_DIRECTORY;
        } else if (Files.isSymbolicLink(path)) {
            fileType = METADATA_LINK;
        } else {
            fileType = METADATA_NORMAL_FILE;
        }

        String relativePath = SERVER_DIR.relativize(path.toAbsolutePath()).toString().replace(File.separator, "/");
        if (".".equals(relativePath)) {
            relativePath = "";
        }
        if (getAccessLevel(relativePath) >= FilePermission.ACCESS_WRITE) {
            fileType |= METADATA_WRITABLE;
        }

        return fileType;
    }

    @Override
    public String[] getChannels() {
        return CHANNELS;
    }

    @Override
    public void onCustomPayload(CPacketCustomPayload packet, EntityPlayerMP player) {
        PacketBuffer buf = PacketSplitter.receive(player, packet, MAX_RECEIVE_LENGTH);
        if (buf == null) {
            return;
        }

        // check permission level
        if (!player.canUseCommand(REQUIRED_PERMISSION_LEVEL, "")) {
            return;
        }

        int command = buf.readUnsignedByte();
        boolean result = false;
        try {
            switch (command) {
                case COMMAND_LIST_PERMISSIONS:
                    result = listPermissions(player);
                    break;
                case COMMAND_LIST_DIR:
                    result = listDir(buf, player);
                    break;
                case COMMAND_DOWNLOAD_FILE:
                    result = downloadFile(buf, player);
                    break;
                case COMMAND_UPLOAD_FILE:
                    result = uploadFile(buf, player);
                    break;
                case COMMAND_UPLOAD_DIR:
                    result = uploadDir(buf, player);
                    break;
                case COMMAND_DELETE_FILE:
                    result = deleteFile(buf, player);
                    break;
                case COMMAND_CREATE_DIR:
                    result = createDir(buf, player);
                    break;
                case COMMAND_MOVE_FILE:
                    result = moveFile(buf, player);
                    break;
                case COMMAND_COPY_FILE:
                    result = copyFile(buf, player);
                    break;
                case COMMAND_FILE_METADATA:
                    result = fileMetadata(buf, player);
                    break;
            }
        } catch (PathValidationException ignore) {
        }

        if (!result) {
            PacketBuffer out = new PacketBuffer(Unpooled.buffer());
            out.writeByte(COMMAND_DENY);
            out.writeByte(command);
            PacketSplitter.send(player, CHANNEL, out);
        }
    }

    private boolean listPermissions(EntityPlayerMP player) {
        PacketBuffer out = new PacketBuffer(Unpooled.buffer());
        out.writeByte(COMMAND_LIST_PERMISSIONS);
        out.writeVarInt(permissions.size());
        for (FilePermission permission : permissions) {
            out.writeString(permission.strPath);
            out.writeByte(permission.accessLevel);
        }
        PacketSplitter.send(player, CHANNEL, out);
        return true;
    }

    private boolean listDir(PacketBuffer buf, EntityPlayerMP player) {
        int numDirs = buf.readVarInt();
        String[] dirs = new String[numDirs];
        for (int i = 0; i < numDirs; i++) {
            dirs[i] = readPath(buf);
            if (getAccessLevel(dirs[i]) < FilePermission.ACCESS_READ) {
                return false;
            }
        }

        PacketBuffer out = new PacketBuffer(Unpooled.buffer());
        out.writeByte(COMMAND_LIST_DIR);
        out.writeVarInt(numDirs);
        for (int i = 0; i < numDirs; i++) {
            String dir = dirs[i];
            out.writeString(dir);
            List<Path> subPaths;
            try {
                subPaths = Files.list(resolve(dir))
                        .filter(path -> getAccessLevel(dir + "/" + path) >= FilePermission.ACCESS_READ)
                        .collect(Collectors.toList());
            } catch (IOException | UncheckedIOException e) {
                out.writeVarInt(0);
                continue;
            }
            out.writeVarInt(subPaths.size());
            for (Path subPath : subPaths) {
                out.writeByte(getMetadata(subPath));
                out.writeString(subPath.getFileName().toString());
            }
        }
        PacketSplitter.send(player, CHANNEL, out);
        return true;
    }

    private boolean downloadFile(PacketBuffer buf, EntityPlayerMP player) {
        String pathStr = readPath(buf);
        if (getAccessLevel(pathStr) < FilePermission.ACCESS_READ) {
            return false;
        }
        Path path = resolve(pathStr);
        while (Files.isSymbolicLink(path)) {
            try {
                path = Files.readSymbolicLink(path);
            } catch (IOException e) {
                return false;
            }
        }
        Path path_f = path;

        HttpUtil.DOWNLOADER_EXECUTOR.submit(() -> {
            PacketBuffer out;
            try {
                out = new PacketBuffer(Unpooled.buffer());
                out.writeByte(COMMAND_DOWNLOAD_FILE);
                out.writeString(pathStr);
                int sizePos = out.writerIndex();
                out.writeLong(0);
                if (Files.isDirectory(path_f)) {
                    try (ZipOutputStream zip = new ZipOutputStream(new GZIPOutputStream(new LimitedOutputStream(new ByteBufOutputStream(out), MAX_DOWNLOAD_SIZE)));
                            Stream<Path> stream = Files.walk(path_f, FileVisitOption.FOLLOW_LINKS)
                    ) {
                        stream.filter(Files::isRegularFile).forEach(file -> {
                            String relativeName = path_f.relativize(file).toString();
                            if (getAccessLevel(pathStr + "/" + relativeName) >= FilePermission.ACCESS_READ) {
                                try {
                                    zip.putNextEntry(new ZipEntry(relativeName));
                                    Files.copy(file, zip);
                                    zip.closeEntry();
                                } catch (IOException e) {
                                    throw new UncheckedIOException(e);
                                }
                            }
                        });
                    }
                } else {
                    try (OutputStream stream = new GZIPOutputStream(new LimitedOutputStream(new ByteBufOutputStream(out), MAX_DOWNLOAD_SIZE))) {
                        Files.copy(path_f, stream);
                    }
                }
                out.setLong(sizePos, out.writerIndex() - sizePos - 8);
            } catch (IOException | UncheckedIOException e) {
                out = new PacketBuffer(Unpooled.buffer());
                out.writeByte(COMMAND_DENY);
                out.writeByte(COMMAND_DOWNLOAD_FILE);
                PacketSplitter.send(player, CHANNEL, out);
                return;
            }
            PacketSplitter.send(player, CHANNEL, out);
        });
        return true;
    }

    private boolean uploadFile(PacketBuffer buf, EntityPlayerMP player) {
        String pathStr = readPath(buf);
        if (getAccessLevel(pathStr) < FilePermission.ACCESS_WRITE) {
            return false;
        }
        Path path = resolve(pathStr);
        if (Files.isDirectory(path) || (Files.exists(path) && !Files.isWritable(path))) {
            return false;
        }
        HttpUtil.DOWNLOADER_EXECUTOR.submit(() -> {
            try {
                Files.createDirectories(path.getParent());
                try (InputStream stream = new GZIPInputStream(new ByteBufInputStream(buf))) {
                    Files.copy(stream, path, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                PacketBuffer out = new PacketBuffer(Unpooled.buffer());
                out.writeByte(COMMAND_DENY);
                out.writeByte(COMMAND_UPLOAD_FILE);
                PacketSplitter.send(player, CHANNEL, out);
                return;
            }
            PacketBuffer out = new PacketBuffer(Unpooled.buffer());
            out.writeByte(COMMAND_UPLOAD_FILE);
            out.writeString(pathStr);
            PacketSplitter.send(player, CHANNEL, out);
        });
        return true;
    }

    private boolean uploadDir(PacketBuffer buf, EntityPlayerMP player) {
        String pathStr = readPath(buf);
        if (getAccessLevel(pathStr) < FilePermission.ACCESS_WRITE) {
            return false;
        }
        Path path = resolve(pathStr);
        if (Files.exists(path)) {
            return false;
        }
        HttpUtil.DOWNLOADER_EXECUTOR.submit(() -> {
            try {
                Files.createDirectories(path);
                try (ZipInputStream zip = new ZipInputStream(new GZIPInputStream(new ByteBufInputStream(buf)))) {
                    ZipEntry entry;
                    while ((entry = zip.getNextEntry()) != null) {
                        if (getAccessLevel(pathStr + "/" + entry.getName()) >= FilePermission.ACCESS_WRITE) {
                            Path file = path.resolve(entry.getName());
                            Files.createDirectories(file.getParent());
                            Files.copy(zip, file, StandardCopyOption.REPLACE_EXISTING);
                            zip.closeEntry();
                        }
                    }
                }
            } catch (IOException e) {
                PacketBuffer out = new PacketBuffer(Unpooled.buffer());
                out.writeByte(COMMAND_DENY);
                out.writeByte(COMMAND_UPLOAD_DIR);
                PacketSplitter.send(player, CHANNEL, out);
                return;
            }
            PacketBuffer out = new PacketBuffer(Unpooled.buffer());
            out.writeByte(COMMAND_UPLOAD_DIR);
            out.writeString(pathStr);
            PacketSplitter.send(player, CHANNEL, out);
        });
        return true;
    }

    private boolean deleteFile(PacketBuffer buf, EntityPlayerMP player) {
        int numFiles = buf.readVarInt();
        String[] files = new String[numFiles];
        for (int i = 0; i < numFiles; i++) {
            files[i] = readPath(buf);
            if (getAccessLevel(files[i]) < FilePermission.ACCESS_WRITE) {
                return false;
            }
        }
        for (String file : files) {
            Path path = resolve(file);
            try {
                if (Files.isDirectory(path)) {
                    FileUtils.deleteDirectory(path.toFile());
                } else {
                    Files.deleteIfExists(path);
                }
            } catch (IOException e) {
                return false;
            }
        }
        PacketBuffer out = new PacketBuffer(Unpooled.buffer());
        out.writeByte(COMMAND_DELETE_FILE);
        out.writeVarInt(numFiles);
        for (String file : files) {
            out.writeString(file);
        }
        PacketSplitter.send(player, CHANNEL, out);
        return true;
    }

    private boolean createDir(PacketBuffer buf, EntityPlayerMP player) {
        int numDirs = buf.readVarInt();
        String[] dirs = new String[numDirs];
        for (int i = 0; i < numDirs; i++) {
            dirs[i] = readPath(buf);
            if (getAccessLevel(dirs[i]) < FilePermission.ACCESS_WRITE) {
                return false;
            }
        }
        for (String dir : dirs) {
            Path path = resolve(dir);
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                return false;
            }
        }
        PacketBuffer out = new PacketBuffer(Unpooled.buffer());
        out.writeByte(COMMAND_CREATE_DIR);
        out.writeVarInt(numDirs);
        for (String dir : dirs) {
            out.writeString(dir);
        }
        PacketSplitter.send(player, CHANNEL, out);
        return true;
    }

    private boolean moveFile(PacketBuffer buf, EntityPlayerMP player) {
        String oldPath = readPath(buf);
        String newPath = readPath(buf);
        if (getAccessLevel(oldPath) < FilePermission.ACCESS_WRITE || getAccessLevel(newPath) < FilePermission.ACCESS_WRITE) {
            return false;
        }
        Path oldPathObj = resolve(oldPath);
        Path newPathObj = resolve(newPath);
        if (!Files.exists(oldPathObj)) {
            return false;
        }
        if (Files.exists(newPathObj)) {
            return false;
        }
        try {
            Files.move(oldPathObj, newPathObj);
        } catch (IOException e) {
            return false;
        }
        PacketBuffer out = new PacketBuffer(Unpooled.buffer());
        out.writeByte(COMMAND_MOVE_FILE);
        out.writeString(oldPath);
        out.writeString(newPath);
        PacketSplitter.send(player, CHANNEL, out);
        return true;
    }

    private boolean copyFile(PacketBuffer buf, EntityPlayerMP player) {
        String oldPath = readPath(buf);
        String newPath = readPath(buf);
        if (getAccessLevel(oldPath) < FilePermission.ACCESS_READ || getAccessLevel(newPath) < FilePermission.ACCESS_WRITE) {
            return false;
        }
        Path oldPathObj = resolve(oldPath);
        Path newPathObj = resolve(newPath);
        if (!Files.exists(oldPathObj)) {
            return false;
        }
        if (Files.exists(newPathObj)) {
            return false;
        }
        try {
            if (Files.isDirectory(oldPathObj)) {
                FileUtils.copyDirectory(oldPathObj.toFile(), newPathObj.toFile());
            } else {
                Files.copy(oldPathObj, newPathObj);
            }
        } catch (IOException e) {
            return false;
        }
        PacketBuffer out = new PacketBuffer(Unpooled.buffer());
        out.writeByte(COMMAND_COPY_FILE);
        out.writeString(oldPath);
        out.writeString(newPath);
        PacketSplitter.send(player, CHANNEL, out);
        return true;
    }

    private boolean fileMetadata(PacketBuffer buf, EntityPlayerMP player) {
        int numFiles = buf.readVarInt();
        String[] files = new String[numFiles];
        for (int i = 0; i < numFiles; i++) {
            files[i] = readPath(buf);
            if (getAccessLevel(files[i]) < FilePermission.ACCESS_READ) {
                return false;
            }
        }
        PacketBuffer out = new PacketBuffer(Unpooled.buffer());
        out.writeByte(COMMAND_FILE_METADATA);
        out.writeVarInt(numFiles);
        for (String file : files) {
            out.writeString(file);
            out.writeByte(getMetadata(resolve(file)));
        }
        PacketSplitter.send(player, CHANNEL, out);
        return true;
    }

    private static String readPath(PacketBuffer buf) {
        String path = buf.readString(256);
        if (path.contains("..")) {
            // nice try
            throw new PathValidationException();
        }
        if (path.startsWith("/") || path.contains(":")) {
            // prevent explicit absolute paths
            throw new PathValidationException();
        }
        if (path.contains("\\")) {
            // make sure we're normalized
            throw new PathValidationException();
        }
        path = PATH_DOT_PATTERN.matcher(path).replaceAll("$1");
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        try {
            String pathName = Paths.get(path.replace("/", File.separator)).toAbsolutePath().toString();
            if (pathName.getBytes(StandardCharsets.UTF_8).length > MAX_PATH_LENGTH) {
                throw new PathValidationException();
            }
        } catch (InvalidPathException | NullPointerException e) {
            throw new PathValidationException();
        }
        return path;
    }

    private static Path resolve(String path) {
        if (path.isEmpty()) {
            return SERVER_DIR;
        } else {
            return SERVER_DIR.resolve(path);
        }
    }

    private static class FilePermission {
        public final String strPath;
        public final Pattern path;
        public final int accessLevel;

        public static final int ACCESS_DENY = 0;
        public static final int ACCESS_READ = 1;
        public static final int ACCESS_WRITE = 2;

        public FilePermission(String strPath, Pattern path, int accessLevel) {
            this.strPath = strPath;
            this.path = path;
            this.accessLevel = accessLevel;
        }

        public static FilePermission parse(String line) {
            String[] parts = line.split(" ", 2);
            if (parts.length != 2) {
                return null;
            }
            String action = parts[0];
            String path = parts[1];
            StringBuilder pathPatternStr = new StringBuilder();
            for (int i = 0; i < path.length(); i++) {
                char c = path.charAt(i);
                if (c == '*') {
                    if (i == 0 || path.charAt(i - 1) != '\\') {
                        if (i != path.length() - 1 && path.charAt(i + 1) == '*') {
                            pathPatternStr.append(".*");
                            i++;
                        } else {
                            pathPatternStr.append("[^/]*");
                        }
                    } else {
                        pathPatternStr.append("\\*");
                    }
                } else if (c == '?') {
                    if (i == 0 || path.charAt(i - 1) != '\\') {
                        pathPatternStr.append(".");
                    } else {
                        pathPatternStr.append("\\?");
                    }
                } else if (c == '.' || c == '^' || c == '$' || c == '(' || c == ')' || c == '|' || c == '+' || c == '[' || c == ']' || c == '{' || c == '}') {
                    pathPatternStr.append('\\').append(c);
                } if (c == '\\') {
                    i++;
                } else {
                    pathPatternStr.append(c);
                }
            }
            Pattern pathPattern = Pattern.compile(pathPatternStr.toString());
            if (action.equalsIgnoreCase("deny")) {
                return new FilePermission(path, pathPattern, ACCESS_DENY);
            } else if (action.equalsIgnoreCase("read")) {
                return new FilePermission(path, pathPattern, ACCESS_READ);
            } else if (action.equalsIgnoreCase("write")) {
                return new FilePermission(path, pathPattern, ACCESS_WRITE);
            } else {
                return null;
            }
        }
    }

    private static class PathValidationException extends RuntimeException {
    }

    private static class LimitedOutputStream extends FilterOutputStream {
        private final int maxSize;

        public LimitedOutputStream(OutputStream out, int maxSize) {
            super(out);
            this.maxSize = maxSize;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            if (len > maxSize) {
                throw new IOException("File too large");
            }
            super.write(b, off, len);
        }
    }
}
