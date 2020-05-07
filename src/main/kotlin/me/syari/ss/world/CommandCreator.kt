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
import me.syari.ss.core.world.Vector5D
import me.syari.ss.world.ConfigLoader.getEnvironmentFromString
import me.syari.ss.world.ConfigLoader.getGenerateStructuresFromString
import me.syari.ss.world.ConfigLoader.getWorldTypeFromString
import me.syari.ss.world.ConfigLoader.loadConfig
import me.syari.ss.world.ConfigLoader.saveWorldConfig
import me.syari.ss.world.Main.Companion.worldPlugin
import me.syari.ss.world.SSWorld.Companion.firstSpawnWorld
import me.syari.ss.world.area.WorldArea
import me.syari.ss.world.creator.SSWorldCreator
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.io.File

object CommandCreator : OnEnable {
    override fun onEnable() {
        createCommand(worldPlugin, "world", "SSWorld",
            tab { _, _ -> element("create", "load", "delete", "config", "list", "tp", "spawn") },
            flag(
                "create *",
                "-w" to element("normal", "nether", "end"),
                "-t" to element("normal", "flat", "void"),
                "-s" to element("true", "false")
            ),
            tab("delete", "unload", "tp") { _, _ ->
                element(SSWorld.worldNameList)
            },
            tab("config") { _, _ -> element("firstspawn", "spawn", "area") },
            tab("config firstspawn", "config spawn", "config area"){ _, _ ->
                element("set")
            }
        ) { sender, args ->
            when (args.whenIndex(0)) {
                "create" -> {
                    createWorld(args, false)
                }
                "load" -> {
                    createWorld(args, true)
                }
                "delete" -> {
                    deleteWorld(args, true)
                }
                "unload" -> {
                    deleteWorld(args, false)
                }
                "config" -> {
                    when (args.whenIndex(1)) {
                        "firstspawn" -> {
                            if(args.whenIndex(2) == "set"){
                                val world = argsToWorld(args, 3) ?: return@createCommand
                                if(world.isFistSpawnWorld){
                                    val lastFirstSpawnWorld = firstSpawnWorld
                                    world.isFistSpawnWorld = true
                                    sendWithPrefix("サーバースポーンを &6${lastFirstSpawnWorld.name} &fから &6${world.name} &fに変更しました")
                                } else {
                                    sendError("既にサーバースポーンです")
                                }
                            } else {
                                sendWithPrefix("サーバースポーン: &6${firstSpawnWorld.name}")
                            }
                        }
                        "spawn" -> {
                            if(sender !is Player) return@createCommand sendError(ErrorMessage.OnlyPlayer)
                            val world = SSWorld.getWorld(sender) ?: return@createCommand sendError("ワールドが存在しません")
                            if(args.whenIndex(2) == "set"){
                                val vector5D = when(val size = args.size){
                                    3 -> {
                                        Vector5D.fromLocation(sender.location)
                                    }
                                    6, 8 -> {
                                        try {
                                            val x = args[3].toDouble()
                                            val y = args[4].toDouble()
                                            val z = args[5].toDouble()
                                            if(size == 8){
                                                val yaw = args[6].toFloat()
                                                val pitch = args[7].toFloat()
                                                Vector5D(x, y, z, yaw, pitch)
                                            } else {
                                                Vector5D(x, y, z)
                                            }
                                        } catch (ex: NumberFormatException){
                                            return@createCommand sendError("座標変換に失敗しました")
                                        }
                                    }
                                    else -> {
                                        return@createCommand sendError("引数の数が間違っています")
                                    }
                                }
                                world.spawnVector5D = vector5D
                                world.saveSpawnLocation()
                                sendWithPrefix("&6${world.name} &fのスポーン地点を &6${vector5D} &fに変更しました")
                            } else {
                                sendWithPrefix("ワールドスポーン: &6${world.spawnVector5D}")
                            }
                        }
                        "area" -> {
                            if(sender !is Player) return@createCommand sendError(ErrorMessage.OnlyPlayer)
                            val world = SSWorld.getWorld(sender) ?: return@createCommand sendError("ワールドが存在しません")
                            val lastArea = world.area
                            if(args.whenIndex(2) == "set"){
                                val area = when(args.size){
                                    4 -> {
                                        if (args.whenIndex(3) != "null") {
                                            val location = sender.location
                                            val x = location.x
                                            val z = location.z
                                            val radius = args[3].toDouble()
                                            WorldArea(x, z, radius)
                                        } else {
                                            null
                                        }
                                    }
                                    6 -> {
                                        val x = args[3].toDouble()
                                        val z = args[4].toDouble()
                                        val radius = args[5].toDouble()
                                        WorldArea(x, z, radius)
                                    }
                                    else -> {
                                        return@createCommand sendError("引数の数が間違っています")
                                    }
                                }
                                world.area = area
                                world.saveArea()
                                sendWithPrefix("&6${world.name} &fのワールドエリアを &6${toString(lastArea)} &fから &6${toString(area)} &fに変更しました")
                            } else {
                                sendWithPrefix("ワールドエリア: ${toString(lastArea)}")
                            }
                        }
                        else -> {
                            sendHelp(
                                "world config firstspawn" to "サーバーのスポーンワールド",
                                "world config spawn" to "ワールドのスポーン地点",
                                "world config area" to "ワールドの範囲"
                            )
                        }
                    }
                }
                "list" -> {
                    sendList("ワールド一覧", SSWorld.worldNameList)
                }
                "tp" -> {
                    teleportWorld(sender, args, 1)
                }
                "spawn" -> {
                    teleportSpawn(sender)
                }
                else -> {
                    sendHelp(
                        "world create" to "新規ワールドを生成します",
                        "world load" to "既存ワールドを読み込みます",
                        "world delete" to "ワールドを削除します",
                        "world config" to "ワールドの設定をします",
                        "world list" to "ワールドの一覧を表示します",
                        "world tp" to "ワールドにテレポートします",
                        "world spawn" to "スポーン地点にテレポートします"
                    )
                }
            }
        }

        createCommand(worldPlugin, "wtp", "SSWorld",
            tab { _, _ -> element(SSWorld.worldNameList) }
        ) { sender, args ->
            teleportWorld(sender, args, 0)
        }

        createCommand(worldPlugin, "spawn", "SSWorld") { sender, _ ->
            teleportSpawn(sender)
        }

        SSWorld.setDataWorld(worldPlugin.server.worlds.first())
        loadConfig(console)
    }

    private fun CommandMessage.argsToWorld(args: CommandArgument, worldNameIndex: Int): SSWorld? {
        args.getOrNull(worldNameIndex)?.let { worldName ->
            SSWorld.getWorld(worldName)?.let { world ->
                return world
            }
            sendError(ErrorMessage.NotExistName)
            return null
        }
        sendError(ErrorMessage.NotEnterName)
        return null
    }

    private fun CommandMessage.teleportWorld(sender: CommandSender, args: CommandArgument, worldNameIndex: Int) {
        if (sender !is Player) return sendError(ErrorMessage.OnlyPlayer)
        val world = argsToWorld(args, worldNameIndex) ?: return
        world.teleportSpawn(sender)
    }

    private fun CommandMessage.teleportSpawn(sender: CommandSender) {
        if (sender !is Player) return sendError(ErrorMessage.OnlyPlayer)
        val world = SSWorld.getWorld(sender) ?: return sendError("ワールドが存在しません")
        world.teleportSpawn(sender)
    }

    private fun CommandMessage.createWorld(args: CommandArgument, isLoad: Boolean) {
        val name = args.getOrNull(1)
        val type = if (isLoad) "読込" else "生成"
        if (name == null) {
            sendError(ErrorMessage.NotEnterName)
            return sendList(
                "ワールド${type}オプション",
                "&fワールドディメンション  &a-w <&6Normal&a/Nether/End>",
                "&fワールドタイプ  &a-t <&6Normal&a/Flat/Void>",
                "&f建造物生成  &a-s <true/&6false&a>"
            )
        }
        if (SSWorld.containsWorld(name)) return sendError(ErrorMessage.AlreadyExist)
        if (isLoad) {
            val loadWorld = File(worldPlugin.server.worldContainer, name)
            if (!loadWorld.exists() || !loadWorld.isDirectory) return sendWithPrefix("&cワールド &a$name &c存在しません")
        }
        val creator = SSWorldCreator(name)
        val environment = args.getFlag("-w")
        creator.environment = getEnvironmentFromString(environment)
            ?: return sendWithPrefix("&cワールドディメンションの値がおかしいです")
        val worldType = args.getFlag("-t")
        creator.worldType = getWorldTypeFromString(creator, worldType)
            ?: return sendWithPrefix("&cワールドタイプの値がおかしいです")
        val generateStructures = getGenerateStructuresFromString(args.getFlag("-s"))
        creator.generateStructures = generateStructures ?: return sendWithPrefix("&c建造物生成の値がおかしいです")
        sendWithPrefix("&fワールド &a$name &fを${type}します")
        if (creator.create() != null) {
            sendWithPrefix("&fワールド &a$name &fを${type}しました")
            saveWorldConfig(name, environment, worldType, generateStructures)
        } else {
            sendWithPrefix("&cワールド &a$name &fの${type}に失敗しました")
        }
    }

    private fun CommandMessage.deleteWorld(args: CommandArgument, deleteWorldFolder: Boolean) {
        val world = argsToWorld(args, 1) ?: return
        sendWithPrefix("&fワールド &a${world.name} &f${if (deleteWorldFolder) "のデータ" else ""}を削除します")
        val result = world.unload(deleteWorldFolder)
        if (result.isSuccess) {
            sendWithPrefix("&fワールド &a${world.name} &fの削除が完了しました")
        } else {
            sendWithPrefix("&cワールド &a${world.name} &cの削除に失敗しました (${result.message})")
        }
    }

    private fun toString(area: WorldArea?): String {
        return if(area != null){
            "&6X:${area.centerX}, Z:${area.centerZ}, radius:${area.radius}"
        } else {
            "&c未設定"
        }
    }
}