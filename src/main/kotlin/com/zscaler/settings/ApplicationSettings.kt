package com.zscaler.settings

import com.intellij.openapi.application.ApplicationManager
import com.zscaler.listeners.SettingsListener
import java.util.Objects

class ApplicationSettings {
    var apiKey: String? = null

    fun setApiKeyNotifying(apiKey:String) {
        var changed = !Objects.equals(this.apiKey, apiKey)
        this.apiKey = apiKey

        if (changed) {
            settingsPublisher().apiKeyChanged()
        }
    }

    companion object {
        fun settingsPublisher(): SettingsListener {
            return ApplicationManager.getApplication().messageBus.syncPublisher(SettingsListener.TOPIC)
        }
    }
}