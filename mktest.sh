#!/bin/sh

# Parse command line args
while [ $# -gt 0 ]; do

    key="$1"

    case $key in
        -d|--download)   patch_url="$2"        ; shift ;;
        -o|--outfile)    jar_copy_to="$2"      ; shift ;;
        -t|--test)       run_server=1                  ;;
        -e|--eula)       eula=1                        ;;
        -D|--deobf)      additional_deobfuscated_jar=1 ;;
        -g|--gradle)     run_gradle=1                  ;;
        -k|--keep)       keep_configs=1                ;;
        -p|--properties) copy_server_properties=1      ;;
        -h|--help)
            # immediately print help
            echo "$0 [-h] [-d URL] [-o OUTFILE] [-D] [-g] [-k] [-p] [-t] [-e]"
            echo " install and test Carpet patches"
            echo ""
            echo "options:"
            echo " -h,--help             print this help and exit"
            echo " -d,--download URL     download patches from given URL"
            echo " -o,--outfile OUTFILE  output to given file"
            echo " -t,--test             run a test (run the server)"
            echo " -e,--eula             agree to Mojang EULA (see <https://account.mojang.com/documents/minecraft_eula>)"
            echo " -D,--deobf            create a deobfuscated jar in the build folder, and use that when to running a test"
            echo " -g,--gradle           run gradlew to compile carpet first"
            echo " -k,--keep             keep the world and config files of the server"
            echo " -p,--properties       copy server.properties.bak to server.properties and replace every instance of \"BUILD\" with the output of \"date -Iseconds\""
            exit 0
            ;;
    esac
    shift
done


project_base_dir="`pwd`"
build_dir="$project_base_dir/build/tmp/fullRelease"
download_dir="$project_base_dir/download"

patch_zip="$project_base_dir/build/distributions/Carpetmod_dev.zip"
patch_dir="$build_dir/patches"

server_jar_backup="$server_jar.bak"
server_jar_gradle_cache="$HOME/.gradle/caches/minecraft/net/minecraft/minecraft_server/1.12.2/minecraft_server-1.12.2.jar"
server_jar_url="https://launcher.mojang.com/v1/objects/886945bfb2b978778c3a0288fd7fab09d315b25f/server.jar"

specialsource_jar="$download_dir/SpecialSource-1.11.0-shaded.jar"
specialsource_jar_url="https://repo.maven.apache.org/maven2/net/md-5/SpecialSource/1.11.0/SpecialSource-1.11.0-shaded.jar"

mappings_file="$project_base_dir/build/tmp/reobfuscate/reobf_cls.srg"

date_str="`date +%F`"

carpet_jar="$project_base_dir/build//carpet-1.12.2-$date_str.jar"
carpet_jar_deob="$project_base_dir/build/carpet-1.12.2-$date_str-dev.jar"

test_dir="$project_base_dir/test"
test_jar="$test_dir/carpet-1.12.2-$date_str.jar"

function downloadIfNotHere(){
    # $1 - the location to check for and download to
    # $2 - the url to download from
    # $3 - the message to print before downloading
    # $4 - the message to print when it fails
    mkdir -p "`dirname $1`"
    if [ ! -f "$1" ]; then
        echo "$3"
        wget "$2" -O "$1" || { echo "$4" && exit 1; }
    fi
}

function runJava(){
    # $1 - the jar to run
    # $@ - the arguments to pass
    jar="$1"
    shift
    "$JAVA_HOME/bin/java" -jar "$jar" $@
}

function runGradle(){
    # $@ - the gradle tasks to run
    "$project_base_dir/gradlew" $@
}

# compile carpet with gradle if wanted
if [ "$run_gradle" == "1" ]; then
    echo y | runGradle genPatches createRelease # answer that question to overwrite working changes with yes
fi

# remove previous installations
echo "Cleaning previous installations ..."
rm -rf "$build_dir/*"

# download the patches if wanted
if [ "x$patch_url" != "x" ]; then
    patch_zip="$download_dir/carpet_patches.zip"
    rm -f "$patch_zip"
    downloadIfNotHere "$patch_zip" "$patch_url" "Downloading patches..." "download failed!"
fi

# get a 1.12.2 minecraft server jar
echo "Getting Minecraft 1.12.2 server jar ..."
if [ -f "$server_jar_gradle_cache" ]; then
    cp "$server_jar_gradle_cache" "$carpet_jar"
else
    downloadIfNotHere "$server_jar_backup" "$server_jar_url"  "Downloading server ..." "failed to download server jar"
    cp "$server_jar_backup" "$carpet_jar"
fi

# unzip the patches
echo "Extracting patches ..."
mkdir -p "$patch_dir"
unzip -q "$patch_zip" -d "$patch_dir" || { echo "failed to extract patches!" && exit 1; }

# patch the jar file
echo "Patch work ..."
pushd "$patch_dir" > /dev/null
zip -q -u "$carpet_jar" `find . -name '*'`
popd > /dev/null

# remove the extracted directory
echo "Cleanup ..."
rm -rf "$patch_dir"

# copy the jar to another location if wanted
if [ "x$jar_copy_to" != "x" ]; then
    cp "$carpet_jar" "$jar_copy_to"
fi

# deobfuscate the jar if wanted
if [ "$additional_deobfuscated_jar" == "1" ]; then
    downloadIfNotHere "$specialsource_jar" "$specialsource_jar_url" "Downloading SpecialSource ..." "failed to download special source"
    runJava "$specialsource_jar" -i "$carpet_jar" -o "$carpet_jar_deob" -m "$mappings_file" -r
fi

# run a test if wanted
if [ "$run_server" == "1" ]; then
    pushd "$test_dir" > /dev/null

    # delete all files or just some
    if [ "$keep_configs" == "1" ]; then
        rm -rf "carpet"
        rm -rf "crash-reports"
        rm -rf "logs"
        rm -f "$test_jar"
    else
        rm -rf *
    fi

    # copy the correct jar to the test folder
    if [ "$additional_deobfuscated_jar" == "1" ]; then
        cp "$carpet_jar_deob" "$test_jar"
    else
        cp "$carpet_jar" "$test_jar"
    fi

    # remove eula.txt, only write it when -e is passed
    rm -f "eula.txt"
    if [ "$eula" == "1" ]; then
        echo "eula=true" > "eula.txt"
    fi

    # copy the server.properties.bak file to server.properties and replace some special strings
    if [ "$copy_server_properties" == "1" ]; then
        sed "s/BUILD/`date -Iseconds`/" server.properties.bak > server.properties
    fi

    # lanch java
    echo "Starting server ..."
    runJava "$test_jar" --nogui

    popd > /dev/null
fi

# vim: ts=4 sw=4 et
