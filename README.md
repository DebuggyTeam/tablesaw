<a href="https://discord.gg/bWN57F5CCA"><img src="https://cdn.jsdelivr.net/gh/intergrav/devins-badges/assets/cozy/social/discord-plural_vector.svg" alt="Join our Discord server!" height=64></a> <a href="Insert Modrinth Link"><img src="https://cdn.jsdelivr.net/gh/intergrav/devins-badges/assets/cozy/available/modrinth_vector.svg" alt="Download on Modrinth." height=64></a>

## About Tablesaw
Tablesaw is a mod that adds a sawmill for cutting wooden blocks with. Its pretty similar to the vanilla stonecutter in function and Tablesaw will make it far easier to utilize wooden blocks.

## How to use tablesaw as a user
Follow the instructional gif below to learn how to use the tablesaw.

![test](https://cdn.modrinth.com/data/EPt60DPT/images/fa8e02e8897e1865b7b9b677e7779ae6001a3747.gif)

## How to integrate Tablesaw into your mod

You'd add new recipes to Tablesaw via a tablesaw recipe datapack. The instructions to successfully creating and testing the datapack are as follows:

- First, navigate to the `datapacks` folder in `.minecraft/saves/<world_name>`.

- Second, create a folder for your tablesaw recipe datapack and rename it.

- Third, create a `pack.mcmeta` in your datapack folder and fill it with the following:
```
{
    "pack": {
        "pack_format": 10,
        "description": "Here, describe the gist of your recipe datapack."
    }
}
```

- Fourth, reate the following folders: `data` > `tablesaw` > `custom_recipes` > `tablesaw`.

- Fifth, make a JSON file that contains your recipe information. Recipe examples are written below.


Example of adding a 1:1 recipe:
```
{
    "input": "minecraft:oak_planks",
    "result": "minecraft:oak_button"
}
```

Example of adding a recipe that takes more than one item and outputs more than one item:
```
{
    "input": {
        "item": "minecraft:oak_planks",
        "count": 3
    },
    "result": {
        "item": "minecraft:oak_stairs",
        "count": 2
    }
}
```
