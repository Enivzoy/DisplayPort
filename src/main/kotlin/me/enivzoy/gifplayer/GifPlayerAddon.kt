package me.enivzoy.gifplayer

import me.enivzoy.gifplayer.features.GifPlayer
import me.enivzoy.gifplayer.features.ImageDisplay
import com.odtheking.odin.config.ModuleConfig
import com.odtheking.odin.features.ModuleManager
import net.fabricmc.api.ClientModInitializer

object GifPlayerAddon : ClientModInitializer {
    override fun onInitializeClient() {
        ModuleManager.registerModules(ModuleConfig("GifPlayer.json"), GifPlayer, ImageDisplay)
    }
}
