# Carpet Mod
Yes.

## Getting Started
### Setting up your sources
- Clone this repository.
- Run `gradlew setupCarpetmod` in the root project directory.

### Using an IDE
- To use Eclipse, run `gradlew eclipse`, then import the project in Eclipse.
- To use Intellij, run `gradlew idea`, then import the project in Intellij.

## Using the build system
Edit the files in the `src` folder, like you would for a normal project. The only special things you have to do are as follows:
### To generate patch files so they show up in version control
Use `gradlew genPatches`
### To apply patches after pulling
Use `gradlew setupCarpetmod`. It WILL overwrite your local changes to src, so be careful.
### To create a release / patch files
In case you made changes to the local copy of the code in `src`, run `genPatches` to update the project according to your src.
Use `gradlew createRelease`. The release will be a ZIP file containing all modified classes, obfuscated, in the `build/distributions` folder.
### To run the server locally (Windows)
Use `mktest.cmd` to run the modified server with generated patches as a localhost server. It requires `gradlew createRelease` to finish successfully as well as using default paths for your minecraft installation folder.

In case you use different paths, you might need to modify the build script.
This will leave a ready server jar file in your saves folder.

It requires to have 7za installed in your paths
### To run the server locally (Linux)
Run `./mktest.sh -t -g` to run the modified server with generated patches as a localhost server. It requires `gradlew genPatches createRelease` to finish successfully.

Add `-e` if you agree to the Minecraft Eula, `-D` if you want the server to use a deobfuscated jar, so you can read the names in crashlogs for example.
Add `-k` if you don't want to clear the `test` folder for every run.

If you want to specify your `JAVA_HOME`, add `JAVA_HOME=/folder/to/java` (with a space seperating the command) at the start of the command.

Use `./mktest.sh --help` to view help that tells you more.

It requires to have zip, unzip, and wget installed (most desktop distributions already have that).
