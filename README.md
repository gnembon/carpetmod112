# Carpet Mod
Yes.

## Getting Started
### Setting up your sources
- Clone this repository.
- Run `gradlew setupDecompWorkspace` in the root project directory.

### Using an IDE
- To use Eclipse, run `gradlew eclipse`, then import the project in Eclipse.
- To use Intellij, run `gradlew idea`, then import the project in Intellij.

## Using the build system
### To create a release / patch files
Use `gradlew build`. The release will be a runnable JAR file in the `build/libs` folder.
### To run the release
Multiple options:
- Use `java -jar carpetmod-<version>.jar` with a `minecraft_server.1.12.2.jar` in the same folder
- Use `java -cp <minecraft-server>.jar:carpetmod-<version>.jar carpet.Main`