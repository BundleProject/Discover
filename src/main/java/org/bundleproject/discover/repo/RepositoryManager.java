package org.bundleproject.discover.repo;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import dev.isxander.xanderlib.utils.HttpsUtils;
import dev.isxander.xanderlib.utils.Multithreading;
import dev.isxander.xanderlib.utils.json.BetterJsonObject;
import org.bundleproject.discover.repo.entry.EntryAction;
import org.bundleproject.discover.repo.entry.EntryWarning;
import org.bundleproject.discover.repo.entry.ModEntry;
import org.bundleproject.discover.utils.Log;
import org.bundleproject.discover.utils.UpdateHook;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class RepositoryManager {

    public static final String MODS_JSON_URL = "https://raw.githubusercontent.com/nacrt/SkyblockClient-REPO/main/files/mods.json";
    public static final String ICONS_DIR_URL = "https://raw.githubusercontent.com/nacrt/SkyblockClient-REPO/main/files/icons/";
    public static final String MC_DIR_URL = "https://raw.githubusercontent.com/nacrt/SkyblockClient-REPO/main/files/mcdir/";

    public static final String FORGE_VERSION_JSON = "https://raw.githubusercontent.com/nacrt/SkyblockClient-REPO/main/files/forge/1.8.9-forge1.8.9-11.15.1.2318-1.8.9.json";
    public static final String FORGE_VERSION_JAR = "https://raw.githubusercontent.com/nacrt/SkyblockClient-REPO/main/files/forge/forge-1.8.9-11.15.1.2318-1.8.9.jar";

    public static final File CACHE_FOLDER = new File(new File(System.getProperty("user.home")), ".skyclient/");
    public static final long CACHE_TIME = TimeUnit.DAYS.toMillis(1);
    public static final File ICON_FOLDER = new File(CACHE_FOLDER, "icons");

    private final List<ModEntry> modEntries;

    private final Map<String, BufferedImage> imageCache;
    private final BufferedImage unknownImage;

    public RepositoryManager() {
        this.modEntries = new ArrayList<>();
        this.imageCache = new HashMap<>();

        try {
            this.unknownImage = ImageIO.read(Objects.requireNonNull(RepositoryManager.class.getResourceAsStream("/skyclient.png")));
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Could not read unknown image.");
        }
    }

    public void fetchFiles() {
        // check if we need to refresh icons and stuff
        boolean refresh = shouldRefreshCache();
        if (refresh) {
            if (!CACHE_FOLDER.exists()) {
                CACHE_FOLDER.mkdirs();
            }
        }

        // get json from web
        JsonArray modsArr = JsonParser.parseString(Objects.requireNonNull(HttpsUtils.getString(MODS_JSON_URL))).getAsJsonArray();

        // Loop thru every element in the array
        for (JsonElement element : modsArr) {
            // Check if element is an object so we don't run into any weird errors
            if (!element.isJsonObject()) {
                Log.warn("Mods JSON included non-json-object.");
                continue;
            }

            // convert the element into a json object
            BetterJsonObject modJson = new BetterJsonObject(element.getAsJsonObject());

            // find all required mods and add them to array
            String[] mods = new String[0];
            if (modJson.has("packages")) {
                JsonArray modArray = modJson.get("packages").getAsJsonArray();
                mods = new String[modArray.size()];
                int i = 0;
                for (JsonElement modIdElement : modArray) {
                    mods[i] = modIdElement.getAsString();
                    i++;
                }
            }

            // find all required files and add them to array
            String[] files = new String[0];
            if (modJson.has("files")) {
                JsonArray fileArray = modJson.get("files").getAsJsonArray();
                files = new String[fileArray.size()];
                int i = 0;
                for (JsonElement fileIdElement : fileArray) {
                    files[i] = fileIdElement.getAsString();
                    i++;
                }
            }

            // find all actions and add them to array
            EntryAction[] actions = new EntryAction[0];
            if (modJson.has("actions")) {
                JsonArray actionsArr = modJson.get("actions").getAsJsonArray();
                List<EntryAction> actionList = new ArrayList<>(actionsArr.size());
                for (JsonElement actionElement : actionsArr) {
                    BetterJsonObject actionObj = new BetterJsonObject(actionElement.getAsJsonObject());

                    String text;
                    Runnable action;
                    if (actionObj.has("document")) {
                        text = "Guide (Built-In)";
                        action = () -> {
                            try {
                                Desktop.getDesktop().browse(new URI(actionObj.optString("document")));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        };
                    } else {
                        text = actionObj.optString("text");
                        action = () -> {
                            try {
                                Desktop.getDesktop().browse(new URI(actionObj.optString("link")));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        };
                    }
                    actionList.add(new EntryAction(
                            text,
                            actionObj.optString("creator"),
                            action
                    ));
                }
                actions = actionList.toArray(new EntryAction[0]);
            }

            // find warning
            EntryWarning warning = null;
            if (modJson.has("warning")) {
                JsonArray lineArr = modJson.get("warning").getAsJsonObject().get("lines").getAsJsonArray();
                List<String> lineList = new ArrayList<>();
                for (JsonElement lineElement : lineArr) {
                    lineList.add(lineElement.getAsString());
                }
                warning = new EntryWarning(lineList);
            }

            // finally create the entry
            modEntries.add(new ModEntry(
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
            ));
        }

        for (ModEntry mod : modEntries) {
            if (mod.isEnabled()) {
                for (String requiredModId : mod.getModRequirements()) {
                    getMod(requiredModId).setEnabled(true);
                }
            }
        }
    }

    public void getIcons(UpdateHook hook) {
        boolean refresh = shouldRefreshCache();

        if (refresh)
            Log.info("Refreshing Icon Cache...");
        else
            Log.info("Loading Icon Cache...");

        // add to another thread to prevent the program from freezing
        if (!ICON_FOLDER.exists())
            ICON_FOLDER.mkdirs();

        // loop through all the mod entries we just made and download the icons from it
        // do this after so we can have a list of all the mods as that is important
        // then get the images async
        for (ModEntry mod : modEntries) {
            if (mod.isHidden()) continue;

            Multithreading.runAsync(() -> {
                String iconFileName = mod.getIconFile();
                try {
                    // e.g. C:\Users\Xander\.skyclient\icons\neu.png
                    File iconFile = new File(ICON_FOLDER, iconFileName);
                    // If the icon doesn't already exist or the cache has expired
                    if (!iconFile.exists() || refresh) {
                        String url = ICONS_DIR_URL + iconFileName;
                        Log.info("Downloading icon: " + url + " -> " + iconFile.getAbsolutePath());
                        HttpsUtils.downloadFile(url, iconFile);
                    }
                    Log.info("Reading Image: " + iconFile.getPath());
                    imageCache.put(iconFileName, ImageIO.read(iconFile));

                    // this can be used to notify the gui that it needs to update
                    // the icon of a specified element. this reduces the work that needs to be done
                    hook.updateMod(mod);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        }
    }

    public ModEntry getMod(String id) {
        for (ModEntry mod : modEntries) {
            if (mod.getId().equalsIgnoreCase(id)) return mod;
        }

        return null;
    }

    public BufferedImage getImage(String fileName) {
        if (!imageCache.containsKey(fileName)) {
            return unknownImage;
        }

        return imageCache.get(fileName);
    }

    public List<ModEntry> getModEntries() {
        return modEntries;
    }

    public boolean shouldRefreshCache() {
        // if the cache folder doesnt exist or the cache was last modified over a day ago
        return !CACHE_FOLDER.exists() || System.currentTimeMillis() - CACHE_FOLDER.lastModified() > CACHE_TIME;
    }

}
