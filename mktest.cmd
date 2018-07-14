rm -rf build\tmp\fullRelease
mkdir build\tmp\fullRelease
cp %userprofile%\.gradle\caches\minecraft\net\minecraft\minecraft_server\1.12\minecraft_server-1.12.jar build\tmp\fullRelease
mkdir build\tmp\fullRelease\patches
7za x build\distributions\Carpetmod_dev.zip -bd -obuild\tmp\fullRelease\patches > nul
7za a build\tmp\fullRelease\minecraft_server-1.12.jar .\build\tmp\fullRelease\patches\* > nul
rm -rf build\tmp\fullRelease\patches
mv -f build\tmp\fullRelease\minecraft_server-1.12.jar %appdata%\.minecraft\saves\minecraft_server.1.12_carpet_test.jar
pushd %appdata%\.minecraft\saves
java -jar minecraft_server.1.12_carpet_test.jar --nogui
popd