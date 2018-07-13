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
Use `gradlew setupCarpetmod`
### To create a release
Use `gradlew createRelease`. The release will be a ZIP file containing all modified classes, obfuscated, in the `build/distributions` folder.
