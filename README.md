# Carpet Mod
Yes.

## Getting Started
### Setting up your sources
- Clone this repository.
- Run `gradlew genSources` in the root project directory.

### Using an IDE
- To use Eclipse, run `gradlew eclipse`, then import the project in Eclipse.
- To use IntelliJ, just import the gradle project.

## Using the build system
### To create a release
Use `gradlew build`. The release will be a JAR file in the `build/libs` folder.

### To run the release
- Put the JAR file into the `mods/` folder
- Run `fabric-server-launch.jar` in the server directory with a `server.jar` there or a different name specified with `serverJar=...` in `fabric-server-launcher.properties`

#### Procedure to create the fabric-server-launch.jar
- Download [fabric-installer](https://jitpack.io/com/github/Legacy-Fabric/fabric-installer/-f6cdf7e17e-1/fabric-installer--f6cdf7e17e-1.jar)
- Use `java -jar fabric-installer--f6cdf7e17e-1.jar server -mcversion 1.12.2`
- Download [shedaniel's fixed fabric-loader](https://jitpack.io/com/github/shedaniel/fabric-loader/e4a8c2f8b6/fabric-loader-e4a8c2f8b6.jar)
- Copy the contents of `fabric-loader-e4a8c2f8b6.jar` into `fabric-server-launch.jar`