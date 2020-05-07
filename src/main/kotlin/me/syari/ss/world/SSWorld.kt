package me.syari.ss.world

import me.syari.ss.core.world.Vector5D
import me.syari.ss.world.ConfigLoader.worldConfig
import me.syari.ss.world.Main.Companion.worldPlugin
import me.syari.ss.world.area.WorldArea
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import java.io.File




class SSWorld(private val world: World) {
    val name = world.name

    var spawnLocation: Location = world.spawnLocation
        private set(value) {
            world.spawnLocation = value
            field = value
        }

    var spawnVector5D: Vector5D
        get() = Vector5D.fromLocation(spawnLocation)
        set(value) {
            spawnLocation = value.toLocation(world)
        }

    fun saveSpawnLocation() {
        worldConfig.set("world.$name.spawn", spawnVector5D.toString(), true)
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
                val successDelete = deleteWorldFolder()
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

    private fun deleteWorldFolder(): Boolean {
        return try {
            deleteDirectory(world.worldFolder)
            true
        } catch (ex: Exception) {
            false
        }
    }

    private fun deleteDirectory(file: File) {
        if (file.exists()) {
            if (file.isFile) {
                file.delete()
            } else if (file.isDirectory) {
                file.listFiles()?.forEach {
                    deleteDirectory(it)
                }
            }
        }
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

    var area: WorldArea? = null

    fun isInArea(x: Double, z: Double): Boolean {
        return area?.isInArea(x, z) ?: true
    }

    fun saveArea(){
        worldConfig.set("world.$name.area", area?.toString(), true)
    }

    companion object {
        private val worldList = mutableMapOf<String, SSWorld>()
        private val worldNameMutableList = mutableSetOf<String>()

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
            worldList[ssWorld.name.toLowerCase()] = ssWorld
            worldNameMutableList.add(ssWorld.name)
        }

        fun containsWorld(name: String) = worldList.contains(name)

        fun getWorld(name: String) = worldList[name.toLowerCase()]

        fun getWorld(world: World) = getWorld(world.name)

        fun getWorld(entity: Entity) = getWorld(entity.world)

        val worldNameList get() = worldNameMutableList.toSet()
    }

    enum class UnloadResult(val message: String) {
        CanNotUnloadDataWorld("データワールド"),
        FailureUnload("アンロードに失敗"),
        FailureDelete("ワールド削除に失敗"),
        Success("成功しました");

        val isSuccess get() = this == Success
    }
}