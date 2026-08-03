package com.libremobileos.freeform.server.ui

/**
 * Per-package freeform window config.
 *
 * [width]/[height] are the freeform window size in pixels, [x]/[y] are the
 * window position (same coordinate space as [android.view.WindowManager.LayoutParams]).
 * [forceResizeable] marks packages that should be forced into [android.content.pm.ActivityInfo.RESIZE_MODE_RESIZEABLE]
 * so they are not letterboxed / reloaded when opened in freeform.
 */
data class WindowConfigEntry(
    val width: Int,
    val height: Int,
    val x: Int = 0,
    val y: Int = 0,
    val forceResizeable: Boolean = false
)
