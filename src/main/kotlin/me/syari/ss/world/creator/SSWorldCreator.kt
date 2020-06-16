package me.syari.ss.world.creator

import me.syari.ss.core.world.Vector5D
import me.syari.ss.world.SSWorld
import me.syari.ss.world.generator.VoidGenerator
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.WorldType

class SSWorldCreator(private val name: String) {
    private val worldCreator = WorldCreator(name)
    var environment = World.Environment.NORMAL
    var worldType = WorldType.NORMAL
    var generateStructures = false
    var voidWorld = false

    fun create(): SSWorld? {
        worldCreator
            .environment(environment)
            .type(worldType)
            .generateStructures(generateStructures)
        if (voidWorld) {
            worldCreator.generator(VoidGenerator())
        }
        if (SSWorld.containsWorld(name)) return null
        return worldCreator.createWorld()?.let { world ->
            SSWorld(world).apply {
                if (voidWorld) {
                    spawnVector5D = Vector5D(0.5, 64.0, 0.5)
                    val spawnBlock = world.getBlockAt(0, 63, 0)
                    if (spawnBlock.isEmpty) {
                        spawnBlock.type = Material.BEDROCK
                    }
                }
                saveSpawnLocation()
                SSWorld.addWorld(this)
            }
        }
    }
}