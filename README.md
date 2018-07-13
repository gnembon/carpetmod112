# Carpet Mod
A build system for Jar Mods, modernized to use ForgeGradle rather than MCP.

## Getting Started
### Creating a new project
- Clone this repository.
- Edit "settings.gradle" to suit your needs. From now on, whenever I say `[project]` or `[Project]` you should replace that with the name of your project, noting the caps.
- Run `gradlew setup[Project]` in the root project directory.

### Migrating from MCP
- Clone this repository.
- Edit "settings.gradle" to suit your needs. Take particular care to turn on compatibility options with MCP where needed. From now on, whenever I say `[project]` or `[Project]` you should replace that with the name of your project, noting the caps.
- Run `gradlew setup[Project]` in the root project directory.
- Delete everything in the `src` directory.
- Copy all the MCP code, along with your modifications and new classes, and paste it into the `src` directory.
- Run `gradlew genPatches` in the root project directory.

### Using an IDE
- To use Eclipse, run `gradlew eclipse`, then import the project in Eclipse.
- To use Intellij, run `gradlew idea`, then import the project in Intellij.

## Using the build system
Edit the files in the `src` folder, like you would for a normal project. The only special things you have to do are as follows:
### To generate patch files so they show up in version control
Use `gradlew genPatches`
### To apply patches after pulling
Use `gradlew setup[Project]`
### To create a release
Use `gradlew createRelease`. The release will be a ZIP file containing all modified classes, obfuscated, in the `build/distributions` folder.
