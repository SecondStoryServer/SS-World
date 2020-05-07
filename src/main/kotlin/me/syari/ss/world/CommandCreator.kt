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
import me.syari.ss.world.ConfigLoader.getEnvironmentFromString
import me.syari.ss.world.ConfigLoader.getGenerateStructuresFromString
import me.syari.ss.world.ConfigLoader.getWorldTypeFromString
import me.syari.ss.world.ConfigLoader.loadConfig
import me.syari.ss.world.ConfigLoader.saveWorldConfig
import me.syari.ss.world.Main.Companion.worldPlugin
import me.syari.ss.world.creator.SSWorldCreator
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
            flag(
                "delete *",
                "-r" to element("true", "false")
            ),
            tab("delete", "tp") { _, _ -> element(SSWorld.worldNameList) }
        ) { sender, args ->
            when (args.whenIndex(0)) {
                "create" -> {
                    createWorld(args, false)
                }
                "load" -> {
                    createWorld(args, true)
                }
                "delete" -> {
                    val name = args.getOrNull(1)
                    if (name == null) {
                        sendError(ErrorMessage.NotEnterName)
                        return@createCommand sendList(
                            "ワールド削除オプション",
                            "&fワールドデータ削除の有無  &a-r <true/&6false&a>"
                        )
                    }
                    val world = SSWorld.getWorld(name) ?: return@createCommand sendError(ErrorMessage.NotExist)
                    val delete = args.getFlag("-r") == "true"
                    sendWithPrefix("&fワールド &a${world.name} &f${if (delete) "のデータ" else ""}を削除します")
                    val result = world.unload(delete)
                    if (result.isSuccess) {
                        sendWithPrefix("&fワールド &a${world.name} &fの削除が完了しました")
                    } else {
                        sendWithPrefix("&cワールド &a${world.name} &cの削除に失敗しました (${result.message})")
                    }
                }
                "config" -> {

                }
                "list" -> {
                    sendList("ワールド一覧", SSWorld.worldNameList)
                }
                "tp" -> {
                    if (sender !is Player) return@createCommand sendError(ErrorMessage.OnlyPlayer)
                    val worldName = args.getOrNull(1) ?: return@createCommand sendError(ErrorMessage.NotEnterName)
                    val world = SSWorld.getWorld(worldName) ?: return@createCommand sendError(ErrorMessage.NotExist)
                    world.teleportSpawn(sender)
                }
                "spawn" -> {
                    if (sender !is Player) return@createCommand sendError(ErrorMessage.OnlyPlayer)
                    val world = SSWorld.getWorld(sender) ?: return@createCommand sendError("ワールドが存在しません")
                    world.teleportSpawn(sender)
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

        SSWorld.setDataWorld(worldPlugin.server.worlds.first())
        loadConfig(console)
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
}