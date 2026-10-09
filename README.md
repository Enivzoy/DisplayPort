# DisplayPort for Odin[Addon]
Love: Cascade

A Kotlin/Fabric conversion of the supplied ChatTriggers GifPlayer module.

This shouldnt require any updates unless starred wants to break cascade for the 90th time

## Frame files

Place numbered PNG frames in `<game directory>/config/odin/gifplayer/`. For the default prefix, use `frame_0.png`, `frame_1.png`, and so on. Set **GIF name** to the filename prefix and **GIF frame count** to the number of frames, then use **Reload frames** in Odin's module settings. With IntelliJ's `runClient`, the game directory is this project's `run/` folder.

For a single still image, place a PNG in the same folder (default `image.png`), enable **Image Display**, and use its **Reload image** action after changing the **Image file** setting. Position it independently in Odin's HUD editor.

The player stops loading at the first missing frame. PNG transparency is preserved. Move and resize the display through Odin's HUD editor.

some slight instructions is to get ffmpeg and use it to convert a .mp4, .mkv etc into individual frames
`ffmpeg -i input.mkv -vf "chromakey=0x00FF00:0.12:0.08,despill=type=green" -start_number 0 -fps_mode passthrough frame_%d.png` use this incase your video is a green screen
`ffmpeg -i input.mkv -start_number 0 -fps_mode passthrough frame_%d.png` use this for normal gifs

Dependency:
OdinFabric
Cascade

## Build

Open the project in IntelliJ IDEA with JDK 25 and run the Gradle `build` task. Tested on Odin 0.3.6 (release commit `833e053`), 26.2

