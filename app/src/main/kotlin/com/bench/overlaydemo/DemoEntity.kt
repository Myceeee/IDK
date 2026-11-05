package com.bench.overlaydemo

/**
 * A single demo entity rendered by the overlay.
 *
 * @param screenX   Centre X on screen (pixels)
 * @param screenY   Top Y of the bounding box (pixels)
 * @param screenFeetY  Bottom Y of the bounding box (pixels)
 * @param health    0–100
 * @param label     Name drawn below the box
 * @param isEnemy   true → red box; false → green box
 */
data class DemoEntity(
    val screenX: Float,
    val screenY: Float,
    val screenFeetY: Float,
    val health: Int,
    val label: String = "",
    val isEnemy: Boolean = true
)
