package me.syari.ss.world.generator

import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Biome
import org.bukkit.generator.ChunkGenerator
import java.util.*

class VoidGenerator: ChunkGenerator() {
    override fun generateChunkData(world: World, random: Random, ChunkX: Int, ChunkZ: Int, biome: BiomeGrid): ChunkData {
        return createChunkData(world).apply {
            for (x in 0..15) {
                for (z in 0..15) {
                    biome.setBiome(x, z, Biome.THE_VOID)
                }
            }
            if (ChunkX shl 4 <= 0 && 0 < ChunkX + 1 shl 4 && ChunkZ shl 4 <= 0 && 0 < ChunkZ + 1 shl 4) {
                setBlock(0, 63, 0, Material.BEDROCK)
            }
        }
    }
}