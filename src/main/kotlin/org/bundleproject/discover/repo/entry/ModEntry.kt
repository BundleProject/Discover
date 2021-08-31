package org.bundleproject.discover.repo.entry

class ModEntry(
    val id: String,
    var isEnabled: Boolean,
    val fileName: String,
    val downloadUrl: String,
    val displayName: String,
    val description: String,
    val iconFile: String,
    val iconScaling: String,
    val creator: String,
    val modRequirements: Array<String?>,
    val actions: Array<EntryAction?>,
    val warning: EntryWarning?,
    val files: Array<String?>,
    val isHidden: Boolean
)
