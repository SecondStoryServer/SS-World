package me.syari.ss.world.creator

import me.syari.ss.core.world.Vector5D
import me.syari.ss.world.SSWorld
import me.syari.ss.world.generator.VoidGenerator
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.WorldType

class SSWorldCreator(private val name: String){
    private val worldCreator = WorldCreator(name)
    var environment = World.Environment.NORMAL
    var worldType = WorldType.NORMAL
    var generateStructures = false
    var voidWorld = false

    fun create(load: Boolean): SSWorld? {
        worldCreator
            .environment(environment)
            .type(worldType)
            .generateStructures(generateStructures)
        if(voidWorld){
            worldCreator.generator(VoidGenerator())
        }
        if(SSWorld.containsWorld(name)) return null
        return worldCreator.createWorld()?.let {
            SSWorld(it).apply {
                if(voidWorld){
                    setSpawnLocation(Vector5D(0.5, 64.0, 0.5), !load)
                }
                SSWorld.addWorld(this)
            }
        }
    }
}