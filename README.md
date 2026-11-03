# Framed Additions

Framed Additions adds framed copies of blocks to Minecraft 1.20.1. It uses the block models and state variants from Minecraft and other installed mods, so the copies can take on the appearance of those blocks when used with FramedBlocks camouflage.

## Requirements

- Minecraft 1.20.1
- Minecraft Forge 47 or newer for Minecraft 1.20.1
- FramedBlocks 9.4.3 or newer

Install Framed Additions and its dependencies in the Minecraft `mods` folder.

## Using the blocks

The **Framed Additions** creative tab contains the available framed block copies. The mod registers copies for non-air blocks from Minecraft and other mods, excluding FramedBlocks blocks and its own blocks.

Each copy uses the source block's model and supported state variants. Apply camouflage as you would to other FramedBlocks blocks.

## Recipes

Every registered copy has a stonecutting recipe: put a **Framed Cube** from FramedBlocks in the stonecutter to craft the framed copy. These recipes are generated automatically for the blocks present in the loaded mod set.

## Building from source

Use Java 17 and run the Gradle build from the project directory:

```shell
./gradlew build
```

On Windows, run `gradlew.bat build`. The built mod jar is placed in `build/libs`.

The build uses Minecraft 1.20.1, Forge 47.4.28, and the FramedBlocks dependency declared in `build.gradle`.

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE).
