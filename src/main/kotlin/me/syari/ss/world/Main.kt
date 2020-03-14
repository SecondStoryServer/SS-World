package me.syari.ss.world

import me.syari.ss.core.auto.FunctionInit
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin(){
    companion object {
        lateinit var worldPlugin: JavaPlugin
    }

    override fun onEnable() {
        worldPlugin = this
        FunctionInit.register(WorldCommand)
    }
}