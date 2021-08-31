package org.bundleproject.discover.utils

import org.bundleproject.discover.repo.entry.ModEntry

interface UpdateHook {
    fun updateMod(mod: ModEntry?)
}
