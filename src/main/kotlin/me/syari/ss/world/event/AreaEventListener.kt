package me.syari.ss.world.event

import me.syari.ss.core.auto.Event
import me.syari.ss.world.SSWorld
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityTeleportEvent
import org.bukkit.event.player.PlayerMoveEvent

object AreaEventListener: Event {
    private fun isNotInArea(location: Location): Boolean {
        val world = SSWorld.getWorld(location.world) ?: return false
        return !world.isInArea(location.x, location.z)
    }

    @EventHandler
    fun on(e: PlayerMoveEvent){
        if(isNotInArea(e.to)){
            e.isCancelled = true
        }
    }

    @EventHandler
    fun on(e: EntityTeleportEvent){
        val to = e.to ?: return
        if(isNotInArea(to)){
            e.isCancelled = true
        }
    }
}