package me.syari.ss.world.generator

import org.bukkit.World
import org.bukkit.block.Biome
import org.bukkit.generator.ChunkGenerator
import java.util.*

class VoidGenerator : ChunkGenerator() {
    override fun generateChunkData(
        world: World,
        random: Random,
        ChunkX: Int,
        ChunkZ: Int,
        biome: BiomeGrid
    ): ChunkData {
        return createChunkData(world).apply {
            for (x in 0..15) {
                for (y in 0..255) {
                    for (z in 0..15) {
                        biome.setBiome(x, y, z, Biome.THE_VOID)
                    }
                }
            }
        }
    }
}