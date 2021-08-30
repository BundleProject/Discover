package org.bundleproject.discover.repo.entry;

public class ModEntry {

    private final String id;
    private boolean enabled;
    private final String fileName;
    private final String downloadUrl;
    private final String displayName;
    private final String description;
    private final String iconFile;
    private final String iconScaling;
    private final String creator;
    private final String[] modRequirements;
    private final EntryAction[] actions;
    private final EntryWarning warnings;
    private final String[] files;
    private final boolean hidden;

    public ModEntry(String id, boolean enabled, String fileName, String downloadUrl, String displayName, String description, String iconFile, String iconScaling, String creator, String[] modRequirements, EntryAction[] actions, EntryWarning warnings, String[] files, boolean hidden) {
        this.id = id;
        this.enabled = enabled;
        this.fileName = fileName;
        this.downloadUrl = downloadUrl;
        this.displayName = displayName;
        this.description = description;
        this.iconFile = iconFile;
        this.iconScaling = iconScaling;
        this.creator = creator;
        this.modRequirements = modRequirements;
        this.actions = actions;
        this.warnings = warnings;
        this.files = files;
        this.hidden = hidden;
    }

    public String getId() {
        return id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getFileName() {
        return fileName;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getIconFile() {
        return iconFile;
    }

    public String getIconScaling() {
        return iconScaling;
    }

    public String getCreator() {
        return creator;
    }

    public String[] getModRequirements() {
        return modRequirements;
    }

    public EntryAction[] getActions() {
        return actions;
    }

    public EntryWarning getWarning() {
        return warnings;
    }

    public String[] getFiles() {
        return files;
    }

    public boolean isHidden() {
        return hidden;
    }
}
