package me.syari.ss.world

import org.bukkit.World

class SSWorld(private val world: World) {
    companion object {
        private val worldList = mutableMapOf<String, SSWorld>()

        fun addWorld(ssWorld: SSWorld){
            worldList[ssWorld.world.name] = ssWorld
        }

        fun containsWorld(name: String) = worldList.contains(name)
    }
}