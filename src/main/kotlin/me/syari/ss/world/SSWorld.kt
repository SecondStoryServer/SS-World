package me.syari.ss.world

import me.syari.ss.core.world.Vector5D
import me.syari.ss.world.Main.Companion.worldPlugin
import me.syari.ss.world.WorldManage.worldConfig
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player

class SSWorld(private val world: World) {
    val name = world.name

    var spawnLocation: Location = world.spawnLocation
        private set(value) {
            world.spawnLocation = value
            field = value
        }

    fun setSpawnLocation(vector5D: Vector5D, save: Boolean){
        spawnLocation = vector5D.toLocation(world)
        if(save){
            worldConfig.set("world.$name.spawn", vector5D.toString())
        }
    }

    fun teleportSpawn(player: Player){
        player.teleport(spawnLocation)
    }

    var isAutoSave: Boolean
        get() = world.isAutoSave
        set(value) {
            world.isAutoSave = value
        }

    fun unload(delete: Boolean) {
        worldList.remove(name)
        worldConfig.set("world.$name", null)
        worldPlugin.server.unloadWorld(world, !delete)
        if(delete){
            world.worldFolder.delete()
        }
    }

    companion object {
        private val worldList = mutableMapOf<String, SSWorld>()

        fun addWorld(ssWorld: SSWorld){
            worldList[ssWorld.name] = ssWorld
        }

        fun containsWorld(name: String) = worldList.contains(name)

        fun getWorld(name: String) = worldList[name]

        fun worldNameList() = worldList.keys.toSet()
    }
}