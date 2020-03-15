package me.syari.ss.world

import me.syari.ss.core.Main.Companion.console
import me.syari.ss.core.auto.OnEnable
import me.syari.ss.core.command.create.CommandArgument
import me.syari.ss.core.command.create.CommandMessage
import me.syari.ss.core.command.create.CreateCommand.createCommand
import me.syari.ss.core.command.create.CreateCommand.element
import me.syari.ss.core.command.create.CreateCommand.flag
import me.syari.ss.core.command.create.CreateCommand.tab
import me.syari.ss.core.command.create.ErrorMessage
import me.syari.ss.core.config.CreateConfig.config
import me.syari.ss.core.config.CustomConfig
import me.syari.ss.core.world.Vector5D
import me.syari.ss.world.Main.Companion.worldPlugin
import me.syari.ss.world.creator.SSWorldCreator
import org.bukkit.World
import org.bukkit.WorldType
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object WorldManage: OnEnable {
    override fun onEnable() {
        fun CommandMessage.createWorld(args: CommandArgument, load: Boolean){
            val name = args.getOrNull(1)
            val type = if(load) "読込" else "生成"
            if(name == null){
                sendError(ErrorMessage.NotEnterName)
                return sendList("ワールド${type}オプション",
                    "&fワールドディメンション  &a-w <&6Normal&a/Nether/End>",
                    "&fワールドタイプ  &a-t <&6Normal&a/Flat/Void>",
                    "&f建造物生成  &a-s <true/&6false&a>"
                )
            }
            if(SSWorld.containsWorld(name)) return sendError(ErrorMessage.AlreadyExist)
            val creator = SSWorldCreator(name)
            val environment = args.getFlag("-w")
            creator.environment = getEnvironmentFromString(environment) ?: return sendWithPrefix("&cワールドディメンションの値がおかしいです")
            val worldType = args.getFlag("-t")
            creator.worldType = getWorldTypeFromString(creator, worldType) ?: return sendWithPrefix("&cワールドタイプの値がおかしいです")
            val generateStructures = getGenerateStructuresFromString(args.getFlag("-s"))
            creator.generateStructures = generateStructures ?: return sendWithPrefix("&c建造物生成の値がおかしいです")
            sendWithPrefix("&fワールドを${type}します")
            if(creator.create(load) != null){
                sendWithPrefix("&fワールドを${type}しました")
                saveWorldConfig(name, environment, worldType, generateStructures)
            } else {
                sendWithPrefix("&cワールド${type}に失敗しました")
            }
        }

        createCommand(worldPlugin, "world", "SSWorld",
            tab { _, _ -> element("create", "load", "delete", "config", "list", "tp") },
            flag("create *",
                "-w" to element("normal", "nether", "end"),
                "-t" to element("normal", "flat", "void"),
                "-s" to element("true", "false")
            ),
            flag("delete *",
                "-r" to element("true", "false")
            ),
            tab("delete", "tp"){ _, _ -> element(SSWorld.worldNameList()) }
        ){ sender, args ->
            when(args.whenIndex(0)){
                "create" -> {
                    createWorld(args, false)
                }
                "load" -> {
                    createWorld(args, true)
                }
                "delete" -> {
                    val name = args.getOrNull(1)
                    if(name == null){
                        sendError(ErrorMessage.NotEnterName)
                        return@createCommand sendList("ワールド削除オプション",
                            "&fワールドデータ削除の有無  &a-r <true/&6false&a>"
                        )
                    }
                    val world = SSWorld.getWorld(name) ?: return@createCommand sendError(ErrorMessage.NotExist)
                    val delete = args.getFlag("-r") == "true"
                    world.unload(delete)
                }
                "config" -> {

                }
                "list" -> {
                    sendList("ワールド一覧", SSWorld.worldNameList())
                }
                "tp" -> {
                    if(sender !is Player) return@createCommand sendError(ErrorMessage.OnlyPlayer)
                    val worldName = args.getOrNull(1) ?: return@createCommand sendError(ErrorMessage.NotEnterName)
                    val world = SSWorld.getWorld(worldName) ?: return@createCommand sendError(ErrorMessage.NotExist)
                    world.teleportSpawn(sender)
                }
                else -> {
                    sendHelp(
                        "world create" to "新規ワールドを生成します",
                        "world load" to "既存ワールドを読み込みます",
                        "world delete" to "ワールドを削除します",
                        "world config" to "ワールドの設定をします",
                        "world list" to "ワールドの一覧を表示します",
                        "world tp" to "ワールドにテレポートします"
                    )
                }
            }
        }

        loadConfig(console)
    }

    val worldConfig by lazy { config(worldPlugin, console, "config.yml", false) }

    private fun loadConfig(sender: CommandSender): CustomConfig {
        return config(worldPlugin, sender, "config.yml", false){
            section("world")?.forEach { worldName ->
                if(!SSWorld.containsWorld(worldName)) {
                    SSWorldCreator(worldName).apply {
                        getEnvironmentFromString(getString("world.$worldName.environment", false))?.let { environment = it }
                        getWorldTypeFromString(this, getString("world.$worldName.type", false))?.let { worldType = it }
                        generateStructures = getBoolean("world.$worldName.generateStructures", false, notFoundError = false)
                    }.create(true)?.let { world ->
                        Vector5D.fromString(getString("world.$worldName.spawn"))?.let { spawn ->
                            world.setSpawnLocation(spawn, false)
                        }
                        world.isAutoSave = getBoolean("world.$worldName.save", true, notFoundError = false)
                        SSWorld.addWorld(world)
                    }
                }
            }
        }
    }

    private fun saveWorldConfig(worldName: String, environment: String?, worldType: String?, generateStructures: Boolean?){
        worldConfig.with {
            set("world.$worldName.save", true)
            set("world.$worldName.environment", environment)
            set("world.$worldName.type", worldType)
            set("world.$worldName.generateStructures", generateStructures)
            save()
        }
    }

    private fun getEnvironmentFromString(string: String?): World.Environment? {
        return when(string){
            "normal", null -> World.Environment.NORMAL
            "nether" -> World.Environment.NETHER
            "end" -> World.Environment.THE_END
            else -> null
        }
    }

    private fun getWorldTypeFromString(creator: SSWorldCreator, string: String?): WorldType? {
        return when(string){
            "normal", null -> WorldType.NORMAL
            "flat" -> WorldType.FLAT
            "void" -> {
                creator.voidWorld = true
                WorldType.NORMAL
            }
            else -> null
        }
    }

    private fun getGenerateStructuresFromString(string: String?): Boolean? {
        return when(string){
            "true" -> true
            "false", null -> false
            else -> null
        }
    }
}