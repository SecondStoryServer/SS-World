package me.syari.ss.world

import me.syari.ss.core.world.Vector5D
import me.syari.ss.world.ConfigLoader.worldConfig
import me.syari.ss.world.Main.Companion.worldPlugin
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Entity
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

    fun unload(deleteWorldFolder: Boolean): UnloadResult {
        if (isDataWorld) return UnloadResult.CanNotUnloadDataWorld
        world.players.forEach { player ->
            teleportSpawn(player)
        }
        val successUnload = worldPlugin.server.unloadWorld(world, !deleteWorldFolder)
        if (successUnload) {
            if (deleteWorldFolder) {
                val successDelete = world.worldFolder.delete()
                if (!successDelete) {
                    return UnloadResult.FailureDelete
                }
            }
        } else {
            return UnloadResult.FailureUnload
        }
        isFistSpawnWorld = false
        worldList.remove(name)
        worldConfig.set("world.$name", null, true)
        return UnloadResult.Success
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

        fun getWorld(world: World) = getWorld(world.name)

        fun getWorld(entity: Entity) = getWorld(entity.world)

        val worldNameList get() = worldList.keys.toSet()
    }

    enum class UnloadResult(val message: String) {
        CanNotUnloadDataWorld("データワールド"),
        FailureUnload("アンロードに失敗"),
        FailureDelete("ワールド削除に失敗"),
        Success("成功しました");

        val isSuccess get() = this == Success
    }
}