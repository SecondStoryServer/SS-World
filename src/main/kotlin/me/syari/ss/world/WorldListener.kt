package me.syari.ss.world

import me.syari.ss.core.auto.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerRespawnEvent

object WorldListener: Event {
    @EventHandler
    fun on(e: PlayerRespawnEvent){
        if(e.isBedSpawn) return
        val world = SSWorld(e.respawnLocation.world)
        e.respawnLocation = world.spawnLocation
    }
}