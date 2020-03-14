package me.syari.ss.world

import me.syari.ss.core.auto.FunctionInit
import me.syari.ss.core.command.create.CreateCommand.createCommand
import me.syari.ss.core.command.create.CreateCommand.element
import me.syari.ss.core.command.create.CreateCommand.flag
import me.syari.ss.core.command.create.CreateCommand.tab
import me.syari.ss.core.command.create.ErrorMessage
import me.syari.ss.core.message.Message.broadcast
import me.syari.ss.world.Main.Companion.worldPlugin
import me.syari.ss.world.creator.SSWorldCreator
import org.bukkit.World
import org.bukkit.WorldType
import org.bukkit.entity.Player

object WorldCommand: FunctionInit {
    override fun init() {
        createCommand(worldPlugin, "world", "SSWorld",
            tab { _, _ -> element("create", "load", "delete", "config", "list") },
            flag("create *",
                "-w" to element("normal", "nether", "end"),
                "-t" to element("normal", "flat", "void"),
                "-s" to element("true", "false")
            )
        ){ sender, args ->
            when(args.whenIndex(0)){
                "create" -> {
                    val name = args.getOrNull(1)
                    if(name == null){
                        sendError(ErrorMessage.NotEnterName)
                        return@createCommand sendList("ワールド作成オプション",
                            "&fワールドディメンション  &a-w <&6Normal&a/Nether/End>",
                            "&fワールドタイプ  &a-t <&6Normal&a/Flat/Void>",
                            "&f建造物生成  &a-s <true/&6false&a>"
                        )
                    }
                    if(SSWorld.containsWorld(name)) return@createCommand sendError(ErrorMessage.AlreadyExist)
                    val creator = SSWorldCreator(name)
                    creator.environment = when(args.getFlag("-w")){
                        "normal", null -> World.Environment.NORMAL
                        "nether" -> World.Environment.NETHER
                        "end" -> World.Environment.THE_END
                        else -> return@createCommand sendWithPrefix("&cワールドディメンションの値がおかしいです")
                    }
                    creator.worldType = when(args.getFlag("-t")){
                        "normal", null -> WorldType.NORMAL
                        "flat" -> WorldType.FLAT
                        "void" -> {
                            creator.voidWorld = true
                            WorldType.NORMAL
                        }
                        else -> return@createCommand sendWithPrefix("&cワールドタイプの値がおかしいです")
                    }
                    creator.generateStructures = when(args.getFlag("-s")){
                        "true" -> true
                        "false", null -> false
                        else -> return@createCommand sendWithPrefix("&c建造物生成の値がおかしいです")
                    }
                    sendWithPrefix("&eワールドを生成します")
                    if(creator.create() != null){
                        sendWithPrefix("&fワールドを生成しました")
                    } else {
                        sendWithPrefix("&cワールド生成に失敗しました")
                    }
                }
                "load" -> {

                }
                "delete" -> {

                }
                "config" -> {

                }
                "list" -> {

                }
                else -> {
                    sendHelp(
                        "world create" to "新規ワールドを生成します",
                        "world load" to "既存ワールドを読み込みます",
                        "world delete" to "ワールドを削除します",
                        "world config" to "ワールドの設定をします",
                        "world list" to "ワールドの一覧を表示します"
                    )
                }
            }
        }
    }
}