package me.enivzoy.gifplayer.features

import com.odtheking.odin.clickgui.settings.impl.ActionSetting
import com.odtheking.odin.clickgui.settings.impl.StringSetting
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Category
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.modMessage
import foo.starred.cascade.graphics.extensions.image.image
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.DynamicTexture
import com.mojang.blaze3d.platform.NativeImage
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import net.minecraft.resources.Identifier
import java.awt.Dimension
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

object GifPlayer : Module(
    name = "GIF Player",
    category = Category.RENDER,
    description = "Play numbered PNG frames in a movable HUD."
) {
    private val frameCount by StringSetting(
        "GIF frame count", "12",
        desc = "Number of frames named <base><index>.png (1 to 100).",
        placeholder = "12"
    )
    private val gifFps by NumberSetting("GIF FPS", 12, 1..60, desc = "Playback speed.")
    private val displayZoom by NumberSetting(
        "Display zoom", 1.0, 0.25..20.0, 0.05,
        desc = "Enlarge the GIF beyond the HUD editor's scale limit; oversized parts are clipped by the screen."
    )
    private val gifName by StringSetting(
        "GIF name", "frame_",
        desc = "Filename prefix, for example frame_ for frame_0.png.",
        placeholder = "frame_"
    )
    private val reload by ActionSetting("Reload frames", "Load frames from config/odin/gifplayer.") {
        loadFrames()
    }

    private val hud by HUD(
        "GIF Display",
        desc = "Move and resize the GIF display in Odin's HUD editor.",
        x = 10,
        y = 10,
        scale = 1f
    ) { _ ->
        val frame = textures.getOrNull(currentFrame)
        if (frame == null) {
            val text = "GIF: load frames"
            text(Minecraft.getInstance().font, text, 0, 0, 0xFFFFFFFF.toInt())
            Minecraft.getInstance().font.width(text) to Minecraft.getInstance().font.lineHeight
        } else {
            val size = frame.second
            val width = (size.width * displayZoom.toDouble()).toInt().coerceAtLeast(1)
            val height = (size.height * displayZoom.toDouble()).toInt().coerceAtLeast(1)
            image(frame.first, 0f, 0f, width.toFloat(), height.toFloat())
            width to height
        }
    }

    private val textures = mutableListOf<Pair<Identifier, Dimension>>()
    private var currentFrame = 0
    private var lastFrameAt = 0L
    private var loadedName = ""
    private var loadedFrameCount = 0

    private fun requestedFrameCount(): Int = frameCount.toIntOrNull()?.coerceIn(1, 100) ?: 12

    init {
        on<TickEvent.End> {
            if (!enabled || textures.isEmpty()) return@on
            val interval = 1_000_000_000L / gifFps.toLong().coerceAtLeast(1)
            val now = System.nanoTime()
            if (now - lastFrameAt >= interval) {
                currentFrame = (currentFrame + 1) % textures.size
                lastFrameAt = now
            }
        }
    }

    override fun onEnable() {
        super.onEnable()
        if (textures.isEmpty() || loadedName != gifName || loadedFrameCount != requestedFrameCount()) {
            loadFrames()
        }
        lastFrameAt = System.nanoTime()
    }

    private fun loadFrames() {
        val mc = Minecraft.getInstance()
        val directory = File(mc.gameDirectory, "config/odin/gifplayer")
        if (!directory.exists()) directory.mkdirs()

        textures.forEach { mc.textureManager.release(it.first) }
        textures.clear()

        val prefix = gifName.trim().ifBlank { "frame_" }
        val requestedCount = requestedFrameCount()
        for (index in 0 until requestedCount) {
            val file = File(directory, "$prefix$index.png")
            val image = runCatching { ImageIO.read(file) }.getOrNull() ?: break
            val id = Identifier.fromNamespaceAndPath("gifplayer", "frame_$index")
            mc.textureManager.register(id, DynamicTexture({ "gifplayer-$index" }, image.toNativeImage()))
            textures += id to Dimension(image.width, image.height)
        }

        currentFrame = 0
        lastFrameAt = System.nanoTime()
        loadedName = gifName
        loadedFrameCount = requestedCount

        if (textures.isEmpty()) {
            modMessage("No GIF frames found. Add ${prefix}0.png, ${prefix}1.png, ... to config/odin/gifplayer.")
        } else {
            modMessage("Loaded ${textures.size} GIF frames.")
        }
    }

    private fun BufferedImage.toNativeImage(): NativeImage {
        val result = NativeImage(width, height, false)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val argb = getRGB(x, y)
                val a = (argb ushr 24) and 0xFF
                val r = (argb ushr 16) and 0xFF
                val g = (argb ushr 8) and 0xFF
                val b = argb and 0xFF
                result.setPixel(x, y, (a shl 24) or (b shl 16) or (g shl 8) or r)
            }
        }
        return result
    }
}
