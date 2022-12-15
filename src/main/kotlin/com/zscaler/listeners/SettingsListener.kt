package com.zscaler.listeners

import com.intellij.util.messages.Topic

interface SettingsListener {

    companion object {
        val TOPIC = Topic.create("Zscanner settings", SettingsListener::class.java)
    }

    fun apiKeyChanged() {
    }

    fun loggedOut() {
    }
}