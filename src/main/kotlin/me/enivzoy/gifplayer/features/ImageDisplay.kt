package me.enivzoy.gifplayer.features

import com.mojang.blaze3d.platform.NativeImage
import com.odtheking.odin.clickgui.settings.impl.ActionSetting
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.clickgui.settings.impl.StringSetting
import com.odtheking.odin.features.Category
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.modMessage
import foo.starred.cascade.graphics.extensions.image.image
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.resources.Identifier
import java.awt.Dimension
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

object ImageDisplay : Module(
    name = "Image Display",
    category = Category.RENDER,
    description = "Display one PNG image as a movable HUD."
) {
    private val imageFileName by StringSetting(
        "Image file",
        "image.png",
        desc = "PNG filename in config/odin/gifplayer.",
        placeholder = "image.png"
    )
    private val displayZoom by NumberSetting(
        "Display zoom", 1.0, 0.25..20.0, 0.25,
        desc = "Enlarge the image beyond the HUD editor's scale limit; oversized parts are clipped by the screen."
    )

    private val reload by ActionSetting("Reload image", "Load the selected PNG image.") {
        loadImage()
    }

    private var texture: Pair<Identifier, Dimension>? = null
    private var loadedFileName = ""

    private val hud by HUD(
        "Image Display",
        desc = "Move and resize the image in Odin's HUD editor.",
        x = 10,
        y = 60,
        scale = 1f
    ) { _ ->
        val loaded = texture
        if (loaded == null) {
            val message = "Image: load image"
            text(Minecraft.getInstance().font, message, 0, 0, 0xFFFFFFFF.toInt())
            Minecraft.getInstance().font.width(message) to Minecraft.getInstance().font.lineHeight
        } else {
            val size = loaded.second
            val width = (size.width * displayZoom.toDouble()).toInt().coerceAtLeast(1)
            val height = (size.height * displayZoom.toDouble()).toInt().coerceAtLeast(1)
            image(loaded.first, 0f, 0f, width.toFloat(), height.toFloat())
            width to height
        }
    }

    override fun onEnable() {
        super.onEnable()
        if (texture == null || loadedFileName != imageFileName) loadImage()
    }

    private fun loadImage() {
        val mc = Minecraft.getInstance()
        val directory = File(mc.gameDirectory, "config/odin/gifplayer")
        if (!directory.exists()) directory.mkdirs()

        texture?.let { mc.textureManager.release(it.first) }
        texture = null

        val fileName = imageFileName.trim().ifBlank { "image.png" }
        val file = File(directory, fileName)
        val bufferedImage = runCatching { ImageIO.read(file) }.getOrNull()
        if (bufferedImage == null) {
            loadedFileName = ""
            modMessage("Could not load $fileName. Put a PNG in config/odin/gifplayer.")
            return
        }

        val id = Identifier.fromNamespaceAndPath("gifplayer", "single_image")
        mc.textureManager.register(id, DynamicTexture({ "gifplayer-image" }, bufferedImage.toNativeImage()))
        texture = id to Dimension(bufferedImage.width, bufferedImage.height)
        loadedFileName = imageFileName
        modMessage("Loaded image $fileName.")
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
