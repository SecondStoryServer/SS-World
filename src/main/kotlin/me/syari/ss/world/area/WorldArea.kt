package me.syari.ss.world.area

import kotlin.math.sqrt

data class WorldArea(
    val centerX: Double,
    val centerZ: Double,
    val radius: Double
) {
    fun isInArea(x: Double, z: Double): Boolean {
        val distanceX = centerX - x
        val distanceZ = centerZ - z
        val distance = sqrt((distanceX * distanceX) + (distanceZ * distanceZ))
        return distance <= radius
    }

    override fun toString(): String {
        return "$centerX, $centerZ, $radius"
    }

    companion object {
        fun fromString(text: String): WorldArea? {
            val splitText = text.split(", ")
            if (splitText.size != 3) return null
            return try {
                val x = splitText[0].toDouble()
                val z = splitText[1].toDouble()
                val radius = splitText[2].toDouble()
                WorldArea(x, z, radius)
            } catch (ex: NumberFormatException) {
                null
            }
        }
    }
}