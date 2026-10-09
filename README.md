# DisplayPort for Odin[Addon]
Love: Cascade

A Kotlin/Fabric conversion of the supplied ChatTriggers GifPlayer module.

This shouldnt require any updates unless starred wants to break cascade for the 90th time

## Frame files

Place numbered PNG frames in `<game directory>/config/odin/gifplayer/`. For the default prefix, use `frame_0.png`, `frame_1.png`, and so on. Set **GIF name** to the filename prefix and **GIF frame count** to the number of frames, then use **Reload frames** in Odin's module settings. With IntelliJ's `runClient`, the game directory is this project's `run/` folder.

For a single still image, place a PNG in the same folder (default `image.png`), enable **Image Display**, and use its **Reload image** action after changing the **Image file** setting. Position it independently in Odin's HUD editor.

The player stops loading at the first missing frame. PNG transparency is preserved. Move and resize the display through Odin's HUD editor.

## Build

Open the project in IntelliJ IDEA with JDK 25 and run the Gradle `build` task. Odin is pinned to the latest release, 0.3.6 (release commit `833e053`), which targets Minecraft 26.2.

## GitHub Releases

The GitHub Actions workflow builds the addon on pushes to `main` and attaches `DisplayPort-{Version}.jar` to a GitHub Release when you push a version tag. Keep the tag version in sync with `mod_version` in `gradle.properties`. For example, with version `1.0.0`, create and push the tag with:

```powershell
git tag v1.0.0
git push origin v1.0.0
```
