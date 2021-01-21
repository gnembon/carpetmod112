package carpetmod;

public final class Build {
    private Build() {}
    public static final String ID = "carpetmod";
    public static final String NAME = "Carpet Mod";
    public static final String VERSION = "${version}";
    public static final String COMMIT = "${commit}";
    public static final String BRANCH = "${branch}";
    public static final String BUILD_TIMESTAMP = "${timestamp}";
    public static final String MINECRAFT_VERSION = "${minecraft_version}";
    public static final String YARN_MAPPINGS = "${yarn_mappings}";
    public static final boolean WORKING_DIR_CLEAN = ${working_dir_clean};
}