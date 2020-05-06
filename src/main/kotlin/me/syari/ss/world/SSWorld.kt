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

    fun setSpawnLocation(vector5D: Vector5D) {
        spawnLocation = vector5D.toLocation(world)
    }

    fun saveSpawnLocation() {
        worldConfig.set("world.$name.spawn", Vector5D.fromLocation(spawnLocation).toString())
    }

    fun teleportSpawn(player: Player) {
        player.teleport(spawnLocation)
    }

    var isAutoSave: Boolean
        get() = world.isAutoSave
        set(value) {
            world.isAutoSave = value
        }

    fun unload(delete: Boolean): Boolean {
        if (isDataWorld) return false
        world.players.forEach { player ->
            teleportSpawn(player)
        }
        val unloaded = worldPlugin.server.unloadWorld(world, !delete)
        if (unloaded) {
            if (delete) {
                val deleted = world.worldFolder.delete()
                if (!deleted) return false
            }
        } else {
            return false
        }
        isFistSpawnWorld = false
        worldList.remove(name)
        worldConfig.set("world.$name", null, true)
        return true
    }

    val isDataWorld
        get() = dataWorld == this

    var isFistSpawnWorld
        get() = firstSpawnWorld == this
        set(value) {
            if (value) {
                firstSpawnWorld = this
            } else if (isFistSpawnWorld) {
                firstSpawnWorld = dataWorld
            }
        }

    companion object {
        private val worldList = mutableMapOf<String, SSWorld>()

        lateinit var dataWorld: SSWorld
            private set

        lateinit var firstSpawnWorld: SSWorld
            private set

        fun setDataWorld(world: World) {
            val dataWorld = SSWorld(world)
            addWorld(dataWorld)
            this.dataWorld = dataWorld
            this.firstSpawnWorld = dataWorld
        }

        fun addWorld(ssWorld: SSWorld) {
            worldList[ssWorld.name] = ssWorld
        }

        fun containsWorld(name: String) = worldList.contains(name)

        fun getWorld(name: String) = worldList[name]

        val worldNameList get() = worldList.keys.toSet()
    }
}