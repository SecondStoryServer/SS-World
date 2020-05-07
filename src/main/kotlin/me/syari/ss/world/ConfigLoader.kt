package me.syari.ss.world

import me.syari.ss.core.Main.Companion.console
import me.syari.ss.core.config.CreateConfig.config
import me.syari.ss.core.config.CustomConfig
import me.syari.ss.core.config.dataType.ConfigDataType
import me.syari.ss.core.world.Vector5D
import me.syari.ss.world.Main.Companion.worldPlugin
import me.syari.ss.world.area.WorldArea
import me.syari.ss.world.creator.SSWorldCreator
import org.bukkit.World
import org.bukkit.WorldType
import org.bukkit.command.CommandSender

object ConfigLoader {
    val worldConfig by lazy { config(worldPlugin, console, "config.yml", false) }

    fun loadConfig(sender: CommandSender): CustomConfig {
        return config(worldPlugin, sender, "config.yml", false) {
            val dataWorldName = SSWorld.dataWorld.name
            var firstSpawnWorldName = get("firstspawn", ConfigDataType.STRING, false)
            section("world")?.forEach { worldName ->
                if (!SSWorld.containsWorld(worldName)) {
                    SSWorldCreator(worldName).apply {
                        getEnvironmentFromString(
                            get(
                                "world.$worldName.environment",
                                ConfigDataType.STRING,
                                false
                            )
                        )?.let { environment = it }
                        getWorldTypeFromString(
                            this,
                            get("world.$worldName.type", ConfigDataType.STRING, false)
                        )?.let { worldType = it }
                        generateStructures = get(
                            "world.$worldName.generateStructures",
                            ConfigDataType.BOOLEAN,
                            false,
                            notFoundError = false
                        )
                    }.create()?.let { world ->
                        Vector5D.fromString(get("world.$worldName.spawn", ConfigDataType.STRING, true))?.let { spawn ->
                            world.setSpawnLocation(spawn)
                        }
                        worldConfig.get("world.$worldName.area", ConfigDataType.STRING, false)?.let {  text ->
                            WorldArea.fromString(text)?.let { area ->
                                world.area = area
                            } ?: nullError("world.$worldName.area", "(X, Y, Radius)")
                        }
                        world.isAutoSave =
                            get("world.$worldName.save", ConfigDataType.BOOLEAN, true, notFoundError = false)
                        if (firstSpawnWorldName == worldName) {
                            world.isFistSpawnWorld = true
                            firstSpawnWorldName = null
                        }
                        SSWorld.addWorld(world)
                    }
                }
            }
            if (firstSpawnWorldName != null) {
                set("firstspawn", dataWorldName, true)
            }
        }
    }

    fun saveWorldConfig(
        worldName: String,
        environment: String?,
        worldType: String?,
        generateStructures: Boolean?
    ) {
        worldConfig.with {
            set("world.$worldName.save", true)
            set("world.$worldName.environment", environment)
            set("world.$worldName.type", worldType)
            set("world.$worldName.generateStructures", generateStructures)
            save()
        }
    }

    fun getEnvironmentFromString(string: String?): World.Environment? {
        return when (string) {
            "normal", null -> World.Environment.NORMAL
            "nether" -> World.Environment.NETHER
            "end" -> World.Environment.THE_END
            else -> null
        }
    }

    fun getWorldTypeFromString(creator: SSWorldCreator, string: String?): WorldType? {
        return when (string) {
            "normal", null -> WorldType.NORMAL
            "flat" -> WorldType.FLAT
            "void" -> {
                creator.voidWorld = true
                WorldType.NORMAL
            }
            else -> null
        }
    }

    fun getGenerateStructuresFromString(string: String?): Boolean? {
        return when (string) {
            "true" -> true
            "false", null -> false
            else -> null
        }
    }
}