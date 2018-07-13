cd reobf
cp -r minecraft_server/* minecraft_server.1.12
7za a minecraft_server.1.12_carpet_test.jar ./minecraft_server.1.12/* | dev.null
mv -f minecraft_server.1.12_carpet_test.jar ../../saves
cd ..
cd ../saves
java -jar minecraft_server.1.12_carpet_test.jar --nogui
cd "../mcp940 - carpet dev"
