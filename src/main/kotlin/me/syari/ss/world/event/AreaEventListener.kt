package me.syari.ss.world.event

import me.syari.ss.core.auto.Event
import me.syari.ss.world.SSWorld
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerMoveEvent

object AreaEventListener: Event {
    @EventHandler
    fun on(e: PlayerMoveEvent){
        val player = e.player
        val world = SSWorld.getWorld(player) ?: return
        val to = e.to
        if(!world.isInArea(to.x, to.z)){
            e.isCancelled = true
        }
    }
}