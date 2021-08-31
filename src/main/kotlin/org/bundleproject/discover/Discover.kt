package org.bundleproject.discover

import dev.isxander.xanderlib.utils.HttpsUtils.downloadFile
import dev.isxander.xanderlib.utils.json.BetterJsonObject
import dev.isxander.xanderlib.utils.json.BetterJsonObject.Companion.getFromFile
import org.bundleproject.discover.gui.MainGui
import org.bundleproject.discover.repo.RepositoryManager
import org.bundleproject.discover.utils.FileUtils
import org.bundleproject.discover.utils.Log
import org.bundleproject.discover.utils.OSChecker
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.swing.JOptionPane


class Discover {
    var mcDir: File? = null
    private val bundlePath: String
    val repositoryManager: RepositoryManager
    val mainGui: MainGui

    @Throws(IOException::class)
    fun install() {
        Log.startSection("Attempting to install Bundle Discover...")
        val repo = repositoryManager
        Log.startSection("Checking possibility of Bundle Discover install...")
        if (!mcDir!!.exists()) {
            Log.sendSectionInfo("Minecraft Data Folder was not found. Warning user.")
            JOptionPane.showMessageDialog(
                null,
                "Could not find your specified minecraft data folder.",
                "Failure",
                JOptionPane.ERROR_MESSAGE
            )
            return
        }
        val bdFile = bdFile
        if (bdFile.exists()) {
            Log.startSection("Bundle Discover is already installed. Warning user.")
            val option = JOptionPane.showConfirmDialog(
                null,
                "Your Bundle Discover directory already exists. All data in the Bundle Discover folder will be deleted.\nDo you want to continue anyway?",
                "Bundle Discover is already installed",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            )
            if (option == JOptionPane.YES_OPTION) {
                Log.sendSectionInfo("User permitted deletion of Bundle Discover directory. Deleting.")
                FileUtils.deleteDirectory(bdFile)
                Log.sendSectionInfo("Finished deleting Bundle Discover.")
            } else {
                Log.sendSectionInfo("User did not accept deletion of Bundle Discover. Aborting install.")
                Log.resetSections()
                return
            }
            Log.endSection()
        }
        Log.endSection()

        // make directories
        Log.startSection("Creating necessary directories...")
        bdFile.mkdirs()
        val versionsDir = File(mcDir, "versions")
        val modsDir = File(bdFile, "mods")
        val packsDir = File(bdFile, "resourcepacks")
        versionsDir.mkdirs()
        modsDir.mkdirs()
        packsDir.mkdirs()
        Log.endSection("Finished.")
        Log.startSection("Installing Minecraft Forge...")
        val forgeVersion = "1.8.9-forge1.8.9-11.15.1.2318-1.8.9"
        // create version
        Log.sendSectionInfo("Creating version files...")
        val versionJsonFile = File(versionsDir, "$forgeVersion/$forgeVersion.json")
        if (!versionJsonFile.exists()) {
            versionJsonFile.parentFile.mkdirs()
            downloadFile(RepositoryManager.FORGE_VERSION_JSON, versionJsonFile)
        }

        // download forge jar
        Log.sendSectionInfo("Downloading forge binary...")
        val forgeJarFile = File(
            mcDir,
            "libraries/net/minecraftforge/forge/1.8.9-11.15.1.2318-1.8.9/forge-1.8.9-11.15.1.2318-1.8.9.jar"
        )
        if (!forgeJarFile.exists()) {
            forgeJarFile.parentFile.mkdirs()
            downloadFile(RepositoryManager.FORGE_VERSION_JAR, forgeJarFile)
        }
        Log.endSection("Finished installing Minecraft Forge.")
        Log.startSection("Installing Mods...")
        for (mod in repo.modEntries) {
            if (!mod.isEnabled) continue
            Log.startSection("Downloading Mod: " + mod.displayName)
            if (!mod.downloadUrl.startsWith("https://")) {
                Log.endSection("${mod.displayName}'s download URL (${mod.downloadUrl}) was either invalid or insecure.")
                continue
            }
            downloadFile(mod.downloadUrl, File(modsDir, mod.fileName))
            for (requiredFileName in mod.files) {
                if (!requiredFileName.isNullOrEmpty()) {
                    val requiredFile = File(bdFile, requiredFileName)
                    requiredFile.parentFile.mkdirs()
                    Log.sendSectionInfo("Downloading Extra File: " + requiredFile.absolutePath)
                    downloadFile(RepositoryManager.MC_DIR_URL + requiredFileName, requiredFile)
                } else {
                    println("Extra File is null, skipping.")
                }
            }
            Log.endSection()
        }
        Log.endSection()

        // no, not a token logger
        Log.startSection("Creating Launcher Profile...")
        val launcherProfilesFile = File(mcDir, "launcher_profiles.json")
        Log.sendSectionInfo("Retrieving JSON file...")
        val launcherProfilesJson = getFromFile(launcherProfilesFile)
        Log.sendSectionInfo("Creating profile...")
        val currentDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(Calendar.getInstance().time)
        val profile = BetterJsonObject()
        profile.addProperty("creator", currentDate)
        profile.addProperty(
            "icon",
            "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAARCSURBVFhHvZfPa1RXFMfvG7WVFkpH0bQiGNSaWQi6C6hBod2VLuzCdpMutCD9gZRGV+J/EKFRsApaaLJpS6V0UcimVJLSkoWQQAuTVNs0FW2iNCJVMSrP9zlzv8/z3jyNbvzA4/54957zPefcmbmThEUY+OODlPbAxhO2NkkW3fJU1GJbSZqa73D07xfCsQsf2oA5zT8tCsbzSAE4wSnO4dgPC2HjwPu5gbKIr8Yns6kWcaoA09goi8gFsMA/4rsNd2IvK8Obz8Xew1Jka9Pxudm0seZlGwNzsWswXPfjwZT9O5YuibMtTEBpvUH0WiwRROBLgWP6W1attmfi6hxDQ+8A58t+ux5HRSpLwIbYNfZ8fzvc3dyKEBFV4Lx5+bq1PJmgttNajh5qVdGjVrUHn3pQFrau7ki+npiyOZy/u7UrYY5HJfI1//nefXs8bRnwB83Tt+6WtV4M4nEKtHE65/xfH+dlBGXPiyoI0EEB1d0rVhkAoYqyCu/E179cwoIARSlHOC9vsIMY51Q+Wu/QR15OOXibuQCiZ5Pf6M8BSCAoUzhnL2sRgfPdF5+3d+CdEZj2bfhsnwnOBcg4jr2jKmSEw+jPDM4UcVXkgFAv3gQoekHfR1+OQjCvMaIxrH20iJAzv08gvoZzHzHOvXoZ8GBcojhgfn/VF463wVofUOEQCkUhw96AHCyd2RfWLj9kz/ilIzbnRYHPhihns6bFi9UOZOj1JX1h21ud1oc/J2+EE5/3xtGjqcpO7eInZxKp4ivXq6Wvd7QyMDJxJHyx61Xrr+96KQy8XQ/vrfwp7L5UtzmhDLKv7JzoZ944mlgJ2PjPqRfDmrNT+eKdX87ZAzjfdn4h7L25vnCYcAz8EEG9Xjdbfs3/36y1fUL9PcOtL7HCGejs7DQRLGo0GqGjo8MM4rzMrlOT1pKJRt+v1gdEwP6xFeZcYEfOvT0TMDs7awPo7u4O8/Pz1pcxoXkZGPj9fth77or19a7ZbFp0Gnt4J/r7+y0FJmBoaChBxPT0NENDixFBNgTG/ZgMcCDHxsYqnQ4PD1fOi7aPobKBGK+4Ck6/MgCI8Pj9ssscAdsgIxfgJ70IqSdqsuHLwidAjIyM2H7ODfiMens+y1DIwOjoaFJeAD6Fiurfqf+sJQuC/XImEDY4OBh4jp/9Nnx0/HR806KtBF4Em71BhBDh40pDJikFNnCWXc/yHys+rlxe/X2xTQAvM8c4zzcj4nFOfRaAIHD+zpZN+XeE4OrGXPZLWPw5hqm5hVQb4sXSUicQwkN0+ir258Djr+kCm4jy5AKI/LVVy6yvi6ZAhMqi9toFa9qiF5n4/MKKYx7dmqHsw/7ZIEL4sWrW09OT8vT29qY7TzbtXdenv6hvdp4Evzb/6AFOSZ2u2HG6DQTMbD9sfT4Nr2xaEc7tt9uxzT0TCJnMkIFIfPMM8KUqEVc8KSE8AJC1I56Vh+j7AAAAAElFTkSuQmCC"
        )
        profile.addProperty("lastUsed", currentDate)
        profile.addProperty(
            "javaArgs",
            "-Xmx3G -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -XX:G1NewSizePercent=20 -XX:G1ReservePercent=20 -XX:MaxGCPauseMillis=50 -XX:G1HeapRegionSize=32M"
        )
        profile.addProperty("gameDir", bdFile.absolutePath)
        profile.addProperty("lastVersionId", forgeVersion)
        profile.addProperty("name", "BundleDiscover")
        val res = BetterJsonObject()
        res.addProperty("width", 1280)
        res.addProperty("height", 720)
        profile.add("resolution", res)
        profile.addProperty("type", "custom")
        Log.sendSectionInfo("Writing new JSON to file...")
        launcherProfilesJson.getObj("profiles").add("BundleDiscover", profile)
        launcherProfilesJson.writeToFile(launcherProfilesFile)
        Log.info("Finished installing Bundle Discover!")
    }

    private val bdFile: File
        get() = File(mcDir, bundlePath)

    companion object {
        var instance: Discover? = null
            get() {
                if (field == null) field = Discover()
                return field
            }
            private set
    }

    init {
        val type = OSChecker.operatingSystemType
        mcDir = when (type) {
            OSChecker.OSType.WINDOWS -> File(File(System.getenv("APPDATA")), ".minecraft")
            OSChecker.OSType.OS_X -> File(File(System.getProperty("user.home")), "Library/Application Support/Minecraft")
            OSChecker.OSType.LINUX -> File(File(System.getProperty("user.home")), ".minecraft")
            else -> {
                Log.err("OS type is not supported. Cannot continue.")
                JOptionPane.showMessageDialog(
                    null,
                    "Your OS type is not supported by Bundle Discover (Java Edition).",
                    "Fatal Error",
                    JOptionPane.ERROR_MESSAGE
                )
                throw IllegalStateException("OS type is not supported.")
            }
        }
        bundlePath = "discover"
        repositoryManager = RepositoryManager()
        mainGui = MainGui(this)
    }
}
