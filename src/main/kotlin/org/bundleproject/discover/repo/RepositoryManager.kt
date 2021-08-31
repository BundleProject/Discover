package org.bundleproject.discover.repo

import com.google.gson.JsonParser
import dev.isxander.xanderlib.utils.HttpsUtils.downloadFile
import dev.isxander.xanderlib.utils.HttpsUtils.getString
import dev.isxander.xanderlib.utils.Multithreading.runAsync
import dev.isxander.xanderlib.utils.json.BetterJsonObject
import org.bundleproject.discover.repo.entry.EntryAction
import org.bundleproject.discover.repo.entry.EntryWarning
import org.bundleproject.discover.repo.entry.ModEntry
import org.bundleproject.discover.utils.Log.info
import org.bundleproject.discover.utils.Log.warn
import org.bundleproject.discover.utils.UpdateHook
import java.awt.Desktop
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.net.URI
import java.util.*
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO


class RepositoryManager {
    val modEntries: MutableList<ModEntry>
    private val imageCache: MutableMap<String, BufferedImage>
    private var unknownImage: BufferedImage? = null
    fun fetchFiles() {
        // check if we need to refresh icons and stuff
        val refresh = shouldRefreshCache()
        if (refresh) {
            if (!CACHE_FOLDER.exists()) {
                CACHE_FOLDER.mkdirs()
            }
        }

        // get json from web
        val modsArr = JsonParser.parseString(Objects.requireNonNull(getString(MODS_JSON_URL))).asJsonArray

        // Loop thru every element in the array
        for (element in modsArr) {
            // Check if element is an object so we don't run into any weird errors
            if (!element.isJsonObject) {
                warn("Mods JSON included non-json-object.")
                continue
            }

            // convert the element into a json object
            val modJson = BetterJsonObject(element.asJsonObject)

            // find all required mods and add them to array
            var mods = arrayOfNulls<String>(0)
            if (modJson.has("packages")) {
                val modArray = modJson["packages"].asJsonArray
                mods = arrayOfNulls(modArray.size())
                for ((i, modIdElement) in modArray.withIndex()) {
                    mods[i] = modIdElement.asString
                }
            }

            // find all required files and add them to array
            var files = arrayOfNulls<String>(0)
            if (modJson.has("files")) {
                val fileArray = modJson["files"].asJsonArray
                files = arrayOfNulls(fileArray.size())
                for ((i, fileIdElement) in fileArray.withIndex()) {
                    files[i] = fileIdElement.asString
                }
            }

            // find all actions and add them to array
            var actions = arrayOfNulls<EntryAction>(0)
            if (modJson.has("actions")) {
                val actionsArr = modJson["actions"].asJsonArray
                val actionList: MutableList<EntryAction?> = ArrayList(actionsArr.size())
                for (actionElement in actionsArr) {
                    val actionObj = BetterJsonObject(actionElement.asJsonObject)
                    var text: String
                    var action: Runnable
                    if (actionObj.has("document")) {
                        text = "Guide (Built-In)"
                        action = Runnable {
                            try {
                                Desktop.getDesktop().browse(URI(actionObj.optString("document")))
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    } else {
                        text = actionObj.optString("text")
                        action = Runnable {
                            try {
                                Desktop.getDesktop().browse(URI(actionObj.optString("link")))
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    actionList.add(
                        EntryAction(
                            text,
                            actionObj.optString("creator"),
                            action
                        )
                    )
                }
                actions = actionList.toTypedArray()
            }

            // find warning
            var warning: EntryWarning? = null
            if (modJson.has("warning")) {
                val lineArr = modJson["warning"].asJsonObject["lines"].asJsonArray
                val lineList: MutableList<String> = ArrayList()
                for (lineElement in lineArr) {
                    lineList.add(lineElement.asString)
                }
                warning = EntryWarning(lineList)
            }

            // finally create the entry
            modEntries.add(
                ModEntry(
                    modJson.optString("id"),
                    modJson.optBoolean("enabled", false),
                    modJson.optString("file"),
                    modJson.optString("url"),
                    modJson.optString("display"),
                    modJson.optString("description"),
                    modJson.optString("icon"),
                    modJson.optString("icon_scaling", "smooth"),
                    modJson.optString("creator", "Unknown"),
                    mods,
                    actions,
                    warning,
                    files,
                    modJson.optBoolean("hidden", false)
                )
            )
        }
        for (mod in modEntries) {
            if (mod.isEnabled) {
                for (requiredModId in mod.modRequirements) {
                    getMod(requiredModId)!!.isEnabled = true
                }
            }
        }
    }

    fun getIcons(hook: UpdateHook) {
        val refresh = shouldRefreshCache()
        if (refresh) info("Refreshing Icon Cache...") else info("Loading Icon Cache...")

        // add to another thread to prevent the program from freezing
        if (!ICON_FOLDER.exists()) ICON_FOLDER.mkdirs()

        // loop through all the mod entries we just made and download the icons from it
        // do this after so we can have a list of all the mods as that is important
        // then get the images async
        for (mod in modEntries) {
            if (mod.isHidden) continue
            runAsync {
                val iconFileName = mod.iconFile
                try {
                    // e.g. C:\Users\Xander\.skyclient\icons\neu.png
                    val iconFile = File(ICON_FOLDER, iconFileName)
                    // If the icon doesn't already exist or the cache has expired
                    if (!iconFile.exists() || refresh) {
                        val url = ICONS_DIR_URL + iconFileName
                        info("Downloading icon: " + url + " -> " + iconFile.absolutePath)
                        downloadFile(url, iconFile)
                    }
                    info("Reading Image: " + iconFile.path)
                    imageCache[iconFileName] = ImageIO.read(iconFile)

                    // this can be used to notify the gui that it needs to update
                    // the icon of a specified element. this reduces the work that needs to be done
                    hook.updateMod(mod)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun getMod(id: String?): ModEntry? {
        for (mod in modEntries) {
            if (mod.id.equals(id, ignoreCase = true)) return mod
        }
        return null
    }

    fun getImage(fileName: String): BufferedImage? {
        return if (!imageCache.containsKey(fileName)) {
            unknownImage
        } else imageCache[fileName]
    }

    private fun shouldRefreshCache(): Boolean {
        // if the cache folder doesnt exist or the cache was last modified over a day ago
        return !CACHE_FOLDER.exists() || System.currentTimeMillis() - CACHE_FOLDER.lastModified() > CACHE_TIME
    }

    companion object {
        const val MODS_JSON_URL = "https://raw.githubusercontent.com/nacrt/SkyblockClient-REPO/main/files/mods.json"
        const val ICONS_DIR_URL = "https://raw.githubusercontent.com/nacrt/SkyblockClient-REPO/main/files/icons/"
        const val MC_DIR_URL = "https://raw.githubusercontent.com/nacrt/SkyblockClient-REPO/main/files/mcdir/"
        const val FORGE_VERSION_JSON =
            "https://raw.githubusercontent.com/nacrt/SkyblockClient-REPO/main/files/forge/1.8.9-forge1.8.9-11.15.1.2318-1.8.9.json"
        const val FORGE_VERSION_JAR =
            "https://raw.githubusercontent.com/nacrt/SkyblockClient-REPO/main/files/forge/forge-1.8.9-11.15.1.2318-1.8.9.jar"
        val CACHE_FOLDER = File(File(System.getProperty("user.home")), ".skyclient/")
        val CACHE_TIME = TimeUnit.DAYS.toMillis(1)
        val ICON_FOLDER = File(CACHE_FOLDER, "icons")
    }

    init {
        modEntries = ArrayList()
        imageCache = HashMap()
        try {
            unknownImage =
                ImageIO.read(Objects.requireNonNull(RepositoryManager::class.java.getResourceAsStream("/skyclient.png")))
        } catch (e: IOException) {
            e.printStackTrace()
            throw IllegalStateException("Could not read unknown image.")
        }
    }
}
