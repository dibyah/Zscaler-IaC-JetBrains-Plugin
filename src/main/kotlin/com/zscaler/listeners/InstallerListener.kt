package com.zscaler.listeners

import com.intellij.util.messages.Topic

interface InstallerListener {
    companion object {
        val TOPIC = Topic.create("Zscanner installer", InstallerListener::class.java)
    }

    fun installerFinished()
}