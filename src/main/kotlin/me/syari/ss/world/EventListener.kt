package me.syari.ss.world

import me.syari.ss.core.auto.Event
import me.syari.ss.core.scheduler.CustomScheduler.runLater
import me.syari.ss.world.Main.Companion.worldPlugin
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerRespawnEvent

object EventListener : Event {
    @EventHandler
    fun on(e: PlayerJoinEvent) {
        val firstSpawnWorld = SSWorld.firstSpawnWorld
        if (firstSpawnWorld.isDataWorld) return
        val player = e.player
        if (!player.hasPlayedBefore()) {
            runLater(worldPlugin, 3) {
                firstSpawnWorld.teleportSpawn(player)
            }
        }
    }

    @EventHandler
    fun on(e: PlayerRespawnEvent) {
        if (e.isBedSpawn) return
        val world = SSWorld(e.respawnLocation.world)
        e.respawnLocation = world.spawnLocation
    }
}